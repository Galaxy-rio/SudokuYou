package com.galaxyrio.sudokusolver.domain.solver

/**
 * Sudoku Coach's n-Y-Wing definition, including multi-cell pivots and wings.
 * https://sudoku.coach/en/learn/wxyz-wing
 *
 * The pivot and either wing are mutually visible, so each such union belongs to
 * one house. Enumerating intersecting houses bounds the search to two sets of
 * nine cells; enumerating arbitrary n-cell combinations of the board does not.
 * These are the Coach wings, whose definition differs from ALS-XZ for n > 4.
 */
class NYWingDetector(private val size: Int) : TechniqueDetector {
    init {
        require(size in 4..9)
    }

    override val technique: TechniqueId = when (size) {
        4 -> TechniqueId.WXYZ_WING
        5 -> TechniqueId.FIVE_Y_WING
        6 -> TechniqueId.SIX_Y_WING
        7 -> TechniqueId.SEVEN_Y_WING
        8 -> TechniqueId.EIGHT_Y_WING
        else -> TechniqueId.NINE_Y_WING
    }

    override fun find(state: SolverState): SolveStep? {
        val eligible = state.unsolvedCells().filter {
            state.candidateMaskAt(it).countOneBits() in 2..size
        }.toSet()
        if (eligible.size < size) return null

        val houses = orderedHouses.map { house -> house.cells().filter { it in eligible } }
        for (firstIndex in houses.indices) {
            val firstHouse = houses[firstIndex]
            if (firstHouse.size < 2) continue
            for (secondIndex in firstIndex + 1 until houses.size) {
                val secondHouse = houses[secondIndex]
                if (secondHouse.size < 2) continue
                val intersection = firstHouse.intersect(secondHouse.toSet()).toList()
                if (intersection.isEmpty()) continue
                if ((firstHouse + secondHouse).distinct().size < size) continue

                // Distinct houses intersect in at most three cells.
                for (pivotSize in 1..minOf(intersection.size, size - 2)) {
                    for (pivots in intersection.combinations(pivotSize)) {
                        val pivotMask = unionMask(state, pivots)
                        val firstGroups = groups(state, firstHouse - pivots.toSet(), size - pivotSize - 1)
                        val secondGroups = groups(state, secondHouse - pivots.toSet(), size - pivotSize - 1)
                            .groupBy { it.digits }

                        for (firstWing in firstGroups) {
                            val secondSize = size - pivotSize - firstWing.cells.size
                            for ((secondMask, secondWings) in secondGroups) {
                                val shared = firstWing.digits and secondMask
                                if (shared.countOneBits() != 1) continue
                                if ((pivotMask or firstWing.digits or secondMask).countOneBits() != size) {
                                    continue
                                }
                                val digit = shared.countTrailingZeroBits() + 1
                                for (secondWing in secondWings) {
                                    if (secondWing.cells.size != secondSize) continue
                                    if (firstWing.cells.any { it in secondWing.cells }) continue
                                    val pattern = pivots + firstWing.cells + secondWing.cells
                                    val mustSee = if (pivotMask and shared != 0) pattern else {
                                        firstWing.cells + secondWing.cells
                                    }
                                    val targets = commonPeers(mustSee).filter {
                                        it !in pattern && state.valueAt(it) == 0 && state.hasCandidate(it, digit)
                                    }
                                    if (targets.isEmpty()) continue

                                    return SolveStep(
                                        technique = technique,
                                        eliminations = targets.map {
                                            CandidateElimination(CandidateRef(it, digit))
                                        },
                                        evidence = StepEvidence(
                                            causeCells = pattern.toSet(),
                                            causeCandidates = pattern.flatMap { cell ->
                                                state.candidatesAt(cell).map { CandidateRef(cell, it) }
                                            }.toSet(),
                                            focusDigits = (pivotMask or firstWing.digits or secondMask).digits(),
                                            houses = listOf(orderedHouses[firstIndex], orderedHouses[secondIndex]),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun groups(state: SolverState, cells: List<CellRef>, maximumSize: Int): List<WingGroup> {
        val result = mutableListOf<WingGroup>()
        val selected = mutableListOf<CellRef>()
        fun visit(start: Int, mask: Int) {
            if (selected.isNotEmpty()) result += WingGroup(selected.toList(), mask)
            if (selected.size == maximumSize) return
            for (index in start until cells.size) {
                val nextMask = mask or state.candidateMaskAt(cells[index])
                if (nextMask.countOneBits() > size) continue
                selected += cells[index]
                visit(index + 1, nextMask)
                selected.removeAt(selected.lastIndex)
            }
        }
        visit(0, 0)
        return result
    }

    private fun unionMask(state: SolverState, cells: List<CellRef>): Int =
        cells.fold(0) { mask, cell -> mask or state.candidateMaskAt(cell) }

    private data class WingGroup(val cells: List<CellRef>, val digits: Int)
}
