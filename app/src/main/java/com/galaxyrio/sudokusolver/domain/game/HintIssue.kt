package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef

sealed interface HintIssue {
    data class IncorrectValues(
        val entries: List<IncorrectValue>,
    ) : HintIssue {
        init {
            require(entries.isNotEmpty())
        }
    }

    data class MissingCandidates(
        val candidates: List<CandidateRef>,
    ) : HintIssue {
        init {
            require(candidates.isNotEmpty())
        }
    }
}

data class IncorrectValue(
    val cell: CellRef,
    val enteredDigit: Int,
) {
    init {
        require(enteredDigit in 1..Sudoku.GRID_SIZE)
    }
}

/** Applies the requested diagnostic priority: entered values first, Sukaku candidates second. */
object HintIssueDetector {
    fun find(
        currentBoard: Sudoku,
        originalSolution: SudokuSolution,
    ): HintIssue? {
        val incorrectValues = currentBoard.cells.mapIndexedNotNull { index, cell ->
            if (!cell.isFixed && cell.value != 0 && cell.value != originalSolution[index]) {
                IncorrectValue(
                    cell = CellRef.fromIndex(index),
                    enteredDigit = cell.value,
                )
            } else {
                null
            }
        }
        if (incorrectValues.isNotEmpty()) {
            return HintIssue.IncorrectValues(incorrectValues)
        }

        val missingCandidates = currentBoard.cells.mapIndexedNotNull { index, cell ->
            val solutionDigit = originalSolution[index]
            if (cell.value == 0 &&
                cell.isCandidateSetExplicit &&
                solutionDigit !in cell.candidates
            ) {
                CandidateRef(CellRef.fromIndex(index), solutionDigit)
            } else {
                null
            }
        }
        return if (missingCandidates.isNotEmpty()) {
            HintIssue.MissingCandidates(missingCandidates)
        } else {
            null
        }
    }
}
