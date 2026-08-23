package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution

/**
 * Resolves imported grids while treating explicit pencil marks as Sukaku constraints.
 * A result is returned only when the constrained puzzle has exactly one solution.
 */
internal class ImportedPuzzleSolutionResolver {

    fun resolve(puzzle: Sudoku): SudokuSolution? {
        val values = IntArray(Sudoku.CELL_COUNT)
        val rowMasks = IntArray(Sudoku.GRID_SIZE)
        val columnMasks = IntArray(Sudoku.GRID_SIZE)
        val boxMasks = IntArray(Sudoku.GRID_SIZE)
        val allowedMasks = IntArray(Sudoku.CELL_COUNT) { ALL_DIGITS_MASK }

        puzzle.cells.forEachIndexed { index, cell ->
            val row = index / Sudoku.GRID_SIZE
            val column = index % Sudoku.GRID_SIZE
            val box = boxIndex(row, column)
            if (cell.isSolved()) {
                val bit = digitBit(cell.value)
                if ((rowMasks[row] or columnMasks[column] or boxMasks[box]) and bit != 0) {
                    return null
                }
                values[index] = cell.value
                rowMasks[row] = rowMasks[row] or bit
                columnMasks[column] = columnMasks[column] or bit
                boxMasks[box] = boxMasks[box] or bit
            } else if (cell.isCandidateSetExplicit) {
                allowedMasks[index] = cell.candidates.fold(0) { mask, digit ->
                    mask or digitBit(digit)
                }
            }
        }

        var solutionCount = 0
        var firstSolution: IntArray? = null

        fun search() {
            if (solutionCount >= MAX_SOLUTIONS) return

            var bestIndex = -1
            var bestMask = 0
            var bestCount = Int.MAX_VALUE
            values.indices.forEach { index ->
                if (values[index] != 0) return@forEach
                val row = index / Sudoku.GRID_SIZE
                val column = index % Sudoku.GRID_SIZE
                val used = rowMasks[row] or columnMasks[column] or boxMasks[boxIndex(row, column)]
                val candidates = allowedMasks[index] and used.inv() and ALL_DIGITS_MASK
                val count = candidates.countOneBits()
                if (count == 0) return
                if (count < bestCount) {
                    bestIndex = index
                    bestMask = candidates
                    bestCount = count
                }
            }

            if (bestIndex == -1) {
                solutionCount += 1
                if (firstSolution == null) firstSolution = values.copyOf()
                return
            }

            val row = bestIndex / Sudoku.GRID_SIZE
            val column = bestIndex % Sudoku.GRID_SIZE
            val box = boxIndex(row, column)
            var candidates = bestMask
            while (candidates != 0 && solutionCount < MAX_SOLUTIONS) {
                val bit = candidates and -candidates
                val digit = bit.countTrailingZeroBits() + 1
                values[bestIndex] = digit
                rowMasks[row] = rowMasks[row] or bit
                columnMasks[column] = columnMasks[column] or bit
                boxMasks[box] = boxMasks[box] or bit

                search()

                values[bestIndex] = 0
                rowMasks[row] = rowMasks[row] xor bit
                columnMasks[column] = columnMasks[column] xor bit
                boxMasks[box] = boxMasks[box] xor bit
                candidates = candidates xor bit
            }
        }

        search()
        return firstSolution
            ?.takeIf { solutionCount == 1 }
            ?.let { SudokuSolution(it.toList()) }
    }

    private fun boxIndex(row: Int, column: Int): Int =
        (row / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE + column / Sudoku.BOX_SIZE

    private fun digitBit(digit: Int): Int = 1 shl (digit - 1)

    private companion object {
        const val ALL_DIGITS_MASK = (1 shl Sudoku.GRID_SIZE) - 1
        const val MAX_SOLUTIONS = 2
    }
}
