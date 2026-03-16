package com.galaxyrio.sudokusolver.game.generator

class ValidSudokuGenerator {

    /**
     * Generates a fully solved grid.
     */
    fun generateFullGrid(): Array<IntArray> {
        val grid = Array(9) { IntArray(9) }
        solve(grid)
        return grid
    }

    /**
     * Solves the given grid using a recursive backtracking algorithm.
     * Returns true if a solution is found, false otherwise.
     * The grid is modified in place.
     */
    fun solve(grid: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (grid[row][col] == 0) {
                    val numbers = (1..9).shuffled() // Randomize order for generating diverse grids
                    for (number in numbers) {
                        if (isValid(grid, row, col, number)) {
                            grid[row][col] = number
                            if (solve(grid)) {
                                return true
                            }
                            grid[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * Checks if placing a number at (row, col) is valid.
     */
    private fun isValid(grid: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        for (i in 0 until 9) {
            if (grid[row][i] == number || grid[i][col] == number ||
                grid[row - row % 3 + i / 3][col - col % 3 + i % 3] == number
            ) {
                return false
            }
        }
        return true
    }
}

