package com.galaxyrio.sudokusolver.data.local

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteEndpoint
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLine
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun sudokuRoundTripPreservesCellState() {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(value = 5, isFixed = true)
        cells[1] = Cell(candidates = setOf(2, 4, 7))
        cells[2] = Cell(value = 3)
        cells[3] = Cell(isCandidateSetExplicit = true)
        val original = Sudoku(cells)

        val restored = converters.toSudoku(converters.fromSudoku(original))

        assertEquals(original, restored)
    }

    @Test
    fun malformedFixedEmptyCellIsSanitized() {
        val data = buildList {
            add("0|true|1,2")
            repeat(Sudoku.CELL_COUNT - 1) { add("0|false|") }
        }.joinToString(";")

        val restored = converters.toSudoku(data)

        assertFalse(restored.cells.first().isFixed)
        assertEquals(setOf(1, 2), restored.cells.first().candidates)
        assertTrue(restored.cells.first().isCandidateSetExplicit)
    }

    @Test
    fun invalidBoardEncodingFallsBackToEmptyBoard() {
        val restored = converters.toSudoku("not-a-board")

        assertTrue(restored.cells.all { !it.isSolved() && !it.isFixed })
    }

    @Test
    fun unknownDifficultyFallsBackToMedium() {
        assertEquals(Difficulty.MEDIUM, converters.toDifficulty("UNKNOWN"))
    }

    @Test
    fun everyDifficultyIncludingBrutalRoundTrips() {
        Difficulty.entries.forEach { difficulty ->
            assertEquals(
                difficulty,
                converters.toDifficulty(converters.fromDifficulty(difficulty)),
            )
        }
    }

    @Test
    fun sudokuSolutionRoundTripPreservesAllDigits() {
        val solution = SudokuSolution(
            ("534678912" +
                "672195348" +
                "198342567" +
                "859761423" +
                "426853791" +
                "713924856" +
                "961537284" +
                "287419635" +
                "345286179").map(Char::digitToInt)
        )

        assertEquals(
            solution,
            converters.toSudokuSolution(converters.fromSudokuSolution(solution)),
        )
    }

    @Test
    fun advancedNotesRoundTripPreservesAllAnnotationTypes() {
        val original = AdvancedNotes(
            highlightedDigits = setOf(2, 7),
            highlightBivalueCandidates = true,
            frameHighlightedCells = true,
            cellColors = mapOf(10 to AdvancedNoteColor.ORANGE),
            candidateColors = mapOf(
                AdvancedNotes.candidateKey(11, 7) to AdvancedNoteColor.BLUE,
            ),
            lines = listOf(
                AdvancedNoteLine(
                    start = AdvancedNoteEndpoint(cellIndex = 11, candidateDigit = 7),
                    end = AdvancedNoteEndpoint(cellIndex = 40),
                    style = AdvancedNoteLineStyle.DASHED,
                    color = AdvancedNoteColor.VIOLET,
                )
            ),
        )

        assertEquals(
            original,
            converters.toAdvancedNotes(converters.fromAdvancedNotes(original)),
        )
    }
}
