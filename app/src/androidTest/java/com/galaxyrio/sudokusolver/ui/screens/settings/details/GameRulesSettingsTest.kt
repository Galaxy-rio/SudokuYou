package com.galaxyrio.sudokusolver.ui.screens.settings.details

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.PreferencesSettingsRepository
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRulesSettingsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val isolatedContext = object : ContextWrapper(context) {
        override fun getApplicationContext(): Context = this
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
            super.getSharedPreferences("game_rules_test_$name", mode)
    }
    private val preferences = isolatedContext.getSharedPreferences("sudoku_settings", Context.MODE_PRIVATE)
    private lateinit var repository: PreferencesSettingsRepository

    @Before
    fun setUp() {
        preferences.edit().clear().commit()
        repository = PreferencesSettingsRepository(isolatedContext)
    }

    @After
    fun clearTestPreferences() {
        preferences.edit().clear().commit()
    }

    @Test
    fun highlightingDefaultsToEnabledAndPersistsAcrossRepositoryReloads() {
        assertTrue(repository.settings.value.highlightConflictingNumbers)
        repository.setHighlightConflictingNumbers(false)
        assertFalse(repository.settings.value.highlightConflictingNumbers)
        repository = PreferencesSettingsRepository(isolatedContext)
        assertFalse(repository.settings.value.highlightConflictingNumbers)
        repository.setHighlightConflictingNumbers(true)
        assertTrue(PreferencesSettingsRepository(isolatedContext).settings.value.highlightConflictingNumbers)
    }

    @Test
    fun conflictHighlightingIsTheThirdRuleAndBothSwitchAndRowSaveTheChoice() {
        compose.setContent {
            SudokuYouTheme(dynamicColor = false) {
                val model: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(repository))
                val state by model.uiState.collectAsStateWithLifecycle()
                GameSettingsScreen(
                    uiState = state,
                    onShowHintDetailsChange = model::setShowHintDetails,
                    onShowErrorDetailsChange = model::setShowErrorDetails,
                    onHighlightConflictingNumbersChange = model::setHighlightConflictingNumbers,
                    onShowErrorsImmediatelyChange = model::setShowErrorsImmediately,
                    onCoordinateNotationChange = model::setCoordinateNotation,
                    onBack = {},
                )
            }
        }
        val rules = listOf(
            R.string.game_settings_show_hint_details,
            R.string.game_settings_show_error_details,
            R.string.game_settings_highlight_conflicting_numbers,
            R.string.game_settings_show_errors_immediately,
        ).map { resource ->
            compose.onNodeWithText(context.getString(resource)).fetchSemanticsNode().boundsInRoot
        }
        rules.zipWithNext().forEach { (before, after) -> assertTrue(before.bottom <= after.top) }
        val switches = compose.onAllNodes(isToggleable()).assertCountEquals(4)
        switches[2].assertIsOn()
        screenshot("settings")
        switches[2].performClick().assertIsOff()
        compose.runOnIdle {
            assertFalse(repository.settings.value.highlightConflictingNumbers)
            assertFalse(PreferencesSettingsRepository(isolatedContext).settings.value.highlightConflictingNumbers)
        }
        compose.onNodeWithText(context.getString(R.string.game_settings_highlight_conflicting_numbers))
            .performClick()
        switches[2].assertIsOn()
        compose.runOnIdle { assertTrue(repository.settings.value.highlightConflictingNumbers) }
    }

    @Test
    fun switchingHighlightingRecolorsConflictingDigitsAndNotesOnTheSameBoard() {
        val board = Sudoku(List(Sudoku.CELL_COUNT) { index ->
            when (index) {
                0 -> Cell(value = 5, isFixed = true)
                1 -> Cell(value = 5)
                2 -> Cell(candidates = setOf(5, 6))
                else -> Cell()
            }
        })
        val theme = lightColorScheme(
            surface = Color.White,
            primary = Color.Blue,
            secondary = Color.Green,
            error = Color.Red,
            errorContainer = Color(0xFFFFCDD2),
        )
        compose.setContent {
            val settings by repository.settings.collectAsState()
            MaterialTheme(colorScheme = theme) {
                Surface {
                    SudokuBoard(
                        sudoku = board,
                        onCellClick = { _, _ -> },
                        config = BoardConfig(highlightConflictingNumbers = settings.highlightConflictingNumbers),
                        modifier = Modifier.size(324.dp),
                    )
                }
            }
        }
        val digitDescription = context.getString(R.string.game_cell_value, 1, 2, 5) +
            ", " + context.getString(R.string.game_cell_editable)
        val noteDescription = context.getString(R.string.game_cell_empty, 1, 3) +
            ", " + context.getString(R.string.game_cell_editable)
        val digit = compose.onNodeWithContentDescription(digitDescription)
        val notes = compose.onNodeWithContentDescription(noteDescription)

        assertEquals(theme.errorContainer, digit.captureToImage().toPixelMap()[2, 2])
        assertTrue(containsColor(notes.captureToImage().toPixelMap(), theme.error))
        screenshot("highlighting-on")

        compose.runOnIdle { repository.setHighlightConflictingNumbers(false) }
        assertEquals(theme.surface, digit.captureToImage().toPixelMap()[2, 2])
        assertTrue(containsColor(digit.captureToImage().toPixelMap(), theme.primary))
        val ordinaryNotes = notes.captureToImage().toPixelMap()
        assertFalse(containsColor(ordinaryNotes, theme.error))
        assertTrue(containsColor(ordinaryNotes, theme.secondary))
        screenshot("highlighting-off")

        compose.runOnIdle { repository.setHighlightConflictingNumbers(true) }
        assertEquals(theme.errorContainer, digit.captureToImage().toPixelMap()[2, 2])
        assertTrue(containsColor(notes.captureToImage().toPixelMap(), theme.error))
    }

    private fun containsColor(pixels: PixelMap, color: Color): Boolean =
        (0 until pixels.width).any { x -> (0 until pixels.height).any { y -> pixels[x, y] == color } }

    private fun screenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureGameRules") != "true") return
        val directory = requireNotNull(context.getExternalFilesDir("game-rules-preview"))
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
