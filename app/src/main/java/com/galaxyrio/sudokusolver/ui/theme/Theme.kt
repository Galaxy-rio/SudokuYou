package com.galaxyrio.sudokusolver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SudokuSolverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    colorSeed: Color = Color(0xFF6750A4),
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = if (dynamicColor) {
        val platformColorScheme = if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
        if (darkTheme && amoled) {
            platformColorScheme.withAmoledSurfaces()
        } else {
            platformColorScheme
        }
    } else {
        rememberDynamicColorScheme(
            seedColor = colorSeed,
            isDark = darkTheme,
            style = paletteStyle,
            isAmoled = darkTheme && amoled,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )
    }

    ExpressiveTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = content,
    )
}

private fun ColorScheme.withAmoledSurfaces(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainer = Color(0xFF050505),
    surfaceContainerHigh = Color(0xFF0A0A0A),
    surfaceContainerHighest = Color(0xFF121212),
    surfaceBright = Color(0xFF1B1B1B),
)
