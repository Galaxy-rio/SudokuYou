package com.galaxyrio.sudokusolver.ui.guide

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.data.settings.PreferencesSettingsRepository
import com.galaxyrio.sudokusolver.data.settings.AppSettings
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.GameStatistics
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.SudokuApp
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.screens.game.GameRoute
import com.galaxyrio.sudokusolver.ui.screens.game.GameViewModel
import com.galaxyrio.sudokusolver.ui.screens.play.PlayMenuScreen
import com.galaxyrio.sudokusolver.ui.screens.play.PlayUiState
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalSharedTransitionApi::class)
class UsageGuideTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun applyRequestedTestOrientation() {
        val orientation = when (InstrumentationRegistry.getArguments().getString("guideOrientation")) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> return
        }
        compose.activityRule.scenario.onActivity { it.requestedOrientation = orientation }
        val expected = if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            Configuration.ORIENTATION_PORTRAIT
        } else Configuration.ORIENTATION_LANDSCAPE
        compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == expected }
    }

    @Test
    fun actualFabAndEveryToolAreReachableWithoutChangingGame() {
        val guide = UsageGuideState(GuideStep.START)
        val repository = GuideGameRepository()
        var openedGame by mutableStateOf(false)
        var settingsOpened = false
        lateinit var gameViewModel: GameViewModel
        compose.setContent {
            SudokuYouTheme(dynamicColor = false) {
                UsageGuideLayout(guide) {
                    SharedTransitionLayout {
                        val sharedScope = this
                        AnimatedVisibility(visible = true) {
                            if (openedGame) {
                                val model: GameViewModel = viewModel(
                                    factory = GameViewModel.factory(
                                        repository, Difficulty.EASY, null, MutableStateFlow(AppSettings()),
                                    ),
                                )
                                SideEffect { gameViewModel = model }
                                GameRoute(
                                    viewModel = model,
                                    boardConfig = BoardConfig(),
                                    originGameId = null,
                                    sharedTransitionScope = sharedScope,
                                    animatedVisibilityScope = this,
                                    onBack = {},
                                )
                            } else {
                                PlayMenuScreen(
                                    uiState = PlayUiState(isLoading = false),
                                    sharedTransitionScope = sharedScope,
                                    animatedVisibilityScope = this,
                                    onOpenNavigation = {},
                                    onOpenSettings = { settingsOpened = true },
                                    onStartGame = { openedGame = true },
                                    onContinueGame = {},
                                    onDeleteGames = {},
                                    onImportGame = { _, _ -> },
                                    onClearImportError = {},
                                    snackbarHostState = remember { SnackbarHostState() },
                                )
                            }
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("guide_action_NEW_GAME").assertIsDisplayed()
        screenshot("start")
        // Tap the masked settings button by position: it must not receive this touch.
        compose.onNodeWithContentDescription(
            context.getString(R.string.nav_open_settings), useUnmergedTree = true,
        ).performTouchInput { click() }
        compose.runOnIdle { assertFalse(settingsOpened) }
        compose.onNodeWithTag("guide_action_NEW_GAME").performTouchInput { click() }
        compose.runOnIdle { assertEquals(GuideStep.DIFFICULTY, guide.step) }
        screenshot("difficulty")
        listOf("EASY", "MEDIUM", "HARD", "BRUTAL").forEach {
            compose.onNodeWithTag("guide_action_$it").assertIsDisplayed()
        }
        compose.onNodeWithTag("guide_action_EASY").performTouchInput { click() }
        compose.waitUntil(10_000) { guide.anchors[GuideTarget.NUMBERS] != null }
        compose.onNodeWithText(context.getString(R.string.guide_numbers_title)).assertIsDisplayed()
        screenshot("keyboard")

        val navigationBounds = mutableMapOf<GuideSeries, List<Rect>>()
        for (step in GuideStep.entries.filterNot { it.isHomeStep }) {
            compose.runOnIdle {
                assertEquals(step, guide.step)
                step.target?.let {
                    assertTrue("Missing target for $step", guide.anchors[it]?.bounds?.isEmpty == false)
                }
                assertFalse(gameViewModel.uiState.value.isAdvancedMode)
                assertEquals(repository.puzzle, gameViewModel.uiState.value.sudoku)
                assertEquals(0L, gameViewModel.uiState.value.timeSpentSeconds)
            }
            compose.onNodeWithTag("usage_guide").assertIsDisplayed()
            if (step.series in listOf(GuideSeries.TOOLBAR, GuideSeries.MENU, GuideSeries.ADVANCED)) {
                val positions = listOf("guide_skip", "guide_previous", "guide_next").map {
                    compose.onNodeWithTag(it).fetchSemanticsNode().boundsInRoot
                }
                val first = navigationBounds.putIfAbsent(step.series, positions)
                if (first != null) assertEquals("Navigation moved at $step", first, positions)
            }
            if (step == GuideStep.NOTES) {
                compose.onNodeWithContentDescription(
                    context.getString(R.string.game_note_mode), useUnmergedTree = true,
                ).performTouchInput { click() }
                compose.runOnIdle { assertFalse(gameViewModel.uiState.value.isNoteMode) }
            }
            if (step in listOf(
                    GuideStep.NOTES, GuideStep.MORE, GuideStep.MENU,
                    GuideStep.ADVANCED_NOTES, GuideStep.PAINT, GuideStep.FINISH,
                )) {
                screenshot(step.name.lowercase())
            }
            if (step == GuideStep.MORE) {
                val collapsed = spotlight()
                compose.onNodeWithTag("guide_action_MORE").performTouchInput { click() }
                val expanded = spotlight()
                assertEquals(collapsed.center.x, expanded.center.x, 0.01f)
                assertEquals(collapsed.center.y, expanded.center.y, 0.01f)
                assertTrue(expanded.width > collapsed.width)
                compose.onNodeWithTag("guide_previous").performClick()
                compose.runOnIdle { assertEquals(GuideStep.MORE, guide.step) }
            }
            if (step == GuideStep.MENU) {
                compose.onNodeWithTag("guide_action_ADVANCED_MODE").assertIsDisplayed()
                    .performTouchInput { click() }
            } else if (step == GuideStep.NUMBERS || step == GuideStep.UNDO) {
                assertAnimatedNext()
            } else {
                val next = if (step == GuideStep.FINISH) R.string.guide_done else R.string.guide_next
                compose.onNodeWithText(context.getString(next)).assertIsDisplayed().performClick()
            }
        }
        compose.onNodeWithTag("usage_guide").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(Difficulty.EASY, repository.createdDifficulty)
            assertFalse(gameViewModel.uiState.value.isAdvancedMode)
            assertFalse(gameViewModel.uiState.value.canUndo)
        }
        compose.waitUntil(3_500) { gameViewModel.uiState.value.timeSpentSeconds > 0 }
    }

    @Test
    fun settingsReplaysGuideFromHomeAndSkipClosesExpandedMenu() {
        val isolatedContext = isolatedSettingsContext("usage_guide_navigation_test")
        val appContainer = AppContainer(isolatedContext)
        appContainer.settingsRepository.markUsageGuideSeen()
        compose.setContent {
            val settingsModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer.settingsRepository),
            )
            SudokuApp(appContainer, settingsModel)
        }
        compose.onNodeWithTag("usage_guide").assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.nav_open_settings)).performClick()
        compose.onNodeWithText(context.getString(R.string.settings_usage_guide)).assertDoesNotExist()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(context.getString(R.string.assistance_settings_title)))
        compose.onNodeWithText(context.getString(R.string.assistance_settings_title)).performClick()
        compose.onNodeWithText(context.getString(R.string.settings_usage_guide)).performClick()
        compose.onNodeWithTag("guide_action_NEW_GAME").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithText(context.getString(R.string.guide_difficulty_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.guide_skip)).performClick()
        compose.onNodeWithTag("usage_guide").assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.play_fab_menu_collapsed)).assertIsDisplayed()
        isolatedContext.getSharedPreferences("sudoku_settings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun recreationRetainsStepAndNewLaunchDoesNotAutomaticallyReplay() {
        val isolatedContext = isolatedSettingsContext("usage_guide_test")
        val preferences = isolatedContext.getSharedPreferences("sudoku_settings", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        try {
            var repository = PreferencesSettingsRepository(isolatedContext)
            var hostId by mutableIntStateOf(0)
            lateinit var guide: UsageGuideState
            val restoration = StateRestorationTester(compose)
            restoration.setContent {
                SudokuYouTheme(dynamicColor = false) {
                    key(hostId) {
                        UsageGuideHost(repository) {
                            val currentGuide = requireNotNull(LocalUsageGuide.current)
                            SideEffect { guide = currentGuide }
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                                Button(
                                    onClick = { currentGuide.onFabExpanded(true) },
                                    modifier = Modifier.size(64.dp).guideTarget(
                                        GuideTarget.NEW_GAME,
                                        "New game",
                                        onClick = { currentGuide.onFabExpanded(true) },
                                    ).guideTarget(GuideTarget.DIFFICULTIES),
                                ) { Text("+") }
                            }
                        }
                    }
                }
            }
            compose.onNodeWithTag("guide_action_NEW_GAME").performClick()
            restoration.emulateSavedInstanceStateRestore()
            compose.runOnIdle {
                assertEquals(GuideStep.DIFFICULTY, guide.step)
                assertTrue(repository.settings.value.hasSeenUsageGuide)
                guide.dismiss()
                repository = PreferencesSettingsRepository(isolatedContext)
                hostId++
            }
            compose.onNodeWithTag("usage_guide").assertDoesNotExist()
            compose.runOnIdle { guide.replay() }
            compose.onNodeWithTag("guide_action_NEW_GAME").assertIsDisplayed()
        } finally {
            preferences.edit().clear().commit()
        }
    }

    private fun spotlight(): Rect =
        compose.onNodeWithTag("guide_spotlight").fetchSemanticsNode().config[GuideSpotlightBounds]

    private fun assertAnimatedNext() {
        val before = spotlight()
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithTag("guide_next").performClick()
            compose.mainClock.advanceTimeBy(160)
            val middle = spotlight()
            compose.mainClock.advanceTimeBy(600)
            val after = spotlight()
            assertTrue("Mask snapped instead of animating", middle != before && middle != after)
            assertTrue(middle.center.x in minOf(before.center.x, after.center.x)..maxOf(before.center.x, after.center.x))
            assertTrue(middle.center.y in minOf(before.center.y, after.center.y)..maxOf(before.center.y, after.center.y))
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    private fun screenshot(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory = requireNotNull(context.getExternalFilesDir("usage-guide"))
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun isolatedSettingsContext(prefix: String): Context = object : ContextWrapper(context) {
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            super.getSharedPreferences("${prefix}_$name", mode)
    }

    private class GuideGameRepository : GameRepository {
        private val solution = SudokuSolution(
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179".map(Char::digitToInt),
        )
        val puzzle = Sudoku(solution.digits.mapIndexed { index, value ->
            if (index % 3 == 0) Cell() else Cell(value = value, isFixed = true)
        })
        var createdDifficulty: Difficulty? = null
        override val savedGames = MutableStateFlow(emptyList<SavedGame>())
        override val statistics = MutableStateFlow(emptyList<GameStatistics>())
        override suspend fun getGame(id: Long): SavedGame? = null
        override suspend fun createGame(difficulty: Difficulty): SavedGame {
            createdDifficulty = difficulty
            return SavedGame(id = 1, difficulty = difficulty, sudoku = puzzle, solution = solution)
        }
        override suspend fun createImportedGame(sudoku: Sudoku): SavedGame? = null
        override suspend fun saveGame(game: SavedGame): Long = game.id
        override suspend fun deleteGame(id: Long) = Unit
        override suspend fun deleteGames(ids: Set<Long>) = Unit
        override suspend fun clearStatistics() = Unit
    }
}
