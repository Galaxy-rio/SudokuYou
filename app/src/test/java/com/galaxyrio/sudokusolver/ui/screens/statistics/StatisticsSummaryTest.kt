package com.galaxyrio.sudokusolver.ui.screens.statistics

import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.GameStatistics
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsSummaryTest {
    private val statistics = listOf(
        GameStatistics(
            difficulty = Difficulty.EASY,
            totalPlayTimeSeconds = 500,
            gamesStarted = 3,
            gamesCompleted = 2,
            totalCompletionTimeSeconds = 300,
            bestCompletionTimeSeconds = 100,
        ),
        GameStatistics(
            difficulty = Difficulty.HARD,
            totalPlayTimeSeconds = 700,
            gamesStarted = 2,
            gamesCompleted = 1,
            totalCompletionTimeSeconds = 300,
            bestCompletionTimeSeconds = 300,
        ),
    )

    @Test
    fun allDifficultiesUsesWeightedCompletionAverage() {
        val summary = statistics.summarize(difficulty = null)

        assertEquals(1_200L, summary.totalPlayTimeSeconds)
        assertEquals(100L, summary.bestCompletionTimeSeconds)
        assertEquals(200L, summary.averageCompletionTimeSeconds)
        assertEquals(5L, summary.gamesStarted)
        assertEquals(3L, summary.gamesCompleted)
    }

    @Test
    fun difficultyFilterIncludesOnlySelectedDifficulty() {
        val summary = statistics.summarize(Difficulty.EASY)

        assertEquals(500L, summary.totalPlayTimeSeconds)
        assertEquals(100L, summary.bestCompletionTimeSeconds)
        assertEquals(150L, summary.averageCompletionTimeSeconds)
        assertEquals(3L, summary.gamesStarted)
        assertEquals(2L, summary.gamesCompleted)
    }
}
