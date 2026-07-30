package com.galaxyrio.sudokusolver.ui.screens.play

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.ui.util.formatElapsedTime
import com.galaxyrio.sudokusolver.ui.util.label
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayMenuRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: PlayViewModel,
    initialDifficulty: Difficulty,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionErrorMessage = stringResource(R.string.play_action_error)

    LaunchedEffect(uiState.hasActionError) {
        if (uiState.hasActionError) {
            viewModel.clearActionError()
            snackbarHostState.showSnackbar(actionErrorMessage)
        }
    }

    PlayMenuScreen(
        uiState = uiState,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        initialDifficulty = initialDifficulty,
        onStartGame = onStartGame,
        onContinueGame = onContinueGame,
        onDeleteGames = viewModel::deleteGames,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayMenuScreen(
    uiState: PlayUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    initialDifficulty: Difficulty,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onDeleteGames: (Set<Long>) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var difficulty by rememberSaveable { mutableStateOf(initialDifficulty) }
    var selectedGameIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    val isSelectionMode = selectedGameIds.isNotEmpty()
    val filteredGames = remember(uiState.savedGames, difficulty) {
        uiState.savedGames.filter { it.difficulty == difficulty }
    }

    LaunchedEffect(uiState.savedGames) {
        val availableIds = uiState.savedGames.mapTo(mutableSetOf()) { it.id }
        selectedGameIds = selectedGameIds.filter { it in availableIds }
    }

    BackHandler(enabled = isSelectionMode) {
        selectedGameIds = emptyList()
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) {
                            pluralStringResource(
                                R.plurals.play_selected_count,
                                selectedGameIds.size,
                                selectedGameIds.size,
                            )
                        } else {
                            stringResource(R.string.play_title)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedGameIds = emptyList() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.play_close_selection),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = {
                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                },
                label = "play_fab",
            ) { selectionMode ->
                if (selectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            onDeleteGames(selectedGameIds.toSet())
                            selectedGameIds = emptyList()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.play_delete_selected),
                            )
                        },
                        text = { Text(stringResource(R.string.common_delete)) },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                } else {
                    with(sharedTransitionScope) {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(
                                    key = NEW_GAME_CONTAINER_KEY
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = RemeasureToBounds,
                            ),
                            onClick = { onStartGame(difficulty) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(
                                        R.string.play_start_new_game
                                    ),
                                )
                            },
                            text = { Text(stringResource(R.string.play_new_game)) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            DifficultyTabs(
                selectedDifficulty = difficulty,
                onDifficultySelected = { difficulty = it },
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.hasLoadError -> {
                    EmptyGamesCard(
                        text = stringResource(R.string.play_load_error),
                        modifier = Modifier.padding(16.dp),
                    )
                }

                else -> {
                    SavedGamesList(
                        games = filteredGames,
                        difficulty = difficulty,
                        selectedGameIds = selectedGameIds.toSet(),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onGameClick = { gameId ->
                            if (isSelectionMode) {
                                selectedGameIds = selectedGameIds.toggle(gameId)
                            } else {
                                onContinueGame(gameId)
                            }
                        },
                        onGameLongClick = { gameId ->
                            selectedGameIds = selectedGameIds.toggle(gameId)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyTabs(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedDifficulty.ordinal,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Difficulty.entries.forEach { difficulty ->
            Tab(
                selected = selectedDifficulty == difficulty,
                onClick = { onDifficultySelected(difficulty) },
                text = { Text(difficulty.label()) },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SavedGamesList(
    games: List<SavedGameSummary>,
    difficulty: Difficulty,
    selectedGameIds: Set<Long>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onGameClick: (Long) -> Unit,
    onGameLongClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        item(key = "heading") {
            Text(
                text = stringResource(R.string.play_recent_games),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
            )
        }

        if (games.isEmpty()) {
            item(key = "empty") {
                EmptyGamesCard(
                    text = stringResource(
                        R.string.play_no_saved_games,
                        difficulty.label(),
                    ),
                )
            }
        } else {
            itemsIndexed(
                items = games,
                key = { _, game -> game.id },
            ) { index, game ->
                SavedGameItem(
                    game = game,
                    index = index,
                    count = games.size,
                    isSelected = game.id in selectedGameIds,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onGameClick(game.id) },
                    onLongClick = { onGameLongClick(game.id) },
                )
            }
        }

        item(key = "fab_spacer") {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SavedGameItem(
    game: SavedGameSummary,
    index: Int,
    count: Int,
    isSelected: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val date = formatDate(game.lastPlayedEpochMillis)
    val selectionLabel = stringResource(R.string.play_select_game)

    with(sharedTransitionScope) {
        SegmentedListItem(
            selected = isSelected,
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickLabel = selectionLabel,
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
            colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            content = { Text(date) },
            supportingContent = {
                Column {
                    Text(formatElapsedTime(game.timeSpentSeconds))
                    Text(
                        pluralStringResource(
                            R.plurals.play_numbers_left,
                            game.emptyCells,
                            game.emptyCells,
                        )
                    )
                    Text(
                        stringResource(
                            R.string.play_completion,
                            game.completionPercentage,
                        )
                    )
                }
            },
            leadingContent = {
                SudokuThumbnail(
                    board = game.board,
                    cornerRadius = if (isSelected) 12.dp else 4.dp,
                    modifier = Modifier
                        .size(64.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = thumbnailKey(game.id)
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = RemeasureToBounds,
                        ),
                )
            },
            trailingContent = {
                Box(
                    modifier = Modifier.height(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isSelected) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (isSelected) R.string.common_selected
                            else R.string.play_resume_game
                        ),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            },
            modifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = gameContainerKey(game.id)),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = RemeasureToBounds,
                placeholderSize = AnimatedSize,
            ),
        )
    }
}

@Composable
private fun EmptyGamesCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SudokuThumbnail(
    board: List<Int>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 4.dp,
    strokeWidth: Dp = 1.5.dp,
) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        val boardSize = size.minDimension
        val cellSize = boardSize / 9
        val strokeWidthPx = strokeWidth.toPx()
        val halfStroke = strokeWidthPx / 2

        board.forEachIndexed { index, value ->
            if (value != 0) {
                val row = index / 9
                val col = index % 9
                drawRect(
                    color = color.copy(alpha = 0.5f),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize),
                )
            }
        }

        for (index in 1..2) {
            val linePosition = index * 3 * cellSize
            drawLine(
                color = color,
                start = Offset(0f, linePosition),
                end = Offset(boardSize, linePosition),
                strokeWidth = strokeWidthPx,
            )
            drawLine(
                color = color,
                start = Offset(linePosition, 0f),
                end = Offset(linePosition, boardSize),
                strokeWidth = strokeWidthPx,
            )
        }

        drawRoundRect(
            color = color,
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(boardSize - strokeWidthPx, boardSize - strokeWidthPx),
            cornerRadius = CornerRadius(cornerRadius.toPx() - halfStroke),
            style = Stroke(width = strokeWidthPx),
        )
    }
}

@Composable
private fun formatDate(epochMillis: Long): String {
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
    }
    return remember(epochMillis, formatter) {
        formatter.format(Date(epochMillis))
    }
}

internal const val NEW_GAME_CONTAINER_KEY = "game_container_new"

internal fun gameContainerKey(gameId: Long): String = "game_container_$gameId"

internal fun thumbnailKey(gameId: Long): String = "game_thumbnail_$gameId"

private fun List<Long>.toggle(value: Long): List<Long> =
    if (value in this) this - value else this + value
