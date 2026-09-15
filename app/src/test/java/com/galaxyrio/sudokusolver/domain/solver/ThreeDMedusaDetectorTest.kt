package com.galaxyrio.sudokusolver.domain.solver

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreeDMedusaDetectorTest {
    @Test
    fun eliminatesCandidateSeeingOppositeColorsInTwoHouses() {
        val state = rectangleChain()
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertEquals(TechniqueId.THREE_D_MEDUSA, step!!.technique)
        assertTrue(step.removes(CellRef(7, 0), 1))
        assertTrue(step.placements.isEmpty())
        assertNull(step.evidence.inferenceGraph.contradiction)
        assertColoredEvidence(step)
    }

    @Test
    fun eliminatesOtherDigitsWhenOneCellContainsBothColors() {
        val state = board(
            mapOf(
                CellRef(0, 0) to setOf(1, 2, 5),
                CellRef(0, 4) to setOf(2, 3),
                CellRef(4, 4) to setOf(3, 4),
                CellRef(4, 0) to setOf(1, 4),
            ),
            listOf(row(0, 2, 0, 4), column(4, 3, 0, 4), row(4, 4, 0, 4), column(0, 1, 0, 4)),
        )
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertTrue(step!!.removes(CellRef(0, 0), 5))
        assertTrue(step.placements.isEmpty())
        assertColoredEvidence(step)
    }

    @Test
    fun eliminatesCandidateThroughOneCellConflictAndOneHouseConflict() {
        val state = rectangleChain(
            extra = mapOf(CellRef(1, 0) to setOf(1, 4, 5)),
            extraLinks = listOf(column(0, 4, 1, 4)),
        )
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertTrue(step!!.removes(CellRef(1, 0), 1))
        assertTrue(step.placements.isEmpty())
    }

    @Test
    fun rejectsColorThatRepeatsADigitInAHouse() {
        val state = board(
            mapOf(
                CellRef(0, 0) to setOf(1, 2),
                CellRef(0, 4) to setOf(1, 5, 6),
                CellRef(4, 4) to setOf(1, 5, 6),
                CellRef(4, 1) to setOf(1, 5, 6),
                CellRef(1, 1) to setOf(1, 5, 6),
            ),
            listOf(row(0, 1, 0, 4), column(4, 1, 0, 4), row(4, 1, 1, 4), column(1, 1, 1, 4)),
        )
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertTrue(step!!.removes(CellRef(0, 0), 1))
        assertTrue(Placement(CellRef(0, 0), 2) in step.placements)
        assertEquals(InferenceContradictionType.OPPOSITE_TRUTHS, step.evidence.inferenceGraph.contradiction?.type)
        assertColoredEvidence(step)
    }

    @Test
    fun rejectsColorThatPlacesTwoDigitsInOneCell() {
        val state = board(
            mapOf(
                CellRef(0, 0) to setOf(1, 2, 3),
                CellRef(0, 4) to setOf(1, 4),
                CellRef(4, 4) to setOf(4, 6, 7),
                CellRef(4, 7) to setOf(2, 4),
                CellRef(0, 7) to setOf(2, 3),
            ),
            listOf(row(0, 1, 0, 4), column(4, 4, 0, 4), row(4, 4, 4, 7), column(7, 2, 0, 4), row(0, 3, 0, 7)),
        )
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertTrue(step!!.removes(CellRef(0, 0), 1))
        assertTrue(step.removes(CellRef(0, 0), 3))
        assertEquals(InferenceContradictionType.OPPOSITE_TRUTHS, step.evidence.inferenceGraph.contradiction?.type)
    }

    @Test
    fun rejectsColorThatWouldEmptyAnUncoloredCell() {
        val state = board(
            mapOf(
                CellRef(0, 0) to setOf(1, 2),
                CellRef(0, 4) to setOf(2, 3),
                CellRef(4, 4) to setOf(3, 4),
                CellRef(4, 7) to setOf(4, 5),
                CellRef(7, 7) to setOf(2, 5),
                CellRef(7, 0) to setOf(2, 6),
                CellRef(4, 0) to setOf(1, 2, 3),
            ),
            listOf(row(0, 2, 0, 4), column(4, 3, 0, 4), row(4, 4, 4, 7), column(7, 5, 4, 7), row(7, 2, 0, 7)),
        )
        val step = ThreeDMedusaDetector().find(state)

        assertNotNull(step)
        assertEquals(InferenceContradictionType.EMPTY_CELL, step!!.evidence.inferenceGraph.contradiction?.type)
        assertEquals(CellRef(4, 0), step.evidence.inferenceGraph.contradiction?.cell)
        assertTrue(step.removes(CellRef(0, 0), 1))
        assertTrue(Placement(CellRef(0, 0), 2) in step.placements)
        assertColoredEvidence(step)
    }

    @Test
    fun aSingleColorConflictDoesNotEliminateAnUncoloredCandidate() {
        val state = board(
            mapOf(CellRef(0, 0) to setOf(1, 2), CellRef(0, 4) to setOf(2, 3)),
            listOf(row(0, 2, 0, 4)),
        )
        assertNull(ThreeDMedusaDetector().find(state))
    }

    @Test
    fun skipsInconsistentOddStrongLinkCycles() {
        val masks = IntArray(81)
        // Five conjugate links: row, column, row, column, box.
        listOf(CellRef(0, 0), CellRef(0, 4), CellRef(4, 4), CellRef(4, 1), CellRef(1, 1))
            .forEach { masks[it.index] = digitMask(1) }
        masks[0] = candidateMaskOf(1, 2)
        assertNull(ThreeDMedusaDetector().find(SolverState.fromValuesAndCandidates(IntArray(81), masks)))
    }

    @Test
    fun deductionsPreserveAKnownSolutionAcrossCandidateBoards() {
        val solution = (
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179"
        ).map(Char::digitToInt)
        val random = Random(319)
        var deductions = 0
        repeat(60) {
            // Every board retains this complete valid solution, independently of
            // the detector's graph construction and contradiction rules.
            val masks = IntArray(81) { index ->
                (1..9).fold(digitMask(solution[index])) { mask, digit ->
                    if (random.nextDouble() < 0.22) mask or digitMask(digit) else mask
                }
            }
            val step = ThreeDMedusaDetector().find(
                SolverState.fromValuesAndCandidates(IntArray(81), masks)
            ) ?: return@repeat
            deductions++
            step.placements.forEach { assertEquals(solution[it.cell.index], it.digit) }
            step.eliminations.forEach {
                assertTrue(solution[it.candidate.cell.index] != it.candidate.digit)
            }
        }
        assertTrue("Candidate-board corpus must exercise Medusa deductions", deductions > 0)
    }

    private fun rectangleChain(
        extra: Map<CellRef, Set<Int>> = emptyMap(),
        extraLinks: List<Conjugate> = emptyList(),
    ): SolverState = board(
        mapOf(
            CellRef(0, 0) to setOf(1, 2),
            CellRef(0, 4) to setOf(2, 3),
            CellRef(4, 4) to setOf(3, 4),
            CellRef(4, 0) to setOf(1, 4),
        ) + extra,
        listOf(row(0, 2, 0, 4), column(4, 3, 0, 4), row(4, 4, 0, 4)) + extraLinks,
    )

    private fun board(entries: Map<CellRef, Set<Int>>, links: List<Conjugate>): SolverState {
        val masks = IntArray(81) { 511 }
        entries.forEach { (cell, digits) -> masks[cell.index] = digits.fold(0) { mask, digit -> mask or digitMask(digit) } }
        links.forEach { link ->
            link.house.cells().filter { it !in link.endpoints }.forEach {
                masks[it.index] = masks[it.index] and digitMask(link.digit).inv()
            }
        }
        return SolverState.fromValuesAndCandidates(IntArray(81), masks)
    }

    private fun assertColoredEvidence(step: SolveStep) {
        val graph = step.evidence.inferenceGraph
        assertTrue(graph.nodes.map { it.candidate.digit }.distinct().size >= 2)
        assertFalse(graph.edges.isEmpty())
        graph.edges.filter { it.type == InferenceLinkType.DUAL }.forEach { edge ->
            val from = graph.nodes.single { it.id == edge.fromNodeId }
            val to = graph.nodes.single { it.id == edge.toNodeId }
            assertTrue(from.truth != to.truth)
        }
    }

    private fun SolveStep.removes(cell: CellRef, digit: Int): Boolean =
        CandidateElimination(CandidateRef(cell, digit)) in eliminations

    private fun row(index: Int, digit: Int, first: Int, second: Int): Conjugate =
        Conjugate(HouseRef(HouseType.ROW, index), digit, setOf(CellRef(index, first), CellRef(index, second)))

    private fun column(index: Int, digit: Int, first: Int, second: Int): Conjugate =
        Conjugate(HouseRef(HouseType.COLUMN, index), digit, setOf(CellRef(first, index), CellRef(second, index)))

    private data class Conjugate(val house: HouseRef, val digit: Int, val endpoints: Set<CellRef>)
}
