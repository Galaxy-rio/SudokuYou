package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CandidateElimination
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.InferenceTruth
import com.galaxyrio.sudokusolver.domain.solver.Placement
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.domain.solver.StepEvidence
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId

internal enum class TutorialStage { RULE, PATTERN, DEDUCTION, RESULT, TAKEAWAY }

internal data class TutorialWingGroups(
    val pivot: Set<CellRef>,
    val firstWing: Set<CellRef>,
    val secondWing: Set<CellRef>,
)

internal data class TutorialLesson(
    val technique: TechniqueId,
    val examples: List<TutorialExample>,
) {
    val id: String get() = technique.tutorialId
}

/** Fixed teaching data, independent of saved games, notes, timers and solver history. */
internal data class TutorialExample(
    val technique: TechniqueId,
    val givens: String,
    val solution: String,
    val house: HouseRef,
    val patternCells: Set<CellRef>,
    val digits: Set<Int>,
    /** Two base-36 characters per cell; advanced positions include earlier logical eliminations. */
    val candidateMasks: String? = null,
    val recordedDeduction: SolveStep? = null,
) {
    val isHidden: Boolean get() = technique in HiddenTechniques
    val isSingle: Boolean get() = technique == TechniqueId.HIDDEN_SINGLE || technique == TechniqueId.NAKED_SINGLE
    val isAdvanced: Boolean get() = recordedDeduction != null
    private val original by lazy {
        val board = Sudoku.fromGridString(givens)
        if (candidateMasks == null) board else {
            require(candidateMasks.length == Sudoku.CELL_COUNT * 2)
            board.copy(cells = board.cells.mapIndexed { index, cell ->
                val mask = candidateMasks.substring(index * 2, index * 2 + 2).toInt(36)
                if (cell.value != 0) cell else cell.copy(
                    candidates = (1..9).filterTo(linkedSetOf()) { mask and (1 shl (it - 1)) != 0 },
                    isCandidateSetExplicit = true,
                )
            })
        }
    }
    val initialState: SolverState by lazy { SolverState.fromSudoku(original) }

    // A naked group can eliminate in every house shared by all its cells.
    val commonHouses: List<HouseRef> by lazy {
        HouseType.entries.flatMap { type -> (0..8).map { HouseRef(type, it) } }
            .filter { it.cells().containsAll(patternCells) }
    }
    val patternCandidates: Set<CandidateRef> by lazy {
        recordedDeduction?.let { return@lazy it.evidence.causeCandidates }
        patternCells.flatMapTo(linkedSetOf()) { cell ->
            initialState.candidatesAt(cell).intersect(digits).map { CandidateRef(cell, it) }
        }
    }
    /** These are alternate hypotheses, not solved/unsolved states. */
    val coloring: Map<CandidateRef, Boolean> by lazy {
        val step = recordedDeduction ?: return@lazy emptyMap()
        when (technique) {
            TechniqueId.THREE_D_MEDUSA -> step.evidence.inferenceGraph.nodes
                .distinctBy { it.candidate }.associate { it.candidate to (it.truth == InferenceTruth.TRUE) }
            TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2 -> {
                val colors = linkedMapOf<CandidateRef, Boolean>()
                val queue = ArrayDeque<CandidateRef>()
                val start = step.evidence.causeCandidates.first()
                colors[start] = true
                queue.add(start)
                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    step.evidence.links.forEach { link ->
                        val other = when (current) { link.from -> link.to; link.to -> link.from; else -> null }
                        if (other != null && other !in colors) {
                            colors[other] = !colors.getValue(current)
                            queue.add(other)
                        }
                    }
                }
                colors
            }
            else -> emptyMap()
        }
    }
    /** Identify the three roles in the already recorded wing; never search the whole board. */
    val wingGroups: TutorialWingGroups? by lazy {
        if (technique !in setOf(TechniqueId.XY_WING, TechniqueId.XYZ_WING, TechniqueId.WXYZ_WING,
                TechniqueId.FIVE_Y_WING, TechniqueId.SIX_Y_WING, TechniqueId.SEVEN_Y_WING)) return@lazy null
        val cells = patternCells.toList()
        val z = deduction.eliminations.first().candidate.digit
        fun Set<CellRef>.digits() = flatMap(initialState::candidatesAt).toSet()
        fun sees(a: CellRef, b: CellRef): Boolean = a != b && (a.row == b.row || a.col == b.col ||
            (a.row / 3 == b.row / 3 && a.col / 3 == b.col / 3))
        fun Set<CellRef>.shareHouse() = commonTutorialHouses.any { it.cells().containsAll(this) }
        val full = (1 shl cells.size) - 1
        for (pivotMask in 1 until full) {
            val pivot = cells.filterIndexed { index, _ -> pivotMask and (1 shl index) != 0 }.toSet()
            if (pivot.size > 3 || !pivot.shareHouse()) continue
            val rest = cells.filterNot { it in pivot }
            if (rest.size < 2 || rest.any { wing -> pivot.any { !sees(wing, it) } }) continue
            for (firstMask in 1 until (1 shl rest.size) - 1) {
                val first = rest.filterIndexed { index, _ -> firstMask and (1 shl index) != 0 }.toSet()
                val second = rest.toSet() - first
                if (!first.shareHouse() || !second.shareHouse() || first.digits().intersect(second.digits()) != setOf(z)) continue
                if (technique == TechniqueId.XY_WING && (pivot.size != 1 || z in pivot.digits())) continue
                if (technique == TechniqueId.XYZ_WING && (pivot.size != 1 || pivot.digits().size != 3)) continue
                val relevant = (if (z in pivot.digits()) patternCells else first + second).filter { initialState.hasCandidate(it, z) }
                if (deduction.eliminations.all { target -> relevant.all { sees(target.candidate.cell, it) } }) {
                    return@lazy TutorialWingGroups(pivot, first, second)
                }
            }
        }
        null
    }
    val deduction: SolveStep by lazy {
        recordedDeduction?.let { return@lazy it }
        val eliminations = if (isSingle) emptyList() else {
            val cells = if (isHidden) patternCells else {
                commonHouses.flatMapTo(linkedSetOf()) { it.cells() } - patternCells
            }
            cells.flatMap { cell ->
                initialState.candidatesAt(cell)
                    .filter { digit -> if (isHidden) digit !in digits else digit in digits }
                    .map { CandidateElimination(CandidateRef(cell, it)) }
            }
        }
        SolveStep(
            technique = technique,
            placements = if (isSingle) listOf(Placement(patternCells.single(), digits.single())) else emptyList(),
            eliminations = eliminations,
            evidence = StepEvidence(
                causeCells = patternCells,
                causeCandidates = patternCandidates,
                focusDigits = digits,
                houses = if (isHidden) listOf(house) else commonHouses,
            ),
        )
    }
    val before: Sudoku by lazy { initialState.toBoard() }
    val after: Sudoku by lazy { initialState.apply(deduction).toBoard() }

    /** Exclusions for singles are already established by givens, not new eliminations. */
    val excludedCells: Set<CellRef> by lazy {
        when {
            !isSingle -> emptySet()
            isHidden -> house.cells().toSet() - patternCells
            else -> commonHouses.flatMapTo(linkedSetOf()) { it.cells() }
                .filterTo(linkedSetOf()) { initialState.valueAt(it) != 0 }
        }
    }
    val blockingGivens: Set<CellRef> by lazy {
        if (!isHidden || !isSingle) emptySet() else {
            before.cells.indices.map(CellRef::fromIndex).filterTo(linkedSetOf()) { cell ->
                initialState.valueAt(cell) in digits && excludedCells.any { excluded ->
                    cell.row == excluded.row || cell.col == excluded.col ||
                        (cell.row / 3 == excluded.row / 3 && cell.col / 3 == excluded.col / 3)
                }
            }
        }
    }

    fun boardAt(stage: TutorialStage): Sudoku = when (stage) {
        TutorialStage.RESULT, TutorialStage.TAKEAWAY -> after
        else -> before
    }

    private fun SolverState.toBoard(): Sudoku = Sudoku(List(Sudoku.CELL_COUNT) { index ->
        val cell = CellRef.fromIndex(index)
        val value = valueAt(cell)
        Cell(
            value = value,
            candidates = if (value == 0) candidatesAt(cell) else emptySet(),
            isFixed = original.cells[index].isFixed,
            isCandidateSetExplicit = value == 0,
        )
    })
}

