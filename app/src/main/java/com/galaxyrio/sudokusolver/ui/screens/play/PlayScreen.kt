package com.galaxyrio.sudokusolver.ui.screens.play

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import com.galaxyrio.sudokusolver.ui.util.formatElapsedTime
import com.galaxyrio.sudokusolver.ui.util.label
import com.galaxyrio.sudokusolver.ui.util.titleResource
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayMenuRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: PlayViewModel,
    onOpenNavigation: () -> Unit,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onOpenImportedGame: (Long) -> Unit,
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

    LaunchedEffect(uiState.importedGameId) {
        val importedGameId = uiState.importedGameId ?: return@LaunchedEffect
        viewModel.consumeImportedGame()
        onOpenImportedGame(importedGameId)
    }

    PlayMenuScreen(
        uiState = uiState,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onOpenNavigation = onOpenNavigation,
        onStartGame = onStartGame,
        onContinueGame = onContinueGame,
        onDeleteGames = viewModel::deleteGames,
        onImportGame = viewModel::importGame,
        onClearImportError = viewModel::clearImportError,
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
    onOpenNavigation: () -> Unit,
    onStartGame: (Difficulty) -> Unit,
    onContinueGame: (Long) -> Unit,
    onDeleteGames: (Set<Long>) -> Unit,
    onImportGame: (SudokuExportFormat, String) -> Unit,
    onClearImportError: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var selectedGameIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isImportSheetVisible by rememberSaveable { mutableStateOf(false) }
    var playScreenOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    var fabMenuBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
    val currentPlayScreenOriginInWindow by rememberUpdatedState(playScreenOriginInWindow)
    val currentFabMenuBoundsInWindow by rememberUpdatedState(fabMenuBoundsInWindow)
    val isSelectionMode = selectedGameIds.isNotEmpty()
    val clearSelection = remember { { selectedGameIds = emptyList() } }
    val recentGames = remember(uiState.savedGames) {
        uiState.savedGames.filterNot(SavedGameSummary::isComplete)
    }
    val completedGames = remember(uiState.savedGames) {
        uiState.savedGames.filter(SavedGameSummary::isComplete)
    }

    LaunchedEffect(uiState.savedGames) {
        val availableIds = uiState.savedGames.mapTo(mutableSetOf()) { it.id }
        selectedGameIds = selectedGameIds.filter { it in availableIds }
    }
    LaunchedEffect(isSelectionMode) {
        if (isSelectionMode) isFabMenuExpanded = false
    }
    BackHandler(enabled = isSelectionMode) {
        clearSelection()
    }
    BackHandler(enabled = isFabMenuExpanded && !isSelectionMode) {
        isFabMenuExpanded = false
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                playScreenOriginInWindow = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(isFabMenuExpanded) {
                if (!isFabMenuExpanded) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    val positionInWindow = currentPlayScreenOriginInWindow + down.position
                    if (!currentFabMenuBoundsInWindow.contains(positionInWindow)) {
                        down.consume()
                        isFabMenuExpanded = false
                    }
                }
            }
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        IconButton(onClick = clearSelection) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(
                                    R.string.play_close_selection,
                                ),
                            )
                        }
                    } else {
                        IconButton(onClick = onOpenNavigation) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.nav_open_menu),
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                selectedGameIds = uiState.savedGames.map { game -> game.id }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = stringResource(R.string.play_select_all_games),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            PlayFabMenu(
                expanded = isFabMenuExpanded && !isSelectionMode,
                isSelectionMode = isSelectionMode,
                onExpandedChange = { isFabMenuExpanded = it },
                onDelete = {
                    onDeleteGames(selectedGameIds.toSet())
                    selectedGameIds = emptyList()
                },
                onCustomGame = {
                    isFabMenuExpanded = false
                    onClearImportError()
                    isImportSheetVisible = true
                },
                onStartGame = { difficulty ->
                    isFabMenuExpanded = false
                    onStartGame(difficulty)
                },
                modifier = Modifier
                    .offset(x = 16.dp, y = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        fabMenuBoundsInWindow = coordinates.boundsInWindow()
                    },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.hasLoadError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.play_load_error),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                SavedGamesList(
                    recentGames = recentGames,
                    completedGames = completedGames,
                    selectedGameIds = selectedGameIds.toSet(),
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onEmptyStateClick = { isFabMenuExpanded = true },
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
                    contentPadding = innerPadding,
                )
            }
        }
    }

    if (isImportSheetVisible) {
        ImportPuzzleBottomSheet(
            isImporting = uiState.isImporting,
            importError = uiState.importError,
            onImport = onImportGame,
            onInputChanged = onClearImportError,
            onDismiss = {
                if (!uiState.isImporting) {
                    isImportSheetVisible = false
                    onClearImportError()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayFabMenu(
    expanded: Boolean,
    isSelectionMode: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onCustomGame: () -> Unit,
    onStartGame: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedDescription = stringResource(R.string.play_fab_menu_expanded)
    val collapsedDescription = stringResource(R.string.play_fab_menu_collapsed)
    val deleteDescription = stringResource(R.string.play_delete_selected)
    val defaultContainerColor = ToggleFloatingActionButtonDefaults.containerColor()
    val defaultIconColor = ToggleFloatingActionButtonDefaults.iconColor()
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val errorContentColor = MaterialTheme.colorScheme.onErrorContainer
    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelectionMode) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "play_fab_selection_progress",
    )

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { checked ->
                    if (isSelectionMode) onDelete() else onExpandedChange(checked)
                },
                containerColor = { checkedProgress ->
                    lerp(
                        start = defaultContainerColor(checkedProgress),
                        stop = errorContainerColor,
                        fraction = selectionProgress,
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = when {
                        isSelectionMode -> deleteDescription
                        expanded -> expandedDescription
                        else -> collapsedDescription
                    }
                    stateDescription = when {
                        isSelectionMode -> deleteDescription
                        expanded -> expandedDescription
                        else -> collapsedDescription
                    }
                },
            ) {
                val animatedIconColor: (Float) -> Color = { progress ->
                    lerp(
                        start = defaultIconColor(progress),
                        stop = errorContentColor,
                        fraction = selectionProgress,
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    // Add and Close are the same cross rotated by 45 degrees. Keeping one vector
                    // makes the change continuous instead of swapping icons halfway through the
                    // official Toggle FAB animation.
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .animateIcon(
                                checkedProgress = { checkedProgress },
                                color = animatedIconColor,
                            )
                            .graphicsLayer {
                                rotationZ = FabCloseRotationDegrees * checkedProgress
                                alpha = 1f - selectionProgress
                                val scale = 1f - FabSelectionIconScaleDelta * selectionProgress
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier
                            .animateIcon(
                                checkedProgress = { checkedProgress },
                                color = animatedIconColor,
                            )
                            .graphicsLayer {
                                alpha = selectionProgress
                                val scale = 1f -
                                    FabSelectionIconScaleDelta * (1f - selectionProgress)
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = onCustomGame,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Input,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(R.string.play_custom_game)) },
        )
        Difficulty.entries.forEachIndexed { index, difficulty ->
            FloatingActionButtonMenuItem(
                onClick = { onStartGame(difficulty) },
                icon = { DifficultyStarsIcon(starCount = index + 1) },
                text = { Text(difficulty.label()) },
            )
        }
    }
}

@Composable
private fun DifficultyStarsIcon(starCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        repeat(starCount) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportPuzzleBottomSheet(
    isImporting: Boolean,
    importError: PlayImportError?,
    onImport: (SudokuExportFormat, String) -> Unit,
    onInputChanged: () -> Unit,
    onDismiss: () -> Unit,
) {
    var format by rememberSaveable { mutableStateOf(SudokuExportFormat.SUSSER) }
    var puzzleText by rememberSaveable { mutableStateOf("") }
    var isFormatMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.play_import_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            ExposedDropdownMenuBox(
                expanded = isFormatMenuExpanded,
                onExpandedChange = { if (!isImporting) isFormatMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = stringResource(format.titleResource()),
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isImporting,
                    label = { Text(stringResource(R.string.play_import_format)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(isFormatMenuExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = isFormatMenuExpanded,
                    onDismissRequest = { isFormatMenuExpanded = false },
                ) {
                    SudokuExportFormat.entries.forEach { item ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(stringResource(item.titleResource())) },
                            onClick = {
                                format = item
                                isFormatMenuExpanded = false
                                onInputChanged()
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = puzzleText,
                onValueChange = {
                    puzzleText = it
                    onInputChanged()
                },
                enabled = !isImporting,
                label = { Text(stringResource(R.string.play_import_puzzle)) },
                placeholder = { Text(stringResource(R.string.play_import_puzzle_placeholder)) },
                minLines = 7,
                maxLines = 12,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                isError = importError != null,
                supportingText = importError?.let { error ->
                    { Text(stringResource(error.messageResource())) }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isImporting,
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = { onImport(format, puzzleText) },
                    enabled = puzzleText.isNotBlank() && !isImporting,
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.play_import_action))
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SavedGamesList(
    recentGames: List<SavedGameSummary>,
    completedGames: List<SavedGameSummary>,
    selectedGameIds: Set<Long>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onEmptyStateClick: () -> Unit,
    onGameClick: (Long) -> Unit,
    onGameLongClick: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding(),
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        item(key = "recent_heading") {
            GamesSectionHeading(
                text = stringResource(R.string.play_recent_games),
                topPadding = 16.dp,
            )
        }

        if (recentGames.isEmpty()) {
            item(key = "empty_recent") {
                EmptyGamesIllustration(onClick = onEmptyStateClick)
            }
        } else {
            savedGameItems(
                games = recentGames,
                selectedGameIds = selectedGameIds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onGameClick = onGameClick,
                onGameLongClick = onGameLongClick,
            )
        }

        if (completedGames.isNotEmpty()) {
            item(key = "completed_heading") {
                GamesSectionHeading(
                    text = stringResource(R.string.play_completed_games),
                    topPadding = 28.dp,
                )
            }
            savedGameItems(
                games = completedGames,
                selectedGameIds = selectedGameIds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onGameClick = onGameClick,
                onGameLongClick = onGameLongClick,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.savedGameItems(
    games: List<SavedGameSummary>,
    selectedGameIds: Set<Long>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onGameClick: (Long) -> Unit,
    onGameLongClick: (Long) -> Unit,
) {
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

@Composable
private fun GamesSectionHeading(
    text: String,
    topPadding: Dp,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = topPadding, bottom = 8.dp),
    )
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
    val numbersLeft = pluralStringResource(
        R.plurals.play_numbers_left,
        game.emptyCells,
        game.emptyCells,
    )

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
            content = {
                Text(
                    text = date,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    text = "${game.difficulty.label()} · " +
                        "${formatElapsedTime(game.timeSpentSeconds)} · $numbersLeft",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
            trailingContent = if (isSelected) {
                {
                    Box(
                        modifier = Modifier.height(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.common_selected),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                null
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
private fun EmptyGamesIllustration(
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val surface = MaterialTheme.colorScheme.surfaceBright
    val actionDescription = stringResource(R.string.play_empty_instruction)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = actionDescription, onClick = onClick)
            .padding(vertical = 20.dp),
    ) {
        Canvas(modifier = Modifier.size(width = 184.dp, height = 132.dp)) {
            drawCircle(
                color = secondaryContainer,
                radius = 30.dp.toPx(),
                center = Offset(35.dp.toPx(), 37.dp.toPx()),
            )
            drawRoundRect(
                color = tertiaryContainer,
                topLeft = Offset(126.dp.toPx(), 73.dp.toPx()),
                size = Size(49.dp.toPx(), 40.dp.toPx()),
                cornerRadius = CornerRadius(18.dp.toPx()),
            )

            val boardTopLeft = Offset(47.dp.toPx(), 15.dp.toPx())
            val boardSize = 96.dp.toPx()
            drawRoundRect(
                color = surface,
                topLeft = boardTopLeft,
                size = Size(boardSize, boardSize),
                cornerRadius = CornerRadius(20.dp.toPx()),
            )
            repeat(2) { offset ->
                val position = boardTopLeft.x + (offset + 1) * boardSize / 3
                drawLine(
                    color = primary.copy(alpha = 0.35f),
                    start = Offset(position, boardTopLeft.y + 12.dp.toPx()),
                    end = Offset(position, boardTopLeft.y + boardSize - 12.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
                val horizontalPosition = boardTopLeft.y + (offset + 1) * boardSize / 3
                drawLine(
                    color = primary.copy(alpha = 0.35f),
                    start = Offset(boardTopLeft.x + 12.dp.toPx(), horizontalPosition),
                    end = Offset(boardTopLeft.x + boardSize - 12.dp.toPx(), horizontalPosition),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            listOf(0 to 0, 0 to 2, 1 to 1, 2 to 0, 2 to 2).forEach { (row, column) ->
                drawCircle(
                    color = primary,
                    radius = 4.dp.toPx(),
                    center = Offset(
                        x = boardTopLeft.x + (column + 0.5f) * boardSize / 3,
                        y = boardTopLeft.y + (row + 0.5f) * boardSize / 3,
                    ),
                )
            }

            val plusCenter = Offset(145.dp.toPx(), 27.dp.toPx())
            drawCircle(color = primaryContainer, radius = 19.dp.toPx(), center = plusCenter)
            drawLine(
                color = primary,
                start = plusCenter - Offset(8.dp.toPx(), 0f),
                end = plusCenter + Offset(8.dp.toPx(), 0f),
                strokeWidth = 3.dp.toPx(),
            )
            drawLine(
                color = primary,
                start = plusCenter - Offset(0f, 8.dp.toPx()),
                end = plusCenter + Offset(0f, 8.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
            )
        }
        Text(
            text = actionDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        val cellSize = boardSize / SudokuGridSize
        val strokeWidthPx = strokeWidth.toPx()
        val halfStroke = strokeWidthPx / 2

        board.forEachIndexed { index, value ->
            if (value != 0) {
                val row = index / SudokuGridSize
                val col = index % SudokuGridSize
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

private fun PlayImportError.messageResource(): Int = when (this) {
    PlayImportError.INVALID_FORMAT -> R.string.play_import_invalid_format
    PlayImportError.NO_UNIQUE_SOLUTION -> R.string.play_import_no_unique_solution
    PlayImportError.SAVE_FAILED -> R.string.play_import_save_failed
}

internal fun gameContainerKey(gameId: Long): String = "game_container_$gameId"

internal fun thumbnailKey(gameId: Long): String = "game_thumbnail_$gameId"

private fun List<Long>.toggle(value: Long): List<Long> =
    if (value in this) this - value else this + value

private const val SudokuGridSize = 9
private const val FabCloseRotationDegrees = 45f
private const val FabSelectionIconScaleDelta = 0.2f
