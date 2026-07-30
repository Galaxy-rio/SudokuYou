package com.galaxyrio.sudokusolver.ui.screens.game

import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
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
    }
}
