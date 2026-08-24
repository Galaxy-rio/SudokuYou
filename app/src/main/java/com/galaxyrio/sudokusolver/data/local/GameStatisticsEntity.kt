package com.galaxyrio.sudokusolver.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.domain.model.Difficulty

@Entity(tableName = "game_statistics")
data class GameStatisticsEntity(
    @PrimaryKey
    val difficulty: Difficulty,
    val totalPlayTimeSeconds: Long = 0,
    val gamesStarted: Long = 0,
    val gamesCompleted: Long = 0,
    val totalCompletionTimeSeconds: Long = 0,
    val bestCompletionTimeSeconds: Long? = null,
)

@Entity(tableName = "statistics_metadata")
data class StatisticsMetadataEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val generation: Long = 0,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
