package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

/**
 * Detects the Unique Rectangle variants exposed by Open Sudoku.
 *
 * Unique Rectangle deductions assume that the puzzle has exactly one solution. The generated
 * puzzles used by the game satisfy that requirement. A future manual-entry solver must verify
 * uniqueness before enabling this detector (and [BugPlusOneDetector]).
 */
class UniqueRectangleDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.UNIQUE_RECTANGLE_TYPE_1

    override fun find(state: SolverState): SolveStep? {
        val candidatePairs = state.biValueCells()
            .map(state::candidatesAt)
            .map(Set<Int>::sorted)
            .distinct()

        for (pairDigits in candidatePairs) {
            val pair = pairDigits.toSet()
            for (cells in rectangleCellSets()) {
                val pattern = completePattern(state, cells, pair) ?: continue
                checkType1(state, pattern)?.let { return it }
                checkType2(state, pattern)?.let { return it }
                checkType5(state, pattern)?.let { return it }
                checkType5Plus(state, pattern)?.let { return it }
                checkType3(state, pattern)?.let { return it }
                checkType4(state, pattern)?.let { return it }
                checkType6(state, pattern)?.let { return it }
                checkType7(state, pattern)?.let { return it }
            }

            for (cells in rectangleCellSets()) {
                checkType1Missing(state, cells, pair)?.let { return it }
                checkType4Missing(state, cells, pair)?.let { return it }
            }
        }
        return null
    }

    private fun checkType1(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.pureCells.size != 3 || pattern.nonPureCells.size != 1) return null
        val target = pattern.nonPureCells.single()
        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1,
            patternCells = pattern.cells,
            pair = pattern.pair,
            eliminations = pattern.pair.map { CandidateElimination(CandidateRef(target, it)) },
        )
    }

    private fun checkType2(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.nonPureCells.size != 2 || !pattern.nonPureCells.areOnSameSide()) return null
        val extras = pattern.nonPureCells.map { state.candidatesAt(it) - pattern.pair }
        if (extras.any { it.size != 1 } || extras.distinct().size != 1) return null
        val extra = extras.first().single()
        val targets = commonPeers(pattern.nonPureCells)
            .filter { state.valueAt(it) == 0 && state.hasCandidate(it, extra) }
        if (targets.isEmpty()) return null

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_2,
            patternCells = pattern.cells,
            pair = pattern.pair,
            extraCauseCells = pattern.nonPureCells,
            eliminations = targets.map { CandidateElimination(CandidateRef(it, extra)) },
        )
    }

    private fun checkType3(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.pureCells.size != 2 || !pattern.pureCells.areOnSameSide()) return null
        val virtualCandidates = pattern.nonPureCells
            .flatMap { state.candidatesAt(it) - pattern.pair }
            .toSet()
        if (virtualCandidates.isEmpty()) return null

        val houses = sharedHouses(pattern.nonPureCells[0], pattern.nonPureCells[1])
        for (house in houses) {
            val possibleCells = house.cells()
                .filter { state.valueAt(it) == 0 && it !in pattern.cells }
                .filter { state.candidatesAt(it).size in 1..3 }
            val possibleCandidates = (
                virtualCandidates + possibleCells.flatMap(state::candidatesAt)
                ).sorted()

            for (subsetSize in 2..4) {
                if (virtualCandidates.size > subsetSize) continue
                for (subsetDigits in possibleCandidates.combinations(subsetSize)) {
                    val subsetCandidates = subsetDigits.toSet()
                    if (!subsetCandidates.containsAll(virtualCandidates)) continue
                    val subsetCells = possibleCells.filter { cell ->
                        subsetCandidates.containsAll(state.candidatesAt(cell))
                    }
                    if (subsetCells.size != subsetSize - 1) continue

                    val causes = pattern.nonPureCells + subsetCells
                    val targets = commonPeers(causes)
                        .filter { state.valueAt(it) == 0 && it !in pattern.cells }
                    val eliminations = targets.flatMap { cell ->
                        subsetCandidates
                            .filter { state.hasCandidate(cell, it) }
                            .map { CandidateElimination(CandidateRef(cell, it)) }
                    }.distinct()
                    if (eliminations.isEmpty()) continue

                    return urStep(
                        state = state,
                        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
                        patternCells = pattern.cells,
                        pair = pattern.pair,
                        extraCauseCells = subsetCells,
                        extraHouses = listOf(house),
                        eliminations = eliminations,
                    )
                }
            }
        }
        return null
    }

    private fun checkType4(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.nonPureCells.size != 2 || !pattern.nonPureCells.areOnSameSide()) return null
        val candidatesSeenOutside = commonPeers(pattern.nonPureCells)
            .filter { state.valueAt(it) == 0 }
            .flatMap(state::candidatesAt)
            .toSet()
            .intersect(pattern.pair)
        if (candidatesSeenOutside.size != 1) return null
        val removable = candidatesSeenOutside.single()

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4,
            patternCells = pattern.cells,
            pair = pattern.pair,
            extraCauseCells = pattern.nonPureCells,
            eliminations = pattern.nonPureCells.map {
                CandidateElimination(CandidateRef(it, removable))
            },
        )
    }

    private fun checkType5(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.nonPureCells.size != 2 || !pattern.nonPureCells.areDiagonal()) return null
        val extras = pattern.nonPureCells.map { state.candidatesAt(it) - pattern.pair }
        if (extras.any { it.size != 1 } || extras.distinct().size != 1) return null
        val extra = extras.first().single()
        val targets = commonPeers(pattern.nonPureCells)
            .filter { state.valueAt(it) == 0 && state.hasCandidate(it, extra) }
        if (targets.isEmpty()) return null

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5,
            patternCells = pattern.cells,
            pair = pattern.pair,
            extraCauseCells = pattern.nonPureCells,
            eliminations = targets.map { CandidateElimination(CandidateRef(it, extra)) },
        )
    }

    private fun checkType5Plus(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.nonPureCells.size != 3) return null
        val extras = pattern.nonPureCells.map { state.candidatesAt(it) - pattern.pair }
        if (extras.any { it.size != 1 } || extras.distinct().size != 1) return null
        val extra = extras.first().single()
        val targets = commonPeers(pattern.nonPureCells)
            .filter { state.valueAt(it) == 0 && state.hasCandidate(it, extra) }
        if (targets.isEmpty()) return null

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
            patternCells = pattern.cells,
            pair = pattern.pair,
            extraCauseCells = pattern.nonPureCells,
            eliminations = targets.map { CandidateElimination(CandidateRef(it, extra)) },
        )
    }

    private fun checkType6(state: SolverState, pattern: UrPattern): SolveStep? {
        if (pattern.pureCells.size != 2 || !pattern.pureCells.areDiagonal()) return null
        val externalPeersOfPureCells = pattern.pureCells
            .flatMap { cell ->
                HouseRef(HouseType.ROW, cell.row).cells() +
                    HouseRef(HouseType.COLUMN, cell.col).cells()
            }
            .filter { it !in pattern.cells && state.valueAt(it) == 0 }
            .toSet()
        val presentDigits = pattern.pair.filter { digit ->
            externalPeersOfPureCells.any { state.hasCandidate(it, digit) }
        }
        if (presentDigits.size != 1) return null

        val nonRestricted = presentDigits.single()
        val restricted = pattern.pair.single { it != nonRestricted }
        val everyCornerHasExternalSupport = pattern.nonPureCells.all { cell ->
            val rowAndColumnPeers =
                HouseRef(HouseType.ROW, cell.row).cells() +
                    HouseRef(HouseType.COLUMN, cell.col).cells()
            rowAndColumnPeers.any { peer ->
                peer !in pattern.cells && state.valueAt(peer) == 0 &&
                    state.hasCandidate(peer, nonRestricted)
            }
        }
        if (!everyCornerHasExternalSupport) return null

        val eliminations = buildList {
            pattern.nonPureCells.forEach {
                add(CandidateElimination(CandidateRef(it, restricted)))
            }
            pattern.pureCells.forEach {
                add(CandidateElimination(CandidateRef(it, nonRestricted)))
            }
        }
        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_6,
            patternCells = pattern.cells,
            pair = pattern.pair,
            eliminations = eliminations,
        )
    }

    private fun checkType7(state: SolverState, pattern: UrPattern): SolveStep? {
        val validPureCells = when {
            pattern.pureCells.size == 1 -> pattern.pureCells
            pattern.pureCells.size == 2 && pattern.pureCells.areDiagonal() -> pattern.pureCells
            else -> return null
        }

        for (start in validPureCells) {
            val target = pattern.cells.single { it.row != start.row && it.col != start.col }
            val externalRowAndColumn = (
                HouseRef(HouseType.ROW, target.row).cells() +
                    HouseRef(HouseType.COLUMN, target.col).cells()
                )
                .filter { it !in pattern.cells && state.valueAt(it) == 0 }
            val presentDigits = pattern.pair.filter { digit ->
                externalRowAndColumn.any { state.hasCandidate(it, digit) }
            }
            if (presentDigits.size != 1) continue
            val removable = presentDigits.single()
            if (!state.hasCandidate(target, removable)) continue

            return urStep(
                state = state,
                technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_7,
                patternCells = pattern.cells,
                pair = pattern.pair,
                extraCauseCells = listOf(start, target),
                eliminations = listOf(
                    CandidateElimination(CandidateRef(target, removable)),
                ),
            )
        }
        return null
    }

    private fun checkType1Missing(
        state: SolverState,
        cells: List<CellRef>,
        pair: Set<Int>,
    ): SolveStep? {
        val pureCells = cells.filter { state.candidatesAt(it) == pair }
        if (pureCells.size != 3) return null
        val corner = cells.single { it !in pureCells }
        if (state.valueAt(corner) != 0) return null
        val cornerCandidates = state.candidatesAt(corner)
        val presentPairDigits = cornerCandidates.intersect(pair)
        if (presentPairDigits.size != 1 || (cornerCandidates - pair).isEmpty()) return null

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
            patternCells = cells,
            pair = pair,
            eliminations = listOf(
                CandidateElimination(CandidateRef(corner, presentPairDigits.single())),
            ),
        )
    }

    private fun checkType4Missing(
        state: SolverState,
        cells: List<CellRef>,
        pair: Set<Int>,
    ): SolveStep? {
        val pureCells = cells.filter { state.candidatesAt(it) == pair }
        if (pureCells.size != 2 || !pureCells.areOnSameSide()) return null
        val nonPureCells = cells.filter { it !in pureCells }
        if (!nonPureCells.areOnSameSide()) return null

        val fullCell = nonPureCells.singleOrNull { cell ->
            state.candidatesAt(cell).containsAll(pair) &&
                (state.candidatesAt(cell) - pair).isNotEmpty()
        } ?: return null
        val incompleteCell = nonPureCells.single { it != fullCell }
        val incompleteCandidates = state.candidatesAt(incompleteCell)
        val incompletePairDigits = incompleteCandidates.intersect(pair)
        if (incompletePairDigits.size != 1 || (incompleteCandidates - pair).isEmpty()) return null

        val candidateToKeep = incompletePairDigits.single()
        val candidateToRemove = pair.single { it != candidateToKeep }
        val commonPeers = commonPeers(nonPureCells).filter { state.valueAt(it) == 0 }
        if (commonPeers.any { state.hasCandidate(it, candidateToKeep) }) return null
        if (commonPeers.none { state.hasCandidate(it, candidateToRemove) }) return null

        return urStep(
            state = state,
            technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
            patternCells = cells,
            pair = pair,
            extraCauseCells = nonPureCells,
            eliminations = listOf(
                CandidateElimination(CandidateRef(fullCell, candidateToRemove)),
            ),
        )
    }

    private fun completePattern(
        state: SolverState,
        cells: List<CellRef>,
        pair: Set<Int>,
    ): UrPattern? {
        if (cells.any { state.valueAt(it) != 0 || !state.candidatesAt(it).containsAll(pair) }) {
            return null
        }
        val pureCells = cells.filter { state.candidatesAt(it) == pair }
        if (pureCells.isEmpty()) return null
        return UrPattern(
            pair = pair,
            cells = cells,
            pureCells = pureCells,
            nonPureCells = cells.filter { it !in pureCells },
        )
    }
}

