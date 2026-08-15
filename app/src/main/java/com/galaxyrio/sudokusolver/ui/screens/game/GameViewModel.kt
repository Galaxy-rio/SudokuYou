package com.galaxyrio.sudokusolver.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.game.CandidateCalculator
import com.galaxyrio.sudokusolver.domain.game.SudokuValidator
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
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
    val isComplete: Boolean = false,
    val isAdvancedMode: Boolean = false,
    val isHintLoading: Boolean = false,
    val hintTrace: SolveTrace? = null,
    val selectedHintStepIndex: Int = 0,
    val hasPersistenceError: Boolean = false,
) {
    val highlightedNumber: Int?
        get() = selectedNumber ?: selectedCell
            ?.let { sudoku.getCell(it.row, it.col).value }
            ?.takeIf { it != 0 }
}

class GameViewModel(
    private val gameRepository: GameRepository,
    private val newGameDifficulty: Difficulty?,
    private val savedGameId: Long?,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val humanSolver: HumanSolver = HumanSolver(),
    private val solverDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(
        GameUiState(difficulty = newGameDifficulty ?: Difficulty.MEDIUM)
    )
    val uiState: StateFlow<GameUiState> = mutableUiState.asStateFlow()

    private val history = ArrayDeque<Sudoku>()
    private var timerJob: Job? = null
    private var persistenceJob: Job? = null
    private var hintJob: Job? = null
    private var logicalSolverState: SolverState? = null
    private var logicalStateBoard: Sudoku? = null
    private var isResumed = false

    init {
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
        mutableUiState.update { it.copy(isAdvancedMode = enabled) }
    }

    fun onCellSelected(row: Int, col: Int) {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return

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
        if (!cell.isFixed && (cell.isSolved() || cell.candidates.isNotEmpty())) {
            updateSudoku(
                state.sudoku.setCell(selectedCell.row, selectedCell.col, value = 0)
            )
        }
    }

    fun undo() {
        if (history.isEmpty() || mutableUiState.value.isComplete) return

        hintJob?.cancel()
        hintJob = null
        logicalSolverState = null
        logicalStateBoard = null
        val previousBoard = history.removeLast()
        mutableUiState.update {
            it.copy(
                sudoku = previousBoard,
                canUndo = history.isNotEmpty(),
                isHintLoading = false,
                hintTrace = null,
                selectedHintStepIndex = 0,
            )
        }
        persistCurrentGame()
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
                selectedHintStepIndex = 0,
            )
        }

        hintJob = viewModelScope.launch {
            try {
                val trace = withContext(solverDispatcher) {
                    humanSolver.solveTrace(solverStateSnapshot)
                }
                if (mutableUiState.value.sudoku != sudokuSnapshot) return@launch

                mutableUiState.update {
                    it.copy(
                        isHintLoading = false,
                        hintTrace = trace,
                        selectedHintStepIndex = 0,
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
                            selectedHintStepIndex = 0,
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

    fun applyNextHintStep(): Boolean {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return false

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
            state.sudoku.cells.any { it.candidates.isNotEmpty() }
        if (shouldShowLogicalCandidates) {
            updatedBoard = Sudoku(
                updatedBoard.cells.mapIndexed { index, cell ->
                    if (cell.isSolved()) {
                        cell
                    } else {
                        cell.copy(
                            candidates = nextSolverState.candidatesAt(CellRef.fromIndex(index))
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

    fun clearHintTrace() {
        hintJob?.cancel()
        hintJob = null
        mutableUiState.update {
            it.copy(
                isHintLoading = false,
                hintTrace = null,
                selectedHintStepIndex = 0,
            )
        }
    }

    fun clearPersistenceError() {
        mutableUiState.update { it.copy(hasPersistenceError = false) }
    }

    private fun loadGame() {
        timerJob?.cancel()
        timerJob = null
        hintJob?.cancel()
        hintJob = null
        logicalSolverState = null
        logicalStateBoard = null
        history.clear()
        mutableUiState.value = GameUiState(
            isLoading = true,
            difficulty = newGameDifficulty ?: Difficulty.MEDIUM,
        )

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
                val isComplete = SudokuValidator.isSolved(game.sudoku)

                mutableUiState.value = GameUiState(
                    isLoading = false,
                    gameId = game.id.takeUnless { isComplete },
                    difficulty = game.difficulty,
                    sudoku = game.sudoku,
                    timeSpentSeconds = game.timeSpentSeconds,
                    isComplete = isComplete,
                )
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
        val newBoard = when {
            cell.isFixed -> return
            noteMode && !cell.isSolved() -> board.toggleCandidate(row, col, number)
            !noteMode && cell.value != number -> board.setCell(row, col, number)
            else -> return
        }
        updateSudoku(newBoard)
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
        val isComplete = SudokuValidator.isSolved(newSudoku)
        val completedGameId = previousState.gameId.takeIf { isComplete }

        mutableUiState.update {
            it.copy(
                sudoku = newSudoku,
                gameId = if (isComplete) null else it.gameId,
                canUndo = history.isNotEmpty() && !isComplete,
                isComplete = isComplete,
                isHintLoading = false,
                hintTrace = null,
                selectedHintStepIndex = 0,
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
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel(
                    gameRepository = repository,
                    newGameDifficulty = newGameDifficulty,
                    savedGameId = savedGameId,
                )
            }
        }
    }
}
