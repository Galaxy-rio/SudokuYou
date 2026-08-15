package com.galaxyrio.sudokusolver.domain.solver

interface TechniqueDetector {
    val technique: TechniqueId

    /** Returns the first deterministic deduction found by this technique. */
    fun find(state: SolverState): SolveStep?
}
