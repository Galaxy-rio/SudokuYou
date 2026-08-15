package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class EmptyRectangleDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.EMPTY_RECTANGLE

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            for (pairType in listOf(HouseType.ROW, HouseType.COLUMN)) {
                val pairHouses = (0 until Sudoku.GRID_SIZE)
                    .map { HouseRef(pairType, it) }
                    .filter { state.candidateCells(it, digit).size == 2 }

                for (pairHouse in pairHouses) {
                    val pair = state.candidateCells(pairHouse, digit)
                    if (pair[0].boxIndex == pair[1].boxIndex) continue

                    for ((anchor, otherEnd) in listOf(
                        pair[0] to pair[1],
                        pair[1] to pair[0],
                    )) {
                        val crossType = if (pairType == HouseType.ROW) {
                            HouseType.COLUMN
                        } else {
                            HouseType.ROW
                        }
                        val crossHouse = HouseRef(
                            crossType,
                            if (crossType == HouseType.ROW) anchor.row else anchor.col,
                        )
                        val possibleBoxes = state.candidateCells(crossHouse, digit)
                            .filter { it.boxIndex != anchor.boxIndex && it.boxIndex != otherEnd.boxIndex }
                            .groupBy(CellRef::boxIndex)

                        for ((boxIndex, crossArm) in possibleBoxes) {
                            val box = HouseRef(HouseType.BOX, boxIndex)
                            val boxCandidates = state.candidateCells(box, digit)
                            val otherArm = boxCandidates.filterNot { it in crossArm }
                            if (crossArm.isEmpty() || otherArm.isEmpty()) continue

                            val alignedOtherArm = if (pairType == HouseType.ROW) {
                                otherArm.map(CellRef::row).distinct().size == 1
                            } else {
                                otherArm.map(CellRef::col).distinct().size == 1
                            }
                            if (!alignedOtherArm) continue
                            if ((crossArm + otherArm).toSet() != boxCandidates.toSet()) continue

                            val target = if (pairType == HouseType.ROW) {
                                CellRef(otherArm.first().row, otherEnd.col)
                            } else {
                                CellRef(otherEnd.row, otherArm.first().col)
                            }
                            if (target in boxCandidates || target in pair) continue
                            if (state.valueAt(target) != 0 || !state.hasCandidate(target, digit)) {
                                continue
                            }

                            val causeCells = (pair + boxCandidates).toSet()
                            return SolveStep(
                                technique = technique,
                                eliminations = listOf(
                                    CandidateElimination(CandidateRef(target, digit))
                                ),
                                evidence = StepEvidence(
                                    causeCells = causeCells,
                                    causeCandidates = causeCells
                                        .map { CandidateRef(it, digit) }
                                        .toSet(),
                                    focusDigits = setOf(digit),
                                    houses = listOf(pairHouse, crossHouse, box) +
                                        if (pairType == HouseType.ROW) {
                                            listOf(HouseRef(HouseType.ROW, otherArm.first().row))
                                        } else {
                                            listOf(HouseRef(HouseType.COLUMN, otherArm.first().col))
                                        },
                                    links = listOf(
                                        InferenceLink(
                                            CandidateRef(anchor, digit),
                                            CandidateRef(otherEnd, digit),
                                            InferenceLinkType.STRONG,
                                        )
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}
