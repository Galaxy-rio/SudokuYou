package com.galaxyrio.sudokusolver.data

import com.galaxyrio.sudokusolver.data.local.GameDao
import com.galaxyrio.sudokusolver.data.local.GameEntity
import com.galaxyrio.sudokusolver.data.local.GameStatisticsEntity
import com.galaxyrio.sudokusolver.data.local.StatisticsMetadataEntity
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineGameRepositoryTest {
    @Test
    fun createGameConsumesInventoryBeforeRequestingRefill() = runTest {
        val cachedPuzzle = Sudoku.fromGridString(PUZZLE)
        val inventory = FakePuzzleInventory(cachedPuzzle)
        val repository = OfflineGameRepository(
            gameDao = FakeGameDao(),
            puzzleInventory = inventory,
        )

        val game = repository.createGame(Difficulty.HARD)

        assertEquals(Difficulty.HARD, game.difficulty)
        assertEquals(cachedPuzzle, game.sudoku)
        assertEquals(SOLUTION, game.solution?.digits?.joinToString(separator = ""))
        assertEquals(1L, game.id)
        assertEquals(listOf(Difficulty.HARD), inventory.takenDifficulties)
        assertEquals(listOf(Difficulty.HARD), inventory.refillRequests)
    }

    @Test
    fun openingLegacyGameCalculatesAndPersistsItsSolutionOnce() = runTest {
        val legacyGame = GameEntity(
            id = 9,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
            solution = null,
        )
        val gameDao = FakeGameDao(legacyGame)
        val repository = OfflineGameRepository(gameDao = gameDao)

        val openedGame = repository.getGame(legacyGame.id)

        assertEquals(SOLUTION, openedGame?.solution?.digits?.joinToString(separator = ""))
        assertEquals(
            SOLUTION,
            gameDao.storedGame(legacyGame.id)?.solution?.digits?.joinToString(separator = ""),
        )
    }

    @Test
    fun clearingStatisticsDoesNotDeleteSavedGames() = runTest {
        val gameDao = FakeGameDao()
        val repository = OfflineGameRepository(
            gameDao = gameDao,
            puzzleInventory = FakePuzzleInventory(Sudoku.fromGridString(PUZZLE)),
        )
        val game = repository.createGame(Difficulty.EASY)

        assertEquals(1L, repository.statistics.first().single().gamesStarted)

        repository.clearStatistics()

        assertEquals(0, repository.statistics.first().size)
        assertEquals(game.id, repository.getGame(game.id)?.id)
    }

    private class FakePuzzleInventory(
        private var cachedPuzzle: Sudoku?,
    ) : PuzzleInventory {
        val takenDifficulties = mutableListOf<Difficulty>()
        val refillRequests = mutableListOf<Difficulty>()

        override suspend fun take(difficulty: Difficulty): Sudoku? {
            takenDifficulties += difficulty
            return cachedPuzzle.also { cachedPuzzle = null }
        }

        override fun requestRefill(priority: Difficulty) {
            refillRequests += priority
        }
    }

    private class FakeGameDao(initialGame: GameEntity? = null) : GameDao {
        private val games = MutableStateFlow(listOfNotNull(initialGame))
        private val statistics = MutableStateFlow(emptyList<GameStatisticsEntity>())
        private var statisticsGeneration = 0L
        private var nextId = (initialGame?.id ?: 0L) + 1L

        fun storedGame(id: Long): GameEntity? = games.value.firstOrNull { it.id == id }

        override fun observeAllGames(): Flow<List<GameEntity>> = games

        override suspend fun getGame(id: Long): GameEntity? =
            games.value.firstOrNull { it.id == id }

        override suspend fun upsertGame(game: GameEntity): Long {
            val id = game.id.takeIf { it != 0L } ?: nextId++
            games.value = games.value.filterNot { it.id == id } + game.copy(id = id)
            return id
        }

        override suspend fun deleteGame(id: Long) {
            games.value = games.value.filterNot { it.id == id }
        }

        override suspend fun deleteGames(ids: Set<Long>) {
            games.value = games.value.filterNot { it.id in ids }
        }

        override fun observeStatistics(): Flow<List<GameStatisticsEntity>> = statistics

        override suspend fun getStatistics(difficulty: Difficulty): GameStatisticsEntity? =
            statistics.value.firstOrNull { it.difficulty == difficulty }

        override suspend fun upsertStatistics(statistics: GameStatisticsEntity) {
            this.statistics.value = this.statistics.value
                .filterNot { it.difficulty == statistics.difficulty } + statistics
        }

        override suspend fun getStatisticsGeneration(): Long = statisticsGeneration

        override suspend fun upsertStatisticsMetadata(metadata: StatisticsMetadataEntity) {
            statisticsGeneration = metadata.generation
        }

        override suspend fun deleteAllStatistics() {
            statistics.value = emptyList()
        }
    }

    private companion object {
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
