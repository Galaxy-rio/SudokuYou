package com.galaxyrio.sudokusolver.data

import com.galaxyrio.sudokusolver.data.local.GameDao
import com.galaxyrio.sudokusolver.data.local.GameEntity
import com.galaxyrio.sudokusolver.domain.game.PuzzleSolutionResolver
import com.galaxyrio.sudokusolver.domain.game.SudokuGenerator
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OfflineGameRepository(
    private val gameDao: GameDao,
    private val generator: SudokuGenerator = SudokuGenerator(),
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val puzzleInventory: PuzzleInventory = EmptyPuzzleInventory,
) : GameRepository {
    private val solutionResolver = PuzzleSolutionResolver()

    override val savedGames: Flow<List<SavedGame>> =
        gameDao.observeAllGames().map { games -> games.map(GameEntity::asExternalModel) }

    override suspend fun getGame(id: Long): SavedGame? {
        val storedGame = gameDao.getGame(id) ?: return null
        if (storedGame.solution != null) return storedGame.asExternalModel()

        val solution = withContext(computationDispatcher) {
            solutionResolver.resolve(storedGame.sudoku)
        } ?: return storedGame.asExternalModel()
        val upgradedGame = storedGame.copy(solution = solution)
        gameDao.upsertGame(upgradedGame)
        return upgradedGame.asExternalModel()
    }

    override suspend fun createGame(difficulty: Difficulty): SavedGame {
        val sudoku = puzzleInventory.take(difficulty) ?: withContext(computationDispatcher) {
            generator.generate(difficulty)
        }
        val solution = withContext(computationDispatcher) {
            requireNotNull(solutionResolver.resolve(sudoku)) {
                "Generated puzzle must have exactly one solution."
            }
        }
        val game = SavedGame(
            difficulty = difficulty,
            sudoku = sudoku,
            solution = solution,
        )
        return game.copy(id = saveGame(game)).also {
            puzzleInventory.requestRefill(priority = difficulty)
        }
    }

    override suspend fun saveGame(game: SavedGame): Long =
        gameDao.upsertGame(game.asEntity())

    override suspend fun deleteGame(id: Long) {
        gameDao.deleteGame(id)
    }

    override suspend fun deleteGames(ids: Set<Long>) {
        if (ids.isNotEmpty()) gameDao.deleteGames(ids)
    }
}

private fun GameEntity.asExternalModel(): SavedGame = SavedGame(
    id = id,
    difficulty = difficulty,
    sudoku = sudoku,
    solution = solution,
    advancedNotes = advancedNotes ?: AdvancedNotes(),
    timeSpentSeconds = timeSpent,
    lastPlayedEpochMillis = lastPlayed,
)

private fun SavedGame.asEntity(): GameEntity = GameEntity(
    id = id,
    difficulty = difficulty,
    sudoku = sudoku,
    solution = solution,
    advancedNotes = advancedNotes,
    timeSpent = timeSpentSeconds,
    lastPlayed = lastPlayedEpochMillis,
)
