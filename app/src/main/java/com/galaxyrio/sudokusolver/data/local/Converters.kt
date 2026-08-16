package com.galaxyrio.sudokusolver.data.local

import androidx.room.TypeConverter
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromSudoku(sudoku: Sudoku): String =
        sudoku.cells.joinToString(separator = ";") { cell ->
            "${cell.value}|${cell.isFixed}|${cell.candidates.sorted().joinToString(",")}" +
                "|${cell.isCandidateSetExplicit}"
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
            val isCandidateSetExplicit = if (value == 0) {
                parts.getOrNull(3)?.toBooleanStrictOrNull() ?: candidates.isNotEmpty()
            } else {
                false
            }

            Cell(
                value = value,
                candidates = if (value == 0) candidates else emptySet(),
                isFixed = isFixed && value != 0,
                isCandidateSetExplicit = isCandidateSetExplicit,
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

    @TypeConverter
    fun fromSudokuSolution(solution: SudokuSolution?): String? =
        solution?.digits?.joinToString(separator = "")

    @TypeConverter
    fun toSudokuSolution(data: String?): SudokuSolution? {
        if (data == null || data.length != Sudoku.CELL_COUNT) return null
        val digits = data.mapNotNull { it.digitToIntOrNull() }
        if (digits.size != Sudoku.CELL_COUNT || digits.any { it !in 1..Sudoku.GRID_SIZE }) {
            return null
        }
        return SudokuSolution(digits)
    }

    @TypeConverter
    fun fromAdvancedNotes(notes: AdvancedNotes?): String? =
        notes?.let { json.encodeToString(it) }

    @TypeConverter
    fun toAdvancedNotes(data: String?): AdvancedNotes? = data?.let {
        runCatching { json.decodeFromString<AdvancedNotes>(it) }.getOrNull()
    }
}
