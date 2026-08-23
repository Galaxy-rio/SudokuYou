package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuTextExporterTest {

    @Test
    fun originalExportKeepsOnlyFixedGivens() {
        val board = annotatedBoard()

        val exported = SudokuTextExporter.exportOriginal(board, SudokuExportFormat.SUSSER)

        assertEquals(Sudoku.CELL_COUNT, exported.length)
        assertTrue(exported.startsWith("5.."))
        assertFalse('+' in exported)
    }

    @Test
    fun susserCurrentExportDistinguishesEntriesAndCandidateEliminations() {
        val exported = SudokuTextExporter.exportCurrent(
            sudoku = annotatedBoard(),
            format = SudokuExportFormat.SUSSER,
            includeCandidates = true,
        )

        assertTrue(exported.startsWith("5+3."))
        assertTrue(exported.contains(':'))
        assertTrue(exported.substringAfter(':').split(' ').contains("213"))
        assertTrue(exported.substringAfter(':').split(' ').contains("913"))
    }

    @Test
    fun everyConfiguredFormatProducesItsExpectedStructure() {
        val board = annotatedBoard()

        val multiline = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.MULTILINE,
            includeCandidates = true,
        )
        assertEquals(13, multiline.lines().size)
        assertTrue(multiline.startsWith(".-------.-------.-------."))

        val pencilmark = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.PENCILMARK,
            includeCandidates = true,
        )
        assertTrue(pencilmark.contains("<5>"))
        assertTrue(pencilmark.contains("*3*"))

        val sukaku = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.SUKAKU,
            includeCandidates = true,
        )
        assertEquals(Sudoku.CELL_COUNT * Sudoku.GRID_SIZE, sukaku.length)

        val excel = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.EXCEL,
            includeCandidates = true,
        )
        assertEquals(Sudoku.GRID_SIZE, excel.lines().size)
        assertTrue('\t' in excel)

        val openSudoku = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.OPEN_SUDOKU,
            includeCandidates = true,
        )
        assertEquals(Sudoku.CELL_COUNT * 3, openSudoku.split('|').size)

        val hodoku = SudokuTextExporter.exportCurrent(
            board,
            SudokuExportFormat.HODOKU,
            includeCandidates = true,
        )
        assertTrue(hodoku.startsWith(":0000:x:5+3"))
        assertTrue(hodoku.endsWith(":::"))
    }

    @Test
    fun disablingCandidateExportOmitsSusserSecondaryDeletions() {
        val exported = SudokuTextExporter.exportCurrent(
            sudoku = annotatedBoard(),
            format = SudokuExportFormat.SUSSER,
            includeCandidates = false,
        )

        assertFalse(':' in exported)
    }

    private fun annotatedBoard(): Sudoku {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(value = 5, isFixed = true)
        cells[1] = Cell(value = 3)
        cells[2] = Cell(candidates = setOf(1))
        return Sudoku(cells)
    }
}
