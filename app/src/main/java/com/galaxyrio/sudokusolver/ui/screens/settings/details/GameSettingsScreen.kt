package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState

@Composable
fun GameSettingsScreen(
    uiState: SettingsUiState,
    onShowHintDetailsChange: (Boolean) -> Unit,
    onShowErrorDetailsChange: (Boolean) -> Unit,
    onShowErrorsImmediatelyChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScaffold(
        titleResource = R.string.game_settings_title,
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            item(key = "input_rules_heading") {
                Text(
                    text = stringResource(R.string.game_settings_input_rules_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
                )
            }

            item(key = "show_hint_details") {
                GameSettingsSwitchItem(
                    titleResource = R.string.game_settings_show_hint_details,
                    summaryResource = R.string.game_settings_show_hint_details_summary,
                    checked = uiState.showHintDetails,
                    index = 0,
                    count = 3,
                    onCheckedChange = onShowHintDetailsChange,
                )
            }

            item(key = "show_error_details") {
                GameSettingsSwitchItem(
                    titleResource = R.string.game_settings_show_error_details,
                    summaryResource = R.string.game_settings_show_error_details_summary,
                    checked = uiState.showErrorDetails,
                    index = 1,
                    count = 3,
                    onCheckedChange = onShowErrorDetailsChange,
                )
            }

            item(key = "show_errors_immediately") {
                GameSettingsSwitchItem(
                    titleResource = R.string.game_settings_show_errors_immediately,
                    summaryResource = R.string.game_settings_show_errors_immediately_summary,
                    checked = uiState.showErrorsImmediately,
                    index = 2,
                    count = 3,
                    onCheckedChange = onShowErrorsImmediatelyChange,
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GameSettingsSwitchItem(
    @StringRes titleResource: Int,
    @StringRes summaryResource: Int,
    checked: Boolean,
    index: Int,
    count: Int,
    onCheckedChange: (Boolean) -> Unit,
) {
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        content = { Text(stringResource(titleResource)) },
        supportingContent = { Text(stringResource(summaryResource)) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}
