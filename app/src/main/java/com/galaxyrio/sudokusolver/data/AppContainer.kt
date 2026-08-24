package com.galaxyrio.sudokusolver.data

import android.content.Context
import com.galaxyrio.sudokusolver.data.crash.CrashHistoryRepository
import com.galaxyrio.sudokusolver.data.crash.FileCrashHistoryRepository
import com.galaxyrio.sudokusolver.data.local.AppDatabase
import com.galaxyrio.sudokusolver.data.licenses.LicensesRepository
import com.galaxyrio.sudokusolver.data.settings.PreferencesSettingsRepository
import com.galaxyrio.sudokusolver.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    private val puzzleInventory: PuzzleInventory by lazy {
        LocalPuzzleInventory(
            puzzleDao = database.puzzleInventoryDao(),
            applicationScope = applicationScope,
        )
    }

    val gameRepository: GameRepository by lazy {
        OfflineGameRepository(
            gameDao = database.gameDao(),
            puzzleInventory = puzzleInventory,
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        PreferencesSettingsRepository(context)
    }

    val licensesRepository: LicensesRepository by lazy {
        LicensesRepository(context)
    }

    val crashHistoryRepository: CrashHistoryRepository by lazy {
        FileCrashHistoryRepository(context)
    }
}
