package com.galaxyrio.sudokusolver.domain.solver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupedXChainDetectorTest {
    @Test
    fun usesAGroupAsOneEndOfAStrongLink() {
        val state = fixture()
        val step = GroupedXChainDetector().find(state)
        assertNotNull(step)
        step!!
        assertEquals(TechniqueId.GROUPED_X_CHAIN, step.technique)
        assertTrue(step.evidence.groupedLinks.any { it.from.size > 1 || it.to.size > 1 })
        assertTrue(step.evidence.links.isEmpty())
        assertTrue(step.eliminations.any { it.candidate == CandidateRef(CellRef(7, 0), 9) })
        step.evidence.groupedLinks.forEachIndexed { index, link ->
            assertEquals(if (index % 2 == 0) InferenceLinkType.STRONG else InferenceLinkType.WEAK, link.type)
            assertFalse(link.from.any { it in link.to })
            if (link.type == InferenceLinkType.STRONG) {
                assertTrue(orderedHouses.any { house ->
                    state.candidateCells(house, 9).toSet() == (link.from + link.to).map { it.cell }.toSet()
                })
            } else {
                assertTrue(link.from.all { a -> link.to.all { b -> a.cell.sees(b.cell) } })
            }
        }
        assertPreservesEveryDigitTemplate(state, step)
    }

    @Test
    fun doesNotTreatAnIncompleteGroupAsAStrongLink() {
        assertNull(GroupedXChainDetector().find(fixture(extra = CellRef(0, 7))))
    }

    @Test
    fun transposedGroupedChainAlsoPreservesEveryDigitTemplate() {
        val original = fixture()
        val masks = IntArray(81) { index ->
            original.candidateMaskAt(CellRef(index % 9, index / 9))
        }
        val state = SolverState.fromValuesAndCandidates(IntArray(81), masks)
        val step = GroupedXChainDetector().find(state)!!
        assertPreservesEveryDigitTemplate(state, step)
    }

    @Test
    fun strongDiscontinuityPlacesTheSingletonStartInBothOrientations() {
        val original = stateWithRestrictions(
            HouseRef(HouseType.ROW, 0) to setOf(CellRef(0, 0), CellRef(0, 4)),
            HouseRef(HouseType.ROW, 3) to setOf(CellRef(3, 1), CellRef(3, 4)),
            HouseRef(HouseType.COLUMN, 0) to setOf(CellRef(0, 0), CellRef(4, 0), CellRef(5, 0)),
        )
        for (transpose in listOf(false, true)) {
            val state = if (transpose) {
                SolverState.fromValuesAndCandidates(
                    IntArray(81),
                    IntArray(81) { original.candidateMaskAt(CellRef(it % 9, it / 9)) },
                )
            } else {
                original
            }
            val step = GroupedXChainDetector().find(state)!!

            assertEquals(listOf(Placement(CellRef(0, 0), 9)), step.placements)
            assertEquals(step.evidence.groupedLinks.first().from, step.evidence.groupedLinks.last().to)
            assertEquals(InferenceLinkType.STRONG, step.evidence.groupedLinks.first().type)
            assertEquals(InferenceLinkType.STRONG, step.evidence.groupedLinks.last().type)
            assertPreservesEveryDigitTemplate(state, step)
        }
    }

    @Test
    fun allowsInternalCandidatesToBeEliminatedByTheirOwnProof() {
        val state = stateWithRestrictions(
            HouseRef(HouseType.ROW, 7) to setOf(CellRef(7, 4), CellRef(7, 5)),
            HouseRef(HouseType.BOX, 7) to setOf(
                CellRef(6, 4), CellRef(6, 5),
                CellRef(7, 4), CellRef(7, 5),
                CellRef(8, 4), CellRef(8, 5),
            ),
        )
        val step = GroupedXChainDetector().find(state)!!

        assertTrue(step.placements.isEmpty())
        assertEquals(setOf(CellRef(6, 4), CellRef(8, 4)), step.eliminations.map { it.candidate.cell }.toSet())
        assertTrue(step.eliminations.all { it.candidate.cell in step.evidence.causeCells })
        assertPreservesEveryDigitTemplate(state, step)
    }

    @Test
    fun keepsNonContiguousGroupsDistinctFromTheCandidateBetweenThem() {
        val state = stateWithRestrictions(
            HouseRef(HouseType.BOX, 3) to setOf(CellRef(3, 0), CellRef(3, 1)),
            HouseRef(HouseType.BOX, 5) to setOf(
                CellRef(3, 6), CellRef(3, 7), CellRef(3, 8),
                CellRef(4, 7), CellRef(5, 7),
            ),
        )
        val step = GroupedXChainDetector().find(state)!!
        val separatedGroup = setOf(CandidateRef(CellRef(3, 6), 9), CandidateRef(CellRef(3, 8), 9))
        val middle = CandidateRef(CellRef(3, 7), 9)

        assertTrue(state.hasCandidate(middle.cell, middle.digit))
        assertTrue(step.evidence.groupedLinks.any { link -> link.from == separatedGroup || link.to == separatedGroup })
        assertTrue(step.evidence.groupedLinks.any { link -> middle in link.from || middle in link.to })
        assertPreservesEveryDigitTemplate(state, step)
    }

    @Test
    fun continuousLoopCanEliminateFromBothWeakLinks() {
        val initial = stateWithRestrictions(
            HouseRef(HouseType.ROW, 0) to setOf(CellRef(0, 0), CellRef(0, 3), CellRef(0, 5)),
            HouseRef(HouseType.ROW, 1) to setOf(CellRef(1, 1), CellRef(1, 4)),
        )
        val first = GroupedXChainDetector().find(initial)!!
        assertTrue(first.placements.isEmpty())
        assertPreservesEveryDigitTemplate(initial, first)
        val nextState = initial.apply(first)
        val second = GroupedXChainDetector().find(nextState)!!
        assertTrue(second.placements.isEmpty())
        assertPreservesEveryDigitTemplate(nextState, second)

        assertEquals(
            (0..5).map { CellRef(2, it) }.toSet(),
            (first.eliminations + second.eliminations).map { it.candidate.cell }.toSet(),
        )
    }

    private fun stateWithRestrictions(vararg restrictions: Pair<HouseRef, Set<CellRef>>): SolverState {
        val masks = IntArray(81) { SolverState.FULL_CANDIDATE_MASK }
        for ((house, allowed) in restrictions) {
            for (cell in house.cells()) {
                if (cell !in allowed) masks[cell.index] = masks[cell.index] and digitMask(9).inv()
            }
        }
        return SolverState.fromValuesAndCandidates(IntArray(81), masks)
    }

    private fun fixture(extra: CellRef? = null): SolverState {
        val masks = IntArray(81) { SolverState.FULL_CANDIDATE_MASK }
        for (col in 0..8) if (col !in listOf(0, 3, 4)) {
            masks[col] = masks[col] and digitMask(9).inv()
        }
        for (row in 0..8) if (row !in listOf(1, 7)) {
            val index = row * 9 + 5
            masks[index] = masks[index] and digitMask(9).inv()
        }
        if (extra != null) masks[extra.index] = masks[extra.index] or digitMask(9)
        return SolverState.fromValuesAndCandidates(IntArray(81), masks)
    }

    private fun assertPreservesEveryDigitTemplate(state: SolverState, step: SolveStep) {
        val selected = mutableSetOf<CellRef>()
        var templates = 0
        fun visit(row: Int, columns: Int, boxes: Int) {
            if (row == 9) {
                templates++
                assertTrue(step.eliminations.none { it.candidate.cell in selected })
                assertTrue(step.placements.all { it.cell in selected && it.digit == 9 })
                return
            }
            for (col in 0..8) {
                val cell = CellRef(row, col)
                val columnBit = 1 shl col
                val boxBit = 1 shl cell.boxIndex
                if (!state.hasCandidate(cell, 9) || columns and columnBit != 0 || boxes and boxBit != 0) continue
                selected.add(cell)
                visit(row + 1, columns or columnBit, boxes or boxBit)
                selected.remove(cell)
            }
        }
        visit(0, 0, 0)
        assertTrue("Fixture must have a legal digit template", templates > 0)
    }
}
