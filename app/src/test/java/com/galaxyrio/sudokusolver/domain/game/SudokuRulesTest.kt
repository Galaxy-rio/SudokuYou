package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuRulesTest {

    @Test
    fun validatorRecognizesSolvedAndConflictingBoards() {
        val solved = Sudoku.fromGridString(SOLUTION)
        val invalidCells = solved.cells.toMutableList().apply {
            this[1] = Cell(value = 5)
        }
        val invalid = Sudoku(invalidCells)

        assertTrue(SudokuValidator.isSolved(solved))
        assertFalse(SudokuValidator.isBoardValid(invalid))
        assertTrue(SudokuValidator.hasConflict(invalid, row = 0, col = 1))
    }

    @Test
    fun candidateCalculatorFindsOnlyLegalValues() {
        val puzzle = Sudoku.fromGridString(PUZZLE)

        val withCandidates = CandidateCalculator.calculateAllCandidates(puzzle)

        assertEquals(setOf(1, 2, 4), withCandidates.getCell(0, 2).candidates)
        assertTrue(withCandidates.getCell(0, 0).candidates.isEmpty())
    }

    @Test
    fun generatedPuzzlesMatchEveryDifficultyAndCanBeSolved() {
        Difficulty.entries.forEachIndexed { index, difficulty ->
            val puzzle = SudokuGenerator(Random(2026 + index))
                .generate(clues = difficulty.clueCount)
            val grid = Array(Sudoku.GRID_SIZE) { row ->
                IntArray(Sudoku.GRID_SIZE) { col ->
                    puzzle.getCell(row, col).value
                }
            }

            assertEquals(difficulty.clueCount, puzzle.cells.count { it.isFixed })
            assertTrue(SudokuValidator.isBoardValid(puzzle))
            assertTrue(ValidSudokuGenerator(Random(index)).solve(grid))

            val solved = Sudoku(
                grid.flatMap { row ->
                    row.map { value -> Cell(value = value, isFixed = true) }
                }
            )
            assertTrue(SudokuValidator.isSolved(solved))
        }
    }

    @Test
    fun solverRejectsConflictingStartingGrid() {
        val grid = Array(Sudoku.GRID_SIZE) { IntArray(Sudoku.GRID_SIZE) }
        grid[0][0] = 5
        grid[0][1] = 5

        assertFalse(ValidSudokuGenerator(Random(1)).solve(grid))
    }

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