private data class UrPattern(
    val pair: Set<Int>,
    val cells: List<CellRef>,
    val pureCells: List<CellRef>,
    val nonPureCells: List<CellRef>,
)

private fun rectangleCellSets(): Sequence<List<CellRef>> = sequence {
    val indices = (0 until Sudoku.GRID_SIZE).toList()
    for ((firstRow, secondRow) in indices.combinations(2)) {
        for ((firstColumn, secondColumn) in indices.combinations(2)) {
            val cells = listOf(
                CellRef(firstRow, firstColumn),
                CellRef(firstRow, secondColumn),
                CellRef(secondRow, firstColumn),
                CellRef(secondRow, secondColumn),
            )
            if (cells.map(CellRef::boxIndex).distinct().size == 2) yield(cells)
        }
    }
}

private fun List<CellRef>.areOnSameSide(): Boolean =
    size == 2 && (this[0].row == this[1].row || this[0].col == this[1].col)

private fun List<CellRef>.areDiagonal(): Boolean =
    size == 2 && this[0].row != this[1].row && this[0].col != this[1].col

private fun urStep(
    state: SolverState,
    technique: TechniqueId,
    patternCells: List<CellRef>,
    pair: Set<Int>,
    eliminations: List<CandidateElimination>,
    extraCauseCells: Collection<CellRef> = emptyList(),
    extraHouses: List<HouseRef> = emptyList(),
): SolveStep {
    val causeCells = (patternCells + extraCauseCells).toSet()
    val rectangleHouses = buildList {
        patternCells.map(CellRef::row).distinct().forEach { add(HouseRef(HouseType.ROW, it)) }
        patternCells.map(CellRef::col).distinct().forEach { add(HouseRef(HouseType.COLUMN, it)) }
        patternCells.map(CellRef::boxIndex).distinct().forEach { add(HouseRef(HouseType.BOX, it)) }
    }
    return SolveStep(
        technique = technique,
        eliminations = eliminations.distinct(),
        evidence = StepEvidence(
            causeCells = causeCells,
            causeCandidates = causeCells.flatMap { cell ->
                state.candidatesAt(cell)
                    .filter { it in pair || cell in extraCauseCells }
                    .map { CandidateRef(cell, it) }
            }.toSet(),
            focusDigits = pair,
            houses = (rectangleHouses + extraHouses).distinct(),
        ),
    )
}
