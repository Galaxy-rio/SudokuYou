package com.galaxyrio.sudokusolver.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.settings.AppSettings
import com.galaxyrio.sudokusolver.data.settings.PaletteStyleOption
import com.galaxyrio.sudokusolver.data.settings.SettingsRepository
import com.galaxyrio.sudokusolver.data.settings.ThemeMode
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themeColor: Color = Color(AppSettings.DEFAULT_THEME_COLOR),
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val useDynamicColors: Boolean = true,
    val isAmoled: Boolean = false,
    val coloredBoard: Boolean = false,
    val positionLines: Boolean = true,
    val positionBlock: Boolean = true,
    val alternativeErrorColor: Boolean = false,
    val showHintDetails: Boolean = true,
    val showErrorDetails: Boolean = true,
    val showErrorsImmediately: Boolean = false,
)

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repository.settings
        .map(AppSettings::asUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = repository.settings.value.asUiState(),
        )

    fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)

    fun setThemeColor(color: Color) = repository.setThemeColorArgb(color.toArgb())

    fun setPaletteStyle(style: PaletteStyle) =
        repository.setPaletteStyle(style.asDataOption())

    fun setUseDynamicColors(enabled: Boolean) = repository.setUseDynamicColors(enabled)

    fun setIsAmoled(enabled: Boolean) = repository.setIsAmoled(enabled)

    fun setColoredBoard(enabled: Boolean) = repository.setColoredBoard(enabled)

    fun setPositionLines(enabled: Boolean) = repository.setPositionLines(enabled)

    fun setPositionBlock(enabled: Boolean) = repository.setPositionBlock(enabled)

    fun setAlternativeErrorColor(enabled: Boolean) =
        repository.setAlternativeErrorColor(enabled)

    fun setShowHintDetails(enabled: Boolean) = repository.setShowHintDetails(enabled)

    fun setShowErrorDetails(enabled: Boolean) = repository.setShowErrorDetails(enabled)

    fun setShowErrorsImmediately(enabled: Boolean) =
        repository.setShowErrorsImmediately(enabled)

    companion object {
        fun factory(repository: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(repository)
            }
        }
    }
}

private fun AppSettings.asUiState(): SettingsUiState = SettingsUiState(
    themeMode = themeMode,
    themeColor = Color(themeColorArgb),
    paletteStyle = paletteStyle.asUiStyle(),
    useDynamicColors = useDynamicColors,
    isAmoled = isAmoled,
    coloredBoard = coloredBoard,
    positionLines = positionLines,
    positionBlock = positionBlock,
    alternativeErrorColor = alternativeErrorColor,
    showHintDetails = showHintDetails,
    showErrorDetails = showErrorDetails,
    showErrorsImmediately = showErrorsImmediately,
)

private fun PaletteStyleOption.asUiStyle(): PaletteStyle = when (this) {
    PaletteStyleOption.TONAL_SPOT -> PaletteStyle.TonalSpot
    PaletteStyleOption.NEUTRAL -> PaletteStyle.Neutral
    PaletteStyleOption.VIBRANT -> PaletteStyle.Vibrant
    PaletteStyleOption.EXPRESSIVE -> PaletteStyle.Expressive
    PaletteStyleOption.RAINBOW -> PaletteStyle.Rainbow
    PaletteStyleOption.FRUIT_SALAD -> PaletteStyle.FruitSalad
    PaletteStyleOption.MONOCHROME -> PaletteStyle.Monochrome
    PaletteStyleOption.FIDELITY -> PaletteStyle.Fidelity
    PaletteStyleOption.CONTENT -> PaletteStyle.Content
}

private fun PaletteStyle.asDataOption(): PaletteStyleOption = when (this) {
    PaletteStyle.TonalSpot -> PaletteStyleOption.TONAL_SPOT
    PaletteStyle.Neutral -> PaletteStyleOption.NEUTRAL
    PaletteStyle.Vibrant -> PaletteStyleOption.VIBRANT
    PaletteStyle.Expressive -> PaletteStyleOption.EXPRESSIVE
    PaletteStyle.Rainbow -> PaletteStyleOption.RAINBOW
    PaletteStyle.FruitSalad -> PaletteStyleOption.FRUIT_SALAD
    PaletteStyle.Monochrome -> PaletteStyleOption.MONOCHROME
    PaletteStyle.Fidelity -> PaletteStyleOption.FIDELITY
    PaletteStyle.Content -> PaletteStyleOption.CONTENT
}
