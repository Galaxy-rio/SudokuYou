package com.galaxyrio.sudokusolver.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

enum class PaletteStyleOption {
    TonalSpot, Neutral, Vibrant, Expressive, Rainbow, FruitSalad, Monochrome, Fidelity, Content
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sudoku_settings", Context.MODE_PRIVATE)

    private val _themeMode =
        MutableStateFlow(ThemeMode.entries[prefs.getInt("theme_mode", ThemeMode.SYSTEM.ordinal)])
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Default Blue color
    private val _themeColorArgb =
        MutableStateFlow(prefs.getInt("theme_color", -16776961)) // Color.Blue default
    val themeColorArgb: StateFlow<Int> = _themeColorArgb.asStateFlow()

    private val _paletteStyle = MutableStateFlow(
        PaletteStyleOption.entries[prefs.getInt(
            "palette_style",
            PaletteStyleOption.TonalSpot.ordinal
        )]
    )
    val paletteStyle: StateFlow<PaletteStyleOption> = _paletteStyle.asStateFlow()

    // Default true for dynamic colors on supported devices
    private val _useDynamicColors = MutableStateFlow(prefs.getBoolean("use_dynamic_colors", true))
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private val _isAmoled = MutableStateFlow(prefs.getBoolean("is_amoled", false))
    val isAmoled: StateFlow<Boolean> = _isAmoled.asStateFlow()

    private val _coloredBoard = MutableStateFlow(prefs.getBoolean("colored_board", false))
    val coloredBoard: StateFlow<Boolean> = _coloredBoard.asStateFlow()

    private val _positionLines = MutableStateFlow(prefs.getBoolean("position_lines", true))
    val positionLines: StateFlow<Boolean> = _positionLines.asStateFlow()

    private val _positionBlock = MutableStateFlow(prefs.getBoolean("position_block", true))
    val positionBlock: StateFlow<Boolean> = _positionBlock.asStateFlow()

    private val _alternativeErrorColor =
        MutableStateFlow(prefs.getBoolean("alternative_error_color", false))
    val alternativeErrorColor: StateFlow<Boolean> = _alternativeErrorColor.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putInt("theme_mode", mode.ordinal).apply()
        _themeMode.value = mode
    }

    fun setThemeColorArgb(color: Int) {
        prefs.edit().putInt("theme_color", color).apply()
        _themeColorArgb.value = color
    }

    fun setPaletteStyle(style: PaletteStyleOption) {
        prefs.edit().putInt("palette_style", style.ordinal).apply()
        _paletteStyle.value = style
    }

    fun setUseDynamicColors(enabled: Boolean) {
        prefs.edit().putBoolean("use_dynamic_colors", enabled).apply()
        _useDynamicColors.value = enabled
    }

    fun setIsAmoled(enabled: Boolean) {
        prefs.edit().putBoolean("is_amoled", enabled).apply()
        _isAmoled.value = enabled
    }

    fun setColoredBoard(enabled: Boolean) {
        prefs.edit().putBoolean("colored_board", enabled).apply()
        _coloredBoard.value = enabled
    }

    fun setPositionLines(enabled: Boolean) {
        prefs.edit().putBoolean("position_lines", enabled).apply()
        _positionLines.value = enabled
    }

    fun setPositionBlock(enabled: Boolean) {
        prefs.edit().putBoolean("position_block", enabled).apply()
        _positionBlock.value = enabled
    }

    fun setAlternativeErrorColor(enabled: Boolean) {
        prefs.edit().putBoolean("alternative_error_color", enabled).apply()
        _alternativeErrorColor.value = enabled
    }
}
