package com.galaxyrio.sudokusolver.data.settings

import android.content.Context
import androidx.core.content.edit
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

enum class PaletteStyleOption {
    TONAL_SPOT,
    NEUTRAL,
    VIBRANT,
    EXPRESSIVE,
    RAINBOW,
    FRUIT_SALAD,
    MONOCHROME,
    FIDELITY,
    CONTENT,
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeColorArgb: Int = DEFAULT_THEME_COLOR,
    val paletteStyle: PaletteStyleOption = PaletteStyleOption.TONAL_SPOT,
    val useDynamicColors: Boolean = true,
    val isAmoled: Boolean = false,
    val coloredBoard: Boolean = false,
    val positionLines: Boolean = true,
    val positionBlock: Boolean = true,
    val alternativeErrorColor: Boolean = false,
    val showHintDetails: Boolean = true,
    val showErrorDetails: Boolean = true,
    val showErrorsImmediately: Boolean = false,
    val exportFormat: SudokuExportFormat = SudokuExportFormat.SUSSER,
    val includeCandidatesInCurrentExport: Boolean = true,
) {
    companion object {
        const val DEFAULT_THEME_COLOR: Int = 0xFF6750A4.toInt()
    }
}

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    fun setThemeMode(mode: ThemeMode)
    fun selectThemeColorArgb(color: Int)
    fun setPaletteStyle(style: PaletteStyleOption)
    fun setUseDynamicColors(enabled: Boolean)
    fun setIsAmoled(enabled: Boolean)
    fun setColoredBoard(enabled: Boolean)
    fun setPositionLines(enabled: Boolean)
    fun setPositionBlock(enabled: Boolean)
    fun setAlternativeErrorColor(enabled: Boolean)
    fun setShowHintDetails(enabled: Boolean)
    fun setShowErrorDetails(enabled: Boolean)
    fun setShowErrorsImmediately(enabled: Boolean)
    fun setExportFormat(format: SudokuExportFormat)
    fun setIncludeCandidatesInCurrentExport(enabled: Boolean)
}

class PreferencesSettingsRepository(context: Context) : SettingsRepository {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val mutableSettings = MutableStateFlow(readSettings())
    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override fun setThemeMode(mode: ThemeMode) {
        preferences.edit { putInt(KEY_THEME_MODE, mode.ordinal) }
        update { copy(themeMode = mode) }
    }

    override fun selectThemeColorArgb(color: Int) {
        preferences.edit {
            putInt(KEY_THEME_COLOR, color)
            putBoolean(KEY_USE_DYNAMIC_COLORS, false)
        }
        update {
            copy(
                themeColorArgb = color,
                useDynamicColors = false,
            )
        }
    }

    override fun setPaletteStyle(style: PaletteStyleOption) {
        preferences.edit { putInt(KEY_PALETTE_STYLE, style.ordinal) }
        update { copy(paletteStyle = style) }
    }

