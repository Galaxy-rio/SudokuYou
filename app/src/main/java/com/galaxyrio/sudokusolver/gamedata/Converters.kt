package com.galaxyrio.sudokusolver.gamedata

import androidx.room.TypeConverter
import com.galaxyrio.sudokusolver.game.Cell
import com.galaxyrio.sudokusolver.game.Sudoku
import com.galaxyrio.sudokusolver.ui.screens.play.Difficulty

class Converters {
    @TypeConverter
    fun fromSudoku(sudoku: Sudoku): String {
        return sudoku.cells.joinToString(";") { cell ->
            "${cell.value}|${cell.isFixed}|${cell.candidates.joinToString(",")}"
        }
    }

    @TypeConverter
    fun toSudoku(data: String): Sudoku {
        if (data.isEmpty()) return Sudoku()
        val cells = data.split(";").map { cellStr ->
            val parts = cellStr.split("|")
            val value = parts[0].toIntOrNull() ?: 0
            val isFixed = parts.getOrNull(1)?.toBoolean() ?: false
            val candidatesStr = parts.getOrNull(2) ?: ""
            val candidates = if (candidatesStr.isEmpty()) emptySet() else candidatesStr.split(",")
                .mapNotNull { it.toIntOrNull() }.toSet()

            Cell(value, candidates, isFixed)
        }
        return Sudoku(cells)
    }

    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toDifficulty(data: String): Difficulty {
        return try {
            Difficulty.valueOf(data)
        } catch (e: Exception) {
            Difficulty.EASY // Default
        }
    }
}
