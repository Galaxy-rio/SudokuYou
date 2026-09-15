package com.galaxyrio.sudokusolver.domain.solver

/** X-chains whose nodes may be a group of candidates in a box/line intersection. */
class GroupedXChainDetector : TechniqueDetector {
    override val technique = TechniqueId.GROUPED_X_CHAIN

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..9) {
            val candidates = state.candidateCells(digit)
            val houseCandidates = orderedHouses.map { state.candidateCells(it, digit).toSet() }
                .filter { it.size in 2..6 }
            // A group contains at most three candidates, so two groups cannot exhaust
            // a house with more than six. Dense boards have no strong links to follow.
            if (houseCandidates.isEmpty()) continue
            val nodeSets = candidates.map { setOf(it) }.toMutableSet()
            for (box in orderedHouses.filter { it.type == HouseType.BOX }) {
                val cells = state.candidateCells(box, digit)
                for (groups in listOf(cells.groupBy(CellRef::row), cells.groupBy(CellRef::col))) {
                    for (group in groups.values) {
                        for (size in 2..group.size) {
                            group.combinations(size).forEach { nodeSets.add(it.toSet()) }
                        }
                    }
                }
            }
            val nodes = nodeSets.toList()
            if (nodes.none { it.size > 1 }) continue
            val weak = Array(nodes.size) { mutableListOf<Int>() }
            val strong = Array(nodes.size) { mutableListOf<Int>() }
            val nodeIndices = nodes.withIndex().associate { it.value to it.index }
            for (house in houseCandidates) {
                for (first in nodes.indices) {
                    if (!house.containsAll(nodes[first])) continue
                    val second = nodeIndices[house - nodes[first]] ?: continue
                    if (second !in strong[first]) strong[first].add(second)
                }
            }
            val linkedNodes = nodes.indices.filter { strong[it].isNotEmpty() }
            if (linkedNodes.none { nodes[it].size > 1 }) continue
            for ((position, first) in linkedNodes.withIndex()) {
                for (second in linkedNodes.drop(position + 1)) {
                    if (nodes[first].all { a -> nodes[second].all(a::sees) }) {
                        weak[first].add(second)
                        weak[second].add(first)
                    }
                }
            }
            for (start in nodes.indices) {
                if (strong[start].isEmpty()) continue
                val initial = SearchNode(start, expectStrong = true, grouped = nodes[start].size > 1)
                val queue = ArrayDeque<SearchNode>()
                val parents = mutableMapOf<SearchNode, SearchNode?>(initial to null)
                var firstElimination: SolveStep? = null
                queue.add(initial)
                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    val neighbors = if (current.expectStrong) strong[current.node] else weak[current.node]
                    for (next in neighbors) {
                        val successor = SearchNode(
                            next,
                            expectStrong = !current.expectStrong,
                            grouped = current.grouped || nodes[next].size > 1,
                        )
                        if (successor in parents) continue
                        parents[successor] = current
                        queue.add(successor)
                        if (!current.expectStrong || !successor.grouped) continue
                        val placesStart = next == start && nodes[start].size == 1
                        if (firstElimination != null && !placesStart) continue
                        val path = mutableListOf<Int>()
                        var cursor: SearchNode? = successor
                        while (cursor != null) {
                            path.add(cursor.node)
                            cursor = parents[cursor]
                        }
                        path.reverse()
                        if (path.size < 4) continue
                        val causeCells = path.flatMap { nodes[it] }.toSet()
                        val targets = candidates.filter { target ->
                            // An internal candidate can be disproved by the endpoints too.
                            // Excluding it would miss weak discontinuities and loop deductions.
                            nodes[start].all(target::sees) &&
                                nodes[next].all(target::sees)
                        }
                        if (targets.isEmpty() && !placesStart) continue
                        val links = path.zipWithNext().mapIndexed { index, (a, b) ->
                            GroupedInferenceLink(
                                from = nodes[a].map { CandidateRef(it, digit) }.toSet(),
                                to = nodes[b].map { CandidateRef(it, digit) }.toSet(),
                                type = if (index % 2 == 0) InferenceLinkType.STRONG else InferenceLinkType.WEAK,
                            )
                        }
                        val step = SolveStep(
                            technique = technique,
                            // A false assumption at the singleton start has returned to the
                            // same candidate as true: the start must therefore be true.
                            placements = if (placesStart) listOf(Placement(nodes[start].single(), digit)) else emptyList(),
                            eliminations = targets.map { CandidateElimination(CandidateRef(it, digit)) },
                            evidence = StepEvidence(
                                causeCells = causeCells,
                                causeCandidates = causeCells.map { CandidateRef(it, digit) }.toSet(),
                                focusDigits = setOf(digit),
                                groupedLinks = links,
                            ),
                        )
                        if (placesStart) return step
                        firstElimination = step
                    }
                }
                // Prefer a proved placement to removing a neighboring candidate on its way
                // around the same loop. The visited-state map bounds this search to 4N states.
                if (firstElimination != null) return firstElimination
            }
        }
        return null
    }

    private data class SearchNode(val node: Int, val expectStrong: Boolean, val grouped: Boolean)
}
