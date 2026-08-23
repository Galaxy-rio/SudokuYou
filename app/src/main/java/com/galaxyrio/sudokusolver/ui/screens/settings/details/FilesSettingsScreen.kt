package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsUiState

@Composable
fun FilesSettingsScreen(
    uiState: SettingsUiState,
    onExportFormatChange: (SudokuExportFormat) -> Unit,
    onIncludeCandidatesChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScaffold(
        titleResource = R.string.files_settings_title,
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
            item(key = "export_format_heading") {
                FilesSectionHeading(R.string.files_settings_export_format)
            }

            itemsIndexed(
                items = SudokuExportFormat.entries,
                key = { _, format -> format.name },
            ) { index, format ->
                SegmentedListItem(
                    onClick = { onExportFormatChange(format) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = SudokuExportFormat.entries.size,
                    ),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                    ),
                    content = { Text(stringResource(format.titleResource())) },
                    supportingContent = {
                        Text(stringResource(format.summaryResource()))
                    },
                    trailingContent = {
                        RadioButton(
                            selected = uiState.exportFormat == format,
                            onClick = null,
                        )
                    },
                )
            }

            item(key = "current_export_heading") {
                FilesSectionHeading(R.string.files_settings_current_export)
            }

            item(key = "include_candidates") {
                SegmentedListItem(
                    onClick = {
                        onIncludeCandidatesChange(!uiState.includeCandidatesInCurrentExport)
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
                    colors = ListItemDefaults.segmentedColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                    ),
                    content = { Text(stringResource(R.string.files_settings_include_candidates)) },
                    supportingContent = {
                        Text(stringResource(R.string.files_settings_include_candidates_summary))
                    },
                    trailingContent = {
                        Switch(
                            checked = uiState.includeCandidatesInCurrentExport,
                            onCheckedChange = onIncludeCandidatesChange,
                        )
                    },
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FilesSectionHeading(@StringRes titleResource: Int) {
    Text(
        text = stringResource(titleResource),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
    )
}

@StringRes
private fun SudokuExportFormat.titleResource(): Int = when (this) {
    SudokuExportFormat.SUSSER -> R.string.export_format_susser
    SudokuExportFormat.MULTILINE -> R.string.export_format_multiline
    SudokuExportFormat.PENCILMARK -> R.string.export_format_pencilmark
    SudokuExportFormat.SUKAKU -> R.string.export_format_sukaku
    SudokuExportFormat.EXCEL -> R.string.export_format_excel
    SudokuExportFormat.OPEN_SUDOKU -> R.string.export_format_open_sudoku
    SudokuExportFormat.HODOKU -> R.string.export_format_hodoku
}

@StringRes
private fun SudokuExportFormat.summaryResource(): Int = when (this) {
    SudokuExportFormat.SUSSER -> R.string.export_format_susser_summary
    SudokuExportFormat.MULTILINE -> R.string.export_format_multiline_summary
    SudokuExportFormat.PENCILMARK -> R.string.export_format_pencilmark_summary
    SudokuExportFormat.SUKAKU -> R.string.export_format_sukaku_summary
    SudokuExportFormat.EXCEL -> R.string.export_format_excel_summary
    SudokuExportFormat.OPEN_SUDOKU -> R.string.export_format_open_sudoku_summary
    SudokuExportFormat.HODOKU -> R.string.export_format_hodoku_summary
}
