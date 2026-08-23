package com.galaxyrio.sudokusolver.ui.util

import androidx.annotation.StringRes
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat

@StringRes
fun SudokuExportFormat.titleResource(): Int = when (this) {
    SudokuExportFormat.SUSSER -> R.string.export_format_susser
    SudokuExportFormat.MULTILINE -> R.string.export_format_multiline
    SudokuExportFormat.PENCILMARK -> R.string.export_format_pencilmark
    SudokuExportFormat.SUKAKU -> R.string.export_format_sukaku
    SudokuExportFormat.EXCEL -> R.string.export_format_excel
    SudokuExportFormat.OPEN_SUDOKU -> R.string.export_format_open_sudoku
    SudokuExportFormat.HODOKU -> R.string.export_format_hodoku
}
