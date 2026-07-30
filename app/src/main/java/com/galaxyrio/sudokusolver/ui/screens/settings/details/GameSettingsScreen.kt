package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.galaxyrio.sudokusolver.R

@Composable
fun GameSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderSettingsScreen(
        titleResource = R.string.game_settings_title,
        messageResource = R.string.game_settings_placeholder,
        onBack = onBack,
        modifier = modifier,
    )
}
