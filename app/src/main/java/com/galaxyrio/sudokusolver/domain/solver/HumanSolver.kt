package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku

class HumanSolver(
    private val detectors: List<TechniqueDetector> = defaultDetectors(),
) {
    init {
        require(detectors.isNotEmpty())
    }

    fun nextStep(state: SolverState): SolveStep? {
        if (!state.isValid() || state.isSolved()) return null
        return detectors.firstNotNullOfOrNull { detector -> detector.find(state) }
    }

    fun solveTrace(
        sudoku: Sudoku,
        maxSteps: Int = DEFAULT_MAX_STEPS,
    ): SolveTrace = solveTrace(SolverState.fromSudoku(sudoku), maxSteps)

    fun solveTrace(
        initialState: SolverState,
        maxSteps: Int = DEFAULT_MAX_STEPS,
    ): SolveTrace {
        require(maxSteps > 0)

        val steps = mutableListOf<SolveStep>()
        val states = mutableListOf(initialState)
        var currentState = initialState

        if (!currentState.isValid()) {
            return SolveTrace(initialState, steps, states, SolveTraceStatus.INVALID)
        }
        if (currentState.isSolved()) {
            return SolveTrace(initialState, steps, states, SolveTraceStatus.SOLVED)
        }

        repeat(maxSteps) {
            val step = nextStep(currentState)
                ?: return SolveTrace(initialState, steps, states, SolveTraceStatus.STALLED)
            val nextState = currentState.apply(step)
            if (nextState == currentState || !nextState.isValid()) {
                return SolveTrace(initialState, steps, states, SolveTraceStatus.INVALID)
            }

            steps += step
            states += nextState
            currentState = nextState
            if (currentState.isSolved()) {
                return SolveTrace(initialState, steps, states, SolveTraceStatus.SOLVED)
            }
        }

        return SolveTrace(initialState, steps, states, SolveTraceStatus.STALLED)
    }

    companion object {
        private const val DEFAULT_MAX_STEPS = 1_000

        /**
         * Deterministic, easiest-first order. Technique tiers follow the agreed Sudoku Coach
         * boundaries so the same trace can also rate generated puzzles.
         */
        fun defaultDetectors(): List<TechniqueDetector> = listOf(
            LastDigitDetector(),
            NakedSingleDetector(),
            HiddenSingleDetector(),
            NakedSubsetDetector(subsetSize = 2),
            HiddenSubsetDetector(subsetSize = 2),
            PointingCandidatesDetector(),
            ClaimingCandidatesDetector(),
            NakedSubsetDetector(subsetSize = 3),
            HiddenSubsetDetector(subsetSize = 3),
            NakedSubsetDetector(subsetSize = 4),
            HiddenSubsetDetector(subsetSize = 4),
            BasicFishDetector(size = 2),
            SkyscraperDetector(),
            TwoStringKiteDetector(),
            TurbotCraneDetector(),
            XYWingDetector(),
            XYZWingDetector(),
            RemotePairDetector(),
            ChuteRemotePairDetector(),
            SimpleColoringDetector(),
            EmptyRectangleDetector(),
            BasicFishDetector(size = 3),
            UniqueRectangleDetector(),
            BasicFishDetector(size = 4),
            BugPlusOneDetector(),
            XChainDetector(),
            WXYZWingDetector(),
            BasicFishDetector(size = 5),
            BasicFishDetector(size = 6),
            BasicFishDetector(size = 7),
            XYChainDetector(),
            AicDetector(),
            ForcingChainDetector(),
        )
    }
}
