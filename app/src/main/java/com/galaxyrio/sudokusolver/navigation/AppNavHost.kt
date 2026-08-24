package com.galaxyrio.sudokusolver.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
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
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AboutSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppLanguage
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AppearanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.AssistanceSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.ChangelogsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.CrashDetailsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.CrashHistoryScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.FilesSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.GameSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.LanguageSettingsScreen
import com.galaxyrio.sudokusolver.ui.screens.settings.details.LicensesScreen
import com.galaxyrio.sudokusolver.ui.screens.statistics.StatisticsViewModel
import com.galaxyrio.sudokusolver.ui.motion.materialBackwardEnter
import com.galaxyrio.sudokusolver.ui.motion.materialBackwardExit
import com.galaxyrio.sudokusolver.ui.motion.materialContainerEnter
import com.galaxyrio.sudokusolver.ui.motion.materialContainerExit
import com.galaxyrio.sudokusolver.ui.motion.materialForwardEnter
import com.galaxyrio.sudokusolver.ui.motion.materialForwardExit

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    settingsUiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val hierarchyTravelPx = with(LocalDensity.current) { HierarchyTravel.roundToPx() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        SharedTransitionLayout {
            val sharedTransitionScope = this

            NavHost(
                navController = navController,
                startDestination = HomeDestination,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable<HomeDestination>(
                    enterTransition = { EnterTransition.None },
                    exitTransition = {
                        if (targetState.usesSavedGameContainerTransform()) {
                            materialContainerExit()
                        } else {
                            materialForwardExit(hierarchyTravelPx)
                        }
                    },
                    popEnterTransition = {
                        if (initialState.usesSavedGameContainerTransform()) {
                            materialContainerEnter()
                        } else {
                            materialBackwardEnter(hierarchyTravelPx)
                        }
                    },
                    popExitTransition = { ExitTransition.None },
                ) {
                    val playViewModel: PlayViewModel = viewModel(
                        factory = PlayViewModel.factory(appContainer.gameRepository)
                    )
                    val statisticsViewModel: StatisticsViewModel = viewModel(
                        factory = StatisticsViewModel.factory(appContainer.gameRepository)
                    )
                    HomeScreen(
                        playViewModel = playViewModel,
                        statisticsViewModel = statisticsViewModel,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onStartGame = { difficulty ->
                            navController.navigate(NewGameDestination(difficulty))
                        },
                        onContinueGame = { gameId ->
                            navController.navigate(
                                SavedGameDestination(
                                    gameId = gameId,
                                    useContainerTransform = true,
                                )
                            )
                        },
                        onOpenImportedGame = { gameId ->
                            navController.navigate(
                                SavedGameDestination(
                                    gameId = gameId,
                                    useContainerTransform = false,
                                )
                            )
                        },
                        onOpenSettings = {
                            navController.navigate(SettingsRootDestination)
                        },
                    )
                }

                composable<SettingsRootDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateTo = { category ->
                            navController.navigate(SettingsDestination(category))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                composable<NewGameDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) { backStackEntry ->
                    val destination = backStackEntry.toRoute<NewGameDestination>()
                    GameDestinationContent(
                        appContainer = appContainer,
                        newGameDifficulty = destination.difficulty,
                        savedGameId = null,
                        useContainerTransform = false,
                        settingsUiState = settingsUiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<SavedGameDestination>(
                    enterTransition = {
                        if (targetState.usesSavedGameContainerTransform()) {
                            materialContainerEnter()
                        } else {
                            materialForwardEnter(hierarchyTravelPx)
                        }
                    },
                    exitTransition = {
                        if (initialState.usesSavedGameContainerTransform()) {
                            materialContainerExit()
                        } else {
                            materialForwardExit(hierarchyTravelPx)
                        }
                    },
                    popEnterTransition = {
                        if (targetState.usesSavedGameContainerTransform()) {
                            materialContainerEnter()
                        } else {
                            materialBackwardEnter(hierarchyTravelPx)
                        }
                    },
                    popExitTransition = {
                        if (initialState.usesSavedGameContainerTransform()) {
                            materialContainerExit()
                        } else {
                            materialBackwardExit(hierarchyTravelPx)
                        }
                    },
                ) { backStackEntry ->
                    val destination = backStackEntry.toRoute<SavedGameDestination>()
                    GameDestinationContent(
                        appContainer = appContainer,
                        newGameDifficulty = null,
                        savedGameId = destination.gameId,
                        useContainerTransform = destination.useContainerTransform,
                        settingsUiState = settingsUiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable<SettingsDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) { backStackEntry ->
                    val destination = backStackEntry.toRoute<SettingsDestination>()
                    val onBack: () -> Unit = { navController.popBackStack() }

                    when (destination.category) {
                        SettingsCategory.APPEARANCE -> AppearanceSettingsScreen(
                            uiState = settingsUiState,
                            onThemeModeChange = settingsViewModel::setThemeMode,
                            onThemeColorChange = settingsViewModel::selectThemeColor,
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
                            onOpenChangelogs = {
                                navController.navigate(ChangelogsDestination)
                            },
                            onOpenLicenses = {
                                navController.navigate(LicensesDestination)
                            },
                            onOpenCrashHistory = {
                                navController.navigate(CrashHistoryDestination)
                            },
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                composable<ChangelogsDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) {
                    ChangelogsScreen(
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                composable<LicensesDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) {
                    LicensesScreen(
                        repository = appContainer.licensesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                composable<CrashHistoryDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) {
                    CrashHistoryScreen(
                        repository = appContainer.crashHistoryRepository,
                        onOpenCrash = { reportId ->
                            navController.navigate(CrashDetailsDestination(reportId))
                        },
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                composable<CrashDetailsDestination>(
                    enterTransition = { materialForwardEnter(hierarchyTravelPx) },
                    exitTransition = { materialForwardExit(hierarchyTravelPx) },
                    popEnterTransition = { materialBackwardEnter(hierarchyTravelPx) },
                    popExitTransition = { materialBackwardExit(hierarchyTravelPx) },
                ) { backStackEntry ->
                    val destination = backStackEntry.toRoute<CrashDetailsDestination>()
                    CrashDetailsScreen(
                        reportId = destination.reportId,
                        repository = appContainer.crashHistoryRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    )
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
    useContainerTransform: Boolean,
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
        originGameId = savedGameId.takeIf { useContainerTransform },
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
    )
}

private fun NavBackStackEntry.usesSavedGameContainerTransform(): Boolean =
    destination.hasRoute<SavedGameDestination>() &&
        toRoute<SavedGameDestination>().useContainerTransform

private val HierarchyTravel = 30.dp
