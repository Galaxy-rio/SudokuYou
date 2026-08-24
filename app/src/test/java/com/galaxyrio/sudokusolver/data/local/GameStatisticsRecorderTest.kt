package com.galaxyrio.sudokusolver.data.local

import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStatisticsRecorderTest {
    @Test
    fun firstSaveStartsSessionAndRecordsPlayTime() {
        val result = GameStatisticsEntity(Difficulty.EASY).recordGameSave(
            previous = null,
            current = game(timeSpent = 40, generation = 2),
            currentGeneration = 2,
        )

        assertEquals(1L, result.gamesStarted)
        assertEquals(40L, result.totalPlayTimeSeconds)
        assertEquals(0L, result.gamesCompleted)
    }

    @Test
    fun completionAddsOnlyTheDeltaAndCompletionTime() {
        val previous = game(timeSpent = 40, generation = 2)
        val result = GameStatisticsEntity(
            difficulty = Difficulty.EASY,
            totalPlayTimeSeconds = 40,
            gamesStarted = 1,
        ).recordGameSave(
            previous = previous,
            current = game(sudoku = Sudoku.fromGridString(SOLUTION), timeSpent = 70, generation = 2),
            currentGeneration = 2,
        )

        assertEquals(70L, result.totalPlayTimeSeconds)
        assertEquals(1L, result.gamesStarted)
        assertEquals(1L, result.gamesCompleted)
        assertEquals(70L, result.totalCompletionTimeSeconds)
        assertEquals(70L, result.bestCompletionTimeSeconds)
    }

    @Test
    fun resetGenerationCountsContinuedGameOnce() {
        val beforeReset = game(timeSpent = 100, generation = 0)
        val firstSave = GameStatisticsEntity(Difficulty.EASY).recordGameSave(
            previous = beforeReset,
            current = game(timeSpent = 105, generation = 1),
            currentGeneration = 1,
        )
        val secondSave = firstSave.recordGameSave(
            previous = game(timeSpent = 105, generation = 1),
            current = game(timeSpent = 110, generation = 1),
            currentGeneration = 1,
        )

        assertEquals(1L, secondSave.gamesStarted)
        assertEquals(10L, secondSave.totalPlayTimeSeconds)
    }

    @Test
    fun restartBeginsAnotherSessionWithoutLosingPreviousTime() {
        val result = GameStatisticsEntity(
            difficulty = Difficulty.EASY,
            totalPlayTimeSeconds = 100,
            gamesStarted = 1,
        ).recordGameSave(
            previous = game(timeSpent = 100, generation = 1),
            current = game(timeSpent = 0, generation = 1),
            currentGeneration = 1,
        )

        assertEquals(2L, result.gamesStarted)
        assertEquals(100L, result.totalPlayTimeSeconds)
    }

    private fun game(
        sudoku: Sudoku = Sudoku.fromGridString(PUZZLE),
        timeSpent: Long,
        generation: Long,
    ) = GameEntity(
        id = 1,
        difficulty = Difficulty.EASY,
        sudoku = sudoku,
        timeSpent = timeSpent,
        statisticsGeneration = generation,
    )

    private companion object {
        const val PUZZLE =
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79"

        const val SOLUTION =
            "534678912" +
                "672195348" +
                "198342567" +
                "859761423" +
                "426853791" +
                "713924856" +
                "961537284" +
                "287419635" +
                "345286179"
    }
}
