package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

/**
 * Builds contradiction and verity forcing proofs from candidate implications.
 *
 * Each assumption is propagated only with normal Sudoku constraints: a true candidate removes
 * weakly linked candidates, while the last remaining candidate in a cell or house becomes true.
 * No solved grid or backtracking result is consulted, so every returned deduction has a proof the
 * UI can display. Multi-parent implications turn the proof into a forcing net.
 */
class ForcingChainDetector(
    private val enabledPremises: Set<InferencePremiseType> = InferencePremiseType.entries.toSet(),
) : TechniqueDetector {
    init {
        require(enabledPremises.isNotEmpty())
    }

    override val technique: TechniqueId = TechniqueId.NISHIO_FORCING_CHAIN

    override fun find(state: SolverState): SolveStep? {
        val candidates = state.unsolvedCells()
            .flatMap { cell ->
                state.candidatesAt(cell).map { digit -> CandidateRef(cell, digit) }
            }
            .sortedWith(
                compareBy<CandidateRef>(
                    { state.candidateMaskAt(it.cell).countOneBits() },
                    { it.cell.index },
                    CandidateRef::digit,
                )
            )
        if (candidates.isEmpty()) return null

        val propagator = ImplicationPropagator(state)
        val cache = mutableMapOf<FactKey, BranchResult>()

        fun branch(candidate: CandidateRef, truth: InferenceTruth): BranchResult =
            cache.getOrPut(FactKey(candidate, truth)) {
                propagator.propagate(FactKey(candidate, truth))
            }

        // Nishio: assuming a candidate true produces an invalid state.
        if (InferencePremiseType.NISHIO in enabledPremises) {
            candidates.forEach { candidate ->
                val result = branch(candidate, InferenceTruth.TRUE)
                if (result.isUsable && result.contradiction != null) {
                    return contradictionStep(
                        premise = InferencePremise(
                            type = InferencePremiseType.NISHIO,
                            candidates = listOf(candidate),
                        ),
                        branch = result,
                        target = FactKey(candidate, InferenceTruth.FALSE),
                    )
                }
            }
        }

        // Digit forcing: the selected candidate is either true or false.
        if (InferencePremiseType.DIGIT in enabledPremises) {
            candidates.forEach { candidate ->
                val trueBranch = branch(candidate, InferenceTruth.TRUE)
                val falseBranch = branch(candidate, InferenceTruth.FALSE)
                if (!trueBranch.isUsable || !falseBranch.isUsable) return@forEach

                if (falseBranch.contradiction != null) {
                    return contradictionStep(
                        premise = InferencePremise(
                            type = InferencePremiseType.DIGIT,
                            candidates = listOf(candidate),
                        ),
                        branch = falseBranch,
                        target = FactKey(candidate, InferenceTruth.TRUE),
                    )
                }
                if (trueBranch.contradiction == null) {
                    commonConsequence(
                        state = state,
                        premise = InferencePremise(
                            type = InferencePremiseType.DIGIT,
                            candidates = listOf(candidate),
                        ),
                        branches = listOf(trueBranch, falseBranch),
                    )?.let { return it }
                }
            }
        }

        // Cell forcing: one of the candidates in an unsolved cell must be true.
        if (InferencePremiseType.CELL in enabledPremises) {
            state.unsolvedCells()
                .asSequence()
                .map { cell ->
                    state.candidatesAt(cell)
                        .sorted()
                        .map { digit -> CandidateRef(cell, digit) }
                }
                .filter { it.size in MIN_GROUP_SIZE..MAX_GROUP_SIZE }
                .sortedWith(compareBy<List<CandidateRef>>({ it.size }, { it.first().cell.index }))
                .forEach { assumptions ->
                    val branches = assumptions.map { candidate ->
                        branch(candidate, InferenceTruth.TRUE)
                    }
                    if (branches.all(BranchResult::isUsable) &&
                        branches.none { it.contradiction != null }
                    ) {
                        commonConsequence(
                            state = state,
                            premise = InferencePremise(
                                type = InferencePremiseType.CELL,
                                candidates = assumptions,
                            ),
                            branches = branches,
                        )?.let { return it }
                    }
                }
        }

        // Region forcing: one position for a digit in a row, column, or box must be true.
        if (InferencePremiseType.REGION in enabledPremises) {
            orderedHouses.forEach { house ->
                for (digit in 1..Sudoku.GRID_SIZE) {
                    val assumptions = state.candidateCells(house, digit)
                        .sortedBy(CellRef::index)
                        .map { cell -> CandidateRef(cell, digit) }
                    if (assumptions.size !in MIN_GROUP_SIZE..MAX_GROUP_SIZE) continue

                    val branches = assumptions.map { candidate ->
                        branch(candidate, InferenceTruth.TRUE)
                    }
                    if (branches.all(BranchResult::isUsable) &&
                        branches.none { it.contradiction != null }
                    ) {
                        commonConsequence(
                            state = state,
                            premise = InferencePremise(
                                type = InferencePremiseType.REGION,
                                candidates = assumptions,
                                house = house,
                            ),
                            branches = branches,
                        )?.let { return it }
                    }
                }
            }
        }

        return null
    }

    private fun contradictionStep(
        premise: InferencePremise,
        branch: BranchResult,
        target: FactKey,
    ): SolveStep {
        val graph = proofGraph(
            premise = premise,
            branches = listOf(branch),
            conclusion = null,
        )
        return forcingStep(
            technique = techniqueFor(premise.type, graph.isNet),
            target = target,
            graph = graph,
        )
    }

    private fun commonConsequence(
        state: SolverState,
        premise: InferencePremise,
        branches: List<BranchResult>,
    ): SolveStep? {
        val commonFacts = branches
            .map { it.facts.keys }
            .reduce(Set<FactKey>::intersect)
            .asSequence()
            .filterNot { fact -> branches.any { fact in it.assumptions } }
            .filter { fact ->
                state.valueAt(fact.candidate.cell) == 0 &&
                    state.hasCandidate(fact.candidate.cell, fact.candidate.digit)
            }
            .sortedWith(
                compareBy<FactKey>(
                    { fact -> branches.sumOf { it.proofSize(fact) } },
                    { it.candidate.cell.index },
                    { it.candidate.digit },
                    { it.truth.ordinal },
                )
            )
            .toList()
        val conclusion = commonFacts.firstOrNull() ?: return null
        val graph = proofGraph(
            premise = premise,
            branches = branches,
            conclusion = conclusion,
        )
        return forcingStep(
            technique = techniqueFor(premise.type, graph.isNet),
            target = conclusion,
            graph = graph,
        )
    }

    private fun forcingStep(
        technique: TechniqueId,
        target: FactKey,
        graph: InferenceGraph,
    ): SolveStep {
        val placement = if (target.truth == InferenceTruth.TRUE) {
            listOf(Placement(target.candidate.cell, target.candidate.digit))
        } else {
            emptyList()
        }
        val elimination = if (target.truth == InferenceTruth.FALSE) {
            listOf(CandidateElimination(target.candidate))
        } else {
            emptyList()
        }
        val graphHouses = graph.edges.mapNotNull(InferenceEdge::house)
        val contradictionHouses = listOfNotNull(graph.contradiction?.house)
        return SolveStep(
            technique = technique,
            placements = placement,
            eliminations = elimination,
            evidence = StepEvidence(
                causeCells = graph.nodes.mapTo(mutableSetOf()) { it.candidate.cell },
                causeCandidates = graph.nodes.mapTo(mutableSetOf(), InferenceNode::candidate),
                focusDigits = graph.nodes.mapTo(mutableSetOf()) { it.candidate.digit },
                houses = (graphHouses + contradictionHouses + listOfNotNull(graph.premise?.house))
                    .distinct(),
                links = graph.links,
                inferenceGraph = graph,
            ),
        )
    }

    private fun proofGraph(
        premise: InferencePremise,
        branches: List<BranchResult>,
        conclusion: FactKey?,
    ): InferenceGraph {
        val nodes = mutableListOf<InferenceNode>()
        val edges = mutableListOf<InferenceEdge>()
        var nextNodeId = 0

        branches.forEachIndexed { branchId, branch ->
            val terminals = if (conclusion != null) {
                setOf(conclusion)
            } else {
                branch.contradiction?.facts.orEmpty()
            }
            val included = mutableSetOf<FactKey>()

            fun includeAncestors(fact: FactKey) {
                if (!included.add(fact)) return
                branch.facts[fact]?.parents?.forEach(::includeAncestors)
            }
            terminals.forEach(::includeAncestors)

            val orderedFacts = included.sortedBy { fact -> branch.facts.getValue(fact).order }
            val ids = orderedFacts.associateWith { nextNodeId++ }
            orderedFacts.forEach { fact ->
                nodes += InferenceNode(
                    id = ids.getValue(fact),
                    candidate = fact.candidate,
                    truth = fact.truth,
                    branchId = branchId,
                    isAssumption = fact in branch.assumptions,
                    isConclusion = fact in terminals,
                )
            }
            orderedFacts.forEach { fact ->
                val derivation = branch.facts.getValue(fact)
                derivation.parents
                    .filter { it in included }
                    .forEach { parent ->
                        edges += InferenceEdge(
                            fromNodeId = ids.getValue(parent),
                            toNodeId = ids.getValue(fact),
                            type = derivation.linkType ?: InferenceLinkType.DUAL,
                            house = derivation.house,
                        )
                    }
            }
        }

        return InferenceGraph(
            nodes = nodes,
            edges = edges,
            premise = premise,
            contradiction = branches.singleOrNull()?.contradiction?.asPublicModel(),
        )
    }

    private fun techniqueFor(
        premiseType: InferencePremiseType,
        isNet: Boolean,
    ): TechniqueId = when (premiseType) {
        InferencePremiseType.NISHIO -> if (isNet) {
            TechniqueId.NISHIO_FORCING_NET
        } else {
            TechniqueId.NISHIO_FORCING_CHAIN
        }
        InferencePremiseType.DIGIT -> if (isNet) {
            TechniqueId.DIGIT_FORCING_NET
        } else {
            TechniqueId.DIGIT_FORCING_CHAIN
        }
        InferencePremiseType.CELL -> if (isNet) {
            TechniqueId.CELL_FORCING_NET
        } else {
            TechniqueId.CELL_FORCING_CHAIN
        }
        InferencePremiseType.REGION -> if (isNet) {
            TechniqueId.REGION_FORCING_NET
        } else {
            TechniqueId.REGION_FORCING_CHAIN
        }
    }

    private companion object {
        const val MIN_GROUP_SIZE = 2
        const val MAX_GROUP_SIZE = Sudoku.GRID_SIZE
    }
}