internal object TutorialLessons {
    val all: List<TutorialLesson> by lazy {
        (tutorialExamples + advancedTutorialExamples).groupBy { it.technique.tutorialId }.map { (_, examples) ->
            TutorialLesson(examples.first().technique, if (examples.first().isAdvanced) examples else examples.sortedBy {
                when (it.house.type) { HouseType.BOX -> 0; HouseType.ROW -> 1; HouseType.COLUMN -> 2 }
            })
        }
    }

    fun find(id: String): TutorialLesson? = all.firstOrNull { it.id == id }
}

/** Catalog entries that teach several variants share one route and one lesson. */
internal val TechniqueId.tutorialId: String get() = when (this) {
    TechniqueId.POINTING_PAIR, TechniqueId.POINTING_TRIPLE,
    TechniqueId.CLAIMING_PAIR, TechniqueId.CLAIMING_TRIPLE -> "locked_candidate"
    TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2 -> "simple_coloring"
    TechniqueId.X_CHAIN_LOOP, TechniqueId.X_CHAIN_ONE_ENDPOINT -> TechniqueId.X_CHAIN.name
    TechniqueId.XY_CHAIN_LOOP -> TechniqueId.XY_CHAIN.name
    TechniqueId.CELL_FORCING_CHAIN, TechniqueId.REGION_FORCING_CHAIN -> "cell_region_forcing_chain"
    TechniqueId.CELL_FORCING_NET, TechniqueId.REGION_FORCING_NET -> "cell_region_forcing_net"
    else -> name
}

private val HiddenTechniques = setOf(
    TechniqueId.HIDDEN_SINGLE, TechniqueId.HIDDEN_PAIR,
    TechniqueId.HIDDEN_TRIPLE, TechniqueId.HIDDEN_QUAD,
)

private val commonTutorialHouses = HouseType.entries.flatMap { type -> (0..8).map { HouseRef(type, it) } }
