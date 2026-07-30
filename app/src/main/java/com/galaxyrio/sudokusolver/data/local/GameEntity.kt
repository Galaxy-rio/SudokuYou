package com.galaxyrio.sudokusolver.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudoku: Sudoku,
    val timeSpent: Long = 0,
    val lastPlayed: Long = System.currentTimeMillis(),
)

