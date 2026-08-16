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
    private val uniquenessChecker = UniqueSolutionChecker()

    /**
     * Generates a unique puzzle whose complete human-solver trace belongs to [difficulty].
     * Clue ranges only guide the digging search; they never decide the returned difficulty.
     */
    fun generate(difficulty: Difficulty): Sudoku {
        val profile = GenerationProfile.forDifficulty(difficulty)
        var best: RatedCandidate? = null
        var solution = ValidSudokuGenerator(random).generateFullGrid()
        var hittingSetConstraints = HittingSetConstraints()

        repeat(profile.maxAttempts) { attempt ->
            if (attempt > 0 && attempt % DIG_PATHS_PER_SOLUTION == 0) {
                solution = ValidSudokuGenerator(random).generateFullGrid()
                hittingSetConstraints = HittingSetConstraints()
            }

            val grid = solution.deepCopy()
            val positions = (0 until Sudoku.CELL_COUNT).shuffled(random)
            var clueCount = Sudoku.CELL_COUNT
            var clueMask = CellMask81.FULL

            for (index in positions) {
                if (clueCount <= profile.minimumClues) break
                val row = index / Sudoku.GRID_SIZE
                val column = index % Sudoku.GRID_SIZE
                val removed = grid[row][column]
                if (removed == 0) continue

                val candidateClueMask = clueMask.without(index)
                if (!hittingSetConstraints.accepts(candidateClueMask)) continue

                grid[row][column] = 0
                when (val uniqueness = uniquenessChecker.check(grid, solution)) {
                    UniquenessResult.Unique -> {
                        clueMask = candidateClueMask
                    }
                    is UniquenessResult.Multiple -> {
                        hittingSetConstraints.add(uniqueness.differenceMask)
                        grid[row][column] = removed
                        continue
                    }
                    UniquenessResult.Invalid -> {
                        grid[row][column] = removed
                        continue
                    }
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
                    clueMask = clueMask.with(index)
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
        val solution = grid.deepCopy()
        val hittingSetConstraints = HittingSetConstraints()
        val positions = (0 until Sudoku.CELL_COUNT).shuffled(random)
        var currentClues = Sudoku.CELL_COUNT
        var clueMask = CellMask81.FULL

        for (index in positions) {
            if (currentClues <= clues) break
            val row = index / Sudoku.GRID_SIZE
            val column = index % Sudoku.GRID_SIZE
            val removed = grid[row][column]
            val candidateClueMask = clueMask.without(index)
            if (!hittingSetConstraints.accepts(candidateClueMask)) continue

            grid[row][column] = 0
            when (val uniqueness = uniquenessChecker.check(grid, solution)) {
                UniquenessResult.Unique -> {
                    clueMask = candidateClueMask
                    currentClues--
                }
                is UniquenessResult.Multiple -> {
                    hittingSetConstraints.add(uniqueness.differenceMask)
                    grid[row][column] = removed
                }
                UniquenessResult.Invalid -> grid[row][column] = removed
            }
        }
        return grid.toSudoku()
    }

    private fun Array<IntArray>.deepCopy(): Array<IntArray> =
        Array(size) { row -> this[row].copyOf() }

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
        const val DIG_PATHS_PER_SOLUTION = 4
    }
}
