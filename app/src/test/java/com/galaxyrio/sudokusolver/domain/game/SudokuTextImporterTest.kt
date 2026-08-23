package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuTextImporterTest {

    @Test
    fun everyExportFormatCanBeImportedAgain() {
        val puzzle = Sudoku.fromGridString(PUZZLE)

        SudokuExportFormat.entries.forEach { format ->
            val exported = SudokuTextExporter.exportCurrent(
                sudoku = puzzle,
                format = format,
                includeCandidates = true,
            )

            val imported = SudokuTextImporter.import(exported, format)

            assertNotNull("Failed to import $format", imported)
            assertEquals(Sudoku.CELL_COUNT, imported?.cells?.size)
            if (format != SudokuExportFormat.SUKAKU) {
                assertEquals(5, imported?.getCell(0, 0)?.value)
                assertTrue(imported?.getCell(0, 0)?.isFixed == true)
            }
        }
    }

    @Test
    fun selectedFormatIsValidated() {
        assertNull(
            SudokuTextImporter.import(
                text = PUZZLE,
                format = SudokuExportFormat.OPEN_SUDOKU,
            )
        )
    }

    private companion object {
        const val PUZZLE =
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79"
    }
}
