package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Sudoku

internal data class CellMask81(
    private val lowBits: Long = 0,
    private val highBits: Long = 0,
) {
    fun contains(index: Int): Boolean {
        require(index in 0 until Sudoku.CELL_COUNT)
        return if (index < Long.SIZE_BITS) {
            lowBits and (1L shl index) != 0L
        } else {
            highBits and (1L shl (index - Long.SIZE_BITS)) != 0L
        }
    }

    fun with(index: Int): CellMask81 {
        require(index in 0 until Sudoku.CELL_COUNT)
        return if (index < Long.SIZE_BITS) {
            copy(lowBits = lowBits or (1L shl index))
        } else {
            copy(highBits = highBits or (1L shl (index - Long.SIZE_BITS)))
        }
    }

    fun without(index: Int): CellMask81 {
        require(index in 0 until Sudoku.CELL_COUNT)
        return if (index < Long.SIZE_BITS) {
            copy(lowBits = lowBits and (1L shl index).inv())
        } else {
            copy(highBits = highBits and (1L shl (index - Long.SIZE_BITS)).inv())
        }
    }

    fun intersects(other: CellMask81): Boolean =
        lowBits and other.lowBits != 0L || highBits and other.highBits != 0L

    fun isSubsetOf(other: CellMask81): Boolean =
        lowBits and other.lowBits == lowBits && highBits and other.highBits == highBits

    fun isEmpty(): Boolean = lowBits == 0L && highBits == 0L

    companion object {
        val FULL = CellMask81(
            lowBits = -1L,
            highBits = (1L shl (Sudoku.CELL_COUNT - Long.SIZE_BITS)) - 1L,
        )

        fun fromIndices(indices: Iterable<Int>): CellMask81 =
            indices.fold(CellMask81()) { mask, index -> mask.with(index) }
    }
}

internal sealed interface UniquenessResult {
    data object Invalid : UniquenessResult

    data object Unique : UniquenessResult

    data class Multiple(
        val alternativeSolution: IntArray,
        val differenceMask: CellMask81,
    ) : UniquenessResult
}

/**
 * Proves uniqueness relative to a known completed grid. When another completion exists, the
 * differing cells form an unavoidable set: any clue set selecting this solution must hit it.
 */
internal class UniqueSolutionChecker {
    fun check(
        puzzle: Array<IntArray>,
        expectedSolution: Array<IntArray>,
    ): UniquenessResult {
        if (!puzzle.hasGridShape() || !expectedSolution.hasGridShape()) {
            return UniquenessResult.Invalid
        }
        if (!expectedSolution.isCompleteValidGrid()) return UniquenessResult.Invalid

        val workingGrid = Array(Sudoku.GRID_SIZE) { row -> puzzle[row].copyOf() }
        val rowMasks = IntArray(Sudoku.GRID_SIZE)
        val columnMasks = IntArray(Sudoku.GRID_SIZE)
        val boxMasks = IntArray(Sudoku.GRID_SIZE)

        repeat(Sudoku.GRID_SIZE) { row ->
            repeat(Sudoku.GRID_SIZE) { column ->
                val value = workingGrid[row][column]
                if (value == 0) return@repeat
                if (value !in 1..Sudoku.GRID_SIZE || value != expectedSolution[row][column]) {
                    return UniquenessResult.Invalid
                }

                val bit = value.toBit()
                val box = boxIndex(row, column)
                if (rowMasks[row] and bit != 0 ||
                    columnMasks[column] and bit != 0 ||
                    boxMasks[box] and bit != 0
                ) {
                    return UniquenessResult.Invalid
                }
                rowMasks[row] = rowMasks[row] or bit
                columnMasks[column] = columnMasks[column] or bit
                boxMasks[box] = boxMasks[box] or bit
            }
        }

        val alternative = AlternativeSearch(
            grid = workingGrid,
            expectedSolution = expectedSolution,
            rowMasks = rowMasks,
            columnMasks = columnMasks,
            boxMasks = boxMasks,
        ).find()
            ?: return UniquenessResult.Unique

        var differenceMask = CellMask81()
        alternative.forEachIndexed { index, value ->
            val expected = expectedSolution[index / Sudoku.GRID_SIZE][index % Sudoku.GRID_SIZE]
            if (value != expected) differenceMask = differenceMask.with(index)
        }
        check(!differenceMask.isEmpty())
        return UniquenessResult.Multiple(alternative, differenceMask)
    }

