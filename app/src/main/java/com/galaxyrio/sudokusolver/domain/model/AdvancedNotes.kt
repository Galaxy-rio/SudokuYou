package com.galaxyrio.sudokusolver.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AdvancedNoteColor(val argb: Long) {
    RED(0xFFE84C4CL),
    ORANGE(0xFFF28C28L),
    YELLOW(0xFFF2C94CL),
    LIME(0xFFA4C639L),
    GREEN(0xFF3FAE65L),
    CYAN(0xFF31AFC4L),
    BLUE(0xFF3976D6L),
    INDIGO(0xFF5B5FC7L),
    VIOLET(0xFF9B59B6L),
}

@Serializable
enum class AdvancedNoteLineStyle {
    SOLID,
    DASHED,
}

@Serializable
data class AdvancedNoteEndpoint(
    val cellIndex: Int,
    val candidateDigit: Int? = null,
) {
    init {
        require(cellIndex in 0 until Sudoku.CELL_COUNT)
        require(candidateDigit == null || candidateDigit in 1..Sudoku.GRID_SIZE)
    }
}

@Serializable
data class AdvancedNoteLine(
    val start: AdvancedNoteEndpoint,
    val end: AdvancedNoteEndpoint,
    val style: AdvancedNoteLineStyle,
    val color: AdvancedNoteColor,
) {
    init {
        require(start != end) { "An advanced-note line needs two distinct endpoints." }
    }

    fun hasEndpoints(first: AdvancedNoteEndpoint, second: AdvancedNoteEndpoint): Boolean =
        (start == first && end == second) || (start == second && end == first)
}

/** Persistent, solver-independent annotations used by the advanced input mode. */
@Serializable
data class AdvancedNotes(
    val highlightedDigits: Set<Int> = emptySet(),
    val highlightBivalueCandidates: Boolean = false,
    val frameHighlightedCells: Boolean = false,
    val cellColors: Map<Int, AdvancedNoteColor> = emptyMap(),
    val candidateColors: Map<Int, AdvancedNoteColor> = emptyMap(),
    val lines: List<AdvancedNoteLine> = emptyList(),
) {
    init {
        require(highlightedDigits.all { it in 1..Sudoku.GRID_SIZE })
        require(cellColors.keys.all { it in 0 until Sudoku.CELL_COUNT })
        require(candidateColors.keys.all(::isValidCandidateKey))
    }

    fun candidateColor(cellIndex: Int, digit: Int): AdvancedNoteColor? =
        candidateColors[candidateKey(cellIndex, digit)]

    companion object {
        fun candidateKey(cellIndex: Int, digit: Int): Int {
            require(cellIndex in 0 until Sudoku.CELL_COUNT)
            require(digit in 1..Sudoku.GRID_SIZE)
            return cellIndex * 10 + digit
        }

        fun candidateCellIndex(key: Int): Int = key / 10

        private fun isValidCandidateKey(key: Int): Boolean =
            candidateCellIndex(key) in 0 until Sudoku.CELL_COUNT &&
                key % 10 in 1..Sudoku.GRID_SIZE
    }
}
