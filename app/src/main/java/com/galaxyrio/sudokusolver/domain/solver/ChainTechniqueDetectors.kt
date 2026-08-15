package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class XYChainDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.XY_CHAIN

    override fun find(state: SolverState): SolveStep? {
        val biValueCells = state.biValueCells().sortedBy(CellRef::index)
        for (start in biValueCells) {
            for (targetDigit in state.candidatesAt(start).sorted()) {
                val outgoing = state.candidatesAt(start).single { it != targetDigit }
                val result = search(
                    state = state,
                    allCells = biValueCells,
                    targetDigit = targetDigit,
                    path = listOf(start),
                    outgoingDigit = outgoing,
                )
                if (result != null) return result
            }
        }
        return null
    }

    private fun search(
        state: SolverState,
        allCells: List<CellRef>,
        targetDigit: Int,
        path: List<CellRef>,
        outgoingDigit: Int,
    ): SolveStep? {
        if (path.size >= MAX_CHAIN_CELLS) return null
        val current = path.last()
        val nextCells = allCells.filter { next ->
            next !in path && current.sees(next) && state.hasCandidate(next, outgoingDigit)
        }

        for (next in nextCells) {
            val nextOutgoing = state.candidatesAt(next).single { it != outgoingDigit }
            val nextPath = path + next
            if (nextPath.size >= MIN_CHAIN_CELLS && nextOutgoing == targetDigit) {
                val targets = commonPeers(listOf(nextPath.first(), nextPath.last()))
                    .filterNot { it in nextPath }
                    .filter { state.valueAt(it) == 0 && state.hasCandidate(it, targetDigit) }
                if (targets.isNotEmpty()) {
                    val isLoop = nextPath.first().sees(nextPath.last())
                    return SolveStep(
                        technique = if (isLoop) TechniqueId.XY_CHAIN_LOOP else technique,
                        eliminations = targets.map { cell ->
                            CandidateElimination(CandidateRef(cell, targetDigit))
                        },
                        evidence = StepEvidence(
                            causeCells = nextPath.toSet(),
                            causeCandidates = nextPath.flatMap { cell ->
                                state.candidatesAt(cell).map { digit -> CandidateRef(cell, digit) }
                            }.toSet(),
                            focusDigits = nextPath.flatMap(state::candidatesAt).toSet(),
                            houses = nextPath.zipWithNext()
                                .flatMap { (first, second) -> sharedHouses(first, second) }
                                .distinct(),
                            links = xyChainLinks(state, nextPath, targetDigit),
                        ),
                    )
                }
            }

            val nested = search(state, allCells, targetDigit, nextPath, nextOutgoing)
            if (nested != null) return nested
        }
        return null
    }

    private fun xyChainLinks(
        state: SolverState,
        path: List<CellRef>,
        targetDigit: Int,
    ): List<InferenceLink> = buildList {
        var incoming = targetDigit
        path.forEachIndexed { index, cell ->
            val outgoing = state.candidatesAt(cell).single { it != incoming }
            add(
                InferenceLink(
                    CandidateRef(cell, incoming),
                    CandidateRef(cell, outgoing),
                    InferenceLinkType.DUAL,
                )
            )
            if (index < path.lastIndex) {
                add(
                    InferenceLink(
                        CandidateRef(cell, outgoing),
                        CandidateRef(path[index + 1], outgoing),
                        InferenceLinkType.WEAK,
                    )
                )
            }
            incoming = outgoing
        }
    }

    private companion object {
        const val MIN_CHAIN_CELLS = 4
        const val MAX_CHAIN_CELLS = 12
    }
}

class XChainDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.X_CHAIN

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            val candidateCells = state.candidateCells(digit).sortedBy(CellRef::index)
            val strongGraph = state.strongLinkGraph(digit)
            if (strongGraph.isEmpty()) continue

            for (start in candidateCells) {
                var explored = 0

                fun search(
                    path: List<CellRef>,
                    links: List<InferenceLinkType>,
                    expectStrong: Boolean,
                ): SolveStep? {
                    if (++explored > MAX_SEARCH_STATES || links.size >= MAX_LINKS) return null
                    val current = path.last()
                    val neighbors = if (expectStrong) {
                        strongGraph[current].orEmpty().sortedBy(CellRef::index)
                    } else {
                        candidateCells.filter(current::sees)
                    }

                    for (next in neighbors) {
                        if (expectStrong && next == start && links.size >= MIN_LINKS - 1) {
                            val closedLinks = links + InferenceLinkType.STRONG
                            return xChainPlacementStep(digit, path, closedLinks, start)
                        }
                        if (next in path) continue

                        val linkType = if (expectStrong) {
                            InferenceLinkType.STRONG
                        } else {
                            InferenceLinkType.WEAK
                        }
                        val nextPath = path + next
                        val nextLinks = links + linkType

                        if (expectStrong && nextLinks.size >= MIN_LINKS) {
                            val step = if (start.sees(next)) {
                                xChainLoopStep(state, digit, nextPath, nextLinks)
                            } else {
                                xChainOpenStep(state, digit, nextPath, nextLinks)
                            }
                            if (step != null) return step
                        }

                        val nested = search(nextPath, nextLinks, !expectStrong)
                        if (nested != null) return nested
                    }
                    return null
                }

                val result = search(
                    path = listOf(start),
                    links = emptyList(),
                    expectStrong = true,
                )
                if (result != null) return result
            }
        }
        return null
    }

    private fun xChainOpenStep(
        state: SolverState,
        digit: Int,
        path: List<CellRef>,
        linkTypes: List<InferenceLinkType>,
    ): SolveStep? {
        val targets = commonPeers(listOf(path.first(), path.last()))
            .filterNot { it in path }
            .filter { state.valueAt(it) == 0 && state.hasCandidate(it, digit) }
        if (targets.isEmpty()) return null
        return xChainEliminationStep(
            technique = TechniqueId.X_CHAIN,
            digit = digit,
            path = path,
            linkTypes = linkTypes,
            targets = targets,
        )
    }

    private fun xChainLoopStep(
        state: SolverState,
        digit: Int,
        path: List<CellRef>,
        linkTypes: List<InferenceLinkType>,
    ): SolveStep? {
        val targets = mutableSetOf<CellRef>()
        path.zipWithNext().forEachIndexed { index, (first, second) ->
            if (linkTypes[index] == InferenceLinkType.WEAK) {
                commonPeers(listOf(first, second))
                    .filterNotTo(targets) { it in path }
            }
        }
        commonPeers(listOf(path.first(), path.last()))
            .filterNotTo(targets) { it in path }
        val validTargets = targets.filter {
            state.valueAt(it) == 0 && state.hasCandidate(it, digit)
        }
        if (validTargets.isEmpty()) return null
        return xChainEliminationStep(
            technique = TechniqueId.X_CHAIN_LOOP,
            digit = digit,
            path = path,
            linkTypes = linkTypes,
            targets = validTargets,
        )
    }

    private fun xChainPlacementStep(
        digit: Int,
        path: List<CellRef>,
        linkTypes: List<InferenceLinkType>,
        target: CellRef,
    ): SolveStep = SolveStep(
        technique = TechniqueId.X_CHAIN_ONE_ENDPOINT,
        placements = listOf(Placement(target, digit)),
        evidence = chainEvidence(digit, path + target, linkTypes),
    )

    private fun xChainEliminationStep(
        technique: TechniqueId,
        digit: Int,
        path: List<CellRef>,
        linkTypes: List<InferenceLinkType>,
        targets: Collection<CellRef>,
    ): SolveStep = SolveStep(
        technique = technique,
        eliminations = targets.map { cell ->
            CandidateElimination(CandidateRef(cell, digit))
        },
        evidence = chainEvidence(digit, path, linkTypes),
    )

    private fun chainEvidence(
        digit: Int,
        path: List<CellRef>,
        linkTypes: List<InferenceLinkType>,
    ): StepEvidence = StepEvidence(
        causeCells = path.toSet(),
        causeCandidates = path.map { CandidateRef(it, digit) }.toSet(),
        focusDigits = setOf(digit),
        houses = path.zipWithNext()
            .flatMap { (first, second) -> sharedHouses(first, second) }
            .distinct(),
        links = path.zipWithNext().mapIndexed { index, (first, second) ->
            InferenceLink(
                CandidateRef(first, digit),
                CandidateRef(second, digit),
                linkTypes[index],
            )
        },
    )

    private companion object {
        const val MIN_LINKS = 5
        const val MAX_LINKS = 11
        const val MAX_SEARCH_STATES = 50_000
    }
}
