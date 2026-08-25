package com.galaxyrio.sudokusolver.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.data.settings.ThemeMode
import com.galaxyrio.sudokusolver.navigation.AppNavHost
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme

@Composable
fun SudokuApp(
    appContainer: AppContainer,
    settingsViewModel: SettingsViewModel,
) {
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val useDarkTheme = when (settingsUiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    val navController = rememberNavController()

    SideEffect {
        view.context.findActivity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    SudokuYouTheme(
        darkTheme = useDarkTheme,
        dynamicColor = settingsUiState.useDynamicColors,
        amoled = settingsUiState.isAmoled,
        colorSeed = settingsUiState.themeColor,
        paletteStyle = settingsUiState.paletteStyle,
    ) {
        AppNavHost(
            navController = navController,
            appContainer = appContainer,
            settingsUiState = settingsUiState,
            settingsViewModel = settingsViewModel,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
