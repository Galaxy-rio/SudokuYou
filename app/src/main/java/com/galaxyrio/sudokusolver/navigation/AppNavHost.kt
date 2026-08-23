package com.galaxyrio.sudokusolver.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.screens.game.GameRoute
import com.galaxyrio.sudokusolver.ui.screens.game.GameViewModel
import com.galaxyrio.sudokusolver.ui.screens.home.HomeScreen
import com.galaxyrio.sudokusolver.ui.screens.play.PlayViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AboutSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppLanguage
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppearanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AssistanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.FilesSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.GameSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.LanguageSettingsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    settingsUiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        SharedTransitionLayout {
            val sharedTransitionScope = this

            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { it },
                    ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { -it / 3 },
                    ) + fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetX = { -it / 3 },
                    ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetX = { it },
                    ) + fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                },
                sizeTransform = {
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { _, _ ->
                            motionScheme.defaultSpatialSpec()
                        },
                    )
                },
            ) {
                composable<HomeDestination> {
                    val playViewModel: PlayViewModel = viewModel(
                        factory = PlayViewModel.factory(appContainer.gameRepository)
                    )
                    HomeScreen(
                        playViewModel = playViewModel,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onStartGame = { difficulty ->
                            navController.navigate(NewGameDestination(difficulty))
                        },
                        onContinueGame = { gameId ->
                            navController.navigate(SavedGameDestination(gameId))
                        },
                        onNavigateToSettings = { category ->
                            navController.navigate(SettingsDestination(category))
                        },
                    )
                }

                composable<NewGameDestination> { backStackEntry ->
                    val destination = backStackEntry.toRoute<NewGameDestination>()
                    GameDestinationContent(
                        appContainer = appContainer,
                        newGameDifficulty = destination.difficulty,
                        savedGameId = null,
                        settingsUiState = settingsUiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<SavedGameDestination> { backStackEntry ->
                    val destination = backStackEntry.toRoute<SavedGameDestination>()
                    GameDestinationContent(
                        appContainer = appContainer,
                        newGameDifficulty = null,
                        savedGameId = destination.gameId,
                        settingsUiState = settingsUiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<SettingsDestination> { backStackEntry ->
                    val destination = backStackEntry.toRoute<SettingsDestination>()
                    val onBack: () -> Unit = { navController.popBackStack() }

                    when (destination.category) {
                        SettingsCategory.APPEARANCE -> AppearanceSettingsScreen(
                            uiState = settingsUiState,
                            onThemeModeChange = settingsViewModel::setThemeMode,
                            onThemeColorChange = settingsViewModel::setThemeColor,
                            onPaletteStyleChange = settingsViewModel::setPaletteStyle,
                            onDynamicColorsChange = settingsViewModel::setUseDynamicColors,
                            onAmoledChange = settingsViewModel::setIsAmoled,
                            onColoredBoardChange = settingsViewModel::setColoredBoard,
                            onPositionLinesChange = settingsViewModel::setPositionLines,
                            onPositionBlockChange = settingsViewModel::setPositionBlock,
                            onAlternativeErrorColorChange =
                                settingsViewModel::setAlternativeErrorColor,
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        SettingsCategory.GAME -> GameSettingsScreen(
                            uiState = settingsUiState,
                            onShowHintDetailsChange = settingsViewModel::setShowHintDetails,
                            onShowErrorDetailsChange = settingsViewModel::setShowErrorDetails,
                            onShowErrorsImmediatelyChange =
                                settingsViewModel::setShowErrorsImmediately,
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        SettingsCategory.ASSISTANCE -> AssistanceSettingsScreen(
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        SettingsCategory.FILES -> FilesSettingsScreen(
                            uiState = settingsUiState,
                            onExportFormatChange = settingsViewModel::setExportFormat,
                            onIncludeCandidatesChange =
                                settingsViewModel::setIncludeCandidatesInCurrentExport,
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        SettingsCategory.LANGUAGE -> LanguageSettingsScreen(
                            selectedLanguage = AppLanguage.current(),
                            onLanguageSelected = AppLanguage::apply,
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        SettingsCategory.ABOUT -> AboutSettingsScreen(
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GameDestinationContent(
    appContainer: AppContainer,
    newGameDifficulty: Difficulty?,
    savedGameId: Long?,
    settingsUiState: SettingsUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
) {
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.factory(
            repository = appContainer.gameRepository,
            newGameDifficulty = newGameDifficulty,
            savedGameId = savedGameId,
            settings = appContainer.settingsRepository.settings,
        )
    )
    GameRoute(
        viewModel = gameViewModel,
        boardConfig = BoardConfig(
            useColoredBoard = settingsUiState.coloredBoard,
            highlightCross = settingsUiState.positionLines,
            highlightBlock = settingsUiState.positionBlock,
            useAltErrorColor = settingsUiState.alternativeErrorColor,
        ),
        originGameId = savedGameId,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
    )
}
