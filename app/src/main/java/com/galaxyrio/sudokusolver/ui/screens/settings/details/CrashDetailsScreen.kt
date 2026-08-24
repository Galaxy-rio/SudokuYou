package com.galaxyrio.sudokusolver.ui.screens.settings.details

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.crash.CrashHistoryRepository
import com.galaxyrio.sudokusolver.data.crash.CrashRecord
import java.text.DateFormat
import java.util.Date

@Composable
fun CrashDetailsScreen(
    reportId: String,
    repository: CrashHistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CrashDetailsViewModel = viewModel(
        key = reportId,
        factory = CrashDetailsViewModel.factory(reportId, repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val record = uiState.record

    LaunchedEffect(uiState.deleteFailed) {
        if (uiState.deleteFailed) {
            Toast.makeText(
                context,
                R.string.crash_details_delete_failed,
                Toast.LENGTH_SHORT,
            ).show()
            viewModel.consumeDeleteFailure()
        }
    }

    SettingsDetailScaffold(
        titleResource = R.string.crash_details_title,
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (record != null) {
                IconButton(
                    onClick = {
                        context.copyCrashReport(record)
                        Toast.makeText(
                            context,
                            R.string.crash_details_copied,
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.crash_details_copy),
                    )
                }
                IconButton(onClick = { context.shareCrashReport(record) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.crash_details_share),
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    enabled = !uiState.isDeleting,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.crash_details_delete),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> CrashDetailsLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            uiState.missing || record == null -> CrashDetailsMissing(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> CrashDetailsContent(
                record = record,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.crash_details_delete_title)) },
            text = { Text(stringResource(R.string.crash_details_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.delete(onDeleted = onBack)
                    },
                    enabled = !uiState.isDeleting,
                ) {
                    Text(stringResource(R.string.crash_details_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun CrashDetailsContent(
    record: CrashRecord,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "summary") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = record.exceptionType.substringAfterLast('.'),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = formattedCrashTime(record.timestampEpochMillis),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        record.exceptionMessage?.takeIf { it.isNotBlank() }?.let {
                            SelectionContainer {
                                Text(text = it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        item(key = "report_notice") {
            Text(
                text = stringResource(R.string.crash_details_share_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        item(key = "details_heading") {
            CrashSectionHeading(R.string.crash_details_environment)
        }
        item(key = "details") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceBright,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CrashInfoRow(
                        label = stringResource(R.string.crash_details_thread),
                        value = record.threadName,
                    )
                    CrashInfoRow(
                        label = stringResource(R.string.crash_details_app_version),
                        value = "${record.appVersionName} (${record.appVersionCode})",
                    )
                    CrashInfoRow(
                        label = stringResource(R.string.crash_details_android),
                        value = "${record.androidRelease} (API ${record.androidApiLevel})",
                    )
                    CrashInfoRow(
                        label = stringResource(R.string.crash_details_device),
                        value = "${record.manufacturer} ${record.model}".trim(),
                    )
                }
            }
        }

        item(key = "stack_heading") {
            CrashSectionHeading(R.string.crash_details_stack_trace)
        }
        item(key = "stack_trace") {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceBright,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SelectionContainer {
                    Text(
                        text = record.stackTrace,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(18.dp),
                    )
                }
            }
        }

        item(key = "bottom_spacing") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CrashSectionHeading(titleResource: Int) {
    Text(
        text = stringResource(titleResource),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp),
    )
}

@Composable
private fun CrashInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        SelectionContainer {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CrashDetailsMissing(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item(key = "missing") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(52.dp),
                )
                Text(
                    text = stringResource(R.string.crash_details_missing),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.crash_details_missing_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CrashDetailsLoading(modifier: Modifier = Modifier) {
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

private fun Context.copyCrashReport(record: CrashRecord) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(
        getString(R.string.crash_details_title),
        formatCrashReport(record),
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)
}

private fun Context.shareCrashReport(record: CrashRecord) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crash_details_share_subject))
        putExtra(Intent.EXTRA_TEXT, formatCrashReport(record))
    }
    startActivity(
        Intent.createChooser(intent, getString(R.string.crash_details_share_chooser)),
    )
}

private fun Context.formatCrashReport(record: CrashRecord): String {
    val locale = resources.configuration.locales[0]
    val formattedTime = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
        locale,
    ).format(Date(record.timestampEpochMillis))

    return buildString {
        appendLine(getString(R.string.crash_details_share_subject))
        appendLine(getString(R.string.crash_report_time, formattedTime))
        appendLine(getString(R.string.crash_report_exception, record.exceptionType))
        record.exceptionMessage?.let {
            appendLine(getString(R.string.crash_report_message, it))
        }
        appendLine(getString(R.string.crash_report_thread, record.threadName))
        appendLine(
            getString(
                R.string.crash_report_app,
                record.appVersionName,
                record.appVersionCode,
            ),
        )
        appendLine(
            getString(
                R.string.crash_report_android,
                record.androidRelease,
                record.androidApiLevel,
            ),
        )
        appendLine(
            getString(
                R.string.crash_report_device,
                "${record.manufacturer} ${record.model}".trim(),
            ),
        )
        appendLine()
        appendLine(getString(R.string.crash_report_stack_trace))
        append(record.stackTrace)
    }
}
