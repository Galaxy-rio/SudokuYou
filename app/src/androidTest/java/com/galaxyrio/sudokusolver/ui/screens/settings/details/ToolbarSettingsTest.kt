package com.galaxyrio.sudokusolver.ui.screens.settings.details

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.AppContainer
import com.galaxyrio.sudokusolver.data.settings.PreferencesSettingsRepository
import com.galaxyrio.sudokusolver.data.settings.ToolbarConfiguration
import com.galaxyrio.sudokusolver.data.settings.ToolbarTool
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.navigation.AppNavHost
import com.galaxyrio.sudokusolver.navigation.SavedGameDestination
import com.galaxyrio.sudokusolver.navigation.SettingsDestination
import com.galaxyrio.sudokusolver.navigation.SettingsRootDestination
import com.galaxyrio.sudokusolver.navigation.ToolbarSettingsDestination
import com.galaxyrio.sudokusolver.ui.components.actionDescriptionResource
import com.galaxyrio.sudokusolver.ui.components.titleResource
import com.galaxyrio.sudokusolver.ui.screens.game.GameViewModel
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsCategory
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolbarSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val isolatedContext = object : ContextWrapper(context) {
        private fun isolated(name: String) = "toolbar_test_$name"
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
    private val preferences = isolatedContext.getSharedPreferences("sudoku_settings", Context.MODE_PRIVATE)
    private lateinit var container: AppContainer
    private lateinit var controller: NavHostController
    private val createdGameIds = mutableSetOf<Long>()

    @Before
    fun showApp() {
        preferences.edit().clear().commit()
        container = AppContainer(isolatedContext)
        compose.setContent {
            controller = rememberNavController()
            val settings: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(container.settingsRepository),
            )
            val state by settings.uiState.collectAsStateWithLifecycle()
            SudokuYouTheme(dynamicColor = false) {
                AppNavHost(controller, container, state, settings)
            }
        }
        compose.waitForIdle()
    }

    @After
    fun cleanUp() {
        compose.mainClock.autoAdvance = true
        compose.activityRule.scenario.close()
        runBlocking { container.gameRepository.deleteGames(createdGameIds) }
        preferences.edit().clear().commit()
    }

    @Test
    fun defaultsAndStoredConfigurationsRetainEveryToolExactlyOnce() {
        assertEquals(ToolbarConfiguration(), container.settingsRepository.settings.value.toolbar)
        val order = ToolbarTool.entries.reversed()
        container.settingsRepository.setToolbarToolOrder(order)
        container.settingsRepository.setToolbarToolCount(3)
        assertEquals(ToolbarConfiguration(3, order),
            PreferencesSettingsRepository(isolatedContext).settings.value.toolbar)

        preferences.edit().putInt("toolbar_tool_count", 99)
            .putString("toolbar_tool_order", "hint,unknown,hint,undo").commit()
        val recovered = PreferencesSettingsRepository(isolatedContext).settings.value.toolbar
        assertEquals(7, recovered.toolCount)
        assertEquals(listOf(ToolbarTool.HINT, ToolbarTool.UNDO, ToolbarTool.REDO,
            ToolbarTool.ERASE, ToolbarTool.NOTES, ToolbarTool.BIVALUE, ToolbarTool.DESELECT),
            recovered.toolOrder)
    }

    @Test
    fun settingsEntrySliderDragAndMoveMenuPersistAndReturnToGameSettings() {
        openToolbarSettings()
        val slider = compose.onNodeWithTag("toolbar_tool_count")
        val range = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(3f..7f, range.range)
        assertEquals(3, range.steps)
        for (count in 3..7) {
            slider.performSemanticsAction(SemanticsActions.SetProgress) { it(count.toFloat()) }
            compose.runOnIdle {
                assertEquals(count, container.settingsRepository.settings.value.toolbar.toolCount)
                assertEquals(count, PreferencesSettingsRepository(isolatedContext).settings.value.toolbar.toolCount)
            }
        }
        screenshot("settings-seven")
        compose.onNodeWithTag("toolbar_tool_undo").performScrollTo()
        val handle = compose.onNodeWithContentDescription(context.getString(
            R.string.toolbar_reorder_tool, context.getString(ToolbarTool.UNDO.titleResource()),
        )).fetchSemanticsNode().boundsInRoot
        val redo = compose.onNodeWithTag("toolbar_tool_redo").fetchSemanticsNode().boundsInRoot
        compose.onRoot().performTouchInput {
            swipe(handle.center, Offset(handle.center.x, redo.center.y + redo.height / 4f), 700)
        }
        compose.runOnIdle {
            assertEquals(listOf(ToolbarTool.REDO, ToolbarTool.UNDO),
                container.settingsRepository.settings.value.toolbar.toolOrder.take(2))
        }
        compose.onNodeWithContentDescription(context.getString(
            R.string.toolbar_reorder_tool, context.getString(ToolbarTool.UNDO.titleResource()),
        )).performClick()
        compose.onNodeWithText(context.getString(R.string.toolbar_move_up)).performClick()
        compose.runOnIdle {
            assertEquals(ToolbarTool.entries.toList(),
                PreferencesSettingsRepository(isolatedContext).settings.value.toolbar.toolOrder)
        }
        compose.onNodeWithContentDescription(context.getString(R.string.common_back)).performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(controller.currentBackStackEntry!!.destination.hasRoute<SettingsDestination>())
        }
        compose.onNodeWithText(context.resources.getQuantityString(R.plurals.toolbar_tool_count_summary, 7, 7))
            .performScrollTo().assertIsDisplayed()
        screenshot("game-settings")
        compose.onNodeWithText(context.getString(R.string.toolbar_settings_title)).performClick()
        compose.waitForIdle()
        assertEquals(7f, slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current)
    }

    @Test
    fun draggingTheLastToolToTheTopScrollsTheListAndSavesTheNewOrder() {
        openToolbarSettings()
        compose.onNodeWithTag("toolbar_tool_deselect").performScrollTo()
        val handle = compose.onNodeWithContentDescription(context.getString(
            R.string.toolbar_reorder_tool, context.getString(ToolbarTool.DESELECT.titleResource()),
        )).fetchSemanticsNode().boundsInRoot
        val list = compose.onNodeWithTag("toolbar_tools_list").fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false
        compose.onRoot().performTouchInput {
            down(handle.center)
            moveTo(Offset(handle.center.x, handle.center.y - 40f), 80)
        }
        compose.mainClock.advanceTimeBy(32)
        compose.onRoot().performTouchInput {
            moveTo(Offset(handle.center.x, list.top + 24f), 500)
        }
        compose.mainClock.advanceTimeBy(3_000)
        compose.onRoot().performTouchInput { up() }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.runOnIdle {
            val expected = listOf(ToolbarTool.DESELECT) + ToolbarTool.entries.filter { it != ToolbarTool.DESELECT }
            assertEquals(expected, container.settingsRepository.settings.value.toolbar.toolOrder)
            assertEquals(expected, PreferencesSettingsRepository(isolatedContext).settings.value.toolbar.toolOrder)
        }
        screenshot("settings-reordered")
    }

    @Test
    fun customToolbarOrderAndCountControlRealGameActionsAndBivalueRendering() {
        val tools = listOf(ToolbarTool.DESELECT, ToolbarTool.BIVALUE, ToolbarTool.NOTES,
            ToolbarTool.UNDO, ToolbarTool.REDO, ToolbarTool.ERASE, ToolbarTool.HINT)
        compose.runOnIdle {
            container.settingsRepository.setToolbarToolOrder(tools)
            container.settingsRepository.setToolbarToolCount(3)
            container.settingsRepository.setHighlightConflictingNumbers(false)
        }
        val puzzle = Sudoku.fromGridString(
            "530070000600195000098000060800060003400803001700020006060000280000419005000080079")
        val cells = puzzle.cells.toMutableList()
        cells[2] = cells[2].copy(candidates = setOf(1, 2), isCandidateSetExplicit = true)
        cells[3] = cells[3].copy(candidates = setOf(1, 2, 3), isCandidateSetExplicit = true)
        val id = runBlocking {
            container.gameRepository.saveGame(SavedGame(
                difficulty = Difficulty.EASY, sudoku = Sudoku(cells),
                solution = SudokuSolution(
                    "534678912672195348198342567859761423426853791713924856961537284287419635345286179"
                        .map(Char::digitToInt)),
            ))
        }
        createdGameIds += id
        compose.runOnIdle { controller.navigate(SavedGameDestination(id, false)) }
        compose.waitForIdle()
        lateinit var game: GameViewModel
        compose.runOnIdle {
            game = ViewModelProvider(controller.currentBackStackEntry!!)[GameViewModel::class.java]
        }
        compose.waitUntil(10_000) { !game.uiState.value.isLoading }
        assertToolbarOrder(tools.take(3))
        tool(ToolbarTool.UNDO).assertDoesNotExist()
        tool(ToolbarTool.HINT).assertDoesNotExist()

        val bivalueCell = compose.onNodeWithContentDescription(cellDescription(1, 3))
        val otherCell = compose.onNodeWithContentDescription(cellDescription(1, 4))
        val before = bivalueCell.captureToImage().asAndroidBitmap()
        val otherBefore = otherCell.captureToImage().asAndroidBitmap()
        tool(ToolbarTool.BIVALUE).assertIsOff().performClick().assertIsOn()
        assertFalse(before.sameAs(bivalueCell.captureToImage().asAndroidBitmap()))
        assertTrue(otherBefore.sameAs(otherCell.captureToImage().asAndroidBitmap()))
        screenshot("game-three-bivalue")
        tool(ToolbarTool.BIVALUE).performClick().assertIsOff()
        assertTrue(before.sameAs(bivalueCell.captureToImage().asAndroidBitmap()))

        compose.onNodeWithContentDescription("1").performClick()
        otherCell.performClick()
        val entered = game.uiState.value.sudoku
        tool(ToolbarTool.DESELECT).performClick()
        compose.runOnIdle {
            assertNull(game.uiState.value.selectedNumber)
            assertNull(game.uiState.value.selectedCell)
            assertEquals(entered, game.uiState.value.sudoku)
        }
        tool(ToolbarTool.NOTES).performClick().assertIsOn()
        compose.runOnIdle { assertTrue(game.uiState.value.isNoteMode) }
        compose.runOnIdle { container.settingsRepository.setToolbarToolCount(7) }
        assertToolbarOrder(tools)
        screenshot("game-seven")
        tool(ToolbarTool.UNDO).performClick()
        compose.runOnIdle { assertEquals(Sudoku(cells), game.uiState.value.sudoku) }
        tool(ToolbarTool.REDO).performClick()
        compose.runOnIdle { assertEquals(entered, game.uiState.value.sudoku) }
    }

    private fun openToolbarSettings() {
        compose.runOnIdle { controller.navigate(SettingsRootDestination) }
        compose.waitForIdle()
        compose.runOnIdle { controller.navigate(SettingsDestination(SettingsCategory.GAME)) }
        compose.waitForIdle()
        compose.onNodeWithText(context.getString(R.string.toolbar_settings_title)).performScrollTo().performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertTrue(controller.currentBackStackEntry!!.destination.hasRoute<ToolbarSettingsDestination>())
        }
    }

    private fun tool(tool: ToolbarTool) =
        compose.onNodeWithContentDescription(context.getString(tool.actionDescriptionResource()))

    private fun assertToolbarOrder(tools: List<ToolbarTool>) {
        val bounds = tools.map { tool(it).assertIsDisplayed().fetchSemanticsNode().boundsInRoot }
        bounds.zipWithNext().forEach { (left, right) ->
            assertTrue("Toolbar tools overlap or have the wrong order", left.right <= right.left + 1f)
        }
    }

    private fun cellDescription(row: Int, col: Int): String =
        context.getString(R.string.game_cell_empty, row, col) + ", " +
            context.getString(R.string.game_cell_editable)

    private fun screenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureToolbar") != "true") return
        val directory = requireNotNull(context.getExternalFilesDir("toolbar-preview"))
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
