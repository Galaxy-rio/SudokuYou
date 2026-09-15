package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinnedFishDetectorTest {
    @Test
    fun detectsAllThreeSizesInBothDirectionsIncludingSashimi() {
        for (size in 2..4) {
            for (transpose in listOf(false, true)) {
                for (sashimi in listOf(false, true)) {
                    val candidates = when (size) {
                        2 -> mapOf(0 to setOf(0, 1, 3), 4 to setOf(0, 3))
                        3 -> mapOf(0 to setOf(0, 1, 3, 6), 3 to setOf(0, 3), 6 to setOf(3, 6))
                        else -> mapOf(
                            0 to setOf(0, 1, 3, 6, 8),
                            3 to setOf(0, 3),
                            6 to setOf(3, 6),
                            7 to setOf(6, 8),
                        )
                    }.toMutableMap()
                    if (sashimi) candidates[0] = candidates.getValue(0) - 0
                    val state = fishState(candidates, transpose)
                    val step = FinnedFishDetector(size).find(state)
                    val description = "size=$size, transpose=$transpose, sashimi=$sashimi"

                    assertNotNull(description, step)
                    step!!
                    assertEquals(expectedTechnique(size), step.technique)
                    val target = if (transpose) CellRef(0, 1) else CellRef(1, 0)
                    val fin = if (transpose) CellRef(1, 0) else CellRef(0, 1)
                    assertTrue(description, step.removes(target))
                    assertEquals(setOf(CandidateRef(fin, DIGIT)), step.evidence.finCandidates)
                    assertEquals(size, step.evidence.baseHouses.size)
                    assertEquals(size, step.evidence.coverHouses.size)
                    assertTrue(step.evidence.baseHouses.all {
                        it.type == if (transpose) HouseType.COLUMN else HouseType.ROW
                    })
                    assertTrue(step.evidence.causeCandidates.containsAll(step.evidence.finCandidates))
                    assertEveryDigitPlacementPreserved(state, step)
                }
            }
        }
    }

    @Test
    fun acceptsMultipleFinsInTheSameBaseHouse() {
        val state = fishState(mapOf(0 to setOf(0, 1, 2, 3), 4 to setOf(0, 3)))
        val step = FinnedFishDetector(2).find(state)

        assertNotNull(step)
        step!!
        assertTrue(step.removes(CellRef(1, 0)))
        assertEquals(
            setOf(CandidateRef(CellRef(0, 1), DIGIT), CandidateRef(CellRef(0, 2), DIGIT)),
            step.evidence.finCandidates,
        )
        assertEveryDigitPlacementPreserved(state, step)
    }

    @Test
    fun acceptsFinsInDifferentBaseHousesWhenTheyShareOneBox() {
        val state = fishState(
            mapOf(
                0 to setOf(0, 1, 3, 6, 8),
                1 to setOf(0, 2, 3),
                3 to setOf(3, 6),
                6 to setOf(6, 8),
            )
        )
        val step = FinnedFishDetector(4).find(state)

        assertNotNull(step)
        step!!
        assertTrue(step.removes(CellRef(2, 0)))
        assertEquals(setOf(CellRef(0, 1), CellRef(1, 2)), step.evidence.finCandidates.map { it.cell }.toSet())
        assertEveryDigitPlacementPreserved(state, step)
    }

    @Test
    fun doesNotEliminateOutsideFinBoxOrCoverHousesOrInsideBaseHouses() {
        val state = fishState(mapOf(0 to setOf(0, 1, 3), 4 to setOf(0, 3)))
        val step = FinnedFishDetector(2).find(state)!!

        assertFalse(step.removes(CellRef(8, 0)))
        assertFalse(step.removes(CellRef(1, 2)))
        assertFalse(step.removes(CellRef(0, 0)))
        assertFalse(step.removes(CellRef(0, 1)))
        assertEquals(setOf(CellRef(1, 0), CellRef(2, 0)), step.eliminations.map { it.candidate.cell }.toSet())
    }

    @Test
    fun rejectsFinsInDifferentBoxes() {
        val state = fishState(mapOf(0 to setOf(0, 1, 3), 4 to setOf(0, 3, 4)))

        assertNull(FinnedFishDetector(2).find(state))
    }

    @Test
    fun doesNotMislabelAnOrdinaryFishAsFinned() {
        val state = fishState(mapOf(0 to setOf(0, 3), 4 to setOf(0, 3)))

        assertNull(FinnedFishDetector(2).find(state))
    }

    @Test
    fun returnsNothingWhenAllAffectedCandidatesAreAlreadyAbsent() {
        val state = fishState(mapOf(0 to setOf(0, 1, 3), 4 to setOf(0, 3)))
        val first = FinnedFishDetector(2).find(state)!!

        assertNull(FinnedFishDetector(2).find(state.apply(first)))
    }

    /**
     * Enumerate every legal placement of the fish digit (one per row, column, box).
     * This independently proves the eliminations for all solutions compatible with
     * the fixture, instead of checking only one selected Sudoku solution.
     */
    private fun assertEveryDigitPlacementPreserved(state: SolverState, step: SolveStep) {
        val placements = IntArray(Sudoku.GRID_SIZE)
        val removedIndices = step.eliminations.map { it.candidate.cell.index }.toSet()
        var solutions = 0
        fun search(row: Int, usedColumns: Int, usedBoxes: Int) {
            if (row == Sudoku.GRID_SIZE) {
                solutions++
                assertTrue(
                    "${step.technique} removed a valid digit placement: ${placements.toList()}",
                    placements.none { it in removedIndices },
                )
                return
            }
            for (col in 0 until Sudoku.GRID_SIZE) {
                val cell = CellRef(row, col)
                val columnBit = 1 shl col
                val boxBit = 1 shl cell.boxIndex
                if (usedColumns and columnBit != 0 || usedBoxes and boxBit != 0) continue
                if (!state.hasCandidate(cell, DIGIT)) continue
                placements[row] = cell.index
                search(row + 1, usedColumns or columnBit, usedBoxes or boxBit)
            }
        }
        search(0, 0, 0)
        assertTrue("The soundness fixture must have at least one solution", solutions > 0)
    }

    private fun fishState(
        rowCandidates: Map<Int, Set<Int>>,
        transpose: Boolean = false,
    ): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        rowCandidates.forEach { (row, columns) ->
            for (col in 0 until Sudoku.GRID_SIZE) {
                if (col !in columns) {
                    val cell = if (transpose) CellRef(col, row) else CellRef(row, col)
                    masks[cell.index] = masks[cell.index] and digitMask(DIGIT).inv()
                }
            }
        }
        return SolverState.fromValuesAndCandidates(IntArray(Sudoku.CELL_COUNT), masks)
    }

    private fun expectedTechnique(size: Int): TechniqueId = when (size) {
        2 -> TechniqueId.FINNED_X_WING
        3 -> TechniqueId.FINNED_SWORDFISH
        else -> TechniqueId.FINNED_JELLYFISH
    }

    private fun SolveStep.removes(cell: CellRef): Boolean =
        eliminations.any { it.candidate == CandidateRef(cell, DIGIT) }

    private companion object {
        const val DIGIT = 9
    }
}
