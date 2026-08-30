package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.BuildConfig
import com.galaxyrio.sudokusolver.R

@Composable
fun AboutSettingsScreen(
    onOpenChangelogs: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenCrashHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScaffold(
        titleResource = R.string.about_title,
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
            item(key = "app_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AnimatedAppIcon()
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(R.string.about_app_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.about_version,
                                BuildConfig.VERSION_NAME,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            item(key = "app_section_heading") {
                Text(
                    text = stringResource(R.string.about_app_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp),
                )
            }

            item(key = "changelogs") {
                AboutAppItem(
                    titleResource = R.string.about_changelogs,
                    summaryResource = R.string.about_changelogs_summary,
                    icon = Icons.Default.History,
                    index = 0,
                    count = 3,
                    onClick = onOpenChangelogs,
                )
            }
            item(key = "licenses") {
                AboutAppItem(
                    titleResource = R.string.about_licenses,
                    summaryResource = R.string.about_licenses_summary,
                    icon = Icons.Default.Policy,
                    index = 1,
                    count = 3,
                    onClick = onOpenLicenses,
                )
            }
            item(key = "crash_history") {
                AboutAppItem(
                    titleResource = R.string.about_crash_history,
                    summaryResource = R.string.about_crash_history_summary,
                    icon = Icons.Default.BugReport,
                    index = 2,
                    count = 3,
                    onClick = onOpenCrashHistory,
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnimatedAppIcon(modifier: Modifier = Modifier) {
    val iconSize = 92.dp
    val animatedShapeContainerSize =
        iconSize *
            (LoadingIndicatorDefaults.ContainerWidth.value /
                LoadingIndicatorDefaults.IndicatorSize.value)
    val centeredLoadingShapes = remember {
        listOf(
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Sunny,
            MaterialShapes.SoftBurst,
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(animatedShapeContainerSize)
            .clearAndSetSemantics { },
    ) {
        LoadingIndicator(
            color = MaterialTheme.colorScheme.primaryContainer,
            polygons = centeredLoadingShapes,
            modifier = Modifier.fillMaxSize(),
        )
        Icon(
            painter = painterResource(R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun AboutAppItem(
    @StringRes titleResource: Int,
    @StringRes summaryResource: Int,
    icon: ImageVector,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        content = { Text(stringResource(titleResource)) },
        supportingContent = { Text(stringResource(summaryResource)) },
    )
}
