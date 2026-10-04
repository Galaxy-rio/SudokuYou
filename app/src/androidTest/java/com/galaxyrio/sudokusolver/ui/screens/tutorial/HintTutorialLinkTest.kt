package com.galaxyrio.sudokusolver.ui.screens.tutorial

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.HumanSolver
import com.galaxyrio.sudokusolver.domain.solver.SolveTrace
import com.galaxyrio.sudokusolver.domain.solver.SolveTraceStatus
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialLessons
import com.galaxyrio.sudokusolver.ui.components.GameHintPanel
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import com.galaxyrio.sudokusolver.ui.util.nameResourceId
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HintTutorialLinkTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun iconFollowsTheSelectedTechniqueAndDoesNotApplyAHint() {
        val example = TutorialLessons.find(TechniqueId.SKYSCRAPER.name)!!.examples.first()
        val initial = example.initialState
        val afterFirst = initial.apply(example.deduction)
        val second = requireNotNull(HumanSolver().nextStep(afterFirst))
        val trace = SolveTrace(initial, listOf(example.deduction, second),
            listOf(initial, afterFirst, afterFirst.apply(second)), SolveTraceStatus.STALLED)
        var selected by mutableIntStateOf(0)
        val opened = mutableListOf<TechniqueId>()
        var applied = 0
        compose.setContent {
            SudokuYouTheme(dynamicColor = false) {
                Surface {
                    GameHintPanel(
                        isLoading = false, trace = trace, issue = null, selectedStepIndex = selected,
                        areHintDetailsVisible = true, showErrorDetails = true,
                        coordinateNotation = CoordinateNotation.LOCALIZED,
                        onStepSelected = { selected = it }, onRevealDetails = {},
                        onApplyNext = { applied++ }, onOpenTutorial = { opened += it },
                    )
                }
            }
        }
        fun open(technique: TechniqueId) = compose.onNodeWithContentDescription(context.getString(
            R.string.game_hint_open_tutorial, context.getString(technique.nameResourceId()),
        )).assertIsDisplayed().performClick()
        open(TechniqueId.SKYSCRAPER)
        if (InstrumentationRegistry.getArguments().getString("captureTutorials") == "true") {
            val directory = requireNotNull(context.getExternalFilesDir("tutorial-preview"))
            File(directory, "hint-skyscraper.png").outputStream().use {
                compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        compose.onNodeWithContentDescription(context.getString(R.string.game_hint_next_step)).performClick()
        open(second.technique)
        compose.runOnIdle {
            assertEquals(listOf(TechniqueId.SKYSCRAPER, second.technique), opened)
            assertEquals(0, applied)
            assertEquals(1, selected)
        }
    }
}
