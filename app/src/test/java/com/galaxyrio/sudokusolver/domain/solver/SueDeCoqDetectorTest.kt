package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SueDeCoqDetectorTest {
    @Test
    fun detectsBothTypesInRowsAndColumnsAndProvesAllEliminations() {
        for (subsetSize in 2..3) {
            for (transpose in listOf(false, true)) {
                val state = stateOf(subsetSize, transpose)
                val step = SueDeCoqDetector(subsetSize).find(state)
                fun cell(row: Int, col: Int): CellRef =
                    if (transpose) CellRef(col, row) else CellRef(row, col)

                assertNotNull("subsetSize=$subsetSize, transpose=$transpose", step)
                step!!
                assertEquals(
                    if (subsetSize == 2) TechniqueId.SUE_DE_COQ_TYPE_1 else TechniqueId.SUE_DE_COQ_TYPE_2,
                    step.technique,
                )
                assertTrue(step.removes(cell(0, 8), 1))
                assertTrue(step.removes(cell(0, 8), 2))
                assertTrue(step.removes(cell(2, 5), 3))
                assertTrue(step.removes(cell(2, 5), 4))
                assertFalse(step.removes(cell(0, 8), 3))
                assertFalse(step.removes(cell(2, 5), 1))
                assertEquals(subsetSize + 2, step.evidence.causeCells.size)
                assertEquals(subsetSize + 2, step.evidence.focusDigits.size)
                assertEquals(setOf(cell(0, 0), cell(1, 3)), step.evidence.wingCells)
                if (subsetSize == 3) {
                    assertTrue(step.removes(cell(0, 8), 5))
                    assertTrue(step.removes(cell(2, 5), 5))
                } else {
                    assertFalse(step.removes(cell(0, 8), 5))
                    assertFalse(step.removes(cell(2, 5), 5))
                }
                assertAllPatternAssignmentsForbidEliminations(state, step)
            }
        }
    }

    @Test
    fun typeOneAlsoEliminatesFromTheUnusedIntersectionCell() {
        val step = SueDeCoqDetector(2).find(stateOf(2))!!

        for (digit in 1..4) assertTrue(step.removes(CellRef(0, 5), digit))
        assertFalse(step.removes(CellRef(0, 5), 5))
    }

    @Test
    fun requiresDisjointWingCandidates() {
        for (size in 2..3) {
            val state = changeCell(stateOf(size), CellRef(1, 3), 2, 3)

            assertNull(SueDeCoqDetector(size).find(state))
        }
    }

    @Test
    fun rejectsAnAdditionalUnaccountedForIntersectionCandidate() {
        val typeOne = changeCell(stateOf(2), CellRef(0, 3), 1, 3, 4, 5)
        val typeTwo = changeCell(stateOf(3), CellRef(0, 5), 1, 2, 5, 6)

        assertNull(SueDeCoqDetector(2).find(typeOne))
        assertNull(SueDeCoqDetector(3).find(typeTwo))
    }

    @Test
    fun requiresBivalueOuterCells() {
        for (size in 2..3) {
            val state = changeCell(stateOf(size), CellRef(1, 3), 3, 4, 6)

            assertNull(SueDeCoqDetector(size).find(state))
        }
    }

    @Test
    fun returnsNothingIfThereAreNoRemainingEliminations() {
        for (size in 2..3) {
            val state = stateOf(size)
            val step = SueDeCoqDetector(size).find(state)!!

            assertNull(SueDeCoqDetector(size).find(state.apply(step)))
        }
    }

    private fun assertAllPatternAssignmentsForbidEliminations(state: SolverState, step: SolveStep) {
        val cells = step.evidence.causeCells.toList()
        val assignment = mutableListOf<CandidateRef>()
        var validAssignments = 0
        fun search(index: Int) {
            if (index == cells.size) {
                validAssignments++
                for (elimination in step.eliminations) {
                    assertTrue(
                        "Elimination ${elimination.candidate} is compatible with $assignment",
                        assignment.any { placed ->
                            placed.digit == elimination.candidate.digit &&
                                placed.cell.sees(elimination.candidate.cell)
                        },
                    )
                }
                return
            }
            val cell = cells[index]
            for (digit in state.candidatesAt(cell)) {
                if (assignment.any { it.digit == digit && it.cell.sees(cell) }) continue
                assignment += CandidateRef(cell, digit)
                search(index + 1)
                assignment.removeAt(assignment.lastIndex)
            }
        }
        search(0)
        assertTrue("The soundness fixture must have a feasible assignment", validAssignments > 0)
        assertFalse(step.eliminations.any { it.candidate.cell in step.evidence.causeCells })
    }

    private fun stateOf(subsetSize: Int, transpose: Boolean = false): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        fun put(row: Int, col: Int, vararg digits: Int) {
            val cell = if (transpose) CellRef(col, row) else CellRef(row, col)
            masks[cell.index] = candidateMaskOf(*digits)
        }
        put(0, 0, 1, 2)
        put(1, 3, 3, 4)
        put(0, 3, 1, 3, 4)
        put(0, 4, 2, 3, 4)
        if (subsetSize == 3) put(0, 5, 1, 2, 5)
        return SolverState.fromValuesAndCandidates(IntArray(Sudoku.CELL_COUNT), masks)
    }

    private fun changeCell(state: SolverState, cell: CellRef, vararg digits: Int): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT) { state.candidateMaskAt(CellRef.fromIndex(it)) }
        masks[cell.index] = candidateMaskOf(*digits)
        return SolverState.fromValuesAndCandidates(IntArray(Sudoku.CELL_COUNT), masks)
    }

    private fun SolveStep.removes(cell: CellRef, digit: Int): Boolean =
        eliminations.any { it.candidate == CandidateRef(cell, digit) }
}
