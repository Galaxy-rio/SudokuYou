package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

data class CellRef(
    val row: Int,
    val col: Int,
) {
    init {
        require(row in 0 until Sudoku.GRID_SIZE)
        require(col in 0 until Sudoku.GRID_SIZE)
    }

    val index: Int
        get() = row * Sudoku.GRID_SIZE + col

    companion object {
        fun fromIndex(index: Int): CellRef {
            require(index in 0 until Sudoku.CELL_COUNT)
            return CellRef(
                row = index / Sudoku.GRID_SIZE,
                col = index % Sudoku.GRID_SIZE,
            )
        }
    }
}

data class CandidateRef(
    val cell: CellRef,
    val digit: Int,
) {
    init {
        require(digit in 1..Sudoku.GRID_SIZE)
    }
}

enum class HouseType {
    ROW,
    COLUMN,
    BOX,
}

data class HouseRef(
    val type: HouseType,
    val index: Int,
) {
    init {
        require(index in 0 until Sudoku.GRID_SIZE)
    }

    fun cells(): List<CellRef> = when (type) {
        HouseType.ROW -> List(Sudoku.GRID_SIZE) { col -> CellRef(index, col) }
        HouseType.COLUMN -> List(Sudoku.GRID_SIZE) { row -> CellRef(row, index) }
        HouseType.BOX -> {
            val startRow = (index / Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
            val startCol = (index % Sudoku.BOX_SIZE) * Sudoku.BOX_SIZE
            List(Sudoku.GRID_SIZE) { offset ->
                CellRef(
                    row = startRow + offset / Sudoku.BOX_SIZE,
                    col = startCol + offset % Sudoku.BOX_SIZE,
                )
            }
        }
    }
}

enum class TechniqueLevel {
    EASY,
    MEDIUM,
    HARD,
    BRUTAL,
}

enum class TechniqueId(val level: TechniqueLevel) {
    LAST_DIGIT(TechniqueLevel.EASY),
    NAKED_SINGLE(TechniqueLevel.EASY),
    HIDDEN_SINGLE(TechniqueLevel.EASY),
    NAKED_PAIR(TechniqueLevel.EASY),
    LOCKED_PAIR(TechniqueLevel.EASY),
    HIDDEN_PAIR(TechniqueLevel.EASY),
    POINTING_PAIR(TechniqueLevel.EASY),
    POINTING_TRIPLE(TechniqueLevel.EASY),
    CLAIMING_PAIR(TechniqueLevel.EASY),
    CLAIMING_TRIPLE(TechniqueLevel.EASY),
    NAKED_TRIPLE(TechniqueLevel.EASY),
    LOCKED_TRIPLE(TechniqueLevel.EASY),
    HIDDEN_TRIPLE(TechniqueLevel.EASY),
    NAKED_QUAD(TechniqueLevel.EASY),
    HIDDEN_QUAD(TechniqueLevel.EASY),
    X_WING(TechniqueLevel.MEDIUM),
    SKYSCRAPER(TechniqueLevel.MEDIUM),
    TWO_STRING_KITE(TechniqueLevel.MEDIUM),
    TURBOT_CRANE(TechniqueLevel.MEDIUM),
    XY_WING(TechniqueLevel.MEDIUM),
    XYZ_WING(TechniqueLevel.MEDIUM),
    REMOTE_PAIR(TechniqueLevel.MEDIUM),
    CHUTE_REMOTE_PAIR_SINGLE(TechniqueLevel.MEDIUM),
    CHUTE_REMOTE_PAIR_DOUBLE(TechniqueLevel.MEDIUM),
    CHUTE_REMOTE_PAIR_BONUS(TechniqueLevel.MEDIUM),
    SIMPLE_COLORING_TYPE_1(TechniqueLevel.MEDIUM),
    SIMPLE_COLORING_TYPE_2(TechniqueLevel.MEDIUM),
    EMPTY_RECTANGLE(TechniqueLevel.MEDIUM),
    SWORDFISH(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_1(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_1M(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_2(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_3(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_4(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_4M(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_5(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_5P(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_6(TechniqueLevel.MEDIUM),
    UNIQUE_RECTANGLE_TYPE_7(TechniqueLevel.MEDIUM),
    JELLYFISH(TechniqueLevel.HARD),
    BUG_PLUS_ONE(TechniqueLevel.HARD),
    X_CHAIN(TechniqueLevel.HARD),
    X_CHAIN_LOOP(TechniqueLevel.HARD),
    X_CHAIN_ONE_ENDPOINT(TechniqueLevel.HARD),
    WXYZ_WING(TechniqueLevel.HARD),
    STARFISH(TechniqueLevel.HARD),
    WHALE(TechniqueLevel.HARD),
    LEVIATHAN(TechniqueLevel.HARD),
    XY_CHAIN(TechniqueLevel.BRUTAL),
    XY_CHAIN_LOOP(TechniqueLevel.BRUTAL),
    AIC(TechniqueLevel.BRUTAL),
    NISHIO_FORCING_CHAIN(TechniqueLevel.BRUTAL),
    NISHIO_FORCING_NET(TechniqueLevel.BRUTAL),
    DIGIT_FORCING_CHAIN(TechniqueLevel.BRUTAL),
    DIGIT_FORCING_NET(TechniqueLevel.BRUTAL),
    CELL_FORCING_CHAIN(TechniqueLevel.BRUTAL),
    CELL_FORCING_NET(TechniqueLevel.BRUTAL),
    REGION_FORCING_CHAIN(TechniqueLevel.BRUTAL),
    REGION_FORCING_NET(TechniqueLevel.BRUTAL),
}

data class Placement(
    val cell: CellRef,
    val digit: Int,
) {
    init {
        require(digit in 1..Sudoku.GRID_SIZE)
    }
}

data class CandidateElimination(
    val candidate: CandidateRef,
)

enum class InferenceLinkType {
    WEAK,
    STRONG,
    DUAL,
}

/**
 * A semantic relationship between two candidates. Basic techniques do not need links yet,
 * but keeping them in the evidence model lets future chain techniques remain UI-independent.
 */
data class InferenceLink(
    val from: CandidateRef,
    val to: CandidateRef,
    val type: InferenceLinkType,
    val branchId: Int? = null,
)

enum class InferenceTruth {
    TRUE,
    FALSE,
}

enum class InferencePremiseType {
    NISHIO,
    DIGIT,
    CELL,
    REGION,
}

data class InferencePremise(
    val type: InferencePremiseType,
    val candidates: List<CandidateRef>,
    val house: HouseRef? = null,
) {
    init {
        require(candidates.isNotEmpty())
        if (type == InferencePremiseType.REGION) requireNotNull(house)
    }
}

enum class InferenceContradictionType {
    OPPOSITE_TRUTHS,
    EMPTY_CELL,
    EMPTY_HOUSE,
}

data class InferenceContradiction(
    val type: InferenceContradictionType,
    val candidate: CandidateRef? = null,
    val cell: CellRef? = null,
    val house: HouseRef? = null,
)

data class InferenceNode(
    val id: Int,
    val candidate: CandidateRef,
    val truth: InferenceTruth,
    val branchId: Int,
    val isAssumption: Boolean = false,
    val isConclusion: Boolean = false,
)

data class InferenceEdge(
    val fromNodeId: Int,
    val toNodeId: Int,
    val type: InferenceLinkType,
    val house: HouseRef? = null,
)

/**
 * UI-independent proof graph. Unlike the former linear link list, this model can represent
 * branches that split and converge, which is required by forcing nets.
 */
data class InferenceGraph(
    val nodes: List<InferenceNode>,
    val edges: List<InferenceEdge>,
    val premise: InferencePremise? = null,
    val contradiction: InferenceContradiction? = null,
) {
    init {
        require(nodes.map(InferenceNode::id).distinct().size == nodes.size)
        val nodeIds = nodes.mapTo(mutableSetOf(), InferenceNode::id)
        require(edges.all { edge ->
            edge.fromNodeId in nodeIds && edge.toNodeId in nodeIds
        })
    }

    private val nodesById: Map<Int, InferenceNode> = nodes.associateBy(InferenceNode::id)

    val links: List<InferenceLink>
        get() = edges.map { edge ->
            val from = nodesById.getValue(edge.fromNodeId)
            val to = nodesById.getValue(edge.toNodeId)
            InferenceLink(
                from = from.candidate,
                to = to.candidate,
                type = edge.type,
                branchId = from.branchId,
            )
        }

    /** A net needs multiple preceding facts to establish at least one derived fact. */
    val isNet: Boolean
        get() = edges.groupingBy(InferenceEdge::toNodeId).eachCount().values.any { it > 1 }

    companion object {
        fun fromLinks(links: List<InferenceLink>): InferenceGraph {
            if (links.isEmpty()) return InferenceGraph(emptyList(), emptyList())

            val candidates = links
                .flatMap { link -> listOf(link.from, link.to) }
                .distinct()
            val nodeIdByCandidate = candidates.withIndex().associate { (index, candidate) ->
                candidate to index
            }
            val nodes = candidates.mapIndexed { index, candidate ->
                InferenceNode(
                    id = index,
                    candidate = candidate,
                    truth = if (index % 2 == 0) InferenceTruth.FALSE else InferenceTruth.TRUE,
                    branchId = links.firstOrNull { link ->
                        link.from == candidate || link.to == candidate
                    }?.branchId ?: 0,
                    isAssumption = index == 0,
                    isConclusion = index == candidates.lastIndex,
                )
            }
            return InferenceGraph(
                nodes = nodes,
                edges = links.map { link ->
                    InferenceEdge(
                        fromNodeId = nodeIdByCandidate.getValue(link.from),
                        toNodeId = nodeIdByCandidate.getValue(link.to),
                        type = link.type,
                    )
                },
            )
        }
    }
}

data class StepEvidence(
    val causeCells: Set<CellRef> = emptySet(),
    val causeCandidates: Set<CandidateRef> = emptySet(),
    /** Digits central to the deduction, kept separate from auxiliary candidates in a chain. */
    val focusDigits: Set<Int> = emptySet(),
    val houses: List<HouseRef> = emptyList(),
    val baseHouses: List<HouseRef> = emptyList(),
    val coverHouses: List<HouseRef> = emptyList(),
    val links: List<InferenceLink> = emptyList(),
    val inferenceGraph: InferenceGraph = InferenceGraph.fromLinks(links),
)

data class SolveStep(
    val technique: TechniqueId,
    val placements: List<Placement> = emptyList(),
    val eliminations: List<CandidateElimination> = emptyList(),
    val evidence: StepEvidence = StepEvidence(),
) {
    init {
        require(placements.isNotEmpty() || eliminations.isNotEmpty()) {
            "A solve step must change at least one value or candidate."
        }
        require(placements.map(Placement::cell).distinct().size == placements.size) {
            "A solve step cannot place multiple values in the same cell."
        }
        require(
            eliminations.map(CandidateElimination::candidate).distinct().size == eliminations.size
        ) {
            "A solve step cannot contain duplicate candidate eliminations."
        }
    }
}

enum class SolveTraceStatus {
    SOLVED,
    STALLED,
    INVALID,
}

data class SolveTrace(
    val initialState: SolverState,
    val steps: List<SolveStep>,
    /** State 0 is the initial board; state n is the board after n steps. */
    val states: List<SolverState>,
    val status: SolveTraceStatus,
) {
    init {
        require(states.size == steps.size + 1) {
            "A trace must contain one more state than solve steps."
        }
        require(states.first() == initialState)
    }

    fun stateBeforeStep(stepIndex: Int): SolverState {
        require(stepIndex in steps.indices)
        return states[stepIndex]
    }

    fun stateAfterStep(stepIndex: Int): SolverState {
        require(stepIndex in steps.indices)
        return states[stepIndex + 1]
    }
}

internal val orderedHouses: List<HouseRef> = buildList {
    HouseType.entries.forEach { type ->
        repeat(Sudoku.GRID_SIZE) { index -> add(HouseRef(type, index)) }
    }
}
