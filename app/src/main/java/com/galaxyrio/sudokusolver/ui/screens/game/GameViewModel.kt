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
import java.util.ArrayDeque
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val isHintUnavailable: Boolean = false,
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
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(
        GameUiState(difficulty = newGameDifficulty ?: Difficulty.MEDIUM)
    )
    val uiState: StateFlow<GameUiState> = mutableUiState.asStateFlow()

    private val history = ArrayDeque<Sudoku>()
    private var timerJob: Job? = null
    private var persistenceJob: Job? = null
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

    fun onCellSelected(row: Int, col: Int) {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return

        val position = CellPosition(row, col)
        val newSelection = if (state.selectedCell == position) null else position
        mutableUiState.update { it.copy(selectedCell = newSelection, isHintUnavailable = false) }

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
                it.copy(
                    selectedNumber = if (it.selectedNumber == number) null else number,
                    isHintUnavailable = false,
                )
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
        mutableUiState.update {
            it.copy(selectedCell = null, isHintUnavailable = false)
        }
    }

    fun toggleNoteMode() {
        if (!mutableUiState.value.isComplete) {
            mutableUiState.update {
                it.copy(isNoteMode = !it.isNoteMode, isHintUnavailable = false)
            }
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

        val previousBoard = history.removeLast()
        mutableUiState.update {
            it.copy(
                sudoku = previousBoard,
                canUndo = history.isNotEmpty(),
                isHintUnavailable = false,
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

    fun applySingleCandidateHint(): Boolean {
        val state = mutableUiState.value
        if (state.isLoading || state.hasLoadError || state.isComplete) return false

        val boardWithCandidates = CandidateCalculator.calculateAllCandidates(state.sudoku)
        val targetIndex = boardWithCandidates.cells.indexOfFirst { cell ->
            !cell.isSolved() && cell.candidates.size == 1
        }

        if (targetIndex == -1) {
            mutableUiState.update { it.copy(isHintUnavailable = true) }
            return false
        }

        val row = targetIndex / Sudoku.GRID_SIZE
        val col = targetIndex % Sudoku.GRID_SIZE
        val number = boardWithCandidates.cells[targetIndex].candidates.single()
        mutableUiState.update {
            it.copy(
                sudoku = boardWithCandidates,
                selectedCell = CellPosition(row, col),
                isHintUnavailable = false,
            )
        }
        updateSudoku(boardWithCandidates.setCell(row, col, number))
        return true
    }

    fun clearHintMessage() {
        mutableUiState.update { it.copy(isHintUnavailable = false) }
    }

    fun clearPersistenceError() {
        mutableUiState.update { it.copy(hasPersistenceError = false) }
    }

    private fun loadGame() {
        timerJob?.cancel()
        timerJob = null
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

    private fun updateSudoku(newSudoku: Sudoku) {
        val previousState = mutableUiState.value
        if (newSudoku == previousState.sudoku) return

        history.addLast(previousState.sudoku)
        val isComplete = SudokuValidator.isSolved(newSudoku)
        val completedGameId = previousState.gameId.takeIf { isComplete }

        mutableUiState.update {
            it.copy(
                sudoku = newSudoku,
                gameId = if (isComplete) null else it.gameId,
                canUndo = history.isNotEmpty() && !isComplete,
                isComplete = isComplete,
                isHintUnavailable = false,
            )
        }

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
