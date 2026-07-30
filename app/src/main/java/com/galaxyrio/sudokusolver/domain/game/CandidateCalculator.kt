package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku

object CandidateCalculator {

    /**
     * Calculates and fills valid candidates for all empty cells in the Sudoku board.
     * Existing candidates will be overwritten.
     *
     * @param sudoku The current Sudoku board state.
     * @return A new Sudoku board with updated candidates.
     */
    fun calculateAllCandidates(sudoku: Sudoku): Sudoku {
        val newCells = sudoku.cells.toMutableList()

        for (row in 0 until Sudoku.GRID_SIZE) {
            for (col in 0 until Sudoku.GRID_SIZE) {
                val index = row * Sudoku.GRID_SIZE + col
                val cell = newCells[index]

                if (cell.value == 0) {
                    val validCandidates = buildSet {
                        for (number in 1..Sudoku.GRID_SIZE) {
                            if (!SudokuValidator.hasConflict(sudoku, row, col, number)) {
                                add(number)
                            }
                        }
                    }
                    newCells[index] = cell.copy(candidates = validCandidates)
                }
            }
        }

        return Sudoku(newCells)
    }
}
