package com.galaxyrio.sudokusolver.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.data.settings.AppSettings
import com.galaxyrio.sudokusolver.domain.game.CandidateCalculator
import com.galaxyrio.sudokusolver.domain.game.HintIssue
import com.galaxyrio.sudokusolver.domain.game.HintIssueDetector
import com.galaxyrio.sudokusolver.domain.game.PuzzleSolutionResolver
import com.galaxyrio.sudokusolver.domain.game.SudokuValidator
import com.galaxyrio.sudokusolver.domain.game.SudokuTextExporter
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteEndpoint
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLine
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HumanSolver
import com.galaxyrio.sudokusolver.domain.solver.SolveTrace
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import java.util.ArrayDeque
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CellPosition(
    val row: Int,
    val col: Int,
)

enum class AdvancedInputTool {
    NONE,
    PAINT,
    SOLID_LINE,
    DASHED_LINE,
}

data class GameUiState(
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val gameId: Long? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val sudoku: Sudoku = Sudoku(),
    val timeSpentSeconds: Long = 0,
    val selectedCell: CellPosition? = null,
    val selectedNumber: Int? = null,
    val isNoteMode: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isComplete: Boolean = false,
    val isAdvancedMode: Boolean = false,
    val isAdvancedNoteMode: Boolean = false,
    val advancedNotes: AdvancedNotes = AdvancedNotes(),
    val advancedInputTool: AdvancedInputTool = AdvancedInputTool.NONE,
    val selectedAdvancedColorIndex: Int = 0,
    val pendingLineStart: AdvancedNoteEndpoint? = null,
    val lastAdvancedDigit: Int? = null,
    val isHintLoading: Boolean = false,
    val hintTrace: SolveTrace? = null,
    val hintIssue: HintIssue? = null,
    val selectedHintStepIndex: Int = 0,
    val showHintDetails: Boolean = true,
    val showErrorDetails: Boolean = true,
    val showErrorsImmediately: Boolean = false,
    val isHintDetailsRevealed: Boolean = false,
    val immediateHintRequestId: Long = 0,
    val hasPersistenceError: Boolean = false,
) {
    val highlightedNumber: Int?
        get() = selectedNumber ?: selectedCell
            ?.let { sudoku.getCell(it.row, it.col).value }
            ?.takeIf { it != 0 }

    val areHintDetailsVisible: Boolean
        get() = showHintDetails || isHintDetailsRevealed

    val isAdvancedColorToolActive: Boolean
        get() = advancedInputTool != AdvancedInputTool.NONE

    val hasAdvancedDraft: Boolean
        get() = advancedNotes.cellColors.isNotEmpty() ||
            advancedNotes.candidateColors.isNotEmpty() ||
            advancedNotes.lines.isNotEmpty() ||
            pendingLineStart != null
}

