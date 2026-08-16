package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HintIssueDetectorTest {
    @Test
    fun incorrectValuesTakePriorityOverMissingCandidates() {
        val constrained = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        )
            .removeCandidate(row = 0, col = 3, candidate = 6)
            .setCell(row = 0, col = 2, value = 1)

        val issue = HintIssueDetector.find(constrained, SOLUTION.toDigits())

        assertTrue(issue is HintIssue.IncorrectValues)
        issue as HintIssue.IncorrectValues
        assertEquals(listOf(CellRef(0, 2)), issue.entries.map { it.cell })
    }

    @Test
    fun missingSolutionCandidateMakesSukakuInvalid() {
        val constrained = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        ).removeCandidate(row = 0, col = 2, candidate = 4)

        val issue = HintIssueDetector.find(constrained, SOLUTION.toDigits())

        assertEquals(
            listOf(CandidateRef(CellRef(0, 2), 4)),
            (issue as HintIssue.MissingCandidates).candidates,
        )
    }

    @Test
    fun validExplicitCandidatesDoNotProduceIssue() {
        val constrained = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        ).removeCandidate(row = 0, col = 2, candidate = 1)

        assertNull(HintIssueDetector.find(constrained, SOLUTION.toDigits()))
    }

    private fun String.toDigits(): SudokuSolution = SudokuSolution(map(Char::digitToInt))

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
