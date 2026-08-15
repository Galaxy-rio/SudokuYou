package com.galaxyrio.sudokusolver.ui.screens.game

import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CandidateElimination
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HumanSolver
import com.galaxyrio.sudokusolver.domain.solver.Placement
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.domain.solver.TechniqueDetector
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @Test
    fun newGameUsesRequestedDifficulty() = runViewModelTest {
        val repository = FakeGameRepository()
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = Difficulty.HARD,
            savedGameId = null,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Difficulty.HARD, viewModel.uiState.value.difficulty)
        assertEquals(Difficulty.HARD, repository.createdDifficulty)
    }

    @Test
    fun savedGameKeepsItsPersistedDifficulty() = runViewModelTest {
        val savedGame = SavedGame(
            id = 7,
            difficulty = Difficulty.HARD,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)

        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Difficulty.HARD, viewModel.uiState.value.difficulty)
        assertEquals(savedGame.id, viewModel.uiState.value.gameId)
    }

    @Test
    fun missingSavedGameProducesLoadError() = runViewModelTest {
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(),
            newGameDifficulty = null,
            savedGameId = 404,
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasLoadError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun enteringNumberUpdatesAndPersistsBoard() = runViewModelTest {
        val savedGame = SavedGame(
            id = 11,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.sudoku.getCell(0, 0).value)
        assertEquals(5, repository.savedGamesHistory.last().sudoku.getCell(0, 0).value)
    }

    @Test
    fun persistenceFailureIsExposedWithoutCrashing() = runViewModelTest {
        val savedGame = SavedGame(
            id = 12,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame).apply {
            failSaves = true
        }
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertEquals(5, viewModel.uiState.value.sudoku.getCell(0, 0).value)
    }

    @Test
    fun completingPuzzleDeletesPersistedGame() = runViewModelTest {
        val cells = SOLUTION.mapIndexed { index, character ->
            if (index == 0) {
                Cell()
            } else {
                Cell(value = character.digitToInt(), isFixed = true)
            }
        }
        val savedGame = SavedGame(
            id = 15,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)
        assertNull(viewModel.uiState.value.gameId)
        assertEquals(listOf(savedGame.id), repository.deletedIds)
    }

    @Test
    fun activeTimerIsPersistedWhenPaused() = runViewModelTest {
        val savedGame = SavedGame(
            id = 20,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onResume()
        advanceTimeBy(3_000)
        runCurrent()
        viewModel.onPause()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.timeSpentSeconds)
        assertEquals(3, repository.savedGamesHistory.last().timeSpentSeconds)
    }

    @Test
    fun advancedModeCanBeChangedWithoutChangingTheBoard() = runViewModelTest {
        val savedGame = SavedGame(
            id = 21,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        val originalBoard = viewModel.uiState.value.sudoku
        viewModel.setAdvancedMode(true)

        assertTrue(viewModel.uiState.value.isAdvancedMode)
        assertEquals(originalBoard, viewModel.uiState.value.sudoku)
    }

    @Test
    fun hintTraceSupportsArbitraryNavigationAndAppliesOnlyTheNextStep() = runViewModelTest {
        val savedGame = SavedGame(
            id = 22,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        assertTrue(viewModel.uiState.value.isHintLoading)
        advanceUntilIdle()

        val trace = requireNotNull(viewModel.uiState.value.hintTrace)
        assertTrue(trace.steps.size > 1)
        viewModel.selectHintStep(Int.MAX_VALUE)
        assertEquals(trace.steps.lastIndex, viewModel.uiState.value.selectedHintStepIndex)

        val expectedNextState = trace.stateAfterStep(0)
        val firstStep = trace.steps.first()
        assertTrue(viewModel.applyNextHintStep())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.hintTrace)
        firstStep.placements.forEach { placement ->
            assertEquals(
                expectedNextState.valueAt(placement.cell),
                viewModel.uiState.value.sudoku
                    .getCell(placement.cell.row, placement.cell.col)
                    .value,
            )
        }
        firstStep.eliminations.forEach { elimination ->
            val candidate = elimination.candidate
            assertFalse(
                candidate.digit in viewModel.uiState.value.sudoku
                    .getCell(candidate.cell.row, candidate.cell.col)
                    .candidates
            )
        }
    }

    @Test
    fun appliedCandidateEliminationIsRetainedForTheNextHint() = runViewModelTest {
        val target = CellRef(0, 0)
        val stagedDetector = object : TechniqueDetector {
            override val technique = TechniqueId.NAKED_PAIR

            override fun find(state: SolverState): SolveStep? = when {
                state.valueAt(target) != 0 -> null
                state.hasCandidate(target, 1) -> SolveStep(
                    technique = TechniqueId.NAKED_PAIR,
                    eliminations = listOf(
                        CandidateElimination(CandidateRef(target, 1))
                    ),
                )
                else -> SolveStep(
                    technique = TechniqueId.NAKED_SINGLE,
                    placements = listOf(Placement(target, 2)),
                )
            }
        }
        val savedGame = SavedGame(
            id = 23,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            humanSolver = HumanSolver(listOf(stagedDetector)),
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertEquals(
            TechniqueId.NAKED_PAIR,
            viewModel.uiState.value.hintTrace?.steps?.first()?.technique,
        )
        assertTrue(viewModel.applyNextHintStep())

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        val nextStep = viewModel.uiState.value.hintTrace?.steps?.first()
        assertEquals(TechniqueId.NAKED_SINGLE, nextStep?.technique)
        assertEquals(Placement(target, 2), nextStep?.placements?.single())
    }

    @Test
    fun consecutiveHintEliminationsDoNotRestoreEarlierCandidates() = runViewModelTest {
        val target = CellRef(0, 0)
        val stagedDetector = object : TechniqueDetector {
            override val technique = TechniqueId.NAKED_PAIR

            override fun find(state: SolverState): SolveStep? = when {
                state.hasCandidate(target, 1) -> eliminationStep(target, 1)
                state.hasCandidate(target, 3) -> eliminationStep(target, 3)
                else -> null
            }

            private fun eliminationStep(cell: CellRef, digit: Int) = SolveStep(
                technique = TechniqueId.NAKED_PAIR,
                eliminations = listOf(CandidateElimination(CandidateRef(cell, digit))),
            )
        }
        val savedGame = SavedGame(
            id = 24,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            humanSolver = HumanSolver(listOf(stagedDetector)),
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertTrue(viewModel.applyNextHintStep())

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertEquals(
            CandidateRef(target, 3),
            viewModel.uiState.value.hintTrace
                ?.steps
                ?.first()
                ?.eliminations
                ?.single()
                ?.candidate,
        )
        assertTrue(viewModel.applyNextHintStep())

        val displayedCandidates = viewModel.uiState.value.sudoku
            .getCell(target.row, target.col)
            .candidates
        assertFalse(1 in displayedCandidates)
        assertFalse(3 in displayedCandidates)
    }

    private fun runViewModelTest(
        block: suspend TestScope.() -> Unit,
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeGameRepository(
        initialGame: SavedGame? = null,
    ) : GameRepository {
        private var game = initialGame
        private val gamesFlow = MutableStateFlow(listOfNotNull(initialGame))

        override val savedGames: Flow<List<SavedGame>> = gamesFlow
        val savedGamesHistory = mutableListOf<SavedGame>()
        val deletedIds = mutableListOf<Long>()
        var createdDifficulty: Difficulty? = null
        var failSaves: Boolean = false

        override suspend fun getGame(id: Long): SavedGame? =
            game?.takeIf { it.id == id }

        override suspend fun createGame(difficulty: Difficulty): SavedGame {
            createdDifficulty = difficulty
            return SavedGame(
                id = 1,
                difficulty = difficulty,
                sudoku = Sudoku(),
            ).also {
                game = it
                gamesFlow.value = listOf(it)
            }
        }

        override suspend fun saveGame(game: SavedGame): Long {
            if (failSaves) error("Simulated persistence failure")
            this.game = game
            savedGamesHistory += game
            gamesFlow.value = listOf(game)
            return game.id
        }

        override suspend fun deleteGame(id: Long) {
            deletedIds += id
            if (game?.id == id) game = null
            gamesFlow.value = listOfNotNull(game)
        }

        override suspend fun deleteGames(ids: Set<Long>) {
            ids.forEach { deleteGame(it) }
        }
    }

    private companion object {
        const val SOLUTION =
            "534678912" +
                "672195348" +
                "198342567" +
                "859761423" +
                "426853791" +
                "713924856" +
                "961537284" +
                "287419635" +
                "345286179"

        const val PUZZLE =
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79"
    }
}
