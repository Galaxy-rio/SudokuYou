package com.galaxyrio.sudokusolver.domain.model

/** The unique completed grid belonging to a saved puzzle. */
data class SudokuSolution(
    val digits: List<Int>,
) {
    init {
        require(digits.size == Sudoku.CELL_COUNT) {
            "A Sudoku solution must contain exactly 81 digits."
        }
        require(digits.all { it in 1..Sudoku.GRID_SIZE }) {
            "Every Sudoku solution digit must be between 1 and 9."
        }
    }

    operator fun get(index: Int): Int = digits[index]
}
