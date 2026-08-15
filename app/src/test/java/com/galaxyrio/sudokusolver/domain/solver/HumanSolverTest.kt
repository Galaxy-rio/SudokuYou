package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HumanSolverTest {

    @Test
    fun solverCandidatesDoNotTrustPlayerPencilMarks() {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(candidates = setOf(9))

        val state = SolverState.fromSudoku(Sudoku(cells))

        assertEquals((1..9).toSet(), state.candidatesAt(CellRef(0, 0)))
    }

    @Test
    fun detectsLastDigitAndBothSingleTypes() {
        val lastDigitState = SolverState.fromSudoku(
            Sudoku.fromGridString("." + SOLUTION.drop(1))
        )
        val lastDigit = LastDigitDetector().find(lastDigitState)
        assertEquals(TechniqueId.LAST_DIGIT, lastDigit?.technique)
        assertEquals(Placement(CellRef(0, 0), 5), lastDigit?.placements?.single())

        val nakedMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(4, 4), 7)
        }
        val naked = NakedSingleDetector().find(stateWith(nakedMasks))
        assertEquals(TechniqueId.NAKED_SINGLE, naked?.technique)
        assertEquals(Placement(CellRef(4, 4), 7), naked?.placements?.single())

        val hiddenMasks = fullCandidateMasks().apply {
            repeat(Sudoku.GRID_SIZE) { col ->
                if (col != 4) removeCandidate(CellRef(0, col), 8)
            }
        }
        val hidden = HiddenSingleDetector().find(stateWith(hiddenMasks))
        assertEquals(TechniqueId.HIDDEN_SINGLE, hidden?.technique)
        assertEquals(Placement(CellRef(0, 4), 8), hidden?.placements?.single())
    }

    @Test
    fun detectsNakedAndHiddenPairs() {
        val nakedMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2)
            setCandidates(CellRef(0, 3), 1, 2)
        }
        val naked = NakedSubsetDetector(2).find(stateWith(nakedMasks))
        assertEquals(TechniqueId.NAKED_PAIR, naked?.technique)
        assertTrue(naked!!.eliminations.any { it.candidate == CandidateRef(CellRef(0, 1), 1) })

        val hiddenMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2, 3)
            setCandidates(CellRef(0, 1), 1, 2, 4)
            for (col in 2 until Sudoku.GRID_SIZE) {
                removeCandidate(CellRef(0, col), 1)
                removeCandidate(CellRef(0, col), 2)
            }
        }
        val hidden = HiddenSubsetDetector(2).find(stateWith(hiddenMasks))
        assertEquals(TechniqueId.HIDDEN_PAIR, hidden?.technique)
        assertEquals(
            setOf(CandidateRef(CellRef(0, 0), 3), CandidateRef(CellRef(0, 1), 4)),
            hidden!!.eliminations.map { it.candidate }.toSet(),
        )
    }

    @Test
    fun detectsPointingAndClaimingCandidates() {
        val pointingMasks = fullCandidateMasks().apply {
            for (row in 0..2) {
                for (col in 0..2) {
                    if (row != 0 || col !in 0..1) removeCandidate(CellRef(row, col), 5)
                }
            }
        }
        val pointing = PointingCandidatesDetector().find(stateWith(pointingMasks))
        assertEquals(TechniqueId.POINTING_PAIR, pointing?.technique)
        assertTrue(pointing!!.eliminations.any { it.candidate == CandidateRef(CellRef(0, 3), 5) })

        val claimingMasks = fullCandidateMasks().apply {
            for (col in 2 until Sudoku.GRID_SIZE) {
                removeCandidate(CellRef(0, col), 6)
            }
        }
        val claiming = ClaimingCandidatesDetector().find(stateWith(claimingMasks))
        assertEquals(TechniqueId.CLAIMING_PAIR, claiming?.technique)
        assertTrue(claiming!!.eliminations.any { it.candidate == CandidateRef(CellRef(1, 0), 6) })
    }

    @Test
    fun detectsNakedAndHiddenTriples() {
        val nakedMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2)
            setCandidates(CellRef(0, 3), 1, 3)
            setCandidates(CellRef(0, 6), 2, 3)
        }
        val naked = NakedSubsetDetector(3).find(stateWith(nakedMasks))
        assertEquals(TechniqueId.NAKED_TRIPLE, naked?.technique)
        assertTrue(naked!!.eliminations.any { it.candidate == CandidateRef(CellRef(0, 1), 1) })

        val hiddenMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2, 4)
            setCandidates(CellRef(0, 1), 1, 3, 5)
            setCandidates(CellRef(0, 2), 2, 3, 6)
            for (col in 3 until Sudoku.GRID_SIZE) {
                removeCandidate(CellRef(0, col), 1)
                removeCandidate(CellRef(0, col), 2)
                removeCandidate(CellRef(0, col), 3)
            }
        }
        val hidden = HiddenSubsetDetector(3).find(stateWith(hiddenMasks))
        assertEquals(TechniqueId.HIDDEN_TRIPLE, hidden?.technique)
        assertEquals(3, hidden!!.eliminations.size)
    }

    @Test
    fun distinguishesLockedPairsAndTriples() {
        val pairMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2)
            setCandidates(CellRef(0, 1), 1, 2)
        }
        val pair = NakedSubsetDetector(2).find(stateWith(pairMasks))
        assertEquals(TechniqueId.LOCKED_PAIR, pair?.technique)
        assertTrue(pair!!.eliminations.any { it.candidate == CandidateRef(CellRef(1, 0), 1) })

        val tripleMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2)
            setCandidates(CellRef(0, 1), 1, 3)
            setCandidates(CellRef(0, 2), 2, 3)
        }
        val triple = NakedSubsetDetector(3).find(stateWith(tripleMasks))
        assertEquals(TechniqueId.LOCKED_TRIPLE, triple?.technique)
        assertTrue(triple!!.eliminations.any { it.candidate == CandidateRef(CellRef(1, 0), 1) })
    }

    @Test
    fun detectsNakedAndHiddenQuads() {
        val nakedMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2)
            setCandidates(CellRef(0, 3), 1, 3)
            setCandidates(CellRef(0, 6), 2, 4)
            setCandidates(CellRef(0, 8), 3, 4)
        }
        val naked = NakedSubsetDetector(4).find(stateWith(nakedMasks))
        assertEquals(TechniqueId.NAKED_QUAD, naked?.technique)
        assertTrue(naked!!.eliminations.any { it.candidate == CandidateRef(CellRef(0, 1), 1) })

        val hiddenMasks = fullCandidateMasks().apply {
            setCandidates(CellRef(0, 0), 1, 2, 5)
            setCandidates(CellRef(0, 1), 1, 3, 6)
            setCandidates(CellRef(0, 2), 2, 4, 7)
            setCandidates(CellRef(0, 3), 3, 4, 8)
            for (col in 4 until Sudoku.GRID_SIZE) {
                for (digit in 1..4) removeCandidate(CellRef(0, col), digit)
            }
        }
        val hidden = HiddenSubsetDetector(4).find(stateWith(hiddenMasks))
        assertEquals(TechniqueId.HIDDEN_QUAD, hidden?.technique)
        assertEquals(4, hidden!!.eliminations.size)
    }

    @Test
    fun traceContainsEveryIntermediateStateAndSolvesBasicPuzzle() {
        val trace = HumanSolver().solveTrace(Sudoku.fromGridString(PUZZLE))

        assertEquals(SolveTraceStatus.SOLVED, trace.status)
        assertTrue(trace.steps.isNotEmpty())
        assertEquals(trace.steps.size + 1, trace.states.size)
        assertTrue(trace.states.last().isSolved())
    }

    @Test
    fun traceReportsStalledAndInvalidBoards() {
        val stalled = HumanSolver().solveTrace(Sudoku())
        assertEquals(SolveTraceStatus.STALLED, stalled.status)
        assertTrue(stalled.steps.isEmpty())

        val invalidCells = Sudoku.fromGridString(PUZZLE).cells.toMutableList()
        invalidCells[0] = Cell(value = 5, isFixed = true)
        invalidCells[1] = Cell(value = 5, isFixed = true)
        val invalid = HumanSolver().solveTrace(Sudoku(invalidCells))
        assertEquals(SolveTraceStatus.INVALID, invalid.status)
        assertFalse(invalid.initialState.isValid())
        assertEquals(invalid.initialState, invalid.states.single())
    }

    @Test
    fun stateRejectsHouseWithNoPositionForAMissingDigit() {
        val masks = fullCandidateMasks().apply {
            repeat(Sudoku.GRID_SIZE) { column ->
                removeCandidate(CellRef(0, column), 9)
            }
        }
        val state = stateWith(masks)

        assertFalse(state.isValid())
        assertEquals(SolveTraceStatus.INVALID, HumanSolver().solveTrace(state).status)
    }

    private fun stateWith(candidateMasks: IntArray): SolverState =
        SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = candidateMasks,
        )

    private fun fullCandidateMasks(): IntArray =
        IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }

    private fun IntArray.setCandidates(cell: CellRef, vararg digits: Int) {
        this[cell.index] = candidateMaskOf(*digits)
    }

    private fun IntArray.removeCandidate(cell: CellRef, digit: Int) {
        this[cell.index] = this[cell.index] and digitMask(digit).inv()
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
