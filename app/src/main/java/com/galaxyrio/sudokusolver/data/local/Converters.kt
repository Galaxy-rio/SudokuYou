package com.galaxyrio.sudokusolver.data.local

import androidx.room.TypeConverter
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku

class Converters {
    @TypeConverter
    fun fromSudoku(sudoku: Sudoku): String =
        sudoku.cells.joinToString(separator = ";") { cell ->
            "${cell.value}|${cell.isFixed}|${cell.candidates.sorted().joinToString(",")}"
        }

    @TypeConverter
    fun toSudoku(data: String): Sudoku {
        val encodedCells = data.split(';')
        if (encodedCells.size != Sudoku.CELL_COUNT) return Sudoku()

        val cells = encodedCells.map { encodedCell ->
            val parts = encodedCell.split('|')
            val value = (parts.firstOrNull()?.toIntOrNull() ?: 0)
                .coerceIn(0, Sudoku.GRID_SIZE)
            val isFixed = parts.getOrNull(1)?.toBoolean() ?: false
            val candidates = parts
                .getOrNull(2)
                .orEmpty()
                .split(',')
                .mapNotNull(String::toIntOrNull)
                .filterTo(sortedSetOf()) { it in 1..Sudoku.GRID_SIZE }

            Cell(
                value = value,
                candidates = if (value == 0) candidates else emptySet(),
                isFixed = isFixed && value != 0,
            )
        }
        return Sudoku(cells)
    }

    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toDifficulty(data: String): Difficulty =
        Difficulty.entries.find { it.name == data } ?: Difficulty.MEDIUM
}
