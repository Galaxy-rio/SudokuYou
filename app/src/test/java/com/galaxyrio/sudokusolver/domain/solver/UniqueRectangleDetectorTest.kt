package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniqueRectangleDetectorTest {
    private val detector = UniqueRectangleDetector()

    @Test
    fun detectsType1AndIncompleteType1() {
        val type1 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2),
                d = setOf(1, 2, 3),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_1, type1?.technique)
        assertTrue(type1!!.removes(D, 1))
        assertTrue(type1.removes(D, 2))

        val type1Missing = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2),
                d = setOf(1, 3),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_1M, type1Missing?.technique)
        assertTrue(type1Missing!!.removes(D, 1))
    }

    @Test
    fun detectsType2AndType3() {
        val type2 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2, 3),
                d = setOf(1, 2, 3),
                extras = listOf(cell(1, 6).withCandidates(3, 4)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_2, type2?.technique)
        assertTrue(type2!!.removes(cell(1, 6), 3))

        val type3 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2, 3),
                d = setOf(1, 2, 4),
                extras = listOf(
                    cell(1, 6).withCandidates(3, 4),
                    cell(1, 7).withCandidates(3, 4, 5),
                ),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_3, type3?.technique)
        assertTrue(type3!!.removes(cell(1, 7), 3))
        assertTrue(type3.removes(cell(1, 7), 4))
    }

    @Test
    fun detectsType4AndIncompleteType4() {
        val type4 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2, 3),
                d = setOf(1, 2, 4),
                extras = listOf(cell(1, 6).withCandidates(1, 5)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_4, type4?.technique)
        assertTrue(type4!!.removes(C, 1))
        assertTrue(type4.removes(D, 1))

        val type4Missing = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2),
                c = setOf(1, 2, 3),
                d = setOf(1, 3),
                extras = listOf(cell(1, 6).withCandidates(2, 4)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_4M, type4Missing?.technique)
        assertTrue(type4Missing!!.removes(C, 2))
    }

    @Test
    fun detectsType5AndType5Plus() {
        val type5 = detector.find(
            rectangleState(
                a = setOf(1, 2, 3),
                b = setOf(1, 2),
                c = setOf(1, 2),
                d = setOf(1, 2, 3),
                extras = listOf(cell(1, 1).withCandidates(3, 4)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_5, type5?.technique)
        assertTrue(type5!!.removes(cell(1, 1), 3))

        val type5Plus = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2, 3),
                c = setOf(1, 2, 3),
                d = setOf(1, 2, 3),
                extras = listOf(cell(1, 4).withCandidates(3, 4)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_5P, type5Plus?.technique)
        assertTrue(type5Plus!!.removes(cell(1, 4), 3))
    }

    @Test
    fun detectsType6AndType7() {
        val type6 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2, 3),
                c = setOf(1, 2, 4),
                d = setOf(1, 2),
                extras = listOf(
                    cell(2, 3).withCandidates(1, 5),
                    cell(2, 0).withCandidates(1, 6),
                ),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_6, type6?.technique)
        assertTrue(type6!!.removes(B, 2))
        assertTrue(type6.removes(C, 2))
        assertTrue(type6.removes(A, 1))
        assertTrue(type6.removes(D, 1))

        val type7 = detector.find(
            rectangleState(
                a = setOf(1, 2),
                b = setOf(1, 2, 3),
                c = setOf(1, 2, 4),
                d = setOf(1, 2, 5),
                extras = listOf(cell(1, 6).withCandidates(1, 6)),
            )
        )
        assertEquals(TechniqueId.UNIQUE_RECTANGLE_TYPE_7, type7?.technique)
        assertTrue(type7!!.removes(D, 1))
    }

    @Test
    fun rejectsFourBoxRectangle() {
        val state = stateOf(
            cell(0, 0).withCandidates(1, 2),
            cell(0, 4).withCandidates(1, 2),
            cell(4, 0).withCandidates(1, 2),
            cell(4, 4).withCandidates(1, 2, 3),
        )

        assertNull(detector.find(state))
    }

    private fun rectangleState(
        a: Set<Int>,
        b: Set<Int>,
        c: Set<Int>,
        d: Set<Int>,
        extras: List<Pair<CellRef, Set<Int>>> = emptyList(),
    ): SolverState = stateOf(
        listOf(A to a, B to b, C to c, D to d) + extras,
    )

    private fun stateOf(vararg entries: Pair<CellRef, Set<Int>>): SolverState =
        stateOf(entries.toList())

    private fun stateOf(entries: List<Pair<CellRef, Set<Int>>>): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT)
        entries.forEach { (cell, digits) ->
            masks[cell.index] = digits.fold(0) { mask, digit -> mask or digitMask(digit) }
        }
        return SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )
    }

    private fun SolveStep.removes(cell: CellRef, digit: Int): Boolean =
        eliminations.any { it.candidate == CandidateRef(cell, digit) }

    private fun CellRef.withCandidates(vararg digits: Int): Pair<CellRef, Set<Int>> =
        this to digits.toSet()

    private companion object {
        val A = cell(0, 0)
        val B = cell(0, 3)
        val C = cell(1, 0)
        val D = cell(1, 3)

        fun cell(row: Int, column: Int): CellRef = CellRef(row, column)
    }
}
