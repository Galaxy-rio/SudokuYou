package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialLessons
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class TutorialDetailScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context by lazy {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val language = InstrumentationRegistry.getArguments().getString("tutorialLocale")
        if (language == null) base else base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
        })
    }
    private val techniqueId = mutableStateOf("HIDDEN_SINGLE")

    private fun showLesson(): StateRestorationTester {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides context.resources.configuration) {
            SudokuYouTheme(dynamicColor = false) {
                SharedTransitionLayout {
                    val sharedScope = this
                    AnimatedVisibility(visible = true) {
                        TutorialDetailScreen(techniqueId.value, sharedScope, this, onBack = {})
                    }
                }
            }
            }
        }
        return restoration
    }

    @Test
    fun everyCatalogLessonHasABoardAndExamplesKeepCorrectBoundaries() {
        showLesson()
        val requestedLesson = InstrumentationRegistry.getArguments().getString("tutorialLesson")
        val lessons = TutorialLessons.all.filter { requestedLesson == null || it.id == requestedLesson }
        check(lessons.isNotEmpty())
        lessons.forEach { lesson ->
            compose.runOnIdle { techniqueId.value = lesson.id }
            val entry = tutorialCategories.flatMap { it.techniques }.first { it.id == lesson.id }
            compose.onNodeWithTag("tutorial_technique_title").assertTextEquals(context.getString(entry.titleResource)).assertIsDisplayed()
            entry.aliasesResource?.let { aliases ->
                compose.onNodeWithTag("tutorial_aliases")
                    .assertTextEquals(context.getString(R.string.tutorial_aliases, context.getString(aliases)))
            }
            check(lesson.examples.isNotEmpty()) { "${lesson.id} is missing its board example" }
            compose.onNodeWithTag("tutorial_board").assertIsDisplayed()
            assertProgress(examples = false, index = 1)
            action(R.string.tutorial_previous_step).assertIsNotEnabled()
            repeat(4) { index ->
                action(R.string.tutorial_next_step).assertIsEnabled().performClick()
                assertProgress(examples = false, index = index + 2)
                if (index == 1) captureForVisualReview(lesson.id)
            }
            action(R.string.tutorial_next_step).assertIsNotEnabled()
            action(R.string.tutorial_show_examples).performClick()
            assertProgress(examples = true, index = 1, count = lesson.examples.size)
            action(R.string.tutorial_previous_example).assertIsNotEnabled()
            repeat(lesson.examples.size - 1) { index ->
                action(R.string.tutorial_next_example).performClick()
                assertProgress(examples = true, index = index + 2, count = lesson.examples.size)
            }
            action(R.string.tutorial_next_example).assertIsNotEnabled()
            repeat(lesson.examples.size - 1) { action(R.string.tutorial_previous_example).performClick() }
            action(R.string.tutorial_previous_example).assertIsNotEnabled()
            action(R.string.tutorial_show_steps).performClick()
            assertProgress(examples = false, index = 5)
            repeat(4) { action(R.string.tutorial_previous_step).performClick() }
            assertProgress(examples = false, index = 1)
        }
    }

    @Test
    fun modeProgressAndCellSelectionSurviveSavedStateRestoration() {
        val restoration = showLesson()
        repeat(2) { action(R.string.tutorial_next_step).performClick() }
        action(R.string.tutorial_show_examples).performClick()
        action(R.string.tutorial_next_example).performClick()
        val example = TutorialLessons.find("HIDDEN_SINGLE")!!.examples[1]
        val cell = example.before.cells.indexOfFirst { it.value != 0 }
        boardCell(example, cell).performClick().assertIsSelected()
        restoration.emulateSavedInstanceStateRestore()
        assertProgress(examples = true, index = 2)
        boardCell(example, cell).assertIsSelected()
        action(R.string.tutorial_show_steps).performClick()
        assertProgress(examples = false, index = 3)
        action(R.string.tutorial_show_examples).performClick()
        assertProgress(examples = true, index = 2)
        boardCell(example, cell).assertIsNotSelected()
    }

    @Test
    fun boardRetainsWholeCellSelectionAndNeverAddsCandidateClickTargets() {
        showLesson()
        action(R.string.tutorial_show_examples).performClick()
        val example = TutorialLessons.find("HIDDEN_SINGLE")!!.examples.first()
        compose.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag("tutorial_board"))).assertCountEquals(81)
        val fixed = example.before.cells.indexOfFirst { it.isFixed }
        val empty = example.before.cells.indexOfFirst { it.candidates.size > 1 }
        boardCell(example, fixed).performClick().assertIsSelected()
        boardCell(example, fixed).performClick().assertIsNotSelected()
        boardCell(example, empty).performClick().assertIsSelected()
        boardCell(example, fixed).assertIsNotSelected()
        boardCell(example, empty).performClick().assertIsNotSelected()
        action(R.string.tutorial_next_example).performClick()
        action(R.string.tutorial_previous_example).performClick()
        boardCell(example, fixed).assertIsNotSelected()
        boardCell(example, empty).assertIsNotSelected()
        compose.onAllNodesWithContentDescription(context.getString(R.string.game_more_options)).assertCountEquals(0)
    }

    private fun action(resource: Int) = compose.onNodeWithContentDescription(context.getString(resource))
    private fun captureForVisualReview(id: String) {
        if (InstrumentationRegistry.getArguments().getString("captureTutorials") != "true") return
        if (id !in setOf("FINNED_JELLYFISH", "FIVE_Y_WING", "GROUPED_X_CHAIN", "THREE_D_MEDUSA",
                "SIMPLE_COLORING_TYPE_1", "CELL_FORCING_NET", "UNIQUE_RECTANGLE_TYPE_3",
                "LAST_DIGIT", "SUE_DE_COQ_TYPE_2", "LOCKED_PAIR", "LOCKED_TRIPLE", "POINTING_TRIPLE",
                "EIGHT_Y_WING", "NINE_Y_WING", "CHUTE_REMOTE_PAIR_BONUS", "LEVIATHAN",
                "UNIQUE_RECTANGLE_TYPE_4M", "DIGIT_FORCING_CHAIN", "DIGIT_FORCING_NET")) return
        val directory = requireNotNull(context.getExternalFilesDir("tutorial-preview"))
        val language = InstrumentationRegistry.getArguments().getString("tutorialLocale") ?: "en"
        File(directory, "$id-$language.png").outputStream().use { output ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
    private fun assertProgress(examples: Boolean, index: Int, count: Int = if (examples) 3 else 5) {
        compose.onNodeWithTag("tutorial_progress").assertTextEquals(context.getString(
            if (examples) R.string.tutorial_example_progress else R.string.tutorial_step_progress,
            index, count,
        ))
    }
    private fun boardCell(example: TutorialExample, index: Int) = compose.onNodeWithContentDescription(
        (if (example.before.cells[index].value == 0) context.getString(R.string.game_cell_empty, index / 9 + 1, index % 9 + 1)
        else context.getString(R.string.game_cell_value, index / 9 + 1, index % 9 + 1, example.before.cells[index].value)) +
            ", " + context.getString(R.string.tutorial_board_read_only),
    )
}
