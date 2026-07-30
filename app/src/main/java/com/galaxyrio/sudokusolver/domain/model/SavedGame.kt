package com.galaxyrio.sudokusolver.domain.model

data class SavedGame(
    val id: Long = 0,
    val difficulty: Difficulty,
    val sudoku: Sudoku,
    val timeSpentSeconds: Long = 0,
    val lastPlayedEpochMillis: Long = System.currentTimeMillis(),
)
