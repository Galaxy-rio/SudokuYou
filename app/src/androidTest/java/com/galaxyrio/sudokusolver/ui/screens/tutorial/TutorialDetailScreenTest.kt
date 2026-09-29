package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialLessons
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import com.galaxyrio.sudokusolver.ui.util.nameResourceId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSharedTransitionApi::class)
@RunWith(AndroidJUnit4::class)
class TutorialDetailScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val techniqueId = mutableStateOf("HIDDEN_SINGLE")

    private fun showLesson(): StateRestorationTester {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            SudokuYouTheme(dynamicColor = false) {
                SharedTransitionLayout {
                    val sharedScope = this
                    AnimatedVisibility(visible = true) {
                        TutorialDetailScreen(techniqueId.value, sharedScope, this, onBack = {})
                    }
                }
            }
        }
        return restoration
    }

    @Test
    fun allEightLessonsHaveFiveStepsAndThreeExamplesWithCorrectBoundaries() {
        showLesson()
        TutorialLessons.all.forEach { lesson ->
            compose.runOnIdle { techniqueId.value = lesson.technique.name }
            compose.onNodeWithText(context.getString(lesson.technique.nameResourceId())).assertIsDisplayed()
            assertProgress(examples = false, index = 1)
            action(R.string.tutorial_previous_step).assertIsNotEnabled()
            repeat(4) { index ->
                action(R.string.tutorial_next_step).assertIsEnabled().performClick()
                assertProgress(examples = false, index = index + 2)
            }
            action(R.string.tutorial_next_step).assertIsNotEnabled()
            action(R.string.tutorial_show_examples).performClick()
            assertProgress(examples = true, index = 1)
            action(R.string.tutorial_previous_example).assertIsNotEnabled()
            repeat(2) { index ->
                action(R.string.tutorial_next_example).performClick()
                assertProgress(examples = true, index = index + 2)
            }
            action(R.string.tutorial_next_example).assertIsNotEnabled()
            repeat(2) { action(R.string.tutorial_previous_example).performClick() }
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
    private fun assertProgress(examples: Boolean, index: Int) {
        compose.onNodeWithTag("tutorial_progress").assertTextEquals(context.getString(
            if (examples) R.string.tutorial_example_progress else R.string.tutorial_step_progress,
            index, if (examples) 3 else 5,
        ))
    }
    private fun boardCell(example: TutorialExample, index: Int) = compose.onNodeWithContentDescription(
        (if (example.before.cells[index].value == 0) context.getString(R.string.game_cell_empty, index / 9 + 1, index % 9 + 1)
        else context.getString(R.string.game_cell_value, index / 9 + 1, index % 9 + 1, example.before.cells[index].value)) +
            ", " + context.getString(R.string.tutorial_board_read_only),
    )
}