private data class FactKey(
    val candidate: CandidateRef,
    val truth: InferenceTruth,
) {
    fun opposite(): FactKey = copy(
        truth = if (truth == InferenceTruth.TRUE) InferenceTruth.FALSE else InferenceTruth.TRUE
    )
}

private data class Derivation(
    val parents: Set<FactKey>,
    val linkType: InferenceLinkType?,
    val house: HouseRef?,
    val order: Int,
)

private data class InternalContradiction(
    val type: InferenceContradictionType,
    val facts: Set<FactKey>,
    val candidate: CandidateRef? = null,
    val cell: CellRef? = null,
    val house: HouseRef? = null,
) {
    fun asPublicModel(): InferenceContradiction = InferenceContradiction(
        type = type,
        candidate = candidate,
        cell = cell,
        house = house,
    )
}

private data class BranchResult(
    val assumptions: Set<FactKey>,
    val facts: Map<FactKey, Derivation>,
    val contradiction: InternalContradiction?,
    val completed: Boolean,
) {
    val isUsable: Boolean
        get() = completed

    fun proofSize(target: FactKey): Int {
        val visited = mutableSetOf<FactKey>()
        fun visit(fact: FactKey) {
            if (!visited.add(fact)) return
            facts[fact]?.parents?.forEach(::visit)
        }
        visit(target)
        return visited.size
    }
}

