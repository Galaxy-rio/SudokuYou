package com.galaxyrio.sudokusolver.ui.components

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.galaxyrio.sudokusolver.ui.theme.SudokuYouTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberPadTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun restingButtonsAreCircularWithMatchingRowAndColumnSpacing() {
        showPad(360.dp, 360.dp)
        val bounds = assertDigitsFitAndAreCentered()
        bounds.forEach { assertEquals(it.width, it.height, 1f) }
        assertEquals(bounds[1].left - bounds[0].right, bounds[3].top - bounds[0].bottom, 1f)
        val pixels = digit(5).captureToImage().toPixelMap()
        assertEquals(Color.Black, pixels[(pixels.width * 0.1f).toInt(), (pixels.height * 0.1f).toInt()])
        assertTrue(pixels[pixels.width / 2, 2] != Color.Black)
        screenshot("normal")
    }

    @Test
    fun pressingMiddleButtonExpandsItAndCompressesBothNeighbors() {
        showPad(360.dp, 360.dp)
        val before = assertDigitsFitAndAreCentered()
        compose.mainClock.autoAdvance = false
        try {
            digit(2).performTouchInput { down(center) }
            compose.mainClock.advanceTimeBy(500)
            val pressed = assertDigitsFitAndAreCentered()
            assertTrue(pressed[1].width > before[1].width)
            assertTrue(pressed[0].width < before[0].width)
            assertTrue(pressed[2].width < before[2].width)
            assertEquals(before[0].left, pressed[0].left, 1f)
            assertEquals(before[2].right, pressed[2].right, 1f)
            screenshot("pressed")
            digit(2).performTouchInput { up() }
            compose.mainClock.advanceTimeBy(1_000)
            digit(2).assertIsSelected()
            val released = assertDigitsFitAndAreCentered()
            before.zip(released).forEach { (initial, final) ->
                assertEquals(initial.width, final.width, 1f)
                assertEquals(initial.height, final.height, 1f)
            }
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun compactPadKeepsWideCenteredDigitsAndAllNineRemainClickable() {
        var lastDigit: Int? = null
        var cleared = false
        showPad(360.dp, 210.dp, onNumberClick = { lastDigit = it }, onBackgroundClick = { cleared = true })
        val bounds = assertDigitsFitAndAreCentered()
        val minimumWidth = with(compose.density) { 80.dp.toPx() }
        bounds.forEach {
            assertTrue(it.width >= minimumWidth - 1f)
            assertTrue(it.width > it.height)
        }
        screenshot("compact")
        for (number in 1..9) {
            digit(number).performTouchInput { click() }.assertIsSelected()
            compose.runOnIdle {
                assertEquals(number, lastDigit)
                assertTrue(!cleared)
            }
        }
        compose.onNodeWithTag("number_pad").performTouchInput { click(Offset(2f, 2f)) }
        compose.runOnIdle { assertTrue(cleared) }
    }

    private fun showPad(
        width: Dp,
        height: Dp,
        onNumberClick: (Int) -> Unit = {},
        onBackgroundClick: () -> Unit = {},
    ) {
        var selected by mutableStateOf<Int?>(null)
        compose.setContent {
            SudokuYouTheme(dynamicColor = false) {
                Surface(color = Color.Black) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        NumberPad(
                            selectedNumber = selected,
                            onNumberClick = { selected = it; onNumberClick(it) },
                            onBackgroundClick = { selected = null; onBackgroundClick() },
                            modifier = Modifier.size(width, height).testTag("number_pad"),
                        )
                    }
                }
            }
        }
    }

    private fun digit(number: Int) = compose.onNode(hasText(number.toString()) and hasClickAction())

    private fun assertDigitsFitAndAreCentered(): List<Rect> {
        val area = compose.onNodeWithTag("number_pad").fetchSemanticsNode().boundsInRoot
        return (1..9).map { number ->
            val button = digit(number).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val text = compose.onNodeWithText(number.toString(), useUnmergedTree = true)
                .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue("Digit $number extends outside the pad", area.contains(button.topLeft))
            assertTrue(button.right <= area.right + 1f && button.bottom <= area.bottom + 1f)
            assertEquals("Digit $number is not horizontally centered", button.center.x, text.center.x, 1f)
            assertEquals("Digit $number is not vertically centered", button.center.y, text.center.y, 1f)
            assertTrue(text.width <= button.width && text.height <= button.height)
            button
        }
    }

    private fun screenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureNumberPad") != "true") return
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = requireNotNull(context.getExternalFilesDir("number-pad-preview"))
        File(directory, "$name.png").outputStream().use {
            compose.onNodeWithTag("number_pad").captureToImage().asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
