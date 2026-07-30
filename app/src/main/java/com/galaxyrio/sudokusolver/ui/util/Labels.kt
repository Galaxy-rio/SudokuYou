package com.galaxyrio.sudokusolver.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.ThemeMode
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.materialkolor.PaletteStyle

@Composable
fun Difficulty.label(): String = stringResource(labelResource)

@get:StringRes
val Difficulty.labelResource: Int
    get() = when (this) {
        Difficulty.EASY -> R.string.difficulty_easy
        Difficulty.MEDIUM -> R.string.difficulty_medium
        Difficulty.HARD -> R.string.difficulty_hard
    }

@Composable
fun ThemeMode.label(): String = stringResource(labelResource)

@get:StringRes
val ThemeMode.labelResource: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_mode_system
        ThemeMode.LIGHT -> R.string.theme_mode_light
        ThemeMode.DARK -> R.string.theme_mode_dark
    }

@Composable
fun PaletteStyle.label(): String = stringResource(labelResource)

@get:StringRes
val PaletteStyle.labelResource: Int
    get() = when (this) {
        PaletteStyle.TonalSpot -> R.string.palette_tonal_spot
        PaletteStyle.Neutral -> R.string.palette_neutral
        PaletteStyle.Vibrant -> R.string.palette_vibrant
        PaletteStyle.Expressive -> R.string.palette_expressive
        PaletteStyle.Rainbow -> R.string.palette_rainbow
        PaletteStyle.FruitSalad -> R.string.palette_fruit_salad
        PaletteStyle.Monochrome -> R.string.palette_monochrome
        PaletteStyle.Fidelity -> R.string.palette_fidelity
        PaletteStyle.Content -> R.string.palette_content
    }
