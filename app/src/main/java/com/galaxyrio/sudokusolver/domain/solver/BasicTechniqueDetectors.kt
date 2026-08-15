package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class LastDigitDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.LAST_DIGIT

    override fun find(state: SolverState): SolveStep? {
        for (house in orderedHouses) {
            val cells = house.cells()
            val emptyCells = cells.filter { state.valueAt(it) == 0 }
            if (emptyCells.size != 1) continue

            val solved = cells.map(state::valueAt).filter { it != 0 }.toSet()
            val missing = (1..Sudoku.GRID_SIZE).singleOrNull { it !in solved } ?: continue
            val target = emptyCells.single()
            if (!state.hasCandidate(target, missing)) continue

            return placementStep(
                technique = technique,
                cell = target,
                digit = missing,
                houses = listOf(house),
            )
        }
        return null
    }
}

class NakedSingleDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.NAKED_SINGLE

    override fun find(state: SolverState): SolveStep? {
        repeat(Sudoku.CELL_COUNT) { index ->
            val cell = CellRef.fromIndex(index)
            if (state.valueAt(cell) != 0) return@repeat

            val mask = state.candidateMaskAt(cell)
            if (mask.countOneBits() == 1) {
                return placementStep(
                    technique = technique,
                    cell = cell,
                    digit = mask.digits().single(),
                )
            }
        }
        return null
    }
}

class HiddenSingleDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.HIDDEN_SINGLE

    override fun find(state: SolverState): SolveStep? {
        for (house in orderedHouses) {
            val cells = house.cells()
            val solved = cells.map(state::valueAt).filter { it != 0 }.toSet()
            for (digit in 1..Sudoku.GRID_SIZE) {
                if (digit in solved) continue
                val possibleCells = cells.filter { cell ->
                    state.valueAt(cell) == 0 && state.hasCandidate(cell, digit)
                }
                if (possibleCells.size == 1) {
                    return placementStep(
                        technique = technique,
                        cell = possibleCells.single(),
                        digit = digit,
                        houses = listOf(house),
                    )
                }
            }
        }
        return null
    }
}

class NakedSubsetDetector(
    private val subsetSize: Int,
) : TechniqueDetector {
    init {
        require(subsetSize in 2..4)
    }

    override val technique: TechniqueId = when (subsetSize) {
        2 -> TechniqueId.NAKED_PAIR
        3 -> TechniqueId.NAKED_TRIPLE
        else -> TechniqueId.NAKED_QUAD
    }

    override fun find(state: SolverState): SolveStep? {
        for (house in orderedHouses) {
            val unsolvedCells = house.cells().filter { state.valueAt(it) == 0 }
            val possibleGroupCells = unsolvedCells.filter { cell ->
                state.candidateMaskAt(cell).countOneBits() in 1..subsetSize
            }
            if (possibleGroupCells.size < subsetSize) continue

            for (groupCells in possibleGroupCells.combinations(subsetSize)) {
                val groupMask = groupCells.fold(0) { mask, cell ->
                    mask or state.candidateMaskAt(cell)
                }
                if (groupMask.countOneBits() != subsetSize) continue

                val houseEliminations = buildList {
                    unsolvedCells
                        .filterNot { it in groupCells }
                        .forEach { cell ->
                            val removableMask = state.candidateMaskAt(cell) and groupMask
                            removableMask.digits().forEach { digit ->
                                add(CandidateElimination(CandidateRef(cell, digit)))
                            }
                        }
                }
                val extraHouse = if (subsetSize < 4) {
                    groupCells
                        .map(CellRef::houses)
                        .reduce { shared, houses -> shared.intersect(houses.toSet()).toList() }
                        .firstOrNull { it != house }
                } else {
                    null
                }
                val extraEliminations = extraHouse?.cells()
                    .orEmpty()
                    .filterNot { it in groupCells || it in unsolvedCells }
                    .filter { state.valueAt(it) == 0 }
                    .flatMap { cell ->
                        (state.candidateMaskAt(cell) and groupMask).digits().map { digit ->
                            CandidateElimination(CandidateRef(cell, digit))
                        }
                    }
                val eliminations = (houseEliminations + extraEliminations).distinct()
                if (eliminations.isEmpty()) continue

                val resultTechnique = when {
                    extraEliminations.isNotEmpty() && subsetSize == 2 -> TechniqueId.LOCKED_PAIR
                    extraEliminations.isNotEmpty() && subsetSize == 3 -> TechniqueId.LOCKED_TRIPLE
                    subsetSize == 2 -> TechniqueId.NAKED_PAIR
                    subsetSize == 3 -> TechniqueId.NAKED_TRIPLE
                    else -> TechniqueId.NAKED_QUAD
                }

                return SolveStep(
                    technique = resultTechnique,
                    eliminations = eliminations,
                    evidence = StepEvidence(
                        causeCells = groupCells.toSet(),
                        causeCandidates = groupCells
                            .flatMap { cell ->
                                groupMask.digits().map { digit -> CandidateRef(cell, digit) }
                            }
                            .filter { candidate -> state.hasCandidate(candidate.cell, candidate.digit) }
                            .toSet(),
                        focusDigits = groupMask.digits(),
                        houses = listOfNotNull(house, extraHouse.takeIf {
                            extraEliminations.isNotEmpty()
                        }),
                    ),
                )
            }
        }
        return null
    }
}

