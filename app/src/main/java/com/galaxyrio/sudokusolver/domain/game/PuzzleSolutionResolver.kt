package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import kotlin.random.Random

/** Resolves immutable givens while deliberately ignoring player entries and candidate notes. */
internal class PuzzleSolutionResolver {
    fun resolve(currentBoard: Sudoku): SudokuSolution? {
        val givens = Array(Sudoku.GRID_SIZE) { row ->
            IntArray(Sudoku.GRID_SIZE) { column ->
                currentBoard.cells[row * Sudoku.GRID_SIZE + column]
                    .takeIf { it.isFixed }
                    ?.value
                    ?: 0
            }
        }
        val solution = Array(Sudoku.GRID_SIZE) { row -> givens[row].copyOf() }
        if (!ValidSudokuGenerator(Random(SOLUTION_RANDOM_SEED)).solve(solution)) return null
        if (UniqueSolutionChecker().check(givens, solution) != UniquenessResult.Unique) return null
        return SudokuSolution(solution.flatMap(IntArray::asIterable))
    }

    private companion object {
        const val SOLUTION_RANDOM_SEED = 0
    }
}
