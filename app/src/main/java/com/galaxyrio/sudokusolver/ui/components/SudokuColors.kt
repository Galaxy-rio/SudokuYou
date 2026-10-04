package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

internal data class DigitColors(val background: Color, val text: Color)

internal fun resolveCellColors(
    theme: ColorScheme,
    digit: ColorScheme,
    config: BoardConfig,
    isFixed: Boolean,
    isSelected: Boolean,
    isError: Boolean,
    isValueHighlighted: Boolean,
    isSelectedCross: Boolean,
    isSelectedBlock: Boolean,
    useColorfulNumbers: Boolean,
): DigitColors {
    val highlightCross = config.highlightCross && isSelectedCross
    val highlightBlock = config.highlightBlock && isSelectedBlock
    // A shared row/block background takes priority over a matching digit's highlight.
    val highlightValue = isValueHighlighted && !highlightCross && !highlightBlock
    val background = when {
        isError && isSelected -> if (config.useAltErrorColor) theme.tertiary else theme.error
        isError -> if (config.useAltErrorColor) theme.tertiaryContainer else theme.errorContainer
        isSelected && (isFixed || config.useHighContrastColors) -> digit.primary
        isSelected -> digit.primaryContainer
        highlightCross -> theme.surfaceContainerHighest
        highlightBlock -> theme.surfaceContainer
        highlightValue && config.useHighContrastColors -> digit.primary
        highlightValue -> digit.secondaryContainer
        else -> digit.surface
    }
    val text = when {
        isError && isSelected -> if (config.useAltErrorColor) theme.onTertiary else theme.onError
        isError -> if (config.useAltErrorColor) theme.onTertiaryContainer else theme.onErrorContainer
        isSelected && (isFixed || config.useHighContrastColors) -> digit.onPrimary
        isSelected -> digit.onPrimaryContainer
        highlightValue && config.useHighContrastColors -> digit.onPrimary
        highlightValue -> digit.onSecondaryContainer
        isFixed -> if (useColorfulNumbers) digit.primary else digit.onSurface
        else -> digit.primary
    }
    return DigitColors(background, text)
}

internal fun resolveCandidateColors(
    theme: ColorScheme,
    digit: ColorScheme,
    config: BoardConfig,
    cellTextColor: Color,
    isSelected: Boolean,
    isError: Boolean,
    isHighlighted: Boolean,
): DigitColors {
    val background = when {
        isError && (isHighlighted || config.useHighContrastColors) ->
            if (config.useAltErrorColor) theme.tertiaryContainer else theme.errorContainer
        isHighlighted && config.useHighContrastColors -> digit.primary
        isHighlighted -> digit.secondaryContainer
        else -> Color.Transparent
    }
    val text = when {
        isError && config.useHighContrastColors ->
            if (config.useAltErrorColor) theme.onTertiaryContainer else theme.onErrorContainer
        isError -> if (config.useAltErrorColor) theme.tertiary else theme.error
        isHighlighted && config.useHighContrastColors -> digit.onPrimary
        isHighlighted -> digit.onSecondaryContainer
        // An unhighlighted candidate sits directly on the selected empty cell's shared background.
        isSelected -> cellTextColor
        config.useHighContrastColors -> digit.primary
        else -> digit.secondary
    }
    return DigitColors(background, text)
}

internal fun resolveNumberButtonColors(digit: ColorScheme, isSelected: Boolean): DigitColors =
    if (isSelected) {
        DigitColors(digit.primary, digit.onPrimary)
    } else {
        DigitColors(digit.secondaryContainer, digit.onSecondaryContainer)
    }
