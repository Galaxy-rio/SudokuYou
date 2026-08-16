package com.galaxyrio.sudokusolver.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku

@Entity(
    tableName = "puzzle_inventory",
    indices = [
        Index(value = ["difficulty", "generatorVersion"]),
        Index(value = ["fingerprint"], unique = true),
    ],
)
data class PuzzleInventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudoku: Sudoku,
    val fingerprint: String,
    val generatorVersion: Int,
    val createdAt: Long = System.currentTimeMillis(),
)