class GameViewModel(
    private val gameRepository: GameRepository,
    private val newGameDifficulty: Difficulty?,
    private val savedGameId: Long?,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val humanSolver: HumanSolver = HumanSolver(),
    private val solverDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val settings: StateFlow<AppSettings> = MutableStateFlow(AppSettings()),
) : ViewModel() {
    private val solutionResolver = PuzzleSolutionResolver()
    private var latestSettings = settings.value

    private val mutableUiState = MutableStateFlow(
        GameUiState(difficulty = newGameDifficulty ?: Difficulty.MEDIUM)
            .withGameSettings(latestSettings)
    )
    val uiState: StateFlow<GameUiState> = mutableUiState.asStateFlow()

    private val history = ArrayDeque<Sudoku>()
    private val redoHistory = ArrayDeque<Sudoku>()
    private var timerJob: Job? = null
    private var persistenceJob: Job? = null
    private var hintJob: Job? = null
    private var logicalSolverState: SolverState? = null
    private var logicalStateBoard: Sudoku? = null
    private var originalSolution: SudokuSolution? = null
    private var isResumed = false

    init {
        viewModelScope.launch {
            settings.collect { appSettings ->
                latestSettings = appSettings
                mutableUiState.update { it.withGameSettings(appSettings) }
            }
        }
        loadGame()
    }

    fun retry() {
        if (!mutableUiState.value.isLoading) loadGame()
    }

    fun onResume() {
        isResumed = true
        startTimerIfNeeded()
    }

    fun onPause() {
        isResumed = false
        timerJob?.cancel()
        timerJob = null
        persistCurrentGame()
    }

    fun setAdvancedMode(enabled: Boolean) {
        mutableUiState.update {
            it.copy(
                isAdvancedMode = enabled,
                isAdvancedNoteMode = it.isAdvancedNoteMode && enabled,
                advancedInputTool = if (enabled) {
                    it.advancedInputTool
                } else {
                    AdvancedInputTool.NONE
                },
                pendingLineStart = if (enabled) it.pendingLineStart else null,
                selectedNumber = if (enabled) it.selectedNumber else null,
            )
        }
    }

    fun onCellSelected(row: Int, col: Int) {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        if (state.isAdvancedMode && state.isAdvancedNoteMode) {
            when (state.advancedInputTool) {
                AdvancedInputTool.PAINT -> {
                    applyAdvancedColor(row, col)
                    return
                }
                AdvancedInputTool.SOLID_LINE,
                AdvancedInputTool.DASHED_LINE,
                -> {
                    selectAdvancedLineEndpoint(row, col)
                    return
                }
                AdvancedInputTool.NONE -> Unit
            }
        }

        val position = CellPosition(row, col)
        val newSelection = if (state.selectedCell == position) null else position
        mutableUiState.update { it.copy(selectedCell = newSelection) }

        val selectedNumber = state.selectedNumber ?: return
        val cell = state.sudoku.getCell(row, col)
        if (cell.isFixed) {
            mutableUiState.update { it.copy(selectedNumber = null) }
            return
        }

        applyNumber(row, col, selectedNumber, state.isNoteMode)
    }

    fun onNumberSelected(number: Int) {
        require(number in 1..Sudoku.GRID_SIZE)

        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        val selectedCell = state.selectedCell
        if (selectedCell == null) {
            mutableUiState.update {
                it.copy(selectedNumber = if (it.selectedNumber == number) null else number)
            }
            return
        }

        val cell = state.sudoku.getCell(selectedCell.row, selectedCell.col)
        if (!cell.isFixed) {
            applyNumber(
                row = selectedCell.row,
                col = selectedCell.col,
                number = number,
                noteMode = state.isNoteMode,
            )
        }
        mutableUiState.update { it.copy(selectedNumber = null) }
    }

    fun toggleAdvancedNoteMode() {
        val state = mutableUiState.value
        if (!state.isAdvancedMode || state.isComplete) return
        val enabled = !state.isAdvancedNoteMode
        mutableUiState.update {
            it.copy(
                isAdvancedNoteMode = enabled,
                isNoteMode = false,
                selectedNumber = null,
                advancedInputTool = if (enabled) {
                    it.advancedInputTool
                } else {
                    AdvancedInputTool.NONE
                },
                pendingLineStart = null,
            )
        }
    }

    fun onAdvancedNumberSelected(number: Int) {
        require(number in 1..Sudoku.GRID_SIZE)
        val state = mutableUiState.value
        if (!state.isAdvancedMode || !state.isAdvancedNoteMode) {
            onNumberSelected(number)
            return
        }
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        val selectedCell = state.selectedCell
        if (state.advancedInputTool == AdvancedInputTool.NONE &&
            state.isNoteMode &&
            selectedCell != null
        ) {
            val cell = state.sudoku.getCell(selectedCell.row, selectedCell.col)
            if (!cell.isFixed && !cell.isSolved()) {
                applyNumber(selectedCell.row, selectedCell.col, number, noteMode = true)
                return
            }
        }

        val wasHighlighted = number in state.advancedNotes.highlightedDigits
        val highlightedDigits = if (wasHighlighted) {
            state.advancedNotes.highlightedDigits - number
        } else {
            state.advancedNotes.highlightedDigits + number
        }
        val lastDigit = when {
            !wasHighlighted -> number
            state.lastAdvancedDigit == number -> highlightedDigits.maxOrNull()
            else -> state.lastAdvancedDigit
        }
        val updatedNotes = state.advancedNotes.copy(highlightedDigits = highlightedDigits)
        val updatedPendingStart = state.pendingLineStart?.let { start ->
            endpointForCell(
                sudoku = state.sudoku,
                cellIndex = start.cellIndex,
                highlightedDigits = highlightedDigits,
                preferredDigit = lastDigit,
            )
        }
        mutableUiState.update {
            it.copy(
                advancedNotes = updatedNotes,
                lastAdvancedDigit = lastDigit,
                pendingLineStart = updatedPendingStart,
            )
        }
        persistCurrentGame()
    }

    fun toggleBivalueHighlights() {
        updateAdvancedFeature { notes ->
            notes.copy(highlightBivalueCandidates = !notes.highlightBivalueCandidates)
        }
    }

    fun toggleFrameHighlights() {
        updateAdvancedFeature { notes ->
            notes.copy(frameHighlightedCells = !notes.frameHighlightedCells)
        }
    }

    fun togglePaintTool() {
        activateAdvancedTool { current ->
            if (current != AdvancedInputTool.NONE) {
                AdvancedInputTool.NONE
            } else {
                AdvancedInputTool.PAINT
            }
        }
    }

    fun toggleLineTool(style: AdvancedNoteLineStyle) {
        val requested = when (style) {
            AdvancedNoteLineStyle.SOLID -> AdvancedInputTool.SOLID_LINE
            AdvancedNoteLineStyle.DASHED -> AdvancedInputTool.DASHED_LINE
        }
        activateAdvancedTool { current ->
            if (current == requested) AdvancedInputTool.NONE else requested
        }
    }

    fun selectAdvancedColor(index: Int) {
        require(index in 0..AdvancedNoteColor.entries.size)
        mutableUiState.update { state ->
            if (!state.isAdvancedColorToolActive) return@update state
            if (state.selectedAdvancedColorIndex == index) {
                state.copy(
                    advancedInputTool = AdvancedInputTool.NONE,
                    pendingLineStart = null,
                )
            } else {
                state.copy(selectedAdvancedColorIndex = index)
            }
        }
    }

    fun clearSelection() {
        mutableUiState.update { it.copy(selectedCell = null) }
    }

    fun toggleNoteMode() {
        if (!mutableUiState.value.isComplete) {
            mutableUiState.update { it.copy(isNoteMode = !it.isNoteMode) }
        }
    }

    fun eraseSelectedCell() {
        val state = mutableUiState.value
        val selectedCell = state.selectedCell ?: return
        val cell = state.sudoku.getCell(selectedCell.row, selectedCell.col)
        if (!cell.isFixed &&
            (cell.isSolved() || cell.candidates.isNotEmpty() || cell.isCandidateSetExplicit)
        ) {
            updateSudoku(
                state.sudoku.setCell(selectedCell.row, selectedCell.col, value = 0)
            )
        }
    }

    fun undo() {
        if (history.isEmpty() || mutableUiState.value.isComplete) return

        val currentBoard = mutableUiState.value.sudoku
        hintJob?.cancel()
        hintJob = null
        logicalSolverState = null
        logicalStateBoard = null
        val previousBoard = history.removeLast()
        redoHistory.addLast(currentBoard)
        mutableUiState.update {
            it.copy(
                sudoku = previousBoard,
                canUndo = history.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty(),
                isHintLoading = false,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }
        persistCurrentGame()
    }

    fun redo() {
        val state = mutableUiState.value
        if (redoHistory.isEmpty() || state.isComplete) return

        hintJob?.cancel()
        hintJob = null
        logicalSolverState = null
        logicalStateBoard = null
        history.addLast(state.sudoku)
        val nextBoard = redoHistory.removeLast()
        mutableUiState.update {
            it.copy(
                sudoku = nextBoard,
                canUndo = history.isNotEmpty(),
                canRedo = redoHistory.isNotEmpty(),
                isHintLoading = false,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }
        persistCurrentGame()
    }

    fun deleteAdvancedDraft() {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return
        if (!state.hasAdvancedDraft) return

        mutableUiState.update {
            it.copy(
                advancedNotes = it.advancedNotes.copy(
                    cellColors = emptyMap(),
                    candidateColors = emptyMap(),
                    lines = emptyList(),
                ),
                advancedInputTool = AdvancedInputTool.NONE,
                pendingLineStart = null,
                lastAdvancedDigit = null,
            )
        }
        persistCurrentGame()
    }

    fun exportOriginalText(): String = SudokuTextExporter.exportOriginal(
        currentBoard = mutableUiState.value.sudoku,
        format = latestSettings.exportFormat,
    )

    fun exportCurrentText(): String = SudokuTextExporter.exportCurrent(
        sudoku = mutableUiState.value.sudoku,
        format = latestSettings.exportFormat,
        includeCandidates = latestSettings.includeCandidatesInCurrentExport,
    )

    fun restartGame() {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete || state.gameId == null) return

        timerJob?.cancel()
        timerJob = null
        hintJob?.cancel()
        hintJob = null
        history.clear()
        redoHistory.clear()
        logicalSolverState = null
        logicalStateBoard = null
        mutableUiState.update {
            it.copy(
                sudoku = SudokuTextExporter.originalPuzzle(state.sudoku),
                timeSpentSeconds = 0,
                selectedCell = null,
                selectedNumber = null,
                isNoteMode = false,
                canUndo = false,
                canRedo = false,
                isComplete = false,
                isAdvancedNoteMode = false,
                advancedNotes = AdvancedNotes(),
                advancedInputTool = AdvancedInputTool.NONE,
                pendingLineStart = null,
                lastAdvancedDigit = null,
                isHintLoading = false,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }
        persistCurrentGame()
        startTimerIfNeeded()
    }

    fun fillCandidates() {
        val state = mutableUiState.value
        if (!state.isLoading && !state.hasLoadError && !state.isComplete) {
            updateSudoku(CandidateCalculator.calculateAllCandidates(state.sudoku))
        }
    }

    fun prepareHintTrace() {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        val sudokuSnapshot = state.sudoku
        val solverStateSnapshot = logicalSolverState
            ?.takeIf { logicalStateBoard == sudokuSnapshot }
            ?: SolverState.fromSudoku(sudokuSnapshot)
        hintJob?.cancel()
        mutableUiState.update {
            it.copy(
                isHintLoading = true,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }

        hintJob = viewModelScope.launch {
            try {
                val cachedOriginalSolution = originalSolution
                val computation = withContext(solverDispatcher) {
                    val solvedOriginal = cachedOriginalSolution
                        ?: solutionResolver.resolve(sudokuSnapshot)
                    val issue = solvedOriginal?.let { solution ->
                        HintIssueDetector.find(sudokuSnapshot, solution)
                    }
                    HintComputation(
                        originalSolution = solvedOriginal,
                        issue = issue,
                        trace = if (issue == null) {
                            humanSolver.solveTrace(solverStateSnapshot)
                        } else {
                            null
                        },
                    )
                }
                if (mutableUiState.value.sudoku != sudokuSnapshot) return@launch
                if (originalSolution == null && computation.originalSolution != null) {
                    originalSolution = computation.originalSolution
                    persistCurrentGame()
                }

                mutableUiState.update {
                    it.copy(
                        isHintLoading = false,
                        hintTrace = computation.trace,
                        hintIssue = computation.issue,
                        selectedHintStepIndex = 0,
                        isHintDetailsRevealed = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                if (mutableUiState.value.sudoku == sudokuSnapshot) {
                    mutableUiState.update {
                        it.copy(
                            isHintLoading = false,
                            hintTrace = null,
                            hintIssue = null,
                            selectedHintStepIndex = 0,
                            isHintDetailsRevealed = false,
                        )
                    }
                }
            }
        }
    }

    fun selectHintStep(index: Int) {
        mutableUiState.update { state ->
            val lastIndex = state.hintTrace?.steps?.lastIndex ?: return@update state
            state.copy(selectedHintStepIndex = index.coerceIn(0, lastIndex))
        }
    }

    fun revealHintDetails() {
        if (mutableUiState.value.hintTrace?.steps?.isNotEmpty() == true) {
            mutableUiState.update { it.copy(isHintDetailsRevealed = true) }
        }
    }

    fun applyNextHintStep(): Boolean {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return false
        if (state.hintIssue != null) return false

        val step = state.hintTrace?.steps?.firstOrNull() ?: return false
        val nextSolverState = state.hintTrace.initialState.apply(step)
        var updatedBoard = state.sudoku

        step.placements.forEach { placement ->
            updatedBoard = updatedBoard.setCell(
                row = placement.cell.row,
                col = placement.cell.col,
                value = placement.digit,
            )
        }
        val shouldShowLogicalCandidates = step.eliminations.isNotEmpty() ||
            state.sudoku.cells.any { it.isCandidateSetExplicit }
        if (shouldShowLogicalCandidates) {
            updatedBoard = Sudoku(
                updatedBoard.cells.mapIndexed { index, cell ->
                    if (cell.isSolved()) {
                        cell
                    } else {
                        cell.copy(
                            candidates = nextSolverState.candidatesAt(CellRef.fromIndex(index)),
                            isCandidateSetExplicit = true,
                        )
                    }
                }
            )
        }

        val focusCell = step.placements.firstOrNull()?.cell
            ?: step.eliminations.firstOrNull()?.candidate?.cell
        if (focusCell != null) {
            mutableUiState.update {
                it.copy(selectedCell = CellPosition(focusCell.row, focusCell.col))
            }
        }
        updateSudoku(updatedBoard, nextSolverState)
        return true
    }

    fun applyHintAction(): Boolean {
        val issue = mutableUiState.value.hintIssue ?: return applyNextHintStep()
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return false
        if (issue is HintIssue.MissingCandidates && !state.showErrorDetails) {
            clearHintTrace()
            return true
        }

        var updatedBoard = state.sudoku
        val focusCell = when (issue) {
            is HintIssue.IncorrectValues -> {
                issue.entries.forEach { entry ->
                    updatedBoard = updatedBoard.setCell(
                        row = entry.cell.row,
                        col = entry.cell.col,
                        value = 0,
                    )
                }
                issue.entries.first().cell
            }
            is HintIssue.MissingCandidates -> {
                val updatedCells = updatedBoard.cells.toMutableList()
                issue.candidates.forEach { candidate ->
                    val index = candidate.cell.index
                    val cell = updatedCells[index]
                    if (cell.value == 0) {
                        updatedCells[index] = cell.copy(
                            candidates = cell.candidates + candidate.digit,
                            isCandidateSetExplicit = true,
                        )
                    }
                }
                updatedBoard = Sudoku(updatedCells)
                issue.candidates.first().cell
            }
        }

        mutableUiState.update {
            it.copy(selectedCell = CellPosition(focusCell.row, focusCell.col))
        }
        updateSudoku(updatedBoard)
        return true
    }

    fun clearHintTrace() {
        hintJob?.cancel()
        hintJob = null
        mutableUiState.update {
            it.copy(
                isHintLoading = false,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }
    }

    fun clearPersistenceError() {
        mutableUiState.update { it.copy(hasPersistenceError = false) }
    }

    private fun updateAdvancedFeature(transform: (AdvancedNotes) -> AdvancedNotes) {
        val state = mutableUiState.value
        if (!state.isAdvancedMode || state.isLoading || state.hasLoadError || state.isComplete) {
            return
        }
        val updatedNotes = transform(state.advancedNotes)
        mutableUiState.update {
            it.copy(
                isAdvancedNoteMode = true,
                isNoteMode = false,
                selectedNumber = null,
                advancedNotes = updatedNotes,
            )
        }
        persistCurrentGame()
    }

    private fun activateAdvancedTool(
        selectTool: (AdvancedInputTool) -> AdvancedInputTool,
    ) {
        val state = mutableUiState.value
        if (!state.isAdvancedMode || state.isLoading || state.hasLoadError || state.isComplete) {
            return
        }
        mutableUiState.update {
            val selectedTool = selectTool(it.advancedInputTool)
            it.copy(
                isAdvancedNoteMode = true,
                isNoteMode = false,
                selectedNumber = null,
                advancedInputTool = selectedTool,
                selectedAdvancedColorIndex = if (
                    it.advancedInputTool == AdvancedInputTool.NONE &&
                    selectedTool != AdvancedInputTool.NONE
                ) {
                    0
                } else {
                    it.selectedAdvancedColorIndex
                },
                pendingLineStart = null,
            )
        }
    }

    private fun applyAdvancedColor(row: Int, col: Int) {
        val state = mutableUiState.value
        val cellIndex = row * Sudoku.GRID_SIZE + col
        val cell = state.sudoku.cells[cellIndex]
        val updatedNotes = if (state.selectedAdvancedColorIndex == AdvancedNoteColor.entries.size) {
            state.advancedNotes.copy(
                cellColors = state.advancedNotes.cellColors - cellIndex,
                candidateColors = state.advancedNotes.candidateColors.filterKeys { key ->
                    AdvancedNotes.candidateCellIndex(key) != cellIndex
                },
            )
        } else {
            val color = AdvancedNoteColor.entries[state.selectedAdvancedColorIndex]
            val selectedCandidates = state.advancedNotes.highlightedDigits
                .filterTo(sortedSetOf()) { it in cell.candidates }
            if (selectedCandidates.isEmpty()) {
                state.advancedNotes.copy(
                    cellColors = state.advancedNotes.cellColors + (cellIndex to color),
                )
            } else {
                val colors = state.advancedNotes.candidateColors.toMutableMap()
                selectedCandidates.forEach { digit ->
                    colors[AdvancedNotes.candidateKey(cellIndex, digit)] = color
                }
                state.advancedNotes.copy(candidateColors = colors)
            }
        }
        if (updatedNotes != state.advancedNotes) {
            mutableUiState.update { it.copy(advancedNotes = updatedNotes) }
            persistCurrentGame()
        }
    }

    private fun selectAdvancedLineEndpoint(row: Int, col: Int) {
        val state = mutableUiState.value
        val style = when (state.advancedInputTool) {
            AdvancedInputTool.SOLID_LINE -> AdvancedNoteLineStyle.SOLID
            AdvancedInputTool.DASHED_LINE -> AdvancedNoteLineStyle.DASHED
            else -> return
        }
        val endpoint = endpointForCell(
            sudoku = state.sudoku,
            cellIndex = row * Sudoku.GRID_SIZE + col,
            highlightedDigits = state.advancedNotes.highlightedDigits,
            preferredDigit = state.lastAdvancedDigit,
        )
        val start = state.pendingLineStart
        if (start == null) {
            mutableUiState.update { it.copy(pendingLineStart = endpoint) }
            return
        }
        if (start == endpoint) {
            mutableUiState.update { it.copy(pendingLineStart = null) }
            return
        }

        val existingIndex = state.advancedNotes.lines.indexOfFirst { line ->
            line.style == style && line.hasEndpoints(start, endpoint)
        }
        val lines = state.advancedNotes.lines.toMutableList()
        val color = AdvancedNoteColor.entries.getOrNull(state.selectedAdvancedColorIndex)
        when {
            color == null && existingIndex >= 0 -> lines.removeAt(existingIndex)
            color == null -> Unit
            existingIndex < 0 -> lines += AdvancedNoteLine(start, endpoint, style, color)
            lines[existingIndex].color == color -> lines.removeAt(existingIndex)
            else -> lines[existingIndex] = AdvancedNoteLine(start, endpoint, style, color)
        }
        val updatedNotes = state.advancedNotes.copy(lines = lines)
        mutableUiState.update {
            it.copy(
                advancedNotes = updatedNotes,
                pendingLineStart = null,
            )
        }
        if (updatedNotes != state.advancedNotes) persistCurrentGame()
    }

    private fun endpointForCell(
        sudoku: Sudoku,
        cellIndex: Int,
        highlightedDigits: Set<Int>,
        preferredDigit: Int?,
    ): AdvancedNoteEndpoint {
        val cell = sudoku.cells[cellIndex]
        val candidateDigit = sequenceOf(preferredDigit)
            .plus(highlightedDigits.asSequence().sorted())
            .filterNotNull()
            .distinct()
            .firstOrNull { it in cell.candidates }
        return AdvancedNoteEndpoint(cellIndex, candidateDigit)
    }

    private fun loadGame() {
        timerJob?.cancel()
        timerJob = null
        hintJob?.cancel()
        hintJob = null
        logicalSolverState = null
        logicalStateBoard = null
        originalSolution = null
        history.clear()
        redoHistory.clear()
        mutableUiState.value = GameUiState(
            isLoading = true,
            difficulty = newGameDifficulty ?: Difficulty.MEDIUM,
        ).withGameSettings(latestSettings)

        viewModelScope.launch {
            try {
                val game = if (savedGameId == null) {
                    gameRepository.createGame(
                        requireNotNull(newGameDifficulty) {
                            "A new game requires a difficulty."
                        }
                    )
                } else {
                    requireNotNull(gameRepository.getGame(savedGameId)) {
                        "Saved game $savedGameId does not exist."
                    }
                }
                originalSolution = game.solution
                val isComplete = SudokuValidator.isSolved(game.sudoku)

                mutableUiState.value = GameUiState(
                    isLoading = false,
                    gameId = game.id.takeUnless { isComplete },
                    difficulty = game.difficulty,
                    sudoku = game.sudoku,
                    advancedNotes = game.advancedNotes,
                    timeSpentSeconds = game.timeSpentSeconds,
                    isComplete = isComplete,
                ).withGameSettings(latestSettings)
                if (isComplete) {
                    enqueuePersistence { gameRepository.deleteGame(game.id) }
                }
                startTimerIfNeeded()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(isLoading = false, hasLoadError = true)
                }
            }
        }
    }

    private fun applyNumber(
        row: Int,
        col: Int,
        number: Int,
        noteMode: Boolean,
    ) {
        val board = mutableUiState.value.sudoku
        val cell = board.getCell(row, col)
        val candidateWasRemoved = noteMode && number in cell.candidates
        val newBoard = when {
            cell.isFixed -> return
            noteMode && !cell.isSolved() -> board.toggleCandidate(row, col, number)
            !noteMode && cell.value != number -> board.setCell(row, col, number)
            else -> return
        }
        updateSudoku(newBoard)

        val state = mutableUiState.value
        val solution = originalSolution
        if (!state.showErrorsImmediately || solution == null || state.isComplete) return
        val index = row * Sudoku.GRID_SIZE + col
        val isNonConflictingWrongEntry = !noteMode &&
            number != solution[index] &&
            !SudokuValidator.hasConflict(newBoard, row, col)
        val isRequiredCandidateRemoval = candidateWasRemoved && number == solution[index]
        if (!isNonConflictingWrongEntry && !isRequiredCandidateRemoval) return

        val issue = HintIssueDetector.find(newBoard, solution) ?: return
        hintJob?.cancel()
        hintJob = null
        mutableUiState.update {
            it.copy(
                isHintLoading = false,
                hintTrace = null,
                hintIssue = issue,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
                immediateHintRequestId = it.immediateHintRequestId + 1,
            )
        }
    }

    private fun updateSudoku(
        newSudoku: Sudoku,
        nextSolverState: SolverState? = null,
    ) {
        val previousState = mutableUiState.value
        if (newSudoku == previousState.sudoku) {
            logicalSolverState = nextSolverState
            logicalStateBoard = newSudoku.takeIf { nextSolverState != null }
            clearHintTrace()
            return
        }

        hintJob?.cancel()
        hintJob = null
        history.addLast(previousState.sudoku)
        redoHistory.clear()
        val isComplete = SudokuValidator.isSolved(newSudoku)
        val completedGameId = previousState.gameId.takeIf { isComplete }

        mutableUiState.update {
            it.copy(
                sudoku = newSudoku,
                gameId = if (isComplete) null else it.gameId,
                canUndo = history.isNotEmpty() && !isComplete,
                canRedo = false,
                isComplete = isComplete,
                isHintLoading = false,
                hintTrace = null,
                hintIssue = null,
                selectedHintStepIndex = 0,
                isHintDetailsRevealed = false,
            )
        }
        logicalSolverState = nextSolverState
        logicalStateBoard = newSudoku.takeIf { nextSolverState != null }

        if (completedGameId != null) {
            timerJob?.cancel()
            timerJob = null
            enqueuePersistence {
                gameRepository.deleteGame(completedGameId)
            }
        } else {
            persistCurrentGame()
        }
    }

    private fun startTimerIfNeeded() {
        val state = mutableUiState.value
        if (!isResumed || state.isLoading || state.hasLoadError || state.isComplete) return
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                mutableUiState.update {
                    it.copy(timeSpentSeconds = it.timeSpentSeconds + 1)
                }
            }
        }
    }

    private fun persistCurrentGame() {
        val state = mutableUiState.value
        val gameId = state.gameId ?: return
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        val game = SavedGame(
            id = gameId,
            difficulty = state.difficulty,
            sudoku = state.sudoku,
            solution = originalSolution,
            advancedNotes = state.advancedNotes,
            timeSpentSeconds = state.timeSpentSeconds,
            lastPlayedEpochMillis = currentTimeMillis(),
        )
        enqueuePersistence {
            gameRepository.saveGame(game)
        }
    }

    private fun enqueuePersistence(block: suspend () -> Unit) {
        val previousJob = persistenceJob
        persistenceJob = viewModelScope.launch {
            try {
                previousJob?.join()
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update { it.copy(hasPersistenceError = true) }
            }
        }
    }

    companion object {
        fun factory(
            repository: GameRepository,
            newGameDifficulty: Difficulty?,
            savedGameId: Long?,
            settings: StateFlow<AppSettings>,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel(
                    gameRepository = repository,
                    newGameDifficulty = newGameDifficulty,
                    savedGameId = savedGameId,
                    settings = settings,
                )
            }
        }
    }
}

private data class HintComputation(
    val originalSolution: SudokuSolution?,
    val issue: HintIssue?,
    val trace: SolveTrace?,
)

private fun GameUiState.withGameSettings(settings: AppSettings): GameUiState = copy(
    showHintDetails = settings.showHintDetails,
    showErrorDetails = settings.showErrorDetails,
    showErrorsImmediately = settings.showErrorsImmediately,
)
