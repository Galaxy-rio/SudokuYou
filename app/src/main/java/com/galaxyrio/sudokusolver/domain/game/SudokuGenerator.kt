package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlin.random.Random

class SudokuGenerator(
    private val random: Random = Random.Default,
) {
    /**
     * Generates a puzzle by removing values from a completed board while
     * preserving a unique solution.
     */
    fun generate(clues: Int = 30): Sudoku {
        require(clues in 17 until Sudoku.CELL_COUNT) {
            "Clue count must be between 17 and 80."
        }

        val solver = ValidSudokuGenerator(random)
        val grid = solver.generateFullGrid()

        val positions = buildList {
            repeat(Sudoku.GRID_SIZE) { row ->
                repeat(Sudoku.GRID_SIZE) { col ->
                    add(row to col)
                }
            }
        }.shuffled(random)

        var currentClues = Sudoku.CELL_COUNT

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
        val cells = buildList {
            repeat(Sudoku.GRID_SIZE) { row ->
                repeat(Sudoku.GRID_SIZE) { col ->
                    val value = grid[row][col]
                    add(
                        Cell(
                            value = value,
                            isFixed = value != 0,
                        )
                    )
                }
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

        if (c == Sudoku.GRID_SIZE) {
            c = 0
            r++
        }

        if (r == Sudoku.GRID_SIZE) return countSoFar + 1

        if (grid[r][c] != 0) {
            return solveCount(grid, r, c + 1, countSoFar)
        }

        var currentCount = countSoFar
        for (num in 1..Sudoku.GRID_SIZE) {
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


