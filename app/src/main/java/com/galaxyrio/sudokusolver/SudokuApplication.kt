package com.galaxyrio.sudokusolver

import android.app.Application
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.data.crash.LocalCrashHandler

class SudokuApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        LocalCrashHandler.install(
            context = this,
            repository = container.crashHistoryRepository,
        )
    }
}
