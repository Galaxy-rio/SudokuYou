package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType

/**
 * Maps logical Sudoku positions to the exact pixel layout used by [SudokuBoard].
 *
 * Keeping this calculation separate lets every overlay use the same coordinate system without
 * coupling solver concepts to the nested Compose rows and columns that render the board.
 */
internal class BoardGeometry(
    boardSize: Float,
    private val outerInset: Float,
    private val blockGap: Float,
    private val cellGap: Float,
    private val candidateInset: Float,
) {
    init {
        require(boardSize > 0f)
        require(outerInset >= 0f)
        require(blockGap >= 0f)
        require(cellGap >= 0f)
        require(candidateInset >= 0f)
    }

    private val contentSize = boardSize - outerInset * 2f
    private val blockSize = (contentSize - blockGap * 2f) / Sudoku.BOX_SIZE
    val cellSize: Float = (blockSize - cellGap * 2f) / Sudoku.BOX_SIZE
    val candidateSize: Float = (cellSize - candidateInset * 2f) / Sudoku.BOX_SIZE

    init {
        require(cellSize > 0f)
        require(candidateSize > 0f)
    }

    fun cellBounds(cell: CellRef): Rect {
        val blockRow = cell.row / Sudoku.BOX_SIZE
        val blockColumn = cell.col / Sudoku.BOX_SIZE
        val rowInBlock = cell.row % Sudoku.BOX_SIZE
        val columnInBlock = cell.col % Sudoku.BOX_SIZE
        val left = outerInset +
            blockColumn * (blockSize + blockGap) +
            columnInBlock * (cellSize + cellGap)
        val top = outerInset +
            blockRow * (blockSize + blockGap) +
            rowInBlock * (cellSize + cellGap)
        return Rect(left, top, left + cellSize, top + cellSize)
    }

    fun candidateBounds(candidate: CandidateRef): Rect {
        val cell = cellBounds(candidate.cell)
        val candidateIndex = candidate.digit - 1
        val candidateRow = candidateIndex / Sudoku.BOX_SIZE
        val candidateColumn = candidateIndex % Sudoku.BOX_SIZE
        val left = cell.left + candidateInset + candidateColumn * candidateSize
        val top = cell.top + candidateInset + candidateRow * candidateSize
        return Rect(left, top, left + candidateSize, top + candidateSize)
    }

    fun candidateCenter(candidate: CandidateRef): Offset = candidateBounds(candidate).center

    fun houseBounds(house: HouseRef): Rect = when (house.type) {
        HouseType.ROW -> {
            val first = cellBounds(CellRef(house.index, 0))
            val last = cellBounds(CellRef(house.index, Sudoku.GRID_SIZE - 1))
            Rect(first.left, first.top, last.right, last.bottom)
        }

        HouseType.COLUMN -> {
            val first = cellBounds(CellRef(0, house.index))
            val last = cellBounds(CellRef(Sudoku.GRID_SIZE - 1, house.index))
            Rect(first.left, first.top, last.right, last.bottom)
        }

        HouseType.BOX -> {
            val startRow = (house.index / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
            val startColumn = (house.index % Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
            val first = cellBounds(CellRef(startRow, startColumn))
            val last = cellBounds(
                CellRef(
                    startRow + Sudoku.BOX_SIZE - 1,
                    startColumn + Sudoku.BOX_SIZE - 1,
                )
            )
            Rect(first.left, first.top, last.right, last.bottom)
        }
    }
}
