package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

/**
 * An n-fish has n base houses and n cover houses. All additional candidates in its
 * base houses are fins and must share one box. A target must lie outside the bases,
 * in a cover house, and see every fin: if a fin is true it eliminates the target;
 * otherwise n placements in n cover houses eliminate the target. Missing body
 * corners (sashimi) and fins in multiple base houses use exactly the same proof.
 */
class FinnedFishDetector(private val size: Int) : TechniqueDetector {
    init {
        require(size in 2..4)
    }

    override val technique: TechniqueId = when (size) {
        2 -> TechniqueId.FINNED_X_WING
        3 -> TechniqueId.FINNED_SWORDFISH
        else -> TechniqueId.FINNED_JELLYFISH
    }

    override fun find(state: SolverState): SolveStep? {
        for (baseType in listOf(HouseType.ROW, HouseType.COLUMN)) {
            val coverType = if (baseType == HouseType.ROW) HouseType.COLUMN else HouseType.ROW
            fun cell(base: Int, cover: Int): CellRef =
                if (baseType == HouseType.ROW) CellRef(base, cover) else CellRef(cover, base)

            for (digit in 1..Sudoku.GRID_SIZE) {
                val masks = IntArray(Sudoku.GRID_SIZE) { base ->
                    (0 until Sudoku.GRID_SIZE).fold(0) { mask, cover ->
                        val cell = cell(base, cover)
                        if (state.valueAt(cell) == 0 && state.hasCandidate(cell, digit)) {
                            mask or (1 shl cover)
                        } else {
                            mask
                        }
                    }
                }
                // With a target in the fin box, at most two additional cover lines can be fins.
                val eligibleBases = masks.indices.filter { masks[it].countOneBits() in 2..(size + 2) }
                for (bases in eligibleBases.combinations(size)) {
                    val union = bases.fold(0) { mask, base -> mask or masks[base] }
                    if (union.countOneBits() !in (size + 1)..(size + 2)) continue
                    val possibleCovers = masks.indices.filter { union and (1 shl it) != 0 }
                    for (covers in possibleCovers.combinations(size)) {
                        val coverMask = covers.fold(0) { mask, cover -> mask or (1 shl cover) }
                        if (bases.any { masks[it] and coverMask == 0 }) continue

                        val fins = bases.flatMap { base ->
                            possibleCovers.filter { cover ->
                                masks[base] and (1 shl cover) != 0 && coverMask and (1 shl cover) == 0
                            }.map { cover -> cell(base, cover) }
                        }
                        val finBox = fins.first().boxIndex
                        if (fins.any { it.boxIndex != finBox }) continue

                        val targets = HouseRef(HouseType.BOX, finBox).cells().filter { target ->
                            val base = if (baseType == HouseType.ROW) target.row else target.col
                            val cover = if (baseType == HouseType.ROW) target.col else target.row
                            base !in bases && cover in covers && state.valueAt(target) == 0 &&
                                state.hasCandidate(target, digit) && fins.all(target::sees)
                        }
                        if (targets.isEmpty()) continue

                        val baseHouses = bases.map { HouseRef(baseType, it) }
                        val coverHouses = covers.map { HouseRef(coverType, it) }
                        val causeCells = baseHouses.flatMap { state.candidateCells(it, digit) }.toSet()
                        return SolveStep(
                            technique = technique,
                            eliminations = targets.map { target ->
                                CandidateElimination(CandidateRef(target, digit))
                            },
                            evidence = StepEvidence(
                                causeCells = causeCells,
                                causeCandidates = causeCells.map { CandidateRef(it, digit) }.toSet(),
                                focusDigits = setOf(digit),
                                houses = baseHouses + coverHouses + HouseRef(HouseType.BOX, finBox),
                                baseHouses = baseHouses,
                                coverHouses = coverHouses,
                                finCandidates = fins.map { CandidateRef(it, digit) }.toSet(),
                            ),
                        )
                    }
                }
            }
        }
        return null
    }
}
