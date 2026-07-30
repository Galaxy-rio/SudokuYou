package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlin.random.Random

class ValidSudokuGenerator(
    private val random: Random = Random.Default,
) {

    /**
     * Generates a fully solved grid.
     */
    fun generateFullGrid(): Array<IntArray> {
        val grid = Array(Sudoku.GRID_SIZE) { IntArray(Sudoku.GRID_SIZE) }
        solve(grid)
        return grid
    }

    /**
     * Solves the given grid using a recursive backtracking algorithm.
     * Returns true if a solution is found, false otherwise.
     * The grid is modified in place.
     */
    fun solve(grid: Array<IntArray>): Boolean {
        require(
            grid.size == Sudoku.GRID_SIZE &&
                grid.all { it.size == Sudoku.GRID_SIZE }
        ) {
            "A Sudoku grid must be 9 by 9."
        }
        require(grid.all { row -> row.all { it in 0..Sudoku.GRID_SIZE } }) {
            "Grid values must be between 0 and 9."
        }
        if (!isGridValid(grid)) return false
        return solveInternal(grid)
    }

    private fun solveInternal(grid: Array<IntArray>): Boolean {
        for (row in 0 until Sudoku.GRID_SIZE) {
            for (col in 0 until Sudoku.GRID_SIZE) {
                if (grid[row][col] == 0) {
                    val numbers = (1..Sudoku.GRID_SIZE).shuffled(random)
                    for (number in numbers) {
                        if (isValid(grid, row, col, number)) {
                            grid[row][col] = number
                            if (solveInternal(grid)) {
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

    private fun isGridValid(grid: Array<IntArray>): Boolean {
        for (row in 0 until Sudoku.GRID_SIZE) {
            for (col in 0 until Sudoku.GRID_SIZE) {
                val value = grid[row][col]
                if (value == 0) continue

                grid[row][col] = 0
                val valid = isValid(grid, row, col, value)
                grid[row][col] = value
                if (!valid) return false
            }
        }
        return true
    }

    /**
     * Checks if placing a number at (row, col) is valid.
     */
    private fun isValid(grid: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        for (i in 0 until Sudoku.GRID_SIZE) {
            if (grid[row][i] == number || grid[i][col] == number ||
                grid[
                    row - row % Sudoku.BOX_SIZE + i / Sudoku.BOX_SIZE
                ][
                    col - col % Sudoku.BOX_SIZE + i % Sudoku.BOX_SIZE
                ] == number
            ) {
                return false
            }
        }
        return true
    }
}

