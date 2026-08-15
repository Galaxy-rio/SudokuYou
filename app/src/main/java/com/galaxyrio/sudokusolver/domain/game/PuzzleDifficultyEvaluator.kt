package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.HumanSolver
import com.galaxyrio.sudokusolver.domain.solver.SolveTrace
import com.galaxyrio.sudokusolver.domain.solver.SolveTraceStatus
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.domain.solver.TechniqueLevel

data class PuzzleRating(
    val difficulty: Difficulty,
    val hardestTechnique: TechniqueId,
    val trace: SolveTrace,
    /** Used only to prefer richer puzzles within the same tier, never to cross tier boundaries. */
    val pathScore: Int,
)

/** Rates a puzzle with the exact same ordered human solver used by hints. */
class PuzzleDifficultyEvaluator(
    private val solver: HumanSolver = HumanSolver(),
) {
    fun evaluate(sudoku: Sudoku): PuzzleRating? {
        val trace = solver.solveTrace(sudoku)
        if (trace.status != SolveTraceStatus.SOLVED || trace.steps.isEmpty()) return null

        val hardest = trace.steps.maxBy { it.technique.level.ordinal }.technique
        return PuzzleRating(
            difficulty = hardest.level.toDifficulty(),
            hardestTechnique = hardest,
            trace = trace,
            pathScore = trace.steps.sumOf { step ->
                LEVEL_WEIGHTS.getValue(step.technique.level) +
                    step.evidence.inferenceGraph.nodes.size.coerceAtMost(MAX_GRAPH_BONUS)
            },
        )
    }

    private fun TechniqueLevel.toDifficulty(): Difficulty = when (this) {
        TechniqueLevel.EASY -> Difficulty.EASY
        TechniqueLevel.MEDIUM -> Difficulty.MEDIUM
        TechniqueLevel.HARD -> Difficulty.HARD
        TechniqueLevel.BRUTAL -> Difficulty.BRUTAL
    }

    private companion object {
        const val MAX_GRAPH_BONUS = 20
        val LEVEL_WEIGHTS = mapOf(
            TechniqueLevel.EASY to 1,
            TechniqueLevel.MEDIUM to 8,
            TechniqueLevel.HARD to 24,
            TechniqueLevel.BRUTAL to 64,
        )
    }
}
