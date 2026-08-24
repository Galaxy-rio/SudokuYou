package com.galaxyrio.sudokusolver.ui.util

import android.content.res.Resources
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.CellRef

internal fun Resources.cellCoordinateLabel(
    cell: CellRef,
    notation: CoordinateNotation,
): String = formatCellCoordinate(cell, notation) { row, column ->
    getString(R.string.game_hint_cell, row, column)
}

internal fun formatCellCoordinate(
    cell: CellRef,
    notation: CoordinateNotation,
    localizedLabel: (row: Int, column: Int) -> String,
): String {
    val row = cell.row + 1
    val column = cell.col + 1
    return when (notation) {
        CoordinateNotation.LOCALIZED -> localizedLabel(row, column)
        CoordinateNotation.RCB -> "R${row}C$column"
        CoordinateNotation.K9 -> "${K9_ROW_LABELS[cell.row]}$column"
        CoordinateNotation.EXCEL -> "${EXCEL_COLUMN_LABELS[cell.col]}$row"
    }
}

private const val K9_ROW_LABELS = "ABCDEFGHK"
private const val EXCEL_COLUMN_LABELS = "ABCDEFGHI"
