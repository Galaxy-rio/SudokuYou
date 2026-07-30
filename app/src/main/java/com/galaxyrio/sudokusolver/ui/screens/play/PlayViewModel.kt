package com.galaxyrio.sudokusolver.ui.screens.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
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
    val completionPercentage: Int,
    val emptyCells: Int,
    val timeSpentSeconds: Long,
    val board: List<Int>,
)

data class PlayUiState(
    val savedGames: List<SavedGameSummary> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val hasActionError: Boolean = false,
)

class PlayViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val actionError = MutableStateFlow(false)
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
    ) { state, hasActionError ->
        state.copy(hasActionError = hasActionError)
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
        completionPercentage = solvedCells * 100 / sudoku.cells.size,
        emptyCells = sudoku.cells.size - solvedCells,
        timeSpentSeconds = timeSpentSeconds,
        board = sudoku.cells.map { it.value },
    )
}
