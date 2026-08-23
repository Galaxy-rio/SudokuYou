package com.galaxyrio.sudokusolver.navigation

import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

@Serializable
data class NewGameDestination(
    val difficulty: Difficulty,
)

@Serializable
data class SavedGameDestination(
    val gameId: Long,
    val useContainerTransform: Boolean = true,
)

@Serializable
data class SettingsDestination(
    val category: SettingsCategory,
)
