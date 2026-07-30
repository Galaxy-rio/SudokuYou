package com.galaxyrio.sudokusolver.domain.model

import com.galaxyrio.sudokusolver.domain.game.CandidateCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuTest {

    @Test
    fun gridStringCreatesFixedCluesAndEmptyEditableCells() {
        val sudoku = Sudoku.fromGridString(PUZZLE)

        assertEquals(5, sudoku.getCell(0, 0).value)
        assertTrue(sudoku.getCell(0, 0).isFixed)
        assertEquals(0, sudoku.getCell(0, 2).value)
        assertFalse(sudoku.getCell(0, 2).isFixed)
        assertEquals(PUZZLE, sudoku.toGridString())
    }

    @Test
    fun editingReturnsNewBoardAndProtectsFixedClues() {
        val original = Sudoku.fromGridString(PUZZLE)

        val unchanged = original.setCell(0, 0, 4)
        val edited = original.setCell(0, 2, 4)

        assertEquals(original, unchanged)
        assertNotSame(original, edited)
        assertEquals(0, original.getCell(0, 2).value)
        assertEquals(4, edited.getCell(0, 2).value)
    }

    @Test
    fun settingValueRemovesItFromPeerCandidatesOnly() {
        val withCandidates = CandidateCalculator.calculateAllCandidates(Sudoku())

        val edited = withCandidates.setCell(0, 0, 5)

        assertFalse(5 in edited.getCell(0, 4).candidates)
        assertFalse(5 in edited.getCell(4, 0).candidates)
        assertFalse(5 in edited.getCell(1, 1).candidates)
        assertTrue(5 in edited.getCell(4, 4).candidates)
    }

    @Test
    fun candidateTogglingIsImmutable() {
        val original = Sudoku()
        val withCandidate = original.toggleCandidate(3, 4, 7)
        val withoutCandidate = withCandidate.toggleCandidate(3, 4, 7)

        assertTrue(7 in withCandidate.getCell(3, 4).candidates)
        assertTrue(withoutCandidate.getCell(3, 4).candidates.isEmpty())
        assertTrue(original.getCell(3, 4).candidates.isEmpty())
    }

    @Test
    fun fixedCellMustContainValue() {
        assertThrows(IllegalArgumentException::class.java) {
            Cell(value = 0, isFixed = true)
        }
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
    }
}