class HiddenSubsetDetector(
    private val subsetSize: Int,
) : TechniqueDetector {
    init {
        require(subsetSize in 2..4)
    }

    override val technique: TechniqueId = when (subsetSize) {
        2 -> TechniqueId.HIDDEN_PAIR
        3 -> TechniqueId.HIDDEN_TRIPLE
        else -> TechniqueId.HIDDEN_QUAD
    }

    override fun find(state: SolverState): SolveStep? {
        for (house in orderedHouses) {
            val unsolvedCells = house.cells().filter { state.valueAt(it) == 0 }
            val solvedDigits = house.cells().map(state::valueAt).filter { it != 0 }.toSet()
            val possibleDigits = (1..Sudoku.GRID_SIZE).filter { digit ->
                if (digit in solvedDigits) return@filter false
                val occurrenceCount = unsolvedCells.count { state.hasCandidate(it, digit) }
                occurrenceCount in 1..subsetSize
            }
            if (possibleDigits.size < subsetSize) continue

            for (groupDigits in possibleDigits.combinations(subsetSize)) {
                val groupMask = groupDigits.fold(0) { mask, digit -> mask or digitMask(digit) }
                val groupCells = unsolvedCells.filter { cell ->
                    state.candidateMaskAt(cell) and groupMask != 0
                }
                if (groupCells.size != subsetSize) continue

                val eliminations = buildList {
                    groupCells.forEach { cell ->
                        val removableMask = state.candidateMaskAt(cell) and groupMask.inv()
                        removableMask.digits().forEach { digit ->
                            add(CandidateElimination(CandidateRef(cell, digit)))
                        }
                    }
                }
                if (eliminations.isEmpty()) continue

                return SolveStep(
                    technique = technique,
                    eliminations = eliminations,
                    evidence = StepEvidence(
                        causeCells = groupCells.toSet(),
                        causeCandidates = groupCells
                            .flatMap { cell ->
                                groupDigits
                                    .filter { digit -> state.hasCandidate(cell, digit) }
                                    .map { digit -> CandidateRef(cell, digit) }
                            }
                            .toSet(),
                        focusDigits = groupDigits.toSet(),
                        houses = listOf(house),
                    ),
                )
            }
        }
        return null
    }
}

class PointingCandidatesDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.POINTING_PAIR

    override fun find(state: SolverState): SolveStep? {
        repeat(Sudoku.GRID_SIZE) { boxIndex ->
            val box = HouseRef(HouseType.BOX, boxIndex)
            val boxCells = box.cells()
            for (digit in 1..Sudoku.GRID_SIZE) {
                val causeCells = boxCells.filter { cell ->
                    state.valueAt(cell) == 0 && state.hasCandidate(cell, digit)
                }
                if (causeCells.size !in 2..3) continue

                val sharedRow = causeCells.map(CellRef::row).distinct().singleOrNull()
                val sharedColumn = causeCells.map(CellRef::col).distinct().singleOrNull()
                val targetHouse = when {
                    sharedRow != null -> HouseRef(HouseType.ROW, sharedRow)
                    sharedColumn != null -> HouseRef(HouseType.COLUMN, sharedColumn)
                    else -> null
                } ?: continue

                val eliminations = targetHouse.cells()
                    .filterNot { it in boxCells }
                    .filter { cell -> state.valueAt(cell) == 0 && state.hasCandidate(cell, digit) }
                    .map { cell -> CandidateElimination(CandidateRef(cell, digit)) }
                if (eliminations.isEmpty()) continue

                return SolveStep(
                    technique = if (causeCells.size == 2) {
                        TechniqueId.POINTING_PAIR
                    } else {
                        TechniqueId.POINTING_TRIPLE
                    },
                    eliminations = eliminations,
                    evidence = StepEvidence(
                        causeCells = causeCells.toSet(),
                        causeCandidates = causeCells
                            .map { cell -> CandidateRef(cell, digit) }
                            .toSet(),
                        focusDigits = setOf(digit),
                        houses = listOf(box, targetHouse),
                    ),
                )
            }
        }
        return null
    }
}

class ClaimingCandidatesDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.CLAIMING_PAIR

    override fun find(state: SolverState): SolveStep? {
        val lineHouses = orderedHouses.filter { it.type != HouseType.BOX }
        for (line in lineHouses) {
            val lineCells = line.cells()
            for (digit in 1..Sudoku.GRID_SIZE) {
                val causeCells = lineCells.filter { cell ->
                    state.valueAt(cell) == 0 && state.hasCandidate(cell, digit)
                }
                if (causeCells.size !in 2..3) continue

                val boxIndices = causeCells
                    .map { cell -> (cell.row / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE + cell.col / Sudoku.BOX_SIZE }
                    .distinct()
                if (boxIndices.size != 1) continue

                val box = HouseRef(HouseType.BOX, boxIndices.single())
                val eliminations = box.cells()
                    .filterNot { it in lineCells }
                    .filter { cell -> state.valueAt(cell) == 0 && state.hasCandidate(cell, digit) }
                    .map { cell -> CandidateElimination(CandidateRef(cell, digit)) }
                if (eliminations.isEmpty()) continue

                return SolveStep(
                    technique = if (causeCells.size == 2) {
                        TechniqueId.CLAIMING_PAIR
                    } else {
                        TechniqueId.CLAIMING_TRIPLE
                    },
                    eliminations = eliminations,
                    evidence = StepEvidence(
                        causeCells = causeCells.toSet(),
                        causeCandidates = causeCells
                            .map { cell -> CandidateRef(cell, digit) }
                            .toSet(),
                        focusDigits = setOf(digit),
                        houses = listOf(line, box),
                    ),
                )
            }
        }
        return null
    }
}

private fun placementStep(
    technique: TechniqueId,
    cell: CellRef,
    digit: Int,
    houses: List<HouseRef> = emptyList(),
): SolveStep = SolveStep(
    technique = technique,
    placements = listOf(Placement(cell, digit)),
    evidence = StepEvidence(
        causeCells = setOf(cell),
        causeCandidates = setOf(CandidateRef(cell, digit)),
        focusDigits = setOf(digit),
        houses = houses,
    ),
)
