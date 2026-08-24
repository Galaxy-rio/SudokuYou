package com.galaxyrio.sudokusolver.ui.screens.settings.details

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.crash.CrashHistoryRepository
import com.galaxyrio.sudokusolver.data.crash.CrashRecord
import java.text.DateFormat
import java.util.Date

@Composable
fun CrashHistoryScreen(
    repository: CrashHistoryRepository,
    onOpenCrash: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CrashHistoryViewModel = viewModel(
        factory = CrashHistoryViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LaunchedEffect(uiState.clearFailed) {
        if (uiState.clearFailed) {
            Toast.makeText(
                context,
                R.string.crash_history_clear_failed,
                Toast.LENGTH_SHORT,
            ).show()
            viewModel.consumeClearFailure()
        }
    }

    SettingsDetailScaffold(
        titleResource = R.string.crash_history_title,
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (uiState.records.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmation = true },
                    enabled = !uiState.isClearing,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.crash_history_clear),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> CrashHistoryLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            uiState.loadFailed -> CrashHistoryMessage(
                icon = Icons.Default.BugReport,
                title = stringResource(R.string.crash_history_load_failed),
                summary = stringResource(R.string.crash_history_load_failed_summary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            uiState.records.isEmpty() -> CrashHistoryMessage(
                icon = Icons.Default.BugReport,
                title = stringResource(R.string.crash_history_empty_title),
                summary = stringResource(R.string.crash_history_empty_summary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> CrashHistoryList(
                records = uiState.records,
                onOpenCrash = onOpenCrash,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.crash_history_clear_title)) },
            text = { Text(stringResource(R.string.crash_history_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clear()
                        showClearConfirmation = false
                    },
                ) {
                    Text(stringResource(R.string.crash_history_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun CrashHistoryList(
    records: List<CrashRecord>,
    onOpenCrash: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        item(key = "privacy_notice") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.crash_history_privacy),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        itemsIndexed(
            items = records,
            key = { _, record -> record.id },
        ) { index, record ->
            CrashHistoryItem(
                record = record,
                index = index,
                count = records.size,
                onClick = { onOpenCrash(record.id) },
            )
        }

        item(key = "bottom_spacing") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CrashHistoryItem(
    record: CrashRecord,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    val timestamp = formattedCrashTime(record.timestampEpochMillis)
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        content = {
            Text(
                text = record.exceptionType.substringAfterLast('.'),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column {
                record.exceptionMessage?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(timestamp)
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun CrashHistoryMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item(key = "message") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CrashHistoryLoading(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item(key = "loading") {
            CircularProgressIndicator()
        }
    }
}

@Composable
internal fun formattedCrashTime(timestampEpochMillis: Long): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    return remember(timestampEpochMillis, locale) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
            .format(Date(timestampEpochMillis))
    }
}
