package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForcingChainDetectorTest {

    @Test
    fun nishioContradictionReturnsAProofGraph() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        masks.setCandidates(cell(0, 0), 1, 2)
        masks.setCandidates(cell(0, 1), 1, 2)
        masks.setCandidates(cell(1, 1), 2, 3)
        masks.setCandidates(cell(1, 0), 1, 3)
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.NISHIO)
        ).find(state)

        assertEquals(TechniqueId.NISHIO_FORCING_CHAIN, step?.technique)
        assertTrue(
            step!!.eliminations.any {
                it.candidate == CandidateRef(cell(0, 0), 1)
            }
        )
        assertEquals(
            InferencePremiseType.NISHIO,
            step.evidence.inferenceGraph.premise?.type,
        )
        assertTrue(step.evidence.inferenceGraph.nodes.any(InferenceNode::isAssumption))
        assertFalse(step.evidence.inferenceGraph.edges.isEmpty())
        assertTrue(step.evidence.inferenceGraph.contradiction != null)
    }

    @Test
    fun graphRecognizesAConvergingForcingNet() {
        val graph = InferenceGraph(
            nodes = listOf(
                node(0, cell(0, 0), 1, InferenceTruth.TRUE),
                node(1, cell(0, 1), 1, InferenceTruth.FALSE),
                node(2, cell(1, 0), 1, InferenceTruth.FALSE),
                node(3, cell(1, 1), 2, InferenceTruth.TRUE),
            ),
            edges = listOf(
                InferenceEdge(0, 1, InferenceLinkType.WEAK),
                InferenceEdge(0, 2, InferenceLinkType.WEAK),
                InferenceEdge(1, 3, InferenceLinkType.STRONG),
                InferenceEdge(2, 3, InferenceLinkType.STRONG),
            ),
        )

        assertTrue(graph.isNet)
        assertEquals(4, graph.links.size)
    }

    @Test
    fun digitForcingChainUsesBothTruthStates() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        masks.setCandidates(cell(0, 0), 1, 2)
        masks.setCandidates(cell(0, 4), 1, 2)
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.DIGIT)
        ).find(state)

        assertEquals(TechniqueId.DIGIT_FORCING_CHAIN, step?.technique)
        assertTrue(step!!.eliminations.any { it.candidate.digit == 1 })
        assertEquals(2, step.evidence.inferenceGraph.nodes.map { it.branchId }.distinct().size)
    }

    @Test
    fun cellForcingNetBuildsConvergingCellBranches() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        masks.setCandidates(cell(0, 0), 1, 2, 3)
        masks.setCandidates(cell(0, 3), 1, 4)
        masks.setCandidates(cell(0, 4), 2, 4)
        masks.setCandidates(cell(0, 5), 3, 4)
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.CELL)
        ).find(state)

        assertEquals(TechniqueId.CELL_FORCING_NET, step?.technique)
        assertEquals(InferencePremiseType.CELL, step!!.evidence.inferenceGraph.premise?.type)
        assertTrue(step.evidence.inferenceGraph.isNet)
        assertTrue(step.evidence.inferenceGraph.nodes.map { it.branchId }.distinct().size >= 2)
    }

    @Test
    fun regionForcingChainUsesEveryPositionForTheDigit() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        repeat(Sudoku.GRID_SIZE) { column ->
            if (column !in setOf(0, 3, 6)) {
                masks[cell(0, column).index] =
                    masks[cell(0, column).index] and digitMask(1).inv()
            }
        }
        masks.setCandidates(cell(4, 0), 1, 4)
        masks.setCandidates(cell(3, 3), 1, 4)
        masks.setCandidates(cell(4, 6), 1, 4)
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.REGION)
        ).find(state)

        assertEquals(TechniqueId.REGION_FORCING_CHAIN, step?.technique)
        assertEquals(3, step!!.evidence.inferenceGraph.premise?.candidates?.size)
        assertEquals(HouseRef(HouseType.ROW, 0), step.evidence.inferenceGraph.premise?.house)
        assertEquals(
            3,
            step.evidence.inferenceGraph.nodes.map { it.branchId }.distinct().size,
        )
    }

    private fun IntArray.setCandidates(cell: CellRef, vararg digits: Int) {
        this[cell.index] = candidateMaskOf(*digits)
    }

    private fun node(
        id: Int,
        cell: CellRef,
        digit: Int,
        truth: InferenceTruth,
    ) = InferenceNode(
        id = id,
        candidate = CandidateRef(cell, digit),
        truth = truth,
        branchId = 0,
    )

    private companion object {
        fun cell(row: Int, column: Int): CellRef = CellRef(row, column)
    }
}
