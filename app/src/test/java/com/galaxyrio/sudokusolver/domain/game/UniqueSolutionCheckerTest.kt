package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class UniqueSolutionCheckerTest {
    private val checker = UniqueSolutionChecker()

    @Test
    fun uniquePuzzleReturnsUnique() {
        val result = checker.check(PUZZLE.toGrid(), SOLUTION.toGrid())

        assertSame(UniquenessResult.Unique, result)
    }

    @Test
    fun multiplePuzzleReturnsAlternativeAndExactDifferenceMask() {
        val result = checker.check(EMPTY_GRID.toGrid(), SOLUTION.toGrid())

        assertTrue(result is UniquenessResult.Multiple)
        result as UniquenessResult.Multiple
        assertEquals(Sudoku.CELL_COUNT, result.alternativeSolution.size)
        result.alternativeSolution.forEachIndexed { index, value ->
            assertEquals(
                SOLUTION[index].digitToInt() != value,
                result.differenceMask.contains(index),
            )
        }
    }

    @Test
    fun mismatchWithExpectedSolutionIsInvalid() {
        val puzzle = PUZZLE.toGrid().also { it[0][0] = 1 }

        assertSame(UniquenessResult.Invalid, checker.check(puzzle, SOLUTION.toGrid()))
    }

    @Test
    fun constraintsKeepOnlyMinimalSetsAndRejectUnhitClueMasks() {
        val constraints = HittingSetConstraints()

        assertTrue(constraints.add(CellMask81.fromIndices(listOf(0, 1, 2))))
        assertTrue(constraints.add(CellMask81.fromIndices(listOf(0, 1))))
        assertFalse(constraints.add(CellMask81.fromIndices(listOf(0, 1, 3))))

        assertEquals(1, constraints.size)
        assertTrue(constraints.accepts(CellMask81.fromIndices(listOf(1, 20))))
        assertFalse(constraints.accepts(CellMask81.fromIndices(listOf(2, 20))))
    }

    private fun String.toGrid(): Array<IntArray> {
        require(length == Sudoku.CELL_COUNT)
        return Array(Sudoku.GRID_SIZE) { row ->
            IntArray(Sudoku.GRID_SIZE) { column ->
                this[row * Sudoku.GRID_SIZE + column].digitToIntOrNull() ?: 0
            }
        }
    }

    private companion object {
        const val EMPTY_GRID =
            "........." +
                "........." +
                "........." +
                "........." +
                "........." +
                "........." +
                "........." +
                "........." +
                "........."

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
