package com.galaxyrio.sudokusolver.game.generator

import com.galaxyrio.sudokusolver.game.Cell
import com.galaxyrio.sudokusolver.game.Sudoku


class SudokuGenerator {

    // Generates a new Sudoku puzzle with the specified difficulty/clues.
    // This is a simple implementation:
    // 1. Generate full grid
    // 2. Remove cells one by one, checking for unique solution
    fun generate(clues: Int = 30): Sudoku {
        val solver = ValidSudokuGenerator()
        val grid = solver.generateFullGrid()

        val random = java.util.Random()

        // Flatten list of coordinates
        val positions = ArrayList<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                positions.add(Pair(r, c))
            }
        }
        positions.shuffle(random)

        var currentClues = 81

        for ((row, col) in positions) {
            if (currentClues <= clues) break

            val backup = grid[row][col]
            if (backup != 0) {
                grid[row][col] = 0 // Remove number temporarily

                // Check if solution is still unique
                val solutions = countSolutions(grid)

                if (solutions != 1) {
                    grid[row][col] = backup // Put it back if not unique
                } else {
                    currentClues--
                }
            }
        }

        // Convert to Sudoku object
        val cells = ArrayList<Cell>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = grid[r][c]
                cells.add(
                    Cell(
                        value,
                        if (value != 0) emptySet() else emptySet(),
                        isFixed = (value != 0)
                    )
                )
            }
        }
        return Sudoku(cells)
    }

    private fun countSolutions(grid: Array<IntArray>): Int {
        return solveCount(grid, 0, 0, 0)
    }

    private fun solveCount(grid: Array<IntArray>, row: Int, col: Int, countSoFar: Int): Int {
        var r = row
        var c = col

        if (c == 9) {
            c = 0
            r++
        }

        if (r == 9) return countSoFar + 1

        if (grid[r][c] != 0) {
            return solveCount(grid, r, c + 1, countSoFar)
        }

        var currentCount = countSoFar
        for (num in 1..9) {
            if (isValid(grid, r, c, num)) {
                grid[r][c] = num
                currentCount = solveCount(grid, r, c + 1, currentCount)
                grid[r][c] = 0 // Backtrack
                if (currentCount > 1) return currentCount
            }
        }
        return currentCount
    }

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


