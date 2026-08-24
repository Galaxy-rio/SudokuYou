package com.galaxyrio.sudokusolver.ui.util

import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import org.junit.Assert.assertEquals
import org.junit.Test

class CellCoordinateTextTest {

    @Test
    fun formatsAllSupportedCoordinateNotations() {
        val cell = CellRef(row = 1, col = 5)
        val localizedLabel: (Int, Int) -> String = { row, column ->
            "row $row, column $column"
        }

        assertEquals(
            "row 2, column 6",
            formatCellCoordinate(cell, CoordinateNotation.LOCALIZED, localizedLabel),
        )
        assertEquals(
            "R2C6",
            formatCellCoordinate(cell, CoordinateNotation.RCB, localizedLabel),
        )
        assertEquals(
            "B6",
            formatCellCoordinate(cell, CoordinateNotation.K9, localizedLabel),
        )
        assertEquals(
            "F2",
            formatCellCoordinate(cell, CoordinateNotation.EXCEL, localizedLabel),
        )
    }

    @Test
    fun k9SkipsAmbiguousIAndJRows() {
        assertEquals(
            "K9",
            formatCellCoordinate(
                cell = CellRef(row = 8, col = 8),
                notation = CoordinateNotation.K9,
                localizedLabel = { _, _ -> error("Localized label should not be used") },
            ),
        )
    }
}
