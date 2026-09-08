package com.galaxyrio.sudokusolver.navigation

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.ui.screens.game.GameViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var container: AppContainer
    private lateinit var controller: NavHostController
    private val createdGameIds = mutableSetOf<Long>()
    private val backLabel get() = context.getString(R.string.common_back)

    @Before
    fun showApp() {
        val isolatedContext = object : ContextWrapper(context) {
            private fun isolated(name: String) = "back_navigation_test_$name"
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                super.getSharedPreferences(isolated(name), mode)
            override fun getDatabasePath(name: String): File = super.getDatabasePath(isolated(name))
            override fun openOrCreateDatabase(
                name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?,
            ): SQLiteDatabase = super.openOrCreateDatabase(isolated(name), mode, factory)
            override fun openOrCreateDatabase(
                name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?,
                errorHandler: DatabaseErrorHandler?,
            ): SQLiteDatabase = super.openOrCreateDatabase(isolated(name), mode, factory, errorHandler)
        }
        container = AppContainer(isolatedContext)
        compose.setContent {
            controller = rememberNavController()
            val settings: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(container.settingsRepository),
            )
            SudokuYouTheme(dynamicColor = false) {
                AppNavHost(controller, container, SettingsUiState(), settings)
            }
        }
        compose.waitForIdle()
        assertDestination<HomeDestination>()
    }

    @After
    fun removeTestGames() {
        compose.mainClock.autoAdvance = true
        compose.activityRule.scenario.close()
        runBlocking { container.gameRepository.deleteGames(createdGameIds) }
    }

    @Test
    fun newGameRapidBackTapsKeepHome() {
        compose.runOnIdle { controller.navigate(NewGameDestination(Difficulty.EASY)) }
        awaitGame()
        compose.runOnIdle {
            val entry = requireNotNull(controller.currentBackStackEntry)
            val game = ViewModelProvider(entry)[GameViewModel::class.java]
            createdGameIds += requireNotNull(game.uiState.value.gameId)
        }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithContentDescription(backLabel).performTouchInput {
            repeat(3) {
                click()
                advanceEventTime(40)
            }
        }
        assertDestination<HomeDestination>()
        assertHomeAfterTransition()
    }

    @Test
    fun savedGameRepeatedBackDuringContainerTransformKeepsHome() {
        openSavedGame(useContainerTransform = true)
        repeatBackDuringTransition()
        assertHomeAfterTransition()
    }

    @Test
    fun importedGameRepeatedBackDuringHierarchyTransitionKeepsHome() {
        openSavedGame(useContainerTransform = false)
        repeatBackDuringTransition()
        assertHomeAfterTransition()
    }

    @Test
    fun settingsRootRepeatedBackKeepsHome() {
        compose.runOnIdle { controller.navigate(SettingsRootDestination) }
        compose.waitForIdle()
        repeatBackDuringTransition()
        assertHomeAfterTransition()
    }

    @Test
    fun settingsDetailRepeatedBackStopsAtSettingsRoot() {
        compose.runOnIdle { controller.navigate(SettingsRootDestination) }
        compose.waitForIdle()
        compose.runOnIdle { controller.navigate(SettingsDestination(SettingsCategory.GAME)) }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        // Capture the detail page's actual toolbar action before both toolbars are composed.
        val backAction = compose.onNodeWithContentDescription(backLabel).fetchSemanticsNode()
            .config[SemanticsActions.OnClick].action!!
        compose.runOnIdle { backAction() }
        assertDestination<SettingsRootDestination>()
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { repeat(3) { backAction() } }
        assertDestination<SettingsRootDestination>()
        compose.runOnIdle {
            assertTrue(controller.previousBackStackEntry?.destination?.hasRoute<HomeDestination>() == true)
        }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithContentDescription(backLabel).performClick()
        assertHomeAfterTransition()
    }

    @Test
    fun backOnEnteringPageWaitsForTransitionToFinish() {
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { controller.navigate(SettingsRootDestination) }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle {
            assertEquals(Lifecycle.State.STARTED, controller.currentBackStackEntry?.lifecycle?.currentState)
        }
        compose.onNodeWithContentDescription(backLabel).performClick()
        assertDestination<SettingsRootDestination>()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithContentDescription(backLabel).performClick()
        assertHomeAfterTransition()
    }

    @Test
    fun normalReturnThenRepeatedRailOpenAndCloseKeepsHome() {
        openSavedGame(useContainerTransform = true)
        compose.onNodeWithContentDescription(backLabel).performClick()
        assertHomeAfterTransition()
        repeat(3) {
            compose.onNodeWithContentDescription(context.getString(R.string.nav_open_menu))
                .assertIsDisplayed().performClick()
            compose.onNodeWithContentDescription(context.getString(R.string.nav_close_menu))
                .assertIsDisplayed().performClick()
            assertDestination<HomeDestination>()
        }
    }

    @Test
    fun tappingSamePositionAcrossReturnAndRailAnimationsKeepsHome() {
        val homeButtonBounds = compose.onNodeWithContentDescription(context.getString(R.string.nav_open_menu))
            .fetchSemanticsNode().boundsInWindow
        openSavedGame(useContainerTransform = true)
        val buttonCenter = compose.onNodeWithContentDescription(backLabel)
            .fetchSemanticsNode().boundsInWindow.center
        assertTrue("Both controls must share the tested position", homeButtonBounds.contains(buttonCenter))
        val windowOrigin = IntArray(2)
        compose.runOnIdle { compose.activity.window.decorView.getLocationOnScreen(windowOrigin) }
        compose.mainClock.autoAdvance = false
        var railOpened = false
        repeat(40) {
            // Inject into the active window, including the modal rail once it appears.
            tapOnScreen(buttonCenter.x + windowOrigin[0], buttonCenter.y + windowOrigin[1])
            compose.mainClock.advanceTimeBy(32)
            assertDestination<HomeDestination>()
            railOpened = railOpened || compose.onAllNodes(
                hasContentDescription(context.getString(R.string.nav_close_menu)),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue("The repeated taps must also reach the home navigation menu", railOpened)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        if (compose.onAllNodes(hasContentDescription(context.getString(R.string.nav_close_menu)))
                .fetchSemanticsNodes().isNotEmpty()
        ) {
            compose.onNodeWithContentDescription(context.getString(R.string.nav_close_menu)).performClick()
        }
        assertHomeAfterTransition()
    }

    private fun tapOnScreen(x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
            try {
                event.source = InputDevice.SOURCE_TOUCHSCREEN
                assertTrue(automation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
    }

    private fun repeatBackDuringTransition() {
        compose.mainClock.autoAdvance = false
        compose.onNodeWithContentDescription(backLabel).performClick()
        assertDestination<HomeDestination>()
        compose.mainClock.advanceTimeBy(32)
        compose.onNodeWithContentDescription(backLabel).assertIsDisplayed().performClick()
        assertDestination<HomeDestination>()
    }

    private fun openSavedGame(useContainerTransform: Boolean) {
        val solution = SudokuSolution(
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179"
                .map(Char::digitToInt),
        )
        val id = runBlocking {
            container.gameRepository.saveGame(
                SavedGame(difficulty = Difficulty.EASY, sudoku = Sudoku(), solution = solution),
            )
        }
        createdGameIds += id
        compose.runOnIdle { controller.navigate(SavedGameDestination(id, useContainerTransform)) }
        awaitGame()
    }

    private fun awaitGame() {
        compose.waitUntil(30_000) {
            compose.onAllNodes(hasContentDescription(backLabel)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
        compose.onNodeWithContentDescription(backLabel).assertIsDisplayed()
    }

    private fun assertHomeAfterTransition() {
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertDestination<HomeDestination>()
        compose.runOnIdle { assertNull(controller.previousBackStackEntry) }
        compose.onNodeWithContentDescription(context.getString(R.string.nav_open_menu)).assertIsDisplayed()
        compose.onNodeWithContentDescription(backLabel).assertDoesNotExist()
    }

    private inline fun <reified T : Any> assertDestination() = compose.runOnIdle {
        assertTrue(
            "Expected ${T::class.simpleName}, actual ${controller.currentDestination?.route}",
            controller.currentDestination?.hasRoute<T>() == true,
        )
    }
}
