package com.galaxyrio.sudokusolver.data

import android.content.Context
import com.galaxyrio.sudokusolver.data.local.AppDatabase
import com.galaxyrio.sudokusolver.data.settings.PreferencesSettingsRepository
import com.galaxyrio.sudokusolver.data.settings.SettingsRepository

class AppContainer(context: Context) {
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val gameRepository: GameRepository by lazy {
        OfflineGameRepository(database.gameDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        PreferencesSettingsRepository(context)
    }
}