    private class AlternativeSearch(
        private val grid: Array<IntArray>,
        private val expectedSolution: Array<IntArray>,
        private val rowMasks: IntArray,
        private val columnMasks: IntArray,
        private val boxMasks: IntArray,
    ) {
        fun find(): IntArray? = search(differsFromExpected = false)

        private fun search(differsFromExpected: Boolean): IntArray? {
            var bestRow = -1
            var bestColumn = -1
            var bestMask = 0
            var bestCount = Sudoku.GRID_SIZE + 1

            repeat(Sudoku.GRID_SIZE) { row ->
                repeat(Sudoku.GRID_SIZE) { column ->
                    if (grid[row][column] != 0) return@repeat
                    val box = boxIndex(row, column)
                    val candidates = FULL_DIGIT_MASK and
                        (rowMasks[row] or columnMasks[column] or boxMasks[box]).inv()
                    val count = candidates.countOneBits()
                    if (count == 0) return null
                    if (count < bestCount) {
                        bestRow = row
                        bestColumn = column
                        bestMask = candidates
                        bestCount = count
                    }
                }
            }

            if (bestRow == -1) {
                return if (differsFromExpected) {
                    grid.flatMap(IntArray::asIterable).toIntArray()
                } else {
                    null
                }
            }

            val expectedDigit = expectedSolution[bestRow][bestColumn]
            val expectedBit = expectedDigit.toBit()
            var alternativesFirst = bestMask and expectedBit.inv()
            while (alternativesFirst != 0) {
                val bit = alternativesFirst and -alternativesFirst
                tryCandidate(bestRow, bestColumn, bit, differsFromExpected = true)?.let {
                    return it
                }
                alternativesFirst = alternativesFirst xor bit
            }

            if (bestMask and expectedBit != 0) {
                return tryCandidate(
                    row = bestRow,
                    column = bestColumn,
                    bit = expectedBit,
                    differsFromExpected = differsFromExpected,
                )
            }
            return null
        }

        private fun tryCandidate(
            row: Int,
            column: Int,
            bit: Int,
            differsFromExpected: Boolean,
        ): IntArray? {
            val digit = bit.countTrailingZeroBits() + 1
            val box = boxIndex(row, column)
            grid[row][column] = digit
            rowMasks[row] = rowMasks[row] or bit
            columnMasks[column] = columnMasks[column] or bit
            boxMasks[box] = boxMasks[box] or bit

            val result = search(
                differsFromExpected = differsFromExpected || digit != expectedSolution[row][column]
            )

            grid[row][column] = 0
            rowMasks[row] = rowMasks[row] xor bit
            columnMasks[column] = columnMasks[column] xor bit
            boxMasks[box] = boxMasks[box] xor bit
            return result
        }
    }

    private fun Array<IntArray>.hasGridShape(): Boolean =
        size == Sudoku.GRID_SIZE && all { row -> row.size == Sudoku.GRID_SIZE }

    private fun Array<IntArray>.isCompleteValidGrid(): Boolean {
        val rowMasks = IntArray(Sudoku.GRID_SIZE)
        val columnMasks = IntArray(Sudoku.GRID_SIZE)
        val boxMasks = IntArray(Sudoku.GRID_SIZE)
        repeat(Sudoku.GRID_SIZE) { row ->
            repeat(Sudoku.GRID_SIZE) { column ->
                val value = this[row][column]
                if (value !in 1..Sudoku.GRID_SIZE) return false
                val bit = value.toBit()
                val box = boxIndex(row, column)
                if (rowMasks[row] and bit != 0 ||
                    columnMasks[column] and bit != 0 ||
                    boxMasks[box] and bit != 0
                ) {
                    return false
                }
                rowMasks[row] = rowMasks[row] or bit
                columnMasks[column] = columnMasks[column] or bit
                boxMasks[box] = boxMasks[box] or bit
            }
        }
        return true
    }

    private companion object {
        const val FULL_DIGIT_MASK = (1 shl Sudoku.GRID_SIZE) - 1

        fun Int.toBit(): Int = 1 shl (this - 1)

        fun boxIndex(row: Int, column: Int): Int =
            row / Sudoku.BOX_SIZE * Sudoku.BOX_SIZE + column / Sudoku.BOX_SIZE
    }
}

/** Maintains the minimal known unavoidable sets used by the lazy hitting-set search. */
internal class HittingSetConstraints {
    private val unavoidableSets = mutableListOf<CellMask81>()

    val size: Int
        get() = unavoidableSets.size

    fun accepts(clueMask: CellMask81): Boolean =
        unavoidableSets.all(clueMask::intersects)

    fun add(unavoidableSet: CellMask81): Boolean {
        require(!unavoidableSet.isEmpty())
        if (unavoidableSets.any { known -> known.isSubsetOf(unavoidableSet) }) return false
        unavoidableSets.removeAll { known -> unavoidableSet.isSubsetOf(known) }
        unavoidableSets += unavoidableSet
        return true
    }
}
