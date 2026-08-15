package com.galaxyrio.sudokusolver.domain.solver

class RemotePairDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.REMOTE_PAIR

    override fun find(state: SolverState): SolveStep? {
        val groups = state.biValueCells().groupBy(state::candidatesAt)
            .filterValues { it.size >= 4 }

        for ((digits, cells) in groups) {
            val graph = buildRemotePairGraph(cells)
            for (start in cells.sortedBy(CellRef::index)) {
                var explored = 0

                fun search(path: List<CellRef>): SolveStep? {
                    if (++explored > MAX_SEARCH_STATES) return null
                    val end = path.last()
                    if (path.size in 4..MAX_CHAIN_CELLS && path.size % 2 == 0) {
                        val targets = commonPeers(listOf(start, end))
                        .filterNot { it in path }
                        .filter { state.valueAt(it) == 0 }
                        .mapNotNull { cell ->
                            val removable = state.candidatesAt(cell).intersect(digits)
                            cell.takeIf { removable.isNotEmpty() }
                        }
                        if (targets.isNotEmpty()) {
                            val eliminations = targets.flatMap { cell ->
                                state.candidatesAt(cell).intersect(digits).map { digit ->
                                    CandidateElimination(CandidateRef(cell, digit))
                                }
                            }
                            return SolveStep(
                                technique = technique,
                                eliminations = eliminations,
                                evidence = StepEvidence(
                                    causeCells = path.toSet(),
                                    causeCandidates = path.flatMap { cell ->
                                        digits.map { digit -> CandidateRef(cell, digit) }
                                    }.toSet(),
                                    focusDigits = digits,
                                    houses = path.zipWithNext()
                                        .flatMap { (first, second) -> sharedHouses(first, second) }
                                        .distinct(),
                                    links = remotePairLinks(path, digits.sorted()),
                                ),
                            )
                        }
                    }

                    if (path.size >= MAX_CHAIN_CELLS) return null
                    for (next in graph[end].orEmpty()) {
                        if (next in path) continue
                        search(path + next)?.let { return it }
                    }
                    return null
                }

                search(listOf(start))?.let { return it }
            }
        }
        return null
    }

    private fun buildRemotePairGraph(cells: List<CellRef>): Map<CellRef, Set<CellRef>> {
        val graph = cells.associateWith { mutableSetOf<CellRef>() }
        for ((first, second) in cells.combinations(2)) {
            if (first.sees(second)) {
                graph.getValue(first).add(second)
                graph.getValue(second).add(first)
            }
        }
        return graph.mapValues { (_, neighbors) -> neighbors.sortedBy(CellRef::index).toSet() }
    }

    private fun remotePairLinks(path: List<CellRef>, digits: List<Int>): List<InferenceLink> =
        buildList {
            path.forEach { cell ->
                add(
                    InferenceLink(
                        CandidateRef(cell, digits[0]),
                        CandidateRef(cell, digits[1]),
                        InferenceLinkType.DUAL,
                    )
                )
            }
            path.zipWithNext().forEachIndexed { index, (first, second) ->
                val digit = digits[index % 2]
                add(
                    InferenceLink(
                        CandidateRef(first, digit),
                        CandidateRef(second, digit),
                        InferenceLinkType.WEAK,
                    )
                )
            }
        }

    private companion object {
        const val MAX_CHAIN_CELLS = 12
        const val MAX_SEARCH_STATES = 50_000
    }
}

class ChuteRemotePairDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.CHUTE_REMOTE_PAIR_SINGLE

    override fun find(state: SolverState): SolveStep? {
        val groups = state.biValueCells().groupBy(state::candidatesAt)
        for ((pairDigits, cells) in groups) {
            for ((first, second) in cells.combinations(2)) {
                if (first.sees(second)) continue

                val horizontal = first.boxIndex / 3 == second.boxIndex / 3
                val vertical = first.boxIndex % 3 == second.boxIndex % 3
                if (!horizontal && !vertical) continue

                val chuteBoxes = if (horizontal) {
                    val start = (first.boxIndex / 3) * 3
                    listOf(start, start + 1, start + 2)
                } else {
                    val start = first.boxIndex % 3
                    listOf(start, start + 3, start + 6)
                }
                val checkBox = chuteBoxes.single {
                    it != first.boxIndex && it != second.boxIndex
                }
                val checkCells = HouseRef(HouseType.BOX, checkBox).cells().filter { cell ->
                    if (horizontal) {
                        cell.row != first.row && cell.row != second.row
                    } else {
                        cell.col != first.col && cell.col != second.col
                    }
                }
                val checkValues = checkCells.flatMap { cell ->
                    state.valueAt(cell).takeIf { it != 0 }?.let(::listOf)
                        ?: state.candidatesAt(cell).toList()
                }.toSet()
                val presentPairDigits = checkValues.intersect(pairDigits)
                if (presentPairDigits.size > 1) continue

                val actionDigits = if (presentPairDigits.size == 1) {
                    presentPairDigits
                } else {
                    pairDigits
                }
                val focusCells = commonPeers(listOf(first, second))
                    .filterNot { it == first || it == second }
                val targetCandidates = mutableSetOf<CandidateRef>()
                focusCells.forEach { cell ->
                    actionDigits.filter { state.hasCandidate(cell, it) }
                        .forEach { digit -> targetCandidates += CandidateRef(cell, digit) }
                }

                var resultTechnique = if (presentPairDigits.size == 1) {
                    TechniqueId.CHUTE_REMOTE_PAIR_SINGLE
                } else {
                    TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE
                }

                if (presentPairDigits.isEmpty()) {
                    val endpointBoxes = setOf(first.boxIndex, second.boxIndex)
                    val bonusCells = endpointBoxes
                        .flatMap { HouseRef(HouseType.BOX, it).cells() }
                        .filter { cell ->
                            val sameLine = if (horizontal) {
                                cell.row == first.row || cell.row == second.row
                            } else {
                                cell.col == first.col || cell.col == second.col
                            }
                            sameLine && cell !in setOf(first, second) && cell !in focusCells
                        }
                        .distinct()
                    bonusCells.forEach { cell ->
                        pairDigits.filter { state.hasCandidate(cell, it) }
                            .forEach { digit -> targetCandidates += CandidateRef(cell, digit) }
                    }
                    if (bonusCells.any { cell -> pairDigits.any { state.hasCandidate(cell, it) } }) {
                        resultTechnique = TechniqueId.CHUTE_REMOTE_PAIR_BONUS
                    }
                }

                if (targetCandidates.isEmpty()) continue
                return SolveStep(
                    technique = resultTechnique,
                    eliminations = targetCandidates
                        .sortedWith(compareBy({ it.cell.index }, CandidateRef::digit))
                        .map(::CandidateElimination),
                    evidence = StepEvidence(
                        causeCells = setOf(first, second) + checkCells,
                        causeCandidates = setOf(first, second).flatMap { cell ->
                            pairDigits.map { digit -> CandidateRef(cell, digit) }
                        }.toSet(),
                        focusDigits = pairDigits,
                        houses = chuteBoxes.map { HouseRef(HouseType.BOX, it) },
                    ),
                )
            }
        }
        return null
    }
}
