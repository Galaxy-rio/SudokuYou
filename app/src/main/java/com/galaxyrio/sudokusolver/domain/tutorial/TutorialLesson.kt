package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CandidateElimination
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.Placement
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.domain.solver.StepEvidence
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId

internal enum class TutorialStage { RULE, PATTERN, DEDUCTION, RESULT, TAKEAWAY }

internal data class TutorialLesson(
    val technique: TechniqueId,
    val examples: List<TutorialExample>,
)

/** Fixed teaching data, independent of saved games, notes, timers and solver history. */
internal data class TutorialExample(
    val technique: TechniqueId,
    val givens: String,
    val solution: String,
    val house: HouseRef,
    val patternCells: Set<CellRef>,
    val digits: Set<Int>,
) {
    val isHidden: Boolean get() = technique in HiddenTechniques
    val isSingle: Boolean get() = digits.size == 1
    private val original by lazy { Sudoku.fromGridString(givens) }
    val initialState: SolverState by lazy { SolverState.fromSudoku(original) }

    // A naked group can eliminate in every house shared by all its cells.
    val commonHouses: List<HouseRef> by lazy {
        HouseType.entries.flatMap { type -> (0..8).map { HouseRef(type, it) } }
            .filter { it.cells().containsAll(patternCells) }
    }
    val patternCandidates: Set<CandidateRef> by lazy {
        patternCells.flatMapTo(linkedSetOf()) { cell ->
            initialState.candidatesAt(cell).intersect(digits).map { CandidateRef(cell, it) }
        }
    }
    val deduction: SolveStep by lazy {
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
        tutorialExamples.groupBy { it.technique }.map { (technique, examples) ->
            TutorialLesson(technique, examples.sortedBy {
                when (it.house.type) { HouseType.BOX -> 0; HouseType.ROW -> 1; HouseType.COLUMN -> 2 }
            })
        }
    }

    fun find(id: String): TutorialLesson? = all.firstOrNull { it.technique.name == id }
}

private val HiddenTechniques = setOf(
    TechniqueId.HIDDEN_SINGLE, TechniqueId.HIDDEN_PAIR,
    TechniqueId.HIDDEN_TRIPLE, TechniqueId.HIDDEN_QUAD,
)
