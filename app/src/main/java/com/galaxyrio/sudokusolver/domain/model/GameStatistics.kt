package com.galaxyrio.sudokusolver.domain.model

data class GameStatistics(
    val difficulty: Difficulty,
    val totalPlayTimeSeconds: Long = 0,
    val gamesStarted: Long = 0,
    val gamesCompleted: Long = 0,
    val totalCompletionTimeSeconds: Long = 0,
    val bestCompletionTimeSeconds: Long? = null,
) {
    val averageCompletionTimeSeconds: Long?
        get() = totalCompletionTimeSeconds
            .takeIf { gamesCompleted > 0 }
            ?.div(gamesCompleted)
}
