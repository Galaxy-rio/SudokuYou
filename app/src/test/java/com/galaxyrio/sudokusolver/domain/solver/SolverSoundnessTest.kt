package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.game.SudokuGenerator
import com.galaxyrio.sudokusolver.domain.game.ValidSudokuGenerator
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolverSoundnessTest {

    @Test
    fun everyDeductionPreservesSolutionsAcrossGeneratedPuzzles() {
        Difficulty.entries.forEachIndexed { difficultyIndex, _ ->
            repeat(2) { sampleIndex ->
                val seed = difficultyIndex * 100 + sampleIndex
                val puzzle = SudokuGenerator(Random(seed)).generateWithClueCount(
                    clues = 40 - difficultyIndex * 4
                )
                val solution = solve(puzzle)
                val trace = HumanSolver().solveTrace(puzzle)

                assertNotEquals(SolveTraceStatus.INVALID, trace.status)
                trace.steps.forEachIndexed { stepIndex, step ->
                    step.placements.forEach { placement ->
                        assertEquals(
                            "${step.technique} made a false placement at step $stepIndex",
                            solution[placement.cell.index],
                            placement.digit,
                        )
                    }
                    step.eliminations.forEach { elimination ->
                        val candidate = elimination.candidate
                        assertTrue(
                            "${step.technique} removed the solution at step $stepIndex",
                            solution[candidate.cell.index] != candidate.digit,
                        )
                    }
                }
            }
        }
    }

    private fun solve(sudoku: Sudoku): IntArray {
        val grid = Array(Sudoku.GRID_SIZE) { row ->
            IntArray(Sudoku.GRID_SIZE) { column ->
                sudoku.cells[row * Sudoku.GRID_SIZE + column].value
            }
        }
        assertTrue(ValidSudokuGenerator(Random(10_000)).solve(grid))
        val solution = grid.flatMap(IntArray::asIterable).toIntArray()
        assertNotNull(solution)
        return solution
    }
}
