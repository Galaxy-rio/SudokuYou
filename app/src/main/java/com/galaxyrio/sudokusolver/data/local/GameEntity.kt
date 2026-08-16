package com.galaxyrio.sudokusolver.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudoku: Sudoku,
    val solution: SudokuSolution? = null,
    val advancedNotes: AdvancedNotes? = null,
    val timeSpent: Long = 0,
    val lastPlayed: Long = System.currentTimeMillis(),
)

