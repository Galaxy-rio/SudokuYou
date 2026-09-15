package com.galaxyrio.sudokusolver.domain.solver

/**
 * Two-sector disjoint subsets, in the Type 1 / Type 2 forms used by OpenSudoku.
 * Two disjoint bivalue wings see every cell in a line-box intersection subset.
 * All pattern cells must take different digits: the only non-seeing cells are
 * the wings, whose candidates are disjoint. The line wing's pair is therefore
 * locked to the line, the box wing's pair to the box, and the Type 2 extra digit
 * to their intersection.
 */
class SueDeCoqDetector(private val subsetSize: Int) : TechniqueDetector {
    init {
        require(subsetSize in 2..3)
    }

    override val technique: TechniqueId = if (subsetSize == 2) {
        TechniqueId.SUE_DE_COQ_TYPE_1
    } else {
        TechniqueId.SUE_DE_COQ_TYPE_2
    }

    override fun find(state: SolverState): SolveStep? {
        val bivalueCells = state.biValueCells()
        for (lineType in listOf(HouseType.ROW, HouseType.COLUMN)) {
            for (lineIndex in 0..8) {
                val line = HouseRef(lineType, lineIndex)
                val lineCells = line.cells().toSet()
                val lineWings = bivalueCells.filter { it in lineCells }
                if (lineWings.isEmpty()) continue
                for (boxIndex in lineCells.map(CellRef::boxIndex).distinct()) {
                    val box = HouseRef(HouseType.BOX, boxIndex)
                    val intersection = lineCells.filter { cell ->
                        cell.boxIndex == boxIndex && state.valueAt(cell) == 0 &&
                            state.candidateMaskAt(cell).countOneBits() in 2..(subsetSize + 2)
                    }
                    if (intersection.size < subsetSize) continue
                    val boxWings = bivalueCells.filter { it.boxIndex == boxIndex && it !in lineCells }
                    for (lineWing in lineWings.filter { it.boxIndex != boxIndex }) {
                        val lineMask = state.candidateMaskAt(lineWing)
                        for (boxWing in boxWings) {
                            val boxMask = state.candidateMaskAt(boxWing)
                            if (lineMask and boxMask != 0) continue
                            val wingMask = lineMask or boxMask
                            for (subset in intersection.combinations(subsetSize)) {
                                val subsetMask = subset.fold(0) { mask, cell ->
                                    mask or state.candidateMaskAt(cell)
                                }
                                if (subsetMask.countOneBits() != subsetSize + 2) continue
                                if (subsetMask and wingMask != wingMask) continue
                                val extraMask = subsetMask and wingMask.inv()
                                val patternCells = (subset + lineWing + boxWing).toSet()
                                val eliminations = linkedSetOf<CandidateRef>()
                                for ((house, mask) in listOf(line to (lineMask or extraMask), box to (boxMask or extraMask))) {
                                    for (cell in house.cells()) {
                                        if (cell in patternCells || state.valueAt(cell) != 0) continue
                                        (state.candidateMaskAt(cell) and mask).digits().forEach { digit ->
                                            eliminations += CandidateRef(cell, digit)
                                        }
                                    }
                                }
                                if (eliminations.isEmpty()) continue
                                return SolveStep(
                                    technique = technique,
                                    eliminations = eliminations.map(::CandidateElimination),
                                    evidence = StepEvidence(
                                        causeCells = patternCells,
                                        causeCandidates = patternCells.flatMap { cell ->
                                            state.candidatesAt(cell).map { CandidateRef(cell, it) }
                                        }.toSet(),
                                        focusDigits = subsetMask.digits(),
                                        houses = listOf(line, box),
                                        wingCells = setOf(lineWing, boxWing),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
