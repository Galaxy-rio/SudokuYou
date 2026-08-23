package com.galaxyrio.sudokusolver.data

import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val savedGames: Flow<List<SavedGame>>

    suspend fun getGame(id: Long): SavedGame?

    suspend fun createGame(difficulty: Difficulty): SavedGame

    /** Returns null when the imported grid is invalid or does not have exactly one solution. */
    suspend fun createImportedGame(sudoku: Sudoku): SavedGame?

    suspend fun saveGame(game: SavedGame): Long

    suspend fun deleteGame(id: Long)

    suspend fun deleteGames(ids: Set<Long>)
}
