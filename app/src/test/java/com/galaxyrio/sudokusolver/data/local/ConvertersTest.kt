package com.galaxyrio.sudokusolver.data.local

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
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
}