    override fun setUseDynamicColors(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_USE_DYNAMIC_COLORS, enabled) }
        update { copy(useDynamicColors = enabled) }
    }

    override fun setIsAmoled(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_IS_AMOLED, enabled) }
        update { copy(isAmoled = enabled) }
    }

    override fun setColoredBoard(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_COLORED_BOARD, enabled) }
        update { copy(coloredBoard = enabled) }
    }

    override fun setPositionLines(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_POSITION_LINES, enabled) }
        update { copy(positionLines = enabled) }
    }

    override fun setPositionBlock(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_POSITION_BLOCK, enabled) }
        update { copy(positionBlock = enabled) }
    }

    override fun setAlternativeErrorColor(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_ALTERNATIVE_ERROR_COLOR, enabled) }
        update { copy(alternativeErrorColor = enabled) }
    }

    override fun setShowHintDetails(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_SHOW_HINT_DETAILS, enabled) }
        update { copy(showHintDetails = enabled) }
    }

    override fun setShowErrorDetails(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_SHOW_ERROR_DETAILS, enabled) }
        update { copy(showErrorDetails = enabled) }
    }

    override fun setShowErrorsImmediately(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_SHOW_ERRORS_IMMEDIATELY, enabled) }
        update { copy(showErrorsImmediately = enabled) }
    }

    override fun setExportFormat(format: SudokuExportFormat) {
        preferences.edit { putInt(KEY_EXPORT_FORMAT, format.ordinal) }
        update { copy(exportFormat = format) }
    }

    override fun setIncludeCandidatesInCurrentExport(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_INCLUDE_CANDIDATES_IN_CURRENT_EXPORT, enabled) }
        update { copy(includeCandidatesInCurrentExport = enabled) }
    }

    private fun readSettings(): AppSettings = AppSettings(
        themeMode = enumValueAtOrDefault(
            values = ThemeMode.entries,
            index = preferences.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal),
            default = ThemeMode.SYSTEM,
        ),
        themeColorArgb = preferences.getInt(KEY_THEME_COLOR, AppSettings.DEFAULT_THEME_COLOR),
        paletteStyle = enumValueAtOrDefault(
            values = PaletteStyleOption.entries,
            index = preferences.getInt(
                KEY_PALETTE_STYLE,
                PaletteStyleOption.TONAL_SPOT.ordinal,
            ),
            default = PaletteStyleOption.TONAL_SPOT,
        ),
        useDynamicColors = preferences.getBoolean(KEY_USE_DYNAMIC_COLORS, true),
        isAmoled = preferences.getBoolean(KEY_IS_AMOLED, false),
        coloredBoard = preferences.getBoolean(KEY_COLORED_BOARD, false),
        positionLines = preferences.getBoolean(KEY_POSITION_LINES, true),
        positionBlock = preferences.getBoolean(KEY_POSITION_BLOCK, true),
        alternativeErrorColor = preferences.getBoolean(KEY_ALTERNATIVE_ERROR_COLOR, false),
        showHintDetails = preferences.getBoolean(KEY_SHOW_HINT_DETAILS, true),
        showErrorDetails = preferences.getBoolean(KEY_SHOW_ERROR_DETAILS, true),
        showErrorsImmediately = preferences.getBoolean(KEY_SHOW_ERRORS_IMMEDIATELY, false),
        exportFormat = enumValueAtOrDefault(
            values = SudokuExportFormat.entries,
            index = preferences.getInt(
                KEY_EXPORT_FORMAT,
                SudokuExportFormat.SUSSER.ordinal,
            ),
            default = SudokuExportFormat.SUSSER,
        ),
        includeCandidatesInCurrentExport = preferences.getBoolean(
            KEY_INCLUDE_CANDIDATES_IN_CURRENT_EXPORT,
            true,
        ),
    )

    private inline fun update(transform: AppSettings.() -> AppSettings) {
        mutableSettings.value = mutableSettings.value.transform()
    }

    private companion object {
        const val PREFERENCES_NAME = "sudoku_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_THEME_COLOR = "theme_color"
        const val KEY_PALETTE_STYLE = "palette_style"
        const val KEY_USE_DYNAMIC_COLORS = "use_dynamic_colors"
        const val KEY_IS_AMOLED = "is_amoled"
        const val KEY_COLORED_BOARD = "colored_board"
        const val KEY_POSITION_LINES = "position_lines"
        const val KEY_POSITION_BLOCK = "position_block"
        const val KEY_ALTERNATIVE_ERROR_COLOR = "alternative_error_color"
        const val KEY_SHOW_HINT_DETAILS = "show_hint_details"
        const val KEY_SHOW_ERROR_DETAILS = "show_error_details"
        const val KEY_SHOW_ERRORS_IMMEDIATELY = "show_errors_immediately"
        const val KEY_EXPORT_FORMAT = "export_format"
        const val KEY_INCLUDE_CANDIDATES_IN_CURRENT_EXPORT =
            "include_candidates_in_current_export"
    }
}

private fun <T> enumValueAtOrDefault(values: List<T>, index: Int, default: T): T =
    values.getOrElse(index) { default }
