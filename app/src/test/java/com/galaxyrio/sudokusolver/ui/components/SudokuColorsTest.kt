package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.galaxyrio.sudokusolver.ui.theme.createNumberColorSchemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuColorsTest {
    private val themes = listOf(lightColorScheme(), darkColorScheme())
    private val palettes = listOf(
        createNumberColorSchemes(isDark = false, isAmoled = false),
        createNumberColorSchemes(isDark = true, isAmoled = false),
    )

    @Test
    fun nineDistinctPalettesAdaptToLightDarkAndAmoled() {
        palettes.forEach { schemes ->
            assertEquals(9, schemes.size)
            assertEquals(9, schemes.map { it.primary }.toSet().size)
            schemes.forEach { scheme ->
                assertReadable(DigitColors(scheme.primary, scheme.onPrimary))
                assertReadable(DigitColors(scheme.primaryContainer, scheme.onPrimaryContainer))
                assertReadable(DigitColors(scheme.secondaryContainer, scheme.onSecondaryContainer))
            }
        }
        palettes[0].zip(palettes[1]).forEach { (light, dark) ->
            assertNotEquals(light.primary, dark.primary)
        }
        val amoled = createNumberColorSchemes(isDark = true, isAmoled = true)
        amoled.zip(palettes[1]).forEach { (black, dark) ->
            assertEquals(Color.Black, black.surface)
            assertEquals(dark.primary, black.primary)
        }
    }

    @Test
    fun rowAndBlockBackgroundsStayUniformAndReadableAcrossAllDigitPalettes() {
        themes.forEachIndexed { themeIndex, theme ->
            for (highContrast in listOf(false, true)) {
                for (fontContrast in listOf(false, true)) {
                    for (fixed in listOf(false, true)) {
                        for (cross in listOf(false, true)) {
                            val config = BoardConfig(
                                useHighContrastColors = highContrast,
                                useHighContrastFont = fontContrast,
                            )
                            palettes[themeIndex].forEach { digit ->
                                val colors = cellColors(
                                    theme, digit, config, fixed = fixed,
                                    highlighted = true, cross = cross, block = true,
                                )
                                assertEquals(
                                    if (cross) theme.surfaceContainerHighest else theme.surfaceContainer,
                                    colors.background,
                                )
                                assertReadable(colors)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun disablingPositionHighlightsRestoresTheDigitHighlight() {
        val theme = themes[0]
        val config = BoardConfig(highlightCross = false, highlightBlock = false)
        palettes[0].forEach { digit ->
            assertEquals(
                cellColors(theme, digit, config, highlighted = true),
                cellColors(theme, digit, config, highlighted = true, cross = true, block = true),
            )
        }
    }

    @Test
    fun highlightedCellsCandidatesAndKeypadsUseTheSameDigitPalette() {
        themes.forEachIndexed { themeIndex, theme ->
            for (highContrast in listOf(false, true)) {
                val config = BoardConfig(useHighContrastColors = highContrast)
                palettes[themeIndex].forEach { digit ->
                    val keypad = resolveNumberButtonColors(digit, isSelected = highContrast)
                    assertEquals(keypad, cellColors(theme, digit, config, highlighted = true))
                    assertEquals(
                        keypad,
                        resolveCandidateColors(
                            theme, digit, config, theme.onSurface,
                            isSelected = true, isError = false, isHighlighted = true,
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun unhighlightedCandidatesMatchTheSelectedEmptyCellsActualBackground() {
        themes.forEachIndexed { themeIndex, theme ->
            for (highContrast in listOf(false, true)) {
                val config = BoardConfig(useHighContrastColors = highContrast)
                val emptyCell = cellColors(theme, theme, config, selected = true)
                palettes[themeIndex].forEach { digit ->
                    val candidate = resolveCandidateColors(
                        theme, digit, config, emptyCell.text,
                        isSelected = true, isError = false, isHighlighted = false,
                    )
                    assertEquals(Color.Transparent, candidate.background)
                    assertEquals(emptyCell.text, candidate.text)
                    assertReadable(candidate.copy(background = emptyCell.background))
                }
            }
        }
    }

    @Test
    fun selectedCellsOverrideSharedPositionHighlightsInEveryContrastMode() {
        themes.forEachIndexed { themeIndex, theme ->
            for (highContrast in listOf(false, true)) {
                for (fixed in listOf(false, true)) {
                    val config = BoardConfig(useHighContrastColors = highContrast)
                    palettes[themeIndex].forEach { digit ->
                        val selected = cellColors(theme, digit, config, fixed = fixed, selected = true)
                        assertEquals(
                            selected,
                            cellColors(
                                theme, digit, config, fixed = fixed, selected = true,
                                highlighted = true, cross = true, block = true,
                            ),
                        )
                        assertReadable(selected)
                    }
                }
            }
        }
    }

    @Test
    fun errorsKeepTheThemePaletteAndTakePriorityOverDigitAndPositionHighlights() {
        themes.forEachIndexed { themeIndex, theme ->
            for (highContrast in listOf(false, true)) {
                for (alternativeError in listOf(false, true)) {
                    for (selected in listOf(false, true)) {
                        val config = BoardConfig(
                            useHighContrastColors = highContrast,
                            useAltErrorColor = alternativeError,
                        )
                        val expected = cellColors(theme, theme, config, selected = selected, error = true)
                        palettes[themeIndex].forEach { digit ->
                            assertEquals(
                                expected,
                                cellColors(
                                    theme, digit, config, selected = selected, error = true,
                                    highlighted = true, cross = true, block = true,
                                ),
                            )
                            assertEquals(
                                resolveCandidateColors(theme, theme, config, theme.onSurface, selected, true, true),
                                resolveCandidateColors(theme, digit, config, theme.onSurface, selected, true, true),
                            )
                        }
                        assertReadable(expected)
                    }
                }
            }
        }
    }

    private fun cellColors(
        theme: ColorScheme,
        digit: ColorScheme,
        config: BoardConfig,
        fixed: Boolean = false,
        selected: Boolean = false,
        error: Boolean = false,
        highlighted: Boolean = false,
        cross: Boolean = false,
        block: Boolean = false,
    ): DigitColors = resolveCellColors(
        theme, digit, config, fixed, selected, error, highlighted, cross, block,
        useColorfulNumbers = true,
    )

    private fun assertReadable(colors: DigitColors) {
        val background = colors.background.luminance()
        val text = colors.text.luminance()
        val contrast = (maxOf(background, text) + 0.05f) / (minOf(background, text) + 0.05f)
        // Allow for 8-bit rounding around Material's 4.5:1 text contrast target.
        assertTrue("Text contrast was $contrast for $colors", contrast >= 4.45f)
    }
}
