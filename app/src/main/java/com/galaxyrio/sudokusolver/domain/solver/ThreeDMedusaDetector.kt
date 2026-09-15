package com.galaxyrio.sudokusolver.domain.solver

import java.util.ArrayDeque

/** Candidate colouring through conjugate pairs and bivalue cells. */
class ThreeDMedusaDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.THREE_D_MEDUSA

    override fun find(state: SolverState): SolveStep? {
        val graph = mutableMapOf<CandidateRef, MutableSet<CandidateRef>>()
        val houseLinks = (1..9).flatMap(state::strongLinks)
        fun connect(first: CandidateRef, second: CandidateRef) {
            graph.getOrPut(first, ::mutableSetOf).add(second)
            graph.getOrPut(second, ::mutableSetOf).add(first)
        }
        houseLinks.forEach { link ->
            connect(CandidateRef(link.first, link.digit), CandidateRef(link.second, link.digit))
        }
        state.biValueCells().forEach { cell ->
            val digits = state.candidatesAt(cell).toList()
            connect(CandidateRef(cell, digits[0]), CandidateRef(cell, digits[1]))
        }

        val remaining = graph.keys.toMutableSet()
        while (remaining.isNotEmpty()) {
            val component = color(remaining.first(), graph)
            remaining.removeAll(component.colors.keys)
            // An odd XOR cycle has no valid colouring. Do not manufacture a deduction
            // from inconsistent input; the normal solver validates the board separately.
            if (!component.consistent) continue
            val colors = component.colors
            if (colors.keys.map { it.digit }.distinct().size < 2) continue
            val groups = List(2) { color -> colors.filterValues { it == color }.keys }
            val contradictions = groups.map { group -> findContradiction(state, group, colors) }
            val falseColors = contradictions.indices.filter { contradictions[it] != null }
            if (falseColors.size > 1) continue

            val houses = houseLinks.filter { link ->
                CandidateRef(link.first, link.digit) in colors &&
                    CandidateRef(link.second, link.digit) in colors
            }.map(StrongLink::house).distinct()

            if (falseColors.size == 1) {
                val falseColor = falseColors.single()
                val contradiction = requireNotNull(contradictions[falseColor])
                return SolveStep(
                    technique = technique,
                    placements = groups[1 - falseColor].sortedBy { it.cell.index }.map {
                        Placement(it.cell, it.digit)
                    },
                    eliminations = groups[falseColor].map(::CandidateElimination),
                    evidence = evidence(component, graph, houses, falseColor, contradiction),
                )
            }

            // This single test covers the three colour-trap forms: both colours in
            // a cell, the same digit in peers, or one cell link and one peer link.
            val targets = state.unsolvedCells().flatMap { cell ->
                state.candidatesAt(cell).map { CandidateRef(cell, it) }
            }.filter { candidate ->
                candidate !in colors && groups.all { group -> group.any { conflicts(candidate, it) } }
            }
            if (targets.isNotEmpty()) {
                return SolveStep(
                    technique = technique,
                    eliminations = targets.map(::CandidateElimination),
                    evidence = evidence(component, graph, houses),
                )
            }
        }
        return null
    }

    private fun findContradiction(
        state: SolverState,
        trueGroup: Set<CandidateRef>,
        colors: Map<CandidateRef, Int>,
    ): ColorContradiction? {
        // Two equally coloured digits in one cell, or one digit twice in a house.
        for (first in trueGroup) {
            val second = trueGroup.firstOrNull { conflicts(first, it) }
            if (second != null) return ColorContradiction(conflict = first to second)
        }

        // Assuming this colour true must leave every unsolved cell a candidate.
        // Opposite-coloured candidates are already false by XOR; uncoloured ones
        // may all be excluded by true candidates of the assumed colour (rule 6).
        for (cell in state.unsolvedCells()) {
            val candidates = state.candidatesAt(cell).map { CandidateRef(cell, it) }
            if (candidates.isEmpty()) continue
            if (candidates.any { it in trueGroup }) continue
            val exclusions = mutableMapOf<CandidateRef, CandidateRef>()
            val emptied = candidates.all { candidate ->
                if (candidate in colors) {
                    true
                } else {
                    val cause = trueGroup.firstOrNull { conflicts(candidate, it) }
                    if (cause != null) exclusions[candidate] = cause
                    cause != null
                }
            }
            if (emptied) return ColorContradiction(emptyCell = cell, exclusions = exclusions)
        }
        return null
    }

    private fun color(
        start: CandidateRef,
        graph: Map<CandidateRef, Set<CandidateRef>>,
    ): ColoredComponent {
        val colors = linkedMapOf(start to 0)
        val queue = ArrayDeque<CandidateRef>().apply { add(start) }
        var consistent = true
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (next in graph[current].orEmpty()) {
                val expected = 1 - colors.getValue(current)
                val existing = colors[next]
                if (existing == null) {
                    colors[next] = expected
                    queue.addLast(next)
                } else if (existing != expected) {
                    consistent = false
                }
            }
        }
        return ColoredComponent(colors, consistent)
    }

    private fun evidence(
        component: ColoredComponent,
        graph: Map<CandidateRef, Set<CandidateRef>>,
        houses: List<HouseRef>,
        assumedTrueColor: Int = 0,
        contradiction: ColorContradiction? = null,
    ): StepEvidence {
        val colors = component.colors
        val nodeIds = colors.keys.withIndex().associate { it.value to it.index }
        val nodes = colors.map { (candidate, color) ->
            InferenceNode(
                id = nodeIds.getValue(candidate),
                candidate = candidate,
                truth = if (color == assumedTrueColor) InferenceTruth.TRUE else InferenceTruth.FALSE,
                branchId = 0,
                isAssumption = nodeIds.getValue(candidate) == 0,
            )
        }.toMutableList()
        val edges = mutableListOf<InferenceEdge>()
        for ((candidate, index) in nodeIds) {
            graph[candidate].orEmpty().forEach { other ->
                val otherIndex = nodeIds.getValue(other)
                if (index < otherIndex) {
                    edges += InferenceEdge(index, otherIndex, InferenceLinkType.DUAL)
                }
            }
        }
        fun addExclusion(candidate: CandidateRef, cause: CandidateRef) {
            val id = nodes.size
            nodes += InferenceNode(id, candidate, InferenceTruth.FALSE, branchId = 0, isConclusion = true)
            edges += InferenceEdge(nodeIds.getValue(cause), id, InferenceLinkType.WEAK)
        }
        contradiction?.conflict?.let { (first, second) -> addExclusion(second, first) }
        contradiction?.exclusions?.forEach { (candidate, cause) -> addExclusion(candidate, cause) }
        val proof = InferenceGraph(
            nodes = nodes,
            edges = edges,
            contradiction = when {
                contradiction?.conflict != null -> InferenceContradiction(
                    type = InferenceContradictionType.OPPOSITE_TRUTHS,
                    candidate = contradiction.conflict.second,
                )
                contradiction?.emptyCell != null -> InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_CELL,
                    cell = contradiction.emptyCell,
                )
                else -> null
            },
        )
        return StepEvidence(
            causeCells = colors.keys.map { it.cell }.toSet() + listOfNotNull(contradiction?.emptyCell),
            causeCandidates = colors.keys + contradiction?.exclusions.orEmpty().keys,
            focusDigits = colors.keys.map { it.digit }.toSet(),
            houses = houses,
            links = proof.links,
            inferenceGraph = proof,
        )
    }

    private fun conflicts(first: CandidateRef, second: CandidateRef): Boolean =
        first != second && (first.cell == second.cell ||
            (first.digit == second.digit && first.cell.sees(second.cell)))

    private data class ColoredComponent(val colors: Map<CandidateRef, Int>, val consistent: Boolean)

    private data class ColorContradiction(
        val conflict: Pair<CandidateRef, CandidateRef>? = null,
        val emptyCell: CellRef? = null,
        val exclusions: Map<CandidateRef, CandidateRef> = emptyMap(),
    )
}
