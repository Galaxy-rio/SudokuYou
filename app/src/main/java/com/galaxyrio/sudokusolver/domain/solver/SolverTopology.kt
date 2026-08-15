package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

internal val allCells: List<CellRef> =
    List(Sudoku.CELL_COUNT, CellRef::fromIndex)

internal val CellRef.boxIndex: Int
    get() = (row / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE + col / Sudoku.BOX_SIZE

internal fun CellRef.sees(other: CellRef): Boolean =
    this != other && (row == other.row || col == other.col || boxIndex == other.boxIndex)

internal fun CellRef.houses(): List<HouseRef> = listOf(
    HouseRef(HouseType.ROW, row),
    HouseRef(HouseType.COLUMN, col),
    HouseRef(HouseType.BOX, boxIndex),
)

internal fun sharedHouses(first: CellRef, second: CellRef): List<HouseRef> =
    first.houses().filter { it in second.houses() }

internal fun SolverState.unsolvedCells(): List<CellRef> =
    allCells.filter { valueAt(it) == 0 }

internal fun SolverState.candidateCells(digit: Int): List<CellRef> =
    unsolvedCells().filter { hasCandidate(it, digit) }

internal fun SolverState.candidateCells(house: HouseRef, digit: Int): List<CellRef> =
    house.cells().filter { valueAt(it) == 0 && hasCandidate(it, digit) }

internal fun SolverState.biValueCells(): List<CellRef> =
    unsolvedCells().filter { candidateMaskAt(it).countOneBits() == 2 }

internal fun commonPeers(cells: Collection<CellRef>): Set<CellRef> {
    if (cells.isEmpty()) return emptySet()
    return allCells
        .asSequence()
        .filter { candidate -> candidate !in cells }
        .filter { candidate -> cells.all(candidate::sees) }
        .toSet()
}

internal data class StrongLink(
    val first: CellRef,
    val second: CellRef,
    val digit: Int,
    val house: HouseRef,
)

internal fun SolverState.strongLinks(digit: Int): List<StrongLink> = buildList {
    for (house in orderedHouses) {
        val cells = candidateCells(house, digit)
        if (cells.size == 2) {
            add(StrongLink(cells[0], cells[1], digit, house))
        }
    }
}.distinctBy { link ->
    val low = minOf(link.first.index, link.second.index)
    val high = maxOf(link.first.index, link.second.index)
    Triple(low, high, link.house)
}

internal fun SolverState.strongLinkGraph(digit: Int): Map<CellRef, Set<CellRef>> {
    val graph = mutableMapOf<CellRef, MutableSet<CellRef>>()
    strongLinks(digit).forEach { link ->
        graph.getOrPut(link.first, ::mutableSetOf).add(link.second)
        graph.getOrPut(link.second, ::mutableSetOf).add(link.first)
    }
    return graph
}

internal fun <T> List<T>.combinations(size: Int): Sequence<List<T>> = sequence {
    require(size >= 0)
    if (size == 0) {
        yield(emptyList())
        return@sequence
    }
    if (this@combinations.size < size) return@sequence

    suspend fun SequenceScope<List<T>>.choose(
        startIndex: Int,
        selected: MutableList<T>,
    ) {
        if (selected.size == size) {
            yield(selected.toList())
            return
        }

        val remainingNeeded = size - selected.size
        val lastStart = this@combinations.size - remainingNeeded
        for (index in startIndex..lastStart) {
            selected += this@combinations[index]
            choose(index + 1, selected)
            selected.removeAt(selected.lastIndex)
        }
    }

    choose(startIndex = 0, selected = mutableListOf())
}
