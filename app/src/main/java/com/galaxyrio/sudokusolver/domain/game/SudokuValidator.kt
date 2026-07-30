package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku

object SudokuValidator {

    /**
     * Checks if placing a value at the given row and column would cause any conflict
     * with other cells in the same row, column, or 3x3 box.
     *
     * @param sudoku The Sudoku board state.
     * @param row The row index (0-8).
     * @param col The column index (0-8).
     * @param value The value to check (1-9).
     * @return True if there is a conflict (invalid move), False if valid.
     */
    fun hasConflict(sudoku: Sudoku, row: Int, col: Int, value: Int): Boolean {
        require(value in 0..Sudoku.GRID_SIZE) {
            "Cell value must be between 0 and 9."
        }
        if (value == 0) return false

        repeat(Sudoku.GRID_SIZE) { index ->
            if (index != col && sudoku.getCell(row, index).value == value) {
                return true
            }
            if (index != row && sudoku.getCell(index, col).value == value) {
                return true
            }
        }

        val boxStartRow = (row / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
        val boxStartCol = (col / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE

        for (r in boxStartRow until boxStartRow + Sudoku.BOX_SIZE) {
            for (c in boxStartCol until boxStartCol + Sudoku.BOX_SIZE) {
                if (r != row || c != col) {
                    if (sudoku.getCell(r, c).value == value) return true
                }
            }
        }

        return false
    }

    /**
     * Checks if the value at the given row and column causes any conflict
     * with other cells in the same row, column, or 3x3 box.

     *
     * @param sudoku The Sudoku board state.
     * @param row The row index (0-8).
     * @param col The column index (0-8).
     * @return True if there is a conflict (invalid move), False if valid.
     */
    fun hasConflict(sudoku: Sudoku, row: Int, col: Int): Boolean =
        hasConflict(sudoku, row, col, sudoku.getCell(row, col).value)

    /**
     * Checks if the entire board is valid (no contradictions).
     */
    fun isBoardValid(sudoku: Sudoku): Boolean {
        for (row in 0 until Sudoku.GRID_SIZE) {
            for (col in 0 until Sudoku.GRID_SIZE) {
                if (hasConflict(sudoku, row, col)) return false
            }
        }
        return true
    }

    fun isSolved(sudoku: Sudoku): Boolean =
        sudoku.cells.all { it.isSolved() } && isBoardValid(sudoku)
}

