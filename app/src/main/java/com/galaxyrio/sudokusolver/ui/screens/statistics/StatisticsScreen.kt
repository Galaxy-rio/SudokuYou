package com.galaxyrio.sudokusolver.ui.screens.statistics

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.util.formatElapsedTime
import com.galaxyrio.sudokusolver.ui.util.label

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel,
    onOpenNavigation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionErrorMessage = stringResource(R.string.statistics_clear_error)

    LaunchedEffect(uiState.hasActionError) {
        if (uiState.hasActionError) {
            viewModel.clearActionError()
            snackbarHostState.showSnackbar(actionErrorMessage)
        }
    }

    StatisticsScreen(
        uiState = uiState,
        onOpenNavigation = onOpenNavigation,
        onOpenSettings = onOpenSettings,
        onClearStatistics = viewModel::clearStatistics,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onOpenNavigation: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearStatistics: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var selectedDifficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    val summary = remember(uiState.statistics, selectedDifficulty) {
        uiState.statistics.summarize(selectedDifficulty)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_statistics),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onOpenNavigation) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.nav_open_menu),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        enabled = uiState.hasStatistics && !uiState.isClearing,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_material_symbol_delete_history),
                            contentDescription = stringResource(R.string.statistics_clear_action),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.nav_open_settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            item(key = "difficulty_filter") {
                StatisticsDifficultyFilter(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = { selectedDifficulty = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp),
                )
            }

            item(key = "time_heading") {
                StatisticsSectionHeading(R.string.statistics_time_section)
            }

            item(key = "total_play_time") {
                StatisticsValueItem(
                    titleResource = R.string.statistics_total_play_time,
                    value = formatElapsedTime(summary.totalPlayTimeSeconds),
                    index = 0,
                    count = 3,
                )
            }

            item(key = "best_time") {
                StatisticsValueItem(
                    titleResource = R.string.statistics_best_time,
                    value = summary.bestCompletionTimeSeconds.toDisplayTime(),
                    index = 1,
                    count = 3,
                )
            }

            item(key = "average_time") {
                StatisticsValueItem(
                    titleResource = R.string.statistics_average_time,
                    value = summary.averageCompletionTimeSeconds.toDisplayTime(),
                    index = 2,
                    count = 3,
                )
            }

            item(key = "games_heading") {
                StatisticsSectionHeading(R.string.statistics_games_section)
            }

            item(key = "games_started") {
                StatisticsValueItem(
                    titleResource = R.string.statistics_games_started,
                    value = summary.gamesStarted.toString(),
                    index = 0,
                    count = 2,
                )
            }

            item(key = "games_completed") {
                StatisticsValueItem(
                    titleResource = R.string.statistics_games_completed,
                    value = summary.gamesCompleted.toString(),
                    index = 1,
                    count = 2,
                )
            }

            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_material_symbol_delete_history),
                    contentDescription = null,
                )
            },
            title = { Text(stringResource(R.string.statistics_clear_title)) },
            text = { Text(stringResource(R.string.statistics_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClearStatistics()
                    },
                ) {
                    Text(stringResource(R.string.common_delete))
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
private fun StatisticsDifficultyFilter(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember { listOf<Difficulty?>(null) + Difficulty.entries }
    val labels = options.map { difficulty ->
        difficulty?.label() ?: stringResource(R.string.statistics_all_difficulties)
    }
    val interactionSources = remember {
        List(options.size) { MutableInteractionSource() }
    }

    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, difficulty ->
            val isSelected = selectedDifficulty == difficulty
            val label = labels[index]
            val interactionSource = interactionSources[index]
            customItem(
                buttonGroupContent = {
                    Button(
                        onClick = { onDifficultySelected(difficulty) },
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(
                                interactionSource = interactionSource,
                                compressionLimit = 8.dp,
                            )
                            .height(48.dp)
                            .semantics { selected = isSelected },
                    ) {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onDifficultySelected(difficulty)
                            menuState.dismiss()
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun StatisticsSectionHeading(@StringRes titleResource: Int) {
    Text(
        text = stringResource(titleResource),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun StatisticsValueItem(
    @StringRes titleResource: Int,
    value: String,
    index: Int,
    count: Int,
) {
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        content = { Text(stringResource(titleResource)) },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

private fun Long?.toDisplayTime(): String = this?.let(::formatElapsedTime) ?: "—"
