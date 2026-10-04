package com.galaxyrio.sudokusolver.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

internal val LocalNumberColorSchemes = staticCompositionLocalOf { emptyList<ColorScheme>() }

// Ordered by digit, from red through violet. These are seeds, not final display colors.
private val numberColorSeeds = listOf(
    Color(0xFFE53935), // 1: red
    Color(0xFFFB8C00), // 2: orange
    Color(0xFFFDD835), // 3: yellow
    Color(0xFF7CB342), // 4: lime
    Color(0xFF00897B), // 5: teal
    Color(0xFF00ACC1), // 6: cyan
    Color(0xFF1E88E5), // 7: blue
    Color(0xFF3949AB), // 8: indigo
    Color(0xFF8E24AA), // 9: violet
)

internal fun createNumberColorSchemes(isDark: Boolean, isAmoled: Boolean): List<ColorScheme> =
    numberColorSeeds.map { seed ->
        dynamicColorScheme(
            seedColor = seed,
            isDark = isDark,
            isAmoled = isDark && isAmoled,
            // Keep each digit's hue even when the app uses a monochrome or expressive palette.
            style = PaletteStyle.TonalSpot,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )
    }

@Composable
internal fun numberColorScheme(number: Int?): ColorScheme =
    LocalNumberColorSchemes.current.getOrNull((number ?: 0) - 1)
        ?: MaterialTheme.colorScheme
