package com.galaxyrio.sudokusolver.domain.solver

class XYWingDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.XY_WING

    override fun find(state: SolverState): SolveStep? {
        val biValueCells = state.biValueCells()
        for (pivot in biValueCells) {
            val pivotDigits = state.candidatesAt(pivot)
            val possibleWings = biValueCells.filter { wing ->
                wing != pivot && pivot.sees(wing) &&
                    state.candidatesAt(wing).intersect(pivotDigits).size == 1
            }

            for ((wingA, wingB) in possibleWings.combinations(2)) {
                val wingADigits = state.candidatesAt(wingA)
                val wingBDigits = state.candidatesAt(wingB)
                val pivotA = wingADigits.intersect(pivotDigits).single()
                val pivotB = wingBDigits.intersect(pivotDigits).single()
                if (pivotA == pivotB) continue

                val outsideA = (wingADigits - pivotA).single()
                val outsideB = (wingBDigits - pivotB).single()
                if (outsideA != outsideB) continue
                val z = outsideA

                val targets = commonPeers(listOf(wingA, wingB))
                    .filter { cell ->
                        cell != pivot && state.valueAt(cell) == 0 && state.hasCandidate(cell, z)
                    }
                if (targets.isEmpty()) continue

                return SolveStep(
                    technique = technique,
                    eliminations = targets.map { cell ->
                        CandidateElimination(CandidateRef(cell, z))
                    },
                    evidence = StepEvidence(
                        causeCells = setOf(pivot, wingA, wingB),
                        causeCandidates = setOf(pivot, wingA, wingB)
                            .flatMap { cell ->
                                state.candidatesAt(cell).map { digit -> CandidateRef(cell, digit) }
                            }
                            .toSet(),
                        focusDigits = setOf(pivotA, pivotB, z),
                        links = listOf(
                            InferenceLink(
                                CandidateRef(wingA, z),
                                CandidateRef(wingA, pivotA),
                                InferenceLinkType.DUAL,
                            ),
                            InferenceLink(
                                CandidateRef(wingA, pivotA),
                                CandidateRef(pivot, pivotA),
                                InferenceLinkType.WEAK,
                            ),
                            InferenceLink(
                                CandidateRef(pivot, pivotA),
                                CandidateRef(pivot, pivotB),
                                InferenceLinkType.DUAL,
                            ),
                            InferenceLink(
                                CandidateRef(pivot, pivotB),
                                CandidateRef(wingB, pivotB),
                                InferenceLinkType.WEAK,
                            ),
                            InferenceLink(
                                CandidateRef(wingB, pivotB),
                                CandidateRef(wingB, z),
                                InferenceLinkType.DUAL,
                            ),
                        ),
                    ),
                )
            }
        }
        return null
    }
}

class XYZWingDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.XYZ_WING

    override fun find(state: SolverState): SolveStep? {
        val pivots = state.unsolvedCells()
            .filter { state.candidateMaskAt(it).countOneBits() == 3 }
        val biValueCells = state.biValueCells()

        for (pivot in pivots) {
            val pivotDigits = state.candidatesAt(pivot)
            val wings = biValueCells.filter { wing ->
                pivot.sees(wing) && pivotDigits.containsAll(state.candidatesAt(wing))
            }
            for ((wingA, wingB) in wings.combinations(2)) {
                val digitsA = state.candidatesAt(wingA)
                val digitsB = state.candidatesAt(wingB)
                if (digitsA.union(digitsB) != pivotDigits) continue
                val shared = digitsA.intersect(digitsB)
                if (shared.size != 1) continue
                val z = shared.single()

                val targets = commonPeers(listOf(pivot, wingA, wingB))
                    .filter { state.valueAt(it) == 0 && state.hasCandidate(it, z) }
                if (targets.isEmpty()) continue

                return wingEliminationStep(
                    technique = technique,
                    state = state,
                    patternCells = listOf(pivot, wingA, wingB),
                    targetCells = targets,
                    digit = z,
                )
            }
        }
        return null
    }
}

class WXYZWingDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.WXYZ_WING

    override fun find(state: SolverState): SolveStep? {
        val eligibleCells = state.unsolvedCells().filter { cell ->
            state.candidateMaskAt(cell).countOneBits() in 2..4
        }

        return findWithSingleCellPivot(state, eligibleCells)
            ?: findWithTwoCellPivot(state, eligibleCells)
    }

    /**
     * A general 4-Y-Wing has four cells using exactly four digits. The pivot and each of
     * the two wings may be a cell group, not just one cell. With one pivot cell, one wing
     * therefore contains two mutually visible cells and the other contains one cell.
     */
    private fun findWithSingleCellPivot(
        state: SolverState,
        eligibleCells: List<CellRef>,
    ): SolveStep? {
        for (pivot in eligibleCells) {
            val possibleWingCells = eligibleCells.filter(pivot::sees)
            for (wingCells in possibleWingCells.combinations(3)) {
                if (candidateUnionMask(state, listOf(pivot) + wingCells).countOneBits() != 4) {
                    continue
                }

                for (singleWingIndex in wingCells.indices) {
                    val singleCellWing = listOf(wingCells[singleWingIndex])
                    val twoCellWing = wingCells.filterIndexed { index, _ ->
                        index != singleWingIndex
                    }
                    if (!twoCellWing[0].sees(twoCellWing[1])) continue

                    createStep(
                        state = state,
                        pivotCells = listOf(pivot),
                        firstWingCells = twoCellWing,
                        secondWingCells = singleCellWing,
                    )?.let { return it }
                }
            }
        }
        return null
    }

    /** With two mutually visible pivot cells, each wing consists of one cell. */
    private fun findWithTwoCellPivot(
        state: SolverState,
        eligibleCells: List<CellRef>,
    ): SolveStep? {
        for (pivotCells in eligibleCells.combinations(2)) {
            if (!pivotCells[0].sees(pivotCells[1])) continue

            val possibleWingCells = eligibleCells.filter { wingCell ->
                wingCell !in pivotCells && pivotCells.all(wingCell::sees)
            }
            for (wingCells in possibleWingCells.combinations(2)) {
                if (candidateUnionMask(state, pivotCells + wingCells).countOneBits() != 4) {
                    continue
                }

                createStep(
                    state = state,
                    pivotCells = pivotCells,
                    firstWingCells = listOf(wingCells[0]),
                    secondWingCells = listOf(wingCells[1]),
                )?.let { return it }
            }
        }
        return null
    }

    private fun createStep(
        state: SolverState,
        pivotCells: List<CellRef>,
        firstWingCells: List<CellRef>,
        secondWingCells: List<CellRef>,
    ): SolveStep? {
        val firstWingMask = candidateUnionMask(state, firstWingCells)
        val secondWingMask = candidateUnionMask(state, secondWingCells)
        val sharedWingMask = firstWingMask and secondWingMask
        if (sharedWingMask.countOneBits() != 1) return null

        val eliminationDigit = sharedWingMask.digits().single()
        val wingCells = firstWingCells + secondWingCells
        val patternCells = pivotCells + wingCells
        val isUnrestricted = pivotCells.any { cell ->
            state.hasCandidate(cell, eliminationDigit)
        }
        val cellsThatMustSeeTarget = if (isUnrestricted) patternCells else wingCells
        val targets = commonPeers(cellsThatMustSeeTarget).filter { cell ->
            state.valueAt(cell) == 0 && state.hasCandidate(cell, eliminationDigit)
        }
        if (targets.isEmpty()) return null

        return wingEliminationStep(
            technique = technique,
            state = state,
            patternCells = patternCells,
            targetCells = targets,
            digit = eliminationDigit,
        )
    }
}

private fun candidateUnionMask(
    state: SolverState,
    cells: Collection<CellRef>,
): Int = cells.fold(0) { mask, cell -> mask or state.candidateMaskAt(cell) }

private fun wingEliminationStep(
    technique: TechniqueId,
    state: SolverState,
    patternCells: List<CellRef>,
    targetCells: Collection<CellRef>,
    digit: Int,
): SolveStep = SolveStep(
    technique = technique,
    eliminations = targetCells.map { cell ->
        CandidateElimination(CandidateRef(cell, digit))
    },
    evidence = StepEvidence(
        causeCells = patternCells.toSet(),
        causeCandidates = patternCells
            .flatMap { cell ->
                state.candidatesAt(cell).map { candidate -> CandidateRef(cell, candidate) }
            }
            .toSet(),
        focusDigits = patternCells.flatMap(state::candidatesAt).toSet(),
    ),
)
