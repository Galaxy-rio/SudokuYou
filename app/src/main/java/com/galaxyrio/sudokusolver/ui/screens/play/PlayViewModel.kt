package com.galaxyrio.sudokusolver.ui.screens.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.game.SudokuTextImporter
import com.galaxyrio.sudokusolver.domain.game.SudokuValidator
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedGameSummary(
    val id: Long,
    val lastPlayedEpochMillis: Long,
    val difficulty: Difficulty,
    val emptyCells: Int,
    val timeSpentSeconds: Long,
    val board: List<Int>,
    val isComplete: Boolean,
)

enum class PlayImportError {
    INVALID_FORMAT,
    NO_UNIQUE_SOLUTION,
    SAVE_FAILED,
}

data class PlayUiState(
    val savedGames: List<SavedGameSummary> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val hasActionError: Boolean = false,
    val isImporting: Boolean = false,
    val importError: PlayImportError? = null,
    val importedGameId: Long? = null,
)

private data class ImportState(
    val isImporting: Boolean = false,
    val error: PlayImportError? = null,
    val importedGameId: Long? = null,
)

class PlayViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val actionError = MutableStateFlow(false)
    private val importState = MutableStateFlow(ImportState())
    private val savedGamesState = gameRepository.savedGames
        .map { games ->
            PlayUiState(
                savedGames = games.map(SavedGame::asSummary),
                isLoading = false,
            )
        }
        .catch {
            emit(PlayUiState(isLoading = false, hasLoadError = true))
        }

    val uiState: StateFlow<PlayUiState> = combine(
        savedGamesState,
        actionError,
        importState,
    ) { state, hasActionError, currentImportState ->
        state.copy(
            hasActionError = hasActionError,
            isImporting = currentImportState.isImporting,
            importError = currentImportState.error,
            importedGameId = currentImportState.importedGameId,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = PlayUiState(),
        )

    fun deleteGames(gameIds: Set<Long>) {
        viewModelScope.launch {
            try {
                gameRepository.deleteGames(gameIds)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }

    fun clearActionError() {
        actionError.value = false
    }

    fun importGame(format: SudokuExportFormat, text: String) {
        if (importState.value.isImporting) return
        viewModelScope.launch {
            importState.value = ImportState(isImporting = true)
            try {
                val sudoku = SudokuTextImporter.import(text, format)
                if (sudoku == null) {
                    importState.value = ImportState(error = PlayImportError.INVALID_FORMAT)
                    return@launch
                }
                val game = gameRepository.createImportedGame(sudoku)
                importState.value = if (game == null) {
                    ImportState(error = PlayImportError.NO_UNIQUE_SOLUTION)
                } else {
                    ImportState(importedGameId = game.id)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                importState.value = ImportState(error = PlayImportError.SAVE_FAILED)
            }
        }
    }

    fun clearImportError() {
        importState.value = importState.value.copy(error = null)
    }

    fun consumeImportedGame() {
        importState.value = importState.value.copy(importedGameId = null)
    }

    companion object {
        fun factory(repository: GameRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayViewModel(repository)
            }
        }
    }
}

private fun SavedGame.asSummary(): SavedGameSummary {
    val solvedCells = sudoku.cells.count { it.isSolved() }
    return SavedGameSummary(
        id = id,
        lastPlayedEpochMillis = lastPlayedEpochMillis,
        difficulty = difficulty,
        emptyCells = sudoku.cells.size - solvedCells,
        timeSpentSeconds = timeSpentSeconds,
        board = sudoku.cells.map { it.value },
        isComplete = SudokuValidator.isSolved(sudoku),
    )
}
