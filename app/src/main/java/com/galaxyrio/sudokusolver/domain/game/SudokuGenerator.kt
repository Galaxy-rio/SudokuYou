package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlin.math.abs
import kotlin.random.Random

class SudokuGenerator(
    private val random: Random = Random.Default,
    private val difficultyEvaluator: PuzzleDifficultyEvaluator = PuzzleDifficultyEvaluator(),
) {
    /**
     * Generates a unique puzzle whose complete human-solver trace belongs to [difficulty].
     * Clue ranges only guide the digging search; they never decide the returned difficulty.
     */
    fun generate(difficulty: Difficulty): Sudoku {
        val profile = GenerationProfile.forDifficulty(difficulty)
        var best: RatedCandidate? = null

        repeat(profile.maxAttempts) {
            val grid = ValidSudokuGenerator(random).generateFullGrid()
            val positions = (0 until Sudoku.CELL_COUNT).shuffled(random)
            var clueCount = Sudoku.CELL_COUNT

            for (index in positions) {
                if (clueCount <= profile.minimumClues) break
                val row = index / Sudoku.GRID_SIZE
                val column = index % Sudoku.GRID_SIZE
                val removed = grid[row][column]
                if (removed == 0) continue

                grid[row][column] = 0
                if (countSolutions(grid) != 1) {
                    grid[row][column] = removed
                    continue
                }
                clueCount--

                if (clueCount > profile.startRatingAtClues) continue
                val puzzle = grid.toSudoku()
                val rating = difficultyEvaluator.evaluate(puzzle)
                if (rating?.difficulty == difficulty) {
                    val candidate = RatedCandidate(
                        sudoku = puzzle,
                        clueCount = clueCount,
                        pathScore = rating.pathScore,
                    )
                    val previousBest = best
                    if (previousBest == null || candidate.isBetterThan(previousBest, profile)) {
                        best = candidate
                    }
                    if (clueCount <= profile.preferredClues) return puzzle
                } else if (rating == null || rating.difficulty.ordinal > difficulty.ordinal) {
                    // This removal left the requested logical envelope. Keep searching other clues
                    // from the last board that still had a complete in-range solve path.
                    grid[row][column] = removed
                    clueCount++
                }
            }

            best?.let { return it.sudoku }
        }

        error("Unable to generate a ${difficulty.name.lowercase()} puzzle with a complete trace.")
    }

    /**
     * Low-level unique-puzzle generator retained for tests and import tools. It deliberately does
     * not assign a difficulty; normal app code must call [generate] with a [Difficulty].
     */
    fun generateWithClueCount(clues: Int = 30): Sudoku {
        require(clues in 17 until Sudoku.CELL_COUNT) {
            "Clue count must be between 17 and 80."
        }

        val grid = ValidSudokuGenerator(random).generateFullGrid()
        val positions = (0 until Sudoku.CELL_COUNT).shuffled(random)
        var currentClues = Sudoku.CELL_COUNT

        for (index in positions) {
            if (currentClues <= clues) break
            val row = index / Sudoku.GRID_SIZE
            val column = index % Sudoku.GRID_SIZE
            val removed = grid[row][column]
            grid[row][column] = 0
            if (countSolutions(grid) == 1) {
                currentClues--
            } else {
                grid[row][column] = removed
            }
        }
        return grid.toSudoku()
    }

    private fun countSolutions(grid: Array<IntArray>): Int {
        var bestRow = -1
        var bestColumn = -1
        var bestMask = 0
        var bestCount = Sudoku.GRID_SIZE + 1

        for (row in 0 until Sudoku.GRID_SIZE) {
            for (column in 0 until Sudoku.GRID_SIZE) {
                if (grid[row][column] != 0) continue
                val mask = availableMask(grid, row, column)
                val count = mask.countOneBits()
                if (count == 0) return 0
                if (count < bestCount) {
                    bestRow = row
                    bestColumn = column
                    bestMask = mask
                    bestCount = count
                    if (count == 1) break
                }
            }
            if (bestCount == 1) break
        }

        if (bestRow == -1) return 1
        var solutions = 0
        var remaining = bestMask
        while (remaining != 0 && solutions < 2) {
            val bit = remaining and -remaining
            val digit = bit.countTrailingZeroBits() + 1
            grid[bestRow][bestColumn] = digit
            solutions += countSolutions(grid)
            grid[bestRow][bestColumn] = 0
            remaining = remaining xor bit
        }
        return solutions.coerceAtMost(2)
    }

    private fun availableMask(
        grid: Array<IntArray>,
        row: Int,
        column: Int,
    ): Int {
        var used = 0
        repeat(Sudoku.GRID_SIZE) { index ->
            val rowValue = grid[row][index]
            val columnValue = grid[index][column]
            if (rowValue != 0) used = used or (1 shl (rowValue - 1))
            if (columnValue != 0) used = used or (1 shl (columnValue - 1))
        }
        val startRow = row - row % Sudoku.BOX_SIZE
        val startColumn = column - column % Sudoku.BOX_SIZE
        repeat(Sudoku.BOX_SIZE) { rowOffset ->
            repeat(Sudoku.BOX_SIZE) { columnOffset ->
                val value = grid[startRow + rowOffset][startColumn + columnOffset]
                if (value != 0) used = used or (1 shl (value - 1))
            }
        }
        return FULL_DIGIT_MASK and used.inv()
    }

    private fun Array<IntArray>.toSudoku(): Sudoku = Sudoku(
        flatMap { row ->
            row.map { value -> Cell(value = value, isFixed = value != 0) }
        }
    )

    private data class RatedCandidate(
        val sudoku: Sudoku,
        val clueCount: Int,
        val pathScore: Int,
    ) {
        fun isBetterThan(other: RatedCandidate, profile: GenerationProfile): Boolean {
            val distance = abs(clueCount - profile.preferredClues)
            val otherDistance = abs(other.clueCount - profile.preferredClues)
            return distance < otherDistance ||
                (distance == otherDistance && pathScore > other.pathScore)
        }
    }

    private data class GenerationProfile(
        val preferredClues: Int,
        val minimumClues: Int,
        val startRatingAtClues: Int,
        val maxAttempts: Int,
    ) {
        companion object {
            fun forDifficulty(difficulty: Difficulty): GenerationProfile = when (difficulty) {
                Difficulty.EASY -> GenerationProfile(
                    preferredClues = 40,
                    minimumClues = 34,
                    startRatingAtClues = 44,
                    maxAttempts = 8,
                )
                Difficulty.MEDIUM -> GenerationProfile(
                    preferredClues = 32,
                    minimumClues = 27,
                    startRatingAtClues = 38,
                    maxAttempts = 20,
                )
                Difficulty.HARD -> GenerationProfile(
                    preferredClues = 26,
                    minimumClues = 21,
                    startRatingAtClues = 32,
                    maxAttempts = 40,
                )
                Difficulty.BRUTAL -> GenerationProfile(
                    preferredClues = 23,
                    minimumClues = 17,
                    startRatingAtClues = 29,
                    maxAttempts = 80,
                )
            }
        }
    }

    private companion object {
        const val FULL_DIGIT_MASK = (1 shl Sudoku.GRID_SIZE) - 1
    }
}
