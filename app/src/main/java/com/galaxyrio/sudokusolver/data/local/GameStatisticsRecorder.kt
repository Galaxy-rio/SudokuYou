package com.galaxyrio.sudokusolver.data.local

import com.galaxyrio.sudokusolver.domain.game.SudokuValidator

internal fun GameStatisticsEntity.recordGameSave(
    previous: GameEntity?,
    current: GameEntity,
    currentGeneration: Long,
): GameStatisticsEntity {
    val previousWasRecordedInCurrentGeneration =
        previous?.statisticsGeneration == currentGeneration
    val isRestart = previousWasRecordedInCurrentGeneration &&
        current.timeSpent < requireNotNull(previous).timeSpent
    val startsNewStatisticsSession = previous == null ||
        !previousWasRecordedInCurrentGeneration ||
        isRestart

    val additionalPlayTime = when {
        previous == null -> current.timeSpent
        isRestart -> current.timeSpent
        else -> (current.timeSpent - previous.timeSpent).coerceAtLeast(0)
    }

    val isComplete = SudokuValidator.isSolved(current.sudoku)
    val wasComplete = previous?.let { SudokuValidator.isSolved(it.sudoku) } == true
    val completedNow = isComplete && !wasComplete
    val completionTime = current.timeSpent.takeIf { completedNow }

    return copy(
        totalPlayTimeSeconds = totalPlayTimeSeconds + additionalPlayTime,
        gamesStarted = gamesStarted + if (startsNewStatisticsSession) 1 else 0,
        gamesCompleted = gamesCompleted + if (completedNow) 1 else 0,
        totalCompletionTimeSeconds =
            totalCompletionTimeSeconds + (completionTime ?: 0),
        bestCompletionTimeSeconds = when {
            completionTime == null -> bestCompletionTimeSeconds
            bestCompletionTimeSeconds == null -> completionTime
            else -> minOf(bestCompletionTimeSeconds, completionTime)
        },
    )
}
