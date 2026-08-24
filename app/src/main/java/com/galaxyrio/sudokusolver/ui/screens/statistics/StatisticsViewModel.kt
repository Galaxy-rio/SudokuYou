package com.galaxyrio.sudokusolver.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.GameStatistics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val statistics: List<GameStatistics> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val isClearing: Boolean = false,
    val hasActionError: Boolean = false,
) {
    val hasStatistics: Boolean
        get() = statistics.any { item ->
            item.totalPlayTimeSeconds > 0 ||
                item.gamesStarted > 0 ||
                item.gamesCompleted > 0
        }
}

data class StatisticsSummary(
    val totalPlayTimeSeconds: Long = 0,
    val bestCompletionTimeSeconds: Long? = null,
    val averageCompletionTimeSeconds: Long? = null,
    val gamesStarted: Long = 0,
    val gamesCompleted: Long = 0,
)

class StatisticsViewModel(
    private val gameRepository: GameRepository,
) : ViewModel() {
    private val actionState = MutableStateFlow(StatisticsActionState())
    private val statisticsState = gameRepository.statistics
        .map { statistics ->
            StatisticsUiState(
                statistics = statistics,
                isLoading = false,
            )
        }
        .catch {
            emit(StatisticsUiState(isLoading = false, hasLoadError = true))
        }

    val uiState: StateFlow<StatisticsUiState> = combine(
        statisticsState,
        actionState,
    ) { state, action ->
        state.copy(
            isClearing = action.isClearing,
            hasActionError = action.hasError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = StatisticsUiState(),
    )

    fun clearStatistics() {
        if (actionState.value.isClearing) return
        viewModelScope.launch {
            actionState.value = StatisticsActionState(isClearing = true)
            try {
                gameRepository.clearStatistics()
                actionState.value = StatisticsActionState()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                actionState.value = StatisticsActionState(hasError = true)
            }
        }
    }

    fun clearActionError() {
        actionState.value = actionState.value.copy(hasError = false)
    }

    companion object {
        fun factory(repository: GameRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { StatisticsViewModel(repository) }
        }
    }
}

fun List<GameStatistics>.summarize(
    difficulty: Difficulty?,
): StatisticsSummary {
    val selected = difficulty?.let { target -> filter { it.difficulty == target } } ?: this
    val gamesCompleted = selected.sumOf(GameStatistics::gamesCompleted)
    val totalCompletionTime = selected.sumOf(GameStatistics::totalCompletionTimeSeconds)
    return StatisticsSummary(
        totalPlayTimeSeconds = selected.sumOf(GameStatistics::totalPlayTimeSeconds),
        bestCompletionTimeSeconds = selected
            .mapNotNull(GameStatistics::bestCompletionTimeSeconds)
            .minOrNull(),
        averageCompletionTimeSeconds = totalCompletionTime
            .takeIf { gamesCompleted > 0 }
            ?.div(gamesCompleted),
        gamesStarted = selected.sumOf(GameStatistics::gamesStarted),
        gamesCompleted = gamesCompleted,
    )
}

private data class StatisticsActionState(
    val isClearing: Boolean = false,
    val hasError: Boolean = false,
)
