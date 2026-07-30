package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.galaxyrio.sudokusolver.R

@Composable
fun FilesSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderSettingsScreen(
        titleResource = R.string.files_settings_title,
        messageResource = R.string.files_settings_placeholder,
        onBack = onBack,
        modifier = modifier,
    )
}
