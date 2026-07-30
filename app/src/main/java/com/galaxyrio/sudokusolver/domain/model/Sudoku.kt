package com.galaxyrio.sudokusolver.domain.model

data class Cell(
    val value: Int = 0,
    val candidates: Set<Int> = emptySet(),
    val isFixed: Boolean = false
) {
    init {
        require(value in EMPTY_VALUE..MAX_VALUE) { "Cell value must be between 0 and 9." }
        require(candidates.all { it in MIN_VALUE..MAX_VALUE }) {
            "Candidates must be between 1 and 9."
        }
        require(value == EMPTY_VALUE || candidates.isEmpty()) {
            "A solved cell cannot contain candidates."
        }
        require(!isFixed || value != EMPTY_VALUE) {
            "A fixed cell must contain a value."
        }
    }

    fun isSolved(): Boolean = value != 0

    override fun toString(): String = if (value == EMPTY_VALUE) "." else value.toString()

    private companion object {
        const val EMPTY_VALUE = 0
        const val MIN_VALUE = 1
        const val MAX_VALUE = 9
    }
}

data class Sudoku(val cells: List<Cell> = List(CELL_COUNT) { Cell() }) {

    init {
        require(cells.size == CELL_COUNT) { "A Sudoku board must contain exactly 81 cells." }
    }

    fun getCell(row: Int, col: Int): Cell = cells[indexOf(row, col)]

    fun setCell(row: Int, col: Int, value: Int, isFixed: Boolean = false): Sudoku {
        require(value in EMPTY_VALUE..GRID_SIZE) { "Cell value must be between 0 and 9." }

        val index = indexOf(row, col)
        val currentCell = cells[index]
        if (currentCell.isFixed && !isFixed) return this

        val newCells = cells.toMutableList()
        newCells[index] = Cell(value, emptySet(), isFixed)

        if (value != EMPTY_VALUE) {
            peerIndices(row, col).forEach { peerIndex ->
                val peer = newCells[peerIndex]
                if (!peer.isSolved() && value in peer.candidates) {
                    newCells[peerIndex] = peer.copy(candidates = peer.candidates - value)
                }
            }
        }

        return Sudoku(newCells)
    }

    fun toggleCandidate(row: Int, col: Int, candidate: Int): Sudoku {
        require(candidate in 1..GRID_SIZE) { "Candidate must be between 1 and 9." }

        val index = indexOf(row, col)
        val currentCell = cells[index]
        if (currentCell.isSolved() || currentCell.isFixed) return this

        val newCandidates = if (candidate in currentCell.candidates) {
            currentCell.candidates - candidate
        } else {
            currentCell.candidates + candidate
        }

        val newCells = cells.toMutableList()
        newCells[index] = currentCell.copy(candidates = newCandidates)
        return Sudoku(newCells)
    }

    override fun toString(): String = buildString {
        repeat(GRID_SIZE) { row ->
            repeat(GRID_SIZE) { col ->
                append(getCell(row, col))
                if (col == 2 || col == 5) append(' ')
            }
            appendLine()
            if (row == 2 || row == 5) appendLine()
        }
    }

    fun toGridString(): String =
        cells.joinToString(separator = "") { if (it.value == EMPTY_VALUE) "." else it.value.toString() }

    private fun indexOf(row: Int, col: Int): Int {
        require(row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) {
            "Row and column must be between 0 and 8."
        }
        return row * GRID_SIZE + col
    }

    private fun peerIndices(row: Int, col: Int): Set<Int> = buildSet {
        repeat(GRID_SIZE) { index ->
            add(indexOf(row, index))
            add(indexOf(index, col))
        }

        val boxStartRow = (row / BOX_SIZE) * BOX_SIZE
        val boxStartCol = (col / BOX_SIZE) * BOX_SIZE
        repeat(BOX_SIZE) { rowOffset ->
            repeat(BOX_SIZE) { colOffset ->
                add(indexOf(boxStartRow + rowOffset, boxStartCol + colOffset))
            }
        }
        remove(indexOf(row, col))
    }

    companion object {
        const val GRID_SIZE = 9
        const val BOX_SIZE = 3
        const val CELL_COUNT = GRID_SIZE * GRID_SIZE
        private const val EMPTY_VALUE = 0

        fun fromGridString(value: String): Sudoku {
            val symbols = value.filter { it == '.' || it in '0'..'9' }
            require(symbols.length == CELL_COUNT) {
                "A Sudoku grid string must contain exactly 81 cells."
            }

            return Sudoku(
                symbols.map { symbol ->
                    when (symbol) {
                        '.', '0' -> Cell()
                        else -> Cell(value = symbol.digitToInt(), isFixed = true)
                    }
                }
            )
        }
    }
}
