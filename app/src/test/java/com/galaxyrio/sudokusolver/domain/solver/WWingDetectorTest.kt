package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WWingDetectorTest {
    @Test
    fun detectsClassicWWingWithAnAuditableAlternatingChain() {
        val state = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 4)))
        val step = WWingDetector().find(state)

        assertNotNull(step)
        step!!
        assertEquals(TechniqueId.W_WING, step.technique)
        assertTrue(step.removes(CellRef(0, 4), 2))
        assertTrue(step.removes(CellRef(4, 0), 2))
        assertEquals(setOf(FIRST_WING, SECOND_WING), step.evidence.wingCells)
        assertEquals(listOf(BRIDGE_HOUSE), step.evidence.houses)
        assertEquals(setOf(1, 2), step.evidence.focusDigits)
        assertEquals(
            listOf(
                InferenceLinkType.DUAL,
                InferenceLinkType.WEAK,
                InferenceLinkType.STRONG,
                InferenceLinkType.WEAK,
                InferenceLinkType.DUAL,
            ),
            step.evidence.links.map(InferenceLink::type),
        )
        assertEquals(CandidateRef(FIRST_WING, 2), step.evidence.links.first().from)
        assertEquals(CandidateRef(SECOND_WING, 2), step.evidence.links.last().to)
        assertLocalProofForAllAssignments(state, step)
    }

    @Test
    fun detectsGroupedWWingWithoutInventingAStrongLinkBetweenIndividualCandidates() {
        val state = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 1), CellRef(1, 4)))
        val step = WWingDetector().find(state)

        assertNotNull(step)
        step!!
        assertTrue(step.removes(CellRef(0, 4), 2))
        assertEquals(5, step.evidence.causeCells.size)
        assertEquals(7, step.evidence.causeCandidates.size)
        assertTrue(step.evidence.links.isEmpty())
        assertLocalProofForAllAssignments(state, step)
    }

    @Test
    fun alsoFindsAColumnBridge() {
        val original = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 4)))
        val transposed = SolverState.fromValuesAndCandidates(
            IntArray(Sudoku.CELL_COUNT),
            IntArray(Sudoku.CELL_COUNT) { index ->
                val cell = CellRef.fromIndex(index)
                original.candidateMaskAt(CellRef(cell.col, cell.row))
            },
        )
        val step = WWingDetector().find(transposed)

        assertNotNull(step)
        assertEquals(listOf(HouseRef(HouseType.COLUMN, 1)), step!!.evidence.houses)
        assertLocalProofForAllAssignments(transposed, step)
    }

    @Test
    fun rejectsBridgeWithAnUnseenExtraCandidate() {
        val state = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 4), CellRef(1, 8)))

        assertNull(WWingDetector().find(state))
    }

    @Test
    fun requiresBothWingsToHaveExactlyTheSameTwoCandidates() {
        val original = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 4)))
        val state = withMasks(original) { masks ->
            masks[SECOND_WING.index] = candidateMaskOf(1, 2, 3)
        }

        assertNull(WWingDetector().find(state))
    }

    @Test
    fun doesNotTreatAWingAsEliminatedFromItsOwnHouse() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        HouseRef(HouseType.ROW, FIRST_WING.row).cells().forEach { cell ->
            if (cell != FIRST_WING && cell != CellRef(0, 4)) {
                masks[cell.index] = masks[cell.index] and digitMask(1).inv()
            }
        }
        masks[FIRST_WING.index] = candidateMaskOf(1, 2)
        masks[SECOND_WING.index] = candidateMaskOf(1, 2)

        assertNull(WWingDetector().find(SolverState.fromValuesAndCandidates(IntArray(81), masks)))
    }

    @Test
    fun returnsNothingWithoutAnElimination() {
        val original = stateWithBridge(setOf(CellRef(1, 0), CellRef(1, 4)))
        val state = withMasks(original) { masks ->
            commonPeers(listOf(FIRST_WING, SECOND_WING)).forEach { cell ->
                masks[cell.index] = masks[cell.index] and digitMask(2).inv()
            }
        }

        assertNull(WWingDetector().find(state))
    }

    /**
     * Exhaust the local proof, independently of the detector: choose both wing values
     * and the house's one bridge digit, respecting their pairwise Sudoku constraints.
     * Every surviving assignment must forbid every reported elimination.
     */
    private fun assertLocalProofForAllAssignments(state: SolverState, step: SolveStep) {
        val wings = step.evidence.wingCells.toList()
        val eliminatedDigit = step.eliminations.first().candidate.digit
        val bridgeDigit = (state.candidatesAt(wings[0]) - eliminatedDigit).single()
        val support = state.candidateCells(step.evidence.houses.single(), bridgeDigit)
        var validAssignments = 0
        for (first in state.candidatesAt(wings[0])) {
            for (second in state.candidatesAt(wings[1])) {
                for (bridgeCell in support) {
                    val assignments = listOf(
                        CandidateRef(wings[0], first),
                        CandidateRef(wings[1], second),
                        CandidateRef(bridgeCell, bridgeDigit),
                    )
                    if (assignments.combinations(2).any { (a, b) ->
                            a.digit == b.digit && a.cell.sees(b.cell)
                        }) continue
                    validAssignments++
                    for (elimination in step.eliminations) {
                        assertTrue(
                            "A reported elimination survives a valid local assignment: $assignments",
                            assignments.any { placed ->
                                placed.digit == elimination.candidate.digit &&
                                    placed.cell.sees(elimination.candidate.cell)
                            },
                        )
                    }
                }
            }
        }
        assertTrue("The proof fixture must have a feasible assignment", validAssignments > 0)
        assertFalse(step.eliminations.any { it.candidate.cell in step.evidence.wingCells })
    }

    private fun stateWithBridge(bridge: Set<CellRef>): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        BRIDGE_HOUSE.cells().forEach { cell ->
            if (cell !in bridge) masks[cell.index] = masks[cell.index] and digitMask(1).inv()
        }
        masks[FIRST_WING.index] = candidateMaskOf(1, 2)
        masks[SECOND_WING.index] = candidateMaskOf(1, 2)
        return SolverState.fromValuesAndCandidates(IntArray(Sudoku.CELL_COUNT), masks)
    }

    private fun withMasks(state: SolverState, update: (IntArray) -> Unit): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT) { state.candidateMaskAt(CellRef.fromIndex(it)) }
        update(masks)
        return SolverState.fromValuesAndCandidates(IntArray(Sudoku.CELL_COUNT), masks)
    }

    private fun SolveStep.removes(cell: CellRef, digit: Int): Boolean =
        eliminations.any { it.candidate == CandidateRef(cell, digit) }

    private companion object {
        val FIRST_WING = CellRef(0, 0)
        val SECOND_WING = CellRef(4, 4)
        val BRIDGE_HOUSE = HouseRef(HouseType.ROW, 1)
    }
}
