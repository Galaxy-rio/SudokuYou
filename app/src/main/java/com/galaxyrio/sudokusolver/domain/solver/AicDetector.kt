package com.galaxyrio.sudokusolver.domain.solver

/**
 * Finds ordinary, single-candidate Alternating Inference Chains.
 *
 * Strong links come from bivalue cells and bilocal candidates in a house. Weak links come from
 * candidates that cannot both be true: different candidates in one cell or equal candidates in
 * peer cells. Grouped nodes, ALS links, uniqueness links, forcing chains, and nets are deliberately
 * outside this detector's scope.
 */
class AicDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.AIC

    override fun find(state: SolverState): SolveStep? {
        val nodes = state.unsolvedCells()
            .flatMap { cell ->
                state.candidatesAt(cell).map { digit -> CandidateRef(cell, digit) }
            }
            .sortedWith(candidateComparator)
        if (nodes.size < MIN_NODES) return null

        val weakGraph = nodes.associateWith { node ->
            nodes.asSequence()
                .filter { other -> node != other && node.isWeaklyLinkedTo(other) }
                .sortedWith(candidateComparator)
                .toList()
        }
        val strongGraph = buildStrongGraph(state, nodes)
        var exploredStates = 0

        fun search(
            path: List<CandidateRef>,
            linkTypes: List<InferenceLinkType>,
            expectStrong: Boolean,
        ): SolveStep? {
            if (++exploredStates > MAX_SEARCH_STATES || linkTypes.size >= MAX_LINKS) return null
            val current = path.last()
            val neighbors = if (expectStrong) {
                strongGraph[current].orEmpty()
            } else {
                weakGraph[current].orEmpty().filter { next ->
                    strongGraph[next].orEmpty().any { strongNeighbor -> strongNeighbor !in path }
                }
            }

            for (next in neighbors) {
                if (next in path) continue
                val linkType = if (expectStrong) {
                    InferenceLinkType.STRONG
                } else {
                    InferenceLinkType.WEAK
                }
                val nextPath = path + next
                val nextLinkTypes = linkTypes + linkType

                if (expectStrong && nextLinkTypes.size >= MIN_LINKS) {
                    val targets = weakGraph[nextPath.first()].orEmpty()
                        .asSequence()
                        .filter { it in weakGraph[next].orEmpty() }
                        .filterNot { it in nextPath }
                        .sortedWith(candidateComparator)
                        .toList()
                    if (targets.isNotEmpty()) {
                        return aicStep(nextPath, nextLinkTypes, targets)
                    }
                }

                val nested = search(nextPath, nextLinkTypes, !expectStrong)
                if (nested != null) return nested
            }
            return null
        }

        for (start in nodes) {
            if (strongGraph[start].isNullOrEmpty()) continue
            val result = search(
                path = listOf(start),
                linkTypes = emptyList(),
                expectStrong = true,
            )
            if (result != null) return result
            if (exploredStates > MAX_SEARCH_STATES) return null
        }
        return null
    }

    private fun buildStrongGraph(
        state: SolverState,
        nodes: List<CandidateRef>,
    ): Map<CandidateRef, List<CandidateRef>> {
        val graph = nodes.associateWith { mutableSetOf<CandidateRef>() }

        state.biValueCells().forEach { cell ->
            val candidates = state.candidatesAt(cell).sorted()
            graph.link(
                CandidateRef(cell, candidates[0]),
                CandidateRef(cell, candidates[1]),
            )
        }
        orderedHouses.forEach { house ->
            for (digit in 1..9) {
                val cells = state.candidateCells(house, digit)
                if (cells.size == 2) {
                    graph.link(
                        CandidateRef(cells[0], digit),
                        CandidateRef(cells[1], digit),
                    )
                }
            }
        }

        return graph.mapValues { (_, neighbors) -> neighbors.sortedWith(candidateComparator) }
    }

    private fun aicStep(
        path: List<CandidateRef>,
        linkTypes: List<InferenceLinkType>,
        targets: List<CandidateRef>,
    ): SolveStep = SolveStep(
        technique = technique,
        eliminations = targets.map { candidate -> CandidateElimination(candidate) },
        evidence = StepEvidence(
            causeCells = path.map(CandidateRef::cell).toSet(),
            causeCandidates = path.toSet(),
            focusDigits = path.map(CandidateRef::digit).toSet(),
            houses = path.zipWithNext()
                .flatMap { (first, second) ->
                    if (first.digit == second.digit) {
                        sharedHouses(first.cell, second.cell)
                    } else {
                        emptyList()
                    }
                }
                .distinct(),
            links = path.zipWithNext().mapIndexed { index, (first, second) ->
                InferenceLink(first, second, linkTypes[index])
            },
        ),
    )

    private fun CandidateRef.isWeaklyLinkedTo(other: CandidateRef): Boolean = when {
        cell == other.cell -> digit != other.digit
        digit == other.digit -> cell.sees(other.cell)
        else -> false
    }

    private fun Map<CandidateRef, MutableSet<CandidateRef>>.link(
        first: CandidateRef,
        second: CandidateRef,
    ) {
        getValue(first).add(second)
        getValue(second).add(first)
    }

    private companion object {
        const val MIN_NODES = 4
        const val MIN_LINKS = 3
        const val MAX_LINKS = 11
        const val MAX_SEARCH_STATES = 150_000

        val candidateComparator = compareBy<CandidateRef>({ it.cell.index }, CandidateRef::digit)
    }
}
