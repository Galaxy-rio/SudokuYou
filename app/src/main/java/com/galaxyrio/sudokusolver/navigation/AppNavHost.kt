package com.galaxyrio.sudokusolver.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.galaxyrio.sudokusolver.HomeScreen
import com.galaxyrio.sudokusolver.ui.screens.info.InfoScreen
import com.galaxyrio.sudokusolver.ui.screens.play.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AboutSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppearanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AssistanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.FilesSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.GameSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.LanguageSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.sudokugame.SudokuGameScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val motionScheme = MaterialTheme.motionScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { it }
                    ) + fadeIn(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        initialAlpha = 1f
                    )
                },

                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { -it / 3 }
                    ) + fadeOut(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        targetAlpha = 1f
                    )
                },

                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { -it / 3 }
                    ) + fadeIn(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        initialAlpha = 1f
                    )
                },

                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { it }
                    ) + fadeOut(
                        animationSpec = motionScheme.defaultEffectsSpec(),
                        targetAlpha = 0f
                    )
                },

                sizeTransform = {
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { _, _ ->
                            motionScheme.defaultSpatialSpec()
                        }
                    )
                }
            ) {
                composable(
                    route = Routes.HOME,
                ) {
                    HomeScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onStartGame = { difficulty ->
                            navController.navigate(Routes.game(difficulty))
                        },
                        onContinueGame = { gameId ->
                            navController.navigate(Routes.game(Difficulty.MEDIUM, gameId.toLongOrNull()))
                        },
                        onNavigateToSettings = { category ->
                            navController.navigate(Routes.settings(category))
                        }
                    )
                }

                composable(
                    route = Routes.GAME_ROUTE,
                    arguments = listOf(
                        navArgument(Routes.ARG_DIFFICULTY) { type = NavType.StringType },
                        navArgument(Routes.ARG_GAME_ID) {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val difficultyStr =
                        backStackEntry.arguments?.getString(Routes.ARG_DIFFICULTY) ?: "MEDIUM"
                    val gameIdArg = backStackEntry.arguments?.getLong(Routes.ARG_GAME_ID) ?: -1L

                    val difficulty = try {
                        Difficulty.valueOf(difficultyStr)
                    } catch (_: Exception) {
                        Difficulty.MEDIUM
                    }
                    val gameId = if (gameIdArg == -1L) null else gameIdArg

                    SudokuGameScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        difficulty = difficulty,
                        gameId = gameId,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                        viewModel = settingsViewModel
                    )
                }

                composable(
                    route = Routes.SETTINGS_ROUTE,
                    arguments = listOf(navArgument(Routes.ARG_CATEGORY) { type = NavType.StringType }),

                    ) { backStackEntry ->
                    val categoryStr = backStackEntry.arguments?.getString(Routes.ARG_CATEGORY)
                    val category = SettingsCategory.entries.find { it.name == categoryStr }

                    val onBack: () -> Unit = { navController.popBackStack() }
                    val modifier = Modifier.fillMaxSize()

                    if (category != null) {
                        // Determine specific settings screen
                        when (category) {
                            SettingsCategory.APPEARANCE -> AppearanceSettingsScreen(
                                onBack,
                                settingsViewModel,
                                modifier
                            )

                            SettingsCategory.GAME -> GameSettingsScreen(onBack, modifier)
                            SettingsCategory.ASSISTANCE -> AssistanceSettingsScreen(
                                onBack,
                                modifier
                            )

                            SettingsCategory.FILES -> FilesSettingsScreen(onBack, modifier)
                            SettingsCategory.LANGUAGE -> LanguageSettingsScreen(onBack, modifier)
                            SettingsCategory.ABOUT -> AboutSettingsScreen(onBack, modifier)
                        }
                    }
                }
                composable(
                    route = Routes.INFO,
                ) {
                    InfoScreen(
                        modifier = Modifier.fillMaxSize()
                    )

                }


            }
        }
    }
}
