package com.galaxyrio.sudokusolver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastPlayed DESC")
    fun observeAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGame(id: Long): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGame(game: GameEntity): Long

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGame(id: Long)

    @Query("DELETE FROM games WHERE id IN (:ids)")
    suspend fun deleteGames(ids: Set<Long>)

    @Query("SELECT * FROM game_statistics")
    fun observeStatistics(): Flow<List<GameStatisticsEntity>>

    @Query("SELECT * FROM game_statistics WHERE difficulty = :difficulty")
    suspend fun getStatistics(difficulty: Difficulty): GameStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatistics(statistics: GameStatisticsEntity)

    @Query("SELECT generation FROM statistics_metadata WHERE id = 0")
    suspend fun getStatisticsGeneration(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatisticsMetadata(metadata: StatisticsMetadataEntity)

    @Query("DELETE FROM game_statistics")
    suspend fun deleteAllStatistics()

    @Transaction
    suspend fun upsertGameWithStatistics(game: GameEntity): Long {
        val generation = getStatisticsGeneration() ?: 0
        val previous = game.id.takeIf { it != 0L }?.let { getGame(it) }
        val current = game.copy(statisticsGeneration = generation)
        val id = upsertGame(current)
        val persisted = current.copy(id = id)
        val statistics = getStatistics(persisted.difficulty) ?: GameStatisticsEntity(
            difficulty = persisted.difficulty,
        )
        upsertStatistics(
            statistics.recordGameSave(
                previous = previous,
                current = persisted,
                currentGeneration = generation,
            )
        )
        return id
    }

    @Transaction
    suspend fun clearStatistics() {
        val nextGeneration = (getStatisticsGeneration() ?: 0) + 1
        deleteAllStatistics()
        upsertStatisticsMetadata(
            StatisticsMetadataEntity(generation = nextGeneration)
        )
    }
}
