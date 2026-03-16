package com.galaxyrio.sudokusolver


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider


import androidx.navigation.compose.rememberNavController

import com.galaxyrio.sudokusolver.data.ThemeMode
import com.galaxyrio.sudokusolver.navigation.AppNavHost
import com.galaxyrio.sudokusolver.ui.screens.info.InfoScreen
import com.galaxyrio.sudokusolver.ui.screens.play.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.play.PlayMenuScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel

import com.galaxyrio.sudokusolver.ui.theme.SudokuSolverTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val themeColor by settingsViewModel.themeColor.collectAsState()
            val isAmoled by settingsViewModel.isAmoled.collectAsState()
            val useDynamicColors by settingsViewModel.useDynamicColors.collectAsState()
            val paletteStyle by settingsViewModel.paletteStyle.collectAsState()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            val navController = rememberNavController()
            SudokuSolverTheme(
                darkTheme = darkTheme,
                dynamicColor = useDynamicColors,
                amoled = isAmoled,
                colorSeed = themeColor,
                paletteStyle = paletteStyle
            ) {
                AppNavHost(navController, settingsViewModel)
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (String) -> Unit,
    onNavigateToSettings: (SettingsCategory) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PLAY) }
    var gameDifficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    val motionScheme = MaterialTheme.motionScheme

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        val modifier = Modifier.fillMaxSize()
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            AnimatedContent(
                targetState = currentDestination,
                label = "main_nav_transition",
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { it }
                        ) + fadeIn(
                            animationSpec = motionScheme.defaultEffectsSpec()
                        )) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = motionScheme.fastSpatialSpec(),
                                    targetOffsetX = { -it }
                                ) + fadeOut(
                                    animationSpec = motionScheme.fastEffectsSpec()
                                )
                                )
                    } else {
                        (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { -it }
                        ) + fadeIn(
                            animationSpec = motionScheme.defaultEffectsSpec()
                        )) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = motionScheme.fastSpatialSpec(),
                                    targetOffsetX = { it }
                                ) + fadeOut(
                                    animationSpec = motionScheme.fastEffectsSpec()
                                )
                                )
                    }
                }
            ) { targetScreen ->
                when (targetScreen) {
                    AppDestinations.INFO -> InfoScreen(modifier)
                    AppDestinations.PLAY -> PlayMenuScreen(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = modifier,
                        initialDifficulty = gameDifficulty,
                        onStartGame = { selectedDifficulty ->
                            gameDifficulty = selectedDifficulty
                            onStartGame(selectedDifficulty)
                        },
                        onContinueGame = { gameId ->
                            onContinueGame(gameId)
                        }
                    )

                    AppDestinations.SETTINGS -> SettingsScreen(
                        onNavigateTo = onNavigateToSettings,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    INFO("Info", Icons.Default.Info),
    PLAY("Play", Icons.Default.SportsEsports),
    SETTINGS("Setting", Icons.Default.Settings),
}
