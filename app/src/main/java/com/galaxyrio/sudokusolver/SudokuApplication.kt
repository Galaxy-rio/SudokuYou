package com.galaxyrio.sudokusolver

import android.app.Application
import com.galaxyrio.sudokusolver.data.AppContainer

class SudokuApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(this)
    }
}