private class ImplicationPropagator(
    private val state: SolverState,
) {
    private val cellCandidates: Map<CellRef, List<CandidateRef>> = state.unsolvedCells()
        .associateWith { cell ->
            state.candidatesAt(cell)
                .sorted()
                .map { digit -> CandidateRef(cell, digit) }
        }
    private val allCandidates: Set<CandidateRef> = cellCandidates.values.flatten().toSet()

    fun propagate(assumption: FactKey): BranchResult {
        val facts = linkedMapOf<FactKey, Derivation>()
        val queue = ArrayDeque<PendingFact>()
        var order = 0
        queue += PendingFact(
            key = assumption,
            parents = emptySet(),
            linkType = null,
            house = null,
        )

        fun result(
            contradiction: InternalContradiction? = null,
            completed: Boolean = true,
        ) = BranchResult(
            assumptions = setOf(assumption),
            facts = facts,
            contradiction = contradiction,
            completed = completed,
        )

        while (queue.isNotEmpty()) {
            if (facts.size >= MAX_FACTS) return result(completed = false)
            val pending = queue.removeFirst()
            if (pending.key in facts) continue

            val opposite = pending.key.opposite()
            facts[pending.key] = Derivation(
                parents = pending.parents,
                linkType = pending.linkType,
                house = pending.house,
                order = order++,
            )
            if (opposite in facts) {
                return result(
                    contradiction = InternalContradiction(
                        type = InferenceContradictionType.OPPOSITE_TRUTHS,
                        facts = setOf(pending.key, opposite),
                        candidate = pending.key.candidate,
                    )
                )
            }

            when (pending.key.truth) {
                InferenceTruth.TRUE -> {
                    val source = pending.key.candidate
                    cellCandidates.getValue(source.cell)
                        .asSequence()
                        .filter { it != source }
                        .forEach { target ->
                            queue += PendingFact(
                                key = FactKey(target, InferenceTruth.FALSE),
                                parents = setOf(pending.key),
                                linkType = InferenceLinkType.WEAK,
                                house = null,
                            )
                        }
                    peerIndices(source.cell)
                        .asSequence()
                        .map(CellRef::fromIndex)
                        .map { peer -> CandidateRef(peer, source.digit) }
                        .filter { it in allCandidates }
                        .forEach { target ->
                            queue += PendingFact(
                                key = FactKey(target, InferenceTruth.FALSE),
                                parents = setOf(pending.key),
                                linkType = InferenceLinkType.WEAK,
                                house = sharedHouses(source.cell, target.cell).firstOrNull(),
                            )
                        }
                }

                InferenceTruth.FALSE -> {
                    val candidate = pending.key.candidate
                    evaluateGroup(
                        candidates = cellCandidates.getValue(candidate.cell),
                        facts = facts,
                        house = null,
                        queue = queue,
                    )?.let { contradiction -> return result(contradiction) }

                    candidate.cell.houses().forEach { house ->
                        evaluateGroup(
                            candidates = state.candidateCells(house, candidate.digit)
                                .map { cell -> CandidateRef(cell, candidate.digit) },
                            facts = facts,
                            house = house,
                            queue = queue,
                        )?.let { contradiction -> return result(contradiction) }
                    }
                }
            }
        }

        return result()
    }

    private fun evaluateGroup(
        candidates: List<CandidateRef>,
        facts: Map<FactKey, Derivation>,
        house: HouseRef?,
        queue: ArrayDeque<PendingFact>,
    ): InternalContradiction? {
        if (candidates.isEmpty()) return null
        val trueFacts = candidates.map { FactKey(it, InferenceTruth.TRUE) }.filter { it in facts }
        if (trueFacts.isNotEmpty()) return null

        val falseFacts = candidates.map { FactKey(it, InferenceTruth.FALSE) }.filter { it in facts }
        val unknown = candidates.filter { candidate ->
            FactKey(candidate, InferenceTruth.TRUE) !in facts &&
                FactKey(candidate, InferenceTruth.FALSE) !in facts
        }
        if (unknown.isEmpty()) {
            return InternalContradiction(
                type = if (house == null) {
                    InferenceContradictionType.EMPTY_CELL
                } else {
                    InferenceContradictionType.EMPTY_HOUSE
                },
                facts = falseFacts.toSet(),
                cell = if (house == null) candidates.first().cell else null,
                house = house,
            )
        }
        if (unknown.size == 1) {
            queue += PendingFact(
                key = FactKey(unknown.single(), InferenceTruth.TRUE),
                parents = falseFacts.toSet(),
                linkType = InferenceLinkType.STRONG,
                house = house,
            )
        }
        return null
    }

    private data class PendingFact(
        val key: FactKey,
        val parents: Set<FactKey>,
        val linkType: InferenceLinkType?,
        val house: HouseRef?,
    )

    private companion object {
        const val MAX_FACTS = 512
    }
}
