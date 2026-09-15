package com.galaxyrio.sudokusolver.domain.solver

/**
 * Two non-seeing bivalue cells {x, y} cannot both be x if that would remove every x
 * from a house. Therefore at least one wing is y, eliminating y from their common
 * peers. Checking the whole house also covers the grouped form taught by sudoku.coach.
 */
class WWingDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.W_WING

    override fun find(state: SolverState): SolveStep? {
        val houseCandidates = (1..9).associateWith { digit ->
            orderedHouses.map { house -> house to state.candidateCells(house, digit) }
                .filter { (_, cells) -> cells.size >= 2 }
        }
        for (wings in state.biValueCells().groupBy(state::candidateMaskAt).values) {
            for ((firstWing, secondWing) in wings.combinations(2)) {
                if (firstWing.sees(secondWing)) continue
                val digits = state.candidatesAt(firstWing)
                val commonPeers = commonPeers(listOf(firstWing, secondWing))
                for (bridgeDigit in digits) {
                    val eliminationDigit = (digits - bridgeDigit).single()
                    val targets = commonPeers.filter { cell ->
                        state.valueAt(cell) == 0 && state.hasCandidate(cell, eliminationDigit)
                    }
                    if (targets.isEmpty()) continue

                    for ((house, bridgeCells) in houseCandidates.getValue(bridgeDigit)) {
                        // A wing inside the house is not eliminated by its own placement.
                        if (firstWing in bridgeCells || secondWing in bridgeCells) continue
                        if (!bridgeCells.all { firstWing.sees(it) || secondWing.sees(it) }) {
                            continue
                        }
                        // Both wings must contribute; otherwise this is a simpler locked candidate.
                        if (bridgeCells.all(firstWing::sees) || bridgeCells.all(secondWing::sees)) {
                            continue
                        }

                        val wingCells = setOf(firstWing, secondWing)
                        val links = if (bridgeCells.size == 2) {
                            val firstBridge = bridgeCells.single(firstWing::sees)
                            val secondBridge = bridgeCells.single(secondWing::sees)
                            listOf(
                                InferenceLink(
                                    CandidateRef(firstWing, eliminationDigit),
                                    CandidateRef(firstWing, bridgeDigit),
                                    InferenceLinkType.DUAL,
                                ),
                                InferenceLink(
                                    CandidateRef(firstWing, bridgeDigit),
                                    CandidateRef(firstBridge, bridgeDigit),
                                    InferenceLinkType.WEAK,
                                ),
                                InferenceLink(
                                    CandidateRef(firstBridge, bridgeDigit),
                                    CandidateRef(secondBridge, bridgeDigit),
                                    InferenceLinkType.STRONG,
                                ),
                                InferenceLink(
                                    CandidateRef(secondBridge, bridgeDigit),
                                    CandidateRef(secondWing, bridgeDigit),
                                    InferenceLinkType.WEAK,
                                ),
                                InferenceLink(
                                    CandidateRef(secondWing, bridgeDigit),
                                    CandidateRef(secondWing, eliminationDigit),
                                    InferenceLinkType.DUAL,
                                ),
                            )
                        } else {
                            // A grouped bridge is not a strong link between arbitrary single cells.
                            emptyList()
                        }
                        return SolveStep(
                            technique = technique,
                            eliminations = targets.map { cell ->
                                CandidateElimination(CandidateRef(cell, eliminationDigit))
                            },
                            evidence = StepEvidence(
                                causeCells = wingCells + bridgeCells,
                                causeCandidates = wingCells.flatMap { wing ->
                                    digits.map { digit -> CandidateRef(wing, digit) }
                                }.toSet() + bridgeCells.map { CandidateRef(it, bridgeDigit) },
                                focusDigits = digits,
                                houses = listOf(house),
                                links = links,
                                wingCells = wingCells,
                            ),
                        )
                    }
                }
            }
        }
        return null
    }
}
