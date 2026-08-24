package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState

@Composable
fun GameSettingsScreen(
    uiState: SettingsUiState,
    onShowHintDetailsChange: (Boolean) -> Unit,
    onShowErrorDetailsChange: (Boolean) -> Unit,
    onShowErrorsImmediatelyChange: (Boolean) -> Unit,
    onCoordinateNotationChange: (CoordinateNotation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCoordinateNotationDialog by rememberSaveable { mutableStateOf(false) }

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
                GameSettingsSectionHeading(R.string.game_settings_input_rules_section)
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

            item(key = "hints_heading") {
                GameSettingsSectionHeading(R.string.game_settings_hints_section)
            }

            item(key = "coordinate_notation") {
                SegmentedListItem(
                    onClick = { showCoordinateNotationDialog = true },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                    colors = gameSettingsItemColors(),
                    content = {
                        Text(stringResource(R.string.game_settings_coordinate_notation))
                    },
                    supportingContent = {
                        Text(
                            stringResource(
                                R.string.game_settings_coordinate_notation_summary,
                                stringResource(uiState.coordinateNotation.titleResource()),
                                stringResource(uiState.coordinateNotation.exampleResource()),
                            )
                        )
                    },
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCoordinateNotationDialog) {
        CoordinateNotationDialog(
            selectedNotation = uiState.coordinateNotation,
            onSelect = { notation ->
                onCoordinateNotationChange(notation)
                showCoordinateNotationDialog = false
            },
            onDismiss = { showCoordinateNotationDialog = false },
        )
    }
}

@Composable
private fun GameSettingsSectionHeading(@StringRes textResource: Int) {
    Text(
        text = stringResource(textResource),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
    )
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
        colors = gameSettingsItemColors(),
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

@Composable
private fun CoordinateNotationDialog(
    selectedNotation: CoordinateNotation,
    onSelect: (CoordinateNotation) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.game_settings_coordinate_notation)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                CoordinateNotation.entries.forEach { notation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(notation) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = notation == selectedNotation,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(notation.titleResource()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(notation.exampleResource()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun gameSettingsItemColors() = ListItemDefaults.segmentedColors(
    containerColor = MaterialTheme.colorScheme.surfaceBright,
)

@StringRes
private fun CoordinateNotation.titleResource(): Int = when (this) {
    CoordinateNotation.LOCALIZED -> R.string.coordinate_notation_localized
    CoordinateNotation.RCB -> R.string.coordinate_notation_rcb
    CoordinateNotation.K9 -> R.string.coordinate_notation_k9
    CoordinateNotation.EXCEL -> R.string.coordinate_notation_excel
}

@StringRes
private fun CoordinateNotation.exampleResource(): Int = when (this) {
    CoordinateNotation.LOCALIZED -> R.string.coordinate_notation_localized_example
    CoordinateNotation.RCB -> R.string.coordinate_notation_rcb_example
    CoordinateNotation.K9 -> R.string.coordinate_notation_k9_example
    CoordinateNotation.EXCEL -> R.string.coordinate_notation_excel_example
}
