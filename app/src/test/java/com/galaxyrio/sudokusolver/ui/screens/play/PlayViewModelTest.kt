package com.galaxyrio.sudokusolver.ui.screens.play

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayViewModelTest {

    @Test
    fun savedGamesAreMappedToUiSummaries() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var collectionJob: Job? = null
        val owner = TestViewModelStoreOwner()
        try {
            val game = SavedGame(
                id = 3,
                difficulty = Difficulty.HARD,
                sudoku = Sudoku().setCell(0, 0, 5),
                timeSpentSeconds = 42,
            )
            val viewModel = ViewModelProvider(
                owner,
                PlayViewModel.factory(FakeGameRepository(game)),
            )[PlayViewModel::class.java]
            collectionJob = backgroundScope.launch {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            val summary = viewModel.uiState.value.savedGames.single()
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(game.id, summary.id)
            assertEquals(Difficulty.HARD, summary.difficulty)
            assertEquals(80, summary.emptyCells)
            assertEquals(42, summary.timeSpentSeconds)
        } finally {
            collectionJob?.cancelAndJoin()
            owner.viewModelStore.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun deleteFailureIsExposedAsActionError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var collectionJob: Job? = null
        val owner = TestViewModelStoreOwner()
        try {
            val repository = FakeGameRepository(
                SavedGame(id = 9, difficulty = Difficulty.EASY, sudoku = Sudoku())
            ).apply {
                failDeletes = true
            }
            val viewModel = ViewModelProvider(
                owner,
                PlayViewModel.factory(repository),
            )[PlayViewModel::class.java]
            collectionJob = backgroundScope.launch {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            viewModel.deleteGames(setOf(9))
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasActionError)
            viewModel.clearActionError()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.hasActionError)
        } finally {
            collectionJob?.cancelAndJoin()
            owner.viewModelStore.clear()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private class FakeGameRepository(
        initialGame: SavedGame? = null,
    ) : GameRepository {
        private val games = MutableStateFlow(listOfNotNull(initialGame))
        override val savedGames: Flow<List<SavedGame>> = games
        var failDeletes = false

        override suspend fun getGame(id: Long): SavedGame? =
            games.value.firstOrNull { it.id == id }

        override suspend fun createGame(difficulty: Difficulty): SavedGame =
            SavedGame(id = 1, difficulty = difficulty, sudoku = Sudoku())

        override suspend fun saveGame(game: SavedGame): Long {
            games.value = games.value.filterNot { it.id == game.id } + game
            return game.id
        }

        override suspend fun deleteGame(id: Long) {
            deleteGames(setOf(id))
        }

        override suspend fun deleteGames(ids: Set<Long>) {
            if (failDeletes) error("Simulated delete failure")
            games.value = games.value.filterNot { it.id in ids }
        }
    }

    private class TestViewModelStoreOwner : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
}
