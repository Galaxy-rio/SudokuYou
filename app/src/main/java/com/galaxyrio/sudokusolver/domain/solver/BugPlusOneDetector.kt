package com.galaxyrio.sudokusolver.domain.solver

class BugPlusOneDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.BUG_PLUS_ONE

    override fun find(state: SolverState): SolveStep? {
        val unsolved = state.unsolvedCells()
        val triValueCells = unsolved.filter {
            state.candidateMaskAt(it).countOneBits() == 3
        }
        if (triValueCells.size != 1) return null
        if (unsolved.any { cell ->
                val count = state.candidateMaskAt(cell).countOneBits()
                count !in 2..3
            }
        ) {
            return null
        }

        val target = triValueCells.single()
        val targetHouses = target.houses()
        val bugDigits = state.candidatesAt(target).filter { digit ->
            targetHouses.all { house -> state.candidateCells(house, digit).size == 3 }
        }
        if (bugDigits.size != 1) return null
        val digit = bugDigits.single()

        val parityIsValid = orderedHouses.all { house ->
            for (candidate in 1..9) {
                val count = state.candidateCells(house, candidate).size
                val expectedOdd = house in targetHouses && candidate == digit
                if (expectedOdd) {
                    if (count != 3) return@all false
                } else if (count != 0 && count != 2) {
                    return@all false
                }
            }
            true
        }
        if (!parityIsValid) return null

        return SolveStep(
            technique = technique,
            placements = listOf(Placement(target, digit)),
            evidence = StepEvidence(
                causeCells = unsolved.toSet(),
                causeCandidates = unsolved.flatMap { cell ->
                    state.candidatesAt(cell).map { candidate -> CandidateRef(cell, candidate) }
                }.toSet(),
                focusDigits = setOf(digit),
                houses = targetHouses,
            ),
        )
    }
}
