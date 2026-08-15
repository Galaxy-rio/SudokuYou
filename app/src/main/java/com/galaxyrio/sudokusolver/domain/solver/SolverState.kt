package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

/**
 * Immutable board state used only by the logical solver. These candidates are deliberately
 * separate from the player's pencil marks stored in [Sudoku].
 */
class SolverState private constructor(
    private val values: IntArray,
    private val candidateMasks: IntArray,
) {
    init {
        require(values.size == Sudoku.CELL_COUNT)
        require(candidateMasks.size == Sudoku.CELL_COUNT)
        require(values.all { it in 0..Sudoku.GRID_SIZE })
        require(candidateMasks.all { it and FULL_CANDIDATE_MASK == it })
    }

    fun valueAt(cell: CellRef): Int = values[cell.index]

    fun valueAt(row: Int, col: Int): Int = valueAt(CellRef(row, col))

    fun candidateMaskAt(cell: CellRef): Int = candidateMasks[cell.index]

    fun candidatesAt(cell: CellRef): Set<Int> = candidateMaskAt(cell).digits()

    fun hasCandidate(cell: CellRef, digit: Int): Boolean =
        candidateMaskAt(cell) and digitMask(digit) != 0

    fun isSolved(): Boolean = values.none { it == 0 } && isValid()

    fun isValid(): Boolean {
        if (values.indices.any { index -> values[index] == 0 && candidateMasks[index] == 0 }) {
            return false
        }

        return orderedHouses.none { house ->
            val cells = house.cells()
            val solvedValues = cells.map(::valueAt).filter { it != 0 }
            val hasDuplicate = solvedValues.size != solvedValues.distinct().size
            val hasMissingDigitWithoutCandidate = (1..Sudoku.GRID_SIZE).any { digit ->
                digit !in solvedValues && cells.none { cell ->
                    valueAt(cell) == 0 && hasCandidate(cell, digit)
                }
            }
            hasDuplicate || hasMissingDigitWithoutCandidate
        }
    }

    fun apply(step: SolveStep): SolverState {
        val nextValues = values.copyOf()
        val nextCandidates = candidateMasks.copyOf()

        step.placements.forEach { placement ->
            val index = placement.cell.index
            require(nextValues[index] == 0) { "Cannot place a value in a solved cell." }
            require(nextCandidates[index] and digitMask(placement.digit) != 0) {
                "The placed digit is not a candidate for the target cell."
            }

            nextValues[index] = placement.digit
            nextCandidates[index] = 0
            peerIndices(placement.cell).forEach { peerIndex ->
                if (nextValues[peerIndex] == 0) {
                    nextCandidates[peerIndex] =
                        nextCandidates[peerIndex] and digitMask(placement.digit).inv()
                }
            }
        }

        step.eliminations.forEach { elimination ->
            val index = elimination.candidate.cell.index
            if (nextValues[index] == 0) {
                nextCandidates[index] = nextCandidates[index] and
                    digitMask(elimination.candidate.digit).inv()
            }
        }

        return SolverState(nextValues, nextCandidates)
    }

    override fun equals(other: Any?): Boolean =
        other is SolverState &&
            values.contentEquals(other.values) &&
            candidateMasks.contentEquals(other.candidateMasks)

    override fun hashCode(): Int = 31 * values.contentHashCode() + candidateMasks.contentHashCode()

    companion object {
        internal const val FULL_CANDIDATE_MASK: Int = (1 shl Sudoku.GRID_SIZE) - 1

        fun fromSudoku(sudoku: Sudoku): SolverState {
            val values = IntArray(Sudoku.CELL_COUNT) { index -> sudoku.cells[index].value }
            val candidateMasks = IntArray(Sudoku.CELL_COUNT)

            repeat(Sudoku.CELL_COUNT) { index ->
                if (values[index] != 0) return@repeat

                val cell = CellRef.fromIndex(index)
                var mask = FULL_CANDIDATE_MASK
                peerIndices(cell).forEach { peerIndex ->
                    val peerValue = values[peerIndex]
                    if (peerValue != 0) mask = mask and digitMask(peerValue).inv()
                }
                candidateMasks[index] = mask
            }

            return SolverState(values, candidateMasks)
        }

        internal fun fromValuesAndCandidates(
            values: IntArray,
            candidateMasks: IntArray,
        ): SolverState = SolverState(values.copyOf(), candidateMasks.copyOf())
    }
}

internal fun digitMask(digit: Int): Int {
    require(digit in 1..Sudoku.GRID_SIZE)
    return 1 shl (digit - 1)
}

internal fun candidateMaskOf(vararg digits: Int): Int =
    digits.fold(0) { mask, digit -> mask or digitMask(digit) }

internal fun Int.digits(): Set<Int> = buildSet {
    for (digit in 1..Sudoku.GRID_SIZE) {
        if (this@digits and digitMask(digit) != 0) add(digit)
    }
}

internal fun peerIndices(cell: CellRef): Set<Int> = buildSet {
    repeat(Sudoku.GRID_SIZE) { index ->
        add(CellRef(cell.row, index).index)
        add(CellRef(index, cell.col).index)
    }

    val boxStartRow = (cell.row / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
    val boxStartCol = (cell.col / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
    repeat(Sudoku.BOX_SIZE) { rowOffset ->
        repeat(Sudoku.BOX_SIZE) { colOffset ->
            add(CellRef(boxStartRow + rowOffset, boxStartCol + colOffset).index)
        }
    }
    remove(cell.index)
}
