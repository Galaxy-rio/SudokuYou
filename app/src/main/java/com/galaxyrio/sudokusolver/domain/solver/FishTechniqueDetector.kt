package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class BasicFishDetector(
    private val size: Int,
) : TechniqueDetector {
    init {
        require(size in 2..7)
    }

    override val technique: TechniqueId = when (size) {
        2 -> TechniqueId.X_WING
        3 -> TechniqueId.SWORDFISH
        4 -> TechniqueId.JELLYFISH
        5 -> TechniqueId.STARFISH
        6 -> TechniqueId.WHALE
        else -> TechniqueId.LEVIATHAN
    }

    override fun find(state: SolverState): SolveStep? {
        for (baseType in listOf(HouseType.ROW, HouseType.COLUMN)) {
            val coverType = if (baseType == HouseType.ROW) {
                HouseType.COLUMN
            } else {
                HouseType.ROW
            }

            for (digit in 1..Sudoku.GRID_SIZE) {
                val possibleBaseHouses = (0 until Sudoku.GRID_SIZE)
                    .map { HouseRef(baseType, it) }
                    .filter { house -> state.candidateCells(house, digit).size in 2..size }
                if (possibleBaseHouses.size < size) continue

                for (baseHouses in possibleBaseHouses.combinations(size)) {
                    val causeCells = baseHouses.flatMap { state.candidateCells(it, digit) }
                    val coverIndices = causeCells
                        .map { cell ->
                            if (coverType == HouseType.COLUMN) cell.col else cell.row
                        }
                        .distinct()
                        .sorted()
                    if (coverIndices.size != size) continue

                    val hasDegenerateCover = coverIndices.any { coverIndex ->
                        causeCells.count { cell ->
                            if (coverType == HouseType.COLUMN) {
                                cell.col == coverIndex
                            } else {
                                cell.row == coverIndex
                            }
                        } < 2
                    }
                    if (hasDegenerateCover) continue

                    val baseIndices = baseHouses.map(HouseRef::index).toSet()
                    val coverHouses = coverIndices.map { HouseRef(coverType, it) }
                    val targets = coverHouses
                        .flatMap { state.candidateCells(it, digit) }
                        .filter { cell ->
                            val baseIndex = if (baseType == HouseType.ROW) cell.row else cell.col
                            baseIndex !in baseIndices
                        }
                        .distinct()
                    if (targets.isEmpty()) continue

                    return SolveStep(
                        technique = technique,
                        eliminations = targets.map { cell ->
                            CandidateElimination(CandidateRef(cell, digit))
                        },
                        evidence = StepEvidence(
                            causeCells = causeCells.toSet(),
                            causeCandidates = causeCells
                                .map { cell -> CandidateRef(cell, digit) }
                                .toSet(),
                            focusDigits = setOf(digit),
                            houses = baseHouses + coverHouses,
                            baseHouses = baseHouses,
                            coverHouses = coverHouses,
                        ),
                    )
                }
            }
        }
        return null
    }
}
