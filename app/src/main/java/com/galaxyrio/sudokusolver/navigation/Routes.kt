package com.galaxyrio.sudokusolver.navigation

import com.galaxyrio.sudokusolver.ui.screens.play.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory

object Routes {
    const val HOME = "home"
    const val INFO = "info"
    const val GAME_ROUTE = "game/{difficulty}?gameId={gameId}"
    const val SETTINGS_ROUTE = "settings/{category}"

    // Argument keys
    const val ARG_DIFFICULTY = "difficulty"
    const val ARG_GAME_ID = "gameId"
    const val ARG_CATEGORY = "category"

    fun game(difficulty: Difficulty, gameId: Long? = null): String {
        val id = gameId ?: -1L
        return "game/${difficulty.name}?gameId=$id"
    }

    fun settings(category: SettingsCategory): String {
        return "settings/${category.name}"
    }
}
