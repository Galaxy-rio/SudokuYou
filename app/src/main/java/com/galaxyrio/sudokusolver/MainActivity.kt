package com.galaxyrio.sudokusolver

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.galaxyrio.sudokusolver.ui.SudokuApp
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SudokuApplication).container
        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.factory(appContainer.settingsRepository),
        )[SettingsViewModel::class.java]

        setContent {
            SudokuApp(
                appContainer = appContainer,
                settingsViewModel = settingsViewModel,
            )
        }
    }
}
