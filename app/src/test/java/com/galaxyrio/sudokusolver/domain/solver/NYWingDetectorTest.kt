package com.galaxyrio.sudokusolver.domain.solver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NYWingDetectorTest {
    @Test
    fun detectsEveryHigherWingSize() {
        val expected = listOf(
            TechniqueId.FIVE_Y_WING, TechniqueId.SIX_Y_WING, TechniqueId.SEVEN_Y_WING,
            TechniqueId.EIGHT_Y_WING, TechniqueId.NINE_Y_WING,
        )
        for (size in 5..9) {
            val entries = linkedMapOf(CellRef(0, 0) to (1 until size).toSet())
            repeat(size - 2) { index -> entries[CellRef(0, index + 2)] = setOf(index + 1, size) }
            entries[CellRef(1, 0)] = setOf(size - 1, size)
            val target = CellRef(0, 1)
            entries[target] = (1..9).toSet()

            val step = NYWingDetector(size).find(state(entries))

            assertNotNull("Missing $size-Y-Wing", step)
            assertEquals(expected[size - 5], step!!.technique)
            assertTrue(step.eliminations.any { it.candidate == CandidateRef(target, size) })
            assertEquals(size, step.evidence.causeCells.size)
            assertEquals((1..size).toSet(), step.evidence.focusDigits)
            assertPigeonholeProof(state(entries), step)
        }
    }

    @Test
    fun supportsMultiplePivotCellsAndMultipleCellsInBothWings() {
        val entries = mapOf(
            CellRef(0, 0) to setOf(1, 2, 3, 4, 5),
            CellRef(0, 1) to setOf(1, 2, 3, 4, 5),
            CellRef(0, 3) to setOf(1, 2, 6),
            CellRef(0, 4) to setOf(2, 3, 6),
            CellRef(1, 0) to setOf(4, 6),
            CellRef(1, 1) to setOf(5, 6),
            CellRef(0, 2) to setOf(6, 7, 8, 9),
        )
        val step = NYWingDetector(6).find(state(entries))

        assertNotNull(step)
        assertEquals(entries.keys - CellRef(0, 2), step!!.evidence.causeCells)
        assertTrue(step.eliminations.any { it.candidate == CandidateRef(CellRef(0, 2), 6) })
        assertPigeonholeProof(state(entries), step)
    }

    @Test
    fun restrictedWingCanEliminateWithoutSeeingPivot() {
        val entries = fiveWingEntries(unrestricted = false)
        val step = NYWingDetector(5).find(state(entries))

        assertNotNull(step)
        assertTrue(step!!.eliminations.any { it.candidate == CandidateRef(CellRef(1, 4), 5) })
        assertPigeonholeProof(state(entries), step)
    }

    @Test
    fun unrestrictedWingRequiresTargetsToSeePivotToo() {
        val entries = fiveWingEntries(unrestricted = true)
        val step = NYWingDetector(5).find(state(entries))

        assertNotNull(step)
        assertTrue(step!!.eliminations.any { it.candidate == CandidateRef(CellRef(0, 1), 5) })
        assertFalse(step.eliminations.any { it.candidate == CandidateRef(CellRef(1, 4), 5) })
        assertPigeonholeProof(state(entries), step)
    }

    @Test
    fun rejectsWingsSharingAnotherDigit() {
        val entries = fiveWingEntries(unrestricted = false).toMutableMap()
        entries[CellRef(1, 1)] = setOf(1, 4, 5)

        assertNull(NYWingDetector(5).find(state(entries)))
    }

    @Test
    fun rejectsAnExtraPatternDigitAndDisconnectedWingCells() {
        val extraDigit = fiveWingEntries(unrestricted = false).toMutableMap()
        extraDigit[CellRef(0, 0)] = setOf(1, 2, 3, 4, 6)
        assertNull(NYWingDetector(5).find(state(extraDigit)))

        val disconnected = fiveWingEntries(unrestricted = false).toMutableMap()
        disconnected[CellRef(4, 4)] = disconnected.remove(CellRef(1, 1))!!
        assertNull(NYWingDetector(5).find(state(disconnected)))
    }

    @Test(timeout = 5_000)
    fun denseCandidateBoardDoesNotEnumerateArbitraryBoardSubsets() {
        val dense = SolverState.fromValuesAndCandidates(IntArray(81), IntArray(81) { 511 })
        for (size in 5..9) assertNull(NYWingDetector(size).find(dense))
    }

    private fun fiveWingEntries(unrestricted: Boolean): Map<CellRef, Set<Int>> = mapOf(
        CellRef(0, 0) to if (unrestricted) setOf(1, 2, 3, 4, 5) else setOf(1, 2, 3, 4),
        CellRef(0, 3) to setOf(1, 5),
        CellRef(0, 4) to setOf(2, 5),
        CellRef(0, 5) to setOf(3, 5),
        CellRef(1, 1) to setOf(4, 5),
        CellRef(0, 1) to setOf(5, 6, 7, 8, 9),
        CellRef(1, 4) to setOf(5, 6, 7, 8, 9),
    )

    /** Independent Hall/pigeonhole check: after the proposed target, each remaining
     * digit can occur at most once in the n pattern cells, but only n-1 remain. */
    private fun assertPigeonholeProof(state: SolverState, step: SolveStep) {
        val pattern = step.evidence.causeCells.toList()
        for (elimination in step.eliminations) {
            val target = elimination.candidate
            val remaining = pattern.associateWith { cell ->
                state.candidatesAt(cell) - if (cell.sees(target.cell)) setOf(target.digit) else emptySet()
            }
            assertTrue(remaining.values.flatten().toSet().size < pattern.size)
            for (first in pattern.indices) for (second in first + 1 until pattern.size) {
                if (!pattern[first].sees(pattern[second])) {
                    assertTrue(remaining.getValue(pattern[first]).intersect(remaining.getValue(pattern[second])).isEmpty())
                }
            }
        }
    }

    private fun state(entries: Map<CellRef, Set<Int>>): SolverState {
        val masks = IntArray(81)
        entries.forEach { (cell, digits) -> masks[cell.index] = digits.fold(0) { mask, digit -> mask or digitMask(digit) } }
        return SolverState.fromValuesAndCandidates(IntArray(81), masks)
    }
}
