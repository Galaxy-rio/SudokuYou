package com.galaxyrio.sudokusolver.ui.screens.play

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.galaxyrio.sudokusolver.gamedata.AppDatabase
import com.galaxyrio.sudokusolver.gamedata.SudokuEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayViewModel(application: Application) : AndroidViewModel(application) {
    private val sudokuDao = AppDatabase.Companion.getDatabase(application).sudokuDao()

    val savedGames: StateFlow<List<SavedGame>> = sudokuDao.getAllGames()
        .map { entities ->
            entities.map { entity ->
                SavedGame(
                    id = entity.id.toString(),
                    date = formatLastPlayed(entity.lastPlayed),
                    difficulty = entity.difficulty,
                    completionPercentage = calculateCompletion(entity),
                    emptyRemains = 81 - entity.sudoku.cells.count { it.value != 0 },
                    timeSpentSeconds = entity.timeSpent,
                    board = entity.sudoku.cells.map { it.value }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun formatLastPlayed(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun calculateCompletion(entity: SudokuEntity): Int {
        val cells = entity.sudoku.cells
        val solvedCount = cells.count { it.value != 0 }
        return (solvedCount * 100) / 81
    }

    fun deleteGames(games: List<SavedGame>) {
        viewModelScope.launch {
            games.forEach { game ->
                sudokuDao.deleteGame(game.id.toLong())
            }
        }
    }
}