package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class SkyscraperDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.SKYSCRAPER

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            for (lineType in listOf(HouseType.ROW, HouseType.COLUMN)) {
                val links = state.strongLinks(digit).filter { it.house.type == lineType }
                for ((first, second) in links.combinations(2)) {
                    val firstCells = setOf(first.first, first.second)
                    val secondCells = setOf(second.first, second.second)
                    val alignedPairs = buildList {
                        for (firstBase in firstCells) {
                            for (secondBase in secondCells) {
                                val aligned = if (lineType == HouseType.ROW) {
                                    firstBase.col == secondBase.col
                                } else {
                                    firstBase.row == secondBase.row
                                }
                                if (aligned) add(firstBase to secondBase)
                            }
                        }
                    }
                    if (alignedPairs.size != 1) continue

                    val (baseA, baseB) = alignedPairs.single()
                    val roofA = firstCells.single { it != baseA }
                    val roofB = secondCells.single { it != baseB }
                    if (roofA.sees(roofB)) continue
                    val targets = commonPeers(listOf(roofA, roofB))
                        .filter { state.valueAt(it) == 0 && state.hasCandidate(it, digit) }
                    if (targets.isEmpty()) continue

                    return turbotStep(
                        technique = technique,
                        digit = digit,
                        chain = listOf(roofA, baseA, baseB, roofB),
                        targets = targets,
                        houses = listOf(first.house, second.house) +
                            sharedHouses(baseA, baseB),
                    )
                }
            }
        }
        return null
    }
}

class TwoStringKiteDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.TWO_STRING_KITE

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            val rowLinks = state.strongLinks(digit).filter { it.house.type == HouseType.ROW }
            val columnLinks = state.strongLinks(digit).filter { it.house.type == HouseType.COLUMN }

            for (rowLink in rowLinks) {
                val rowCells = listOf(rowLink.first, rowLink.second)
                if (rowCells[0].boxIndex == rowCells[1].boxIndex) continue
                for (columnLink in columnLinks) {
                    val columnCells = listOf(columnLink.first, columnLink.second)
                    if (columnCells[0].boxIndex == columnCells[1].boxIndex) continue
                    if (rowCells.any { it in columnCells }) continue

                    for (rowBase in rowCells) {
                        for (columnBase in columnCells) {
                            if (rowBase.boxIndex != columnBase.boxIndex) continue
                            val rowEnd = rowCells.single { it != rowBase }
                            val columnEnd = columnCells.single { it != columnBase }
                            val targets = commonPeers(listOf(rowEnd, columnEnd))
                                .filter { state.valueAt(it) == 0 && state.hasCandidate(it, digit) }
                            if (targets.isEmpty()) continue

                            return turbotStep(
                                technique = technique,
                                digit = digit,
                                chain = listOf(rowEnd, rowBase, columnBase, columnEnd),
                                targets = targets,
                                houses = listOf(
                                    rowLink.house,
                                    HouseRef(HouseType.BOX, rowBase.boxIndex),
                                    columnLink.house,
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

class TurbotCraneDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.TURBOT_CRANE

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            val boxLinks = state.strongLinks(digit).filter { link ->
                link.house.type == HouseType.BOX &&
                    link.first.row != link.second.row &&
                    link.first.col != link.second.col
            }
            for (boxLink in boxLinks) {
                for ((endpoint, boxExit) in listOf(
                    boxLink.first to boxLink.second,
                    boxLink.second to boxLink.first,
                )) {
                    for (weakHouseType in listOf(HouseType.ROW, HouseType.COLUMN)) {
                        val weakHouse = HouseRef(
                            weakHouseType,
                            if (weakHouseType == HouseType.ROW) boxExit.row else boxExit.col,
                        )
                        val possibleMiddle = state.candidateCells(weakHouse, digit)
                            .filter { it != boxExit && it.boxIndex != boxExit.boxIndex }
                        for (middle in possibleMiddle) {
                            val strongHouseType = if (weakHouseType == HouseType.ROW) {
                                HouseType.COLUMN
                            } else {
                                HouseType.ROW
                            }
                            val strongHouse = HouseRef(
                                strongHouseType,
                                if (strongHouseType == HouseType.ROW) middle.row else middle.col,
                            )
                            val strongCells = state.candidateCells(strongHouse, digit)
                            if (strongCells.size != 2 || middle !in strongCells) continue
                            val otherEndpoint = strongCells.single { it != middle }
                            val chain = listOf(endpoint, boxExit, middle, otherEndpoint)
                            if (chain.toSet().size != chain.size) continue
                            val targets = commonPeers(listOf(endpoint, otherEndpoint))
                                .filterNot { it in chain }
                                .filter { state.valueAt(it) == 0 && state.hasCandidate(it, digit) }
                            if (targets.isEmpty()) continue

                            return turbotStep(
                                technique = technique,
                                digit = digit,
                                chain = chain,
                                targets = targets,
                                houses = listOf(boxLink.house, weakHouse, strongHouse),
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}

private fun turbotStep(
    technique: TechniqueId,
    digit: Int,
    chain: List<CellRef>,
    targets: Collection<CellRef>,
    houses: List<HouseRef>,
): SolveStep {
    val links = chain.zipWithNext().mapIndexed { index, (from, to) ->
        InferenceLink(
            from = CandidateRef(from, digit),
            to = CandidateRef(to, digit),
            type = if (index % 2 == 0) {
                InferenceLinkType.STRONG
            } else {
                InferenceLinkType.WEAK
            },
        )
    }
    return SolveStep(
        technique = technique,
        eliminations = targets.map { cell ->
            CandidateElimination(CandidateRef(cell, digit))
        },
        evidence = StepEvidence(
            causeCells = chain.toSet(),
            causeCandidates = chain.map { CandidateRef(it, digit) }.toSet(),
            focusDigits = setOf(digit),
            houses = houses.distinct(),
            links = links,
        ),
    )
}
