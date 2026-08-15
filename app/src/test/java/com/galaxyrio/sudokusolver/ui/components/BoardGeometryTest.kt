package com.galaxyrio.sudokusolver.ui.components

import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardGeometryTest {

    private val geometry = BoardGeometry(
        boardSize = 900f,
        outerInset = 2f,
        blockGap = 2f,
        cellGap = 1f,
        candidateInset = 1f,
    )

    @Test
    fun geometryPreservesCellAndBlockGaps() {
        val first = geometry.cellBounds(CellRef(0, 0))
        val nextCell = geometry.cellBounds(CellRef(0, 1))
        val nextBlock = geometry.cellBounds(CellRef(0, 3))
        val endOfFirstBlock = geometry.cellBounds(CellRef(0, 2))

        assertEquals(2f, first.left, TOLERANCE)
        assertEquals(1f, nextCell.left - first.right, TOLERANCE)
        assertEquals(2f, nextBlock.left - endOfFirstBlock.right, TOLERANCE)
    }

    @Test
    fun centerCandidateUsesTheCenterOfItsCell() {
        val cell = CellRef(4, 4)
        val cellCenter = geometry.cellBounds(cell).center
        val candidateCenter = geometry.candidateCenter(CandidateRef(cell, 5))

        assertEquals(cellCenter.x, candidateCenter.x, TOLERANCE)
        assertEquals(cellCenter.y, candidateCenter.y, TOLERANCE)
    }

    @Test
    fun houseBoundsContainTheirFirstAndLastCells() {
        val rowBounds = geometry.houseBounds(HouseRef(HouseType.ROW, 4))
        val boxBounds = geometry.houseBounds(HouseRef(HouseType.BOX, 8))

        assertTrue(rowBounds.contains(geometry.cellBounds(CellRef(4, 0)).center))
        assertTrue(rowBounds.contains(geometry.cellBounds(CellRef(4, 8)).center))
        assertTrue(boxBounds.contains(geometry.cellBounds(CellRef(6, 6)).center))
        assertTrue(boxBounds.contains(geometry.cellBounds(CellRef(8, 8)).center))
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
