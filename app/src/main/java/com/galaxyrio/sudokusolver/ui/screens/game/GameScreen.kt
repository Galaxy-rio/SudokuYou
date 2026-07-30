package com.galaxyrio.sudokusolver.ui.screens.game

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.GameHintPanel
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screens.play.NEW_GAME_CONTAINER_KEY
import com.galaxyrio.sudokusolver.ui.screens.play.gameContainerKey
import com.galaxyrio.sudokusolver.ui.screens.play.thumbnailKey
import com.galaxyrio.sudokusolver.ui.util.formatElapsedTime
import com.galaxyrio.sudokusolver.ui.util.label
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GameRoute(
    viewModel: GameViewModel,
    boardConfig: BoardConfig,
    originGameId: Long?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showHint by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val persistenceErrorMessage = stringResource(R.string.game_save_error)
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        onPauseOrDispose {
            viewModel.onPause()
        }
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) showHint = false
    }

    LaunchedEffect(uiState.hasPersistenceError) {
        if (uiState.hasPersistenceError) {
            viewModel.clearPersistenceError()
            snackbarHostState.showSnackbar(persistenceErrorMessage)
        }
    }

    GameScreen(
        uiState = uiState,
        boardConfig = boardConfig,
        originGameId = originGameId,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onBack = onBack,
        onRetry = viewModel::retry,
        onCellSelected = viewModel::onCellSelected,
        onNumberSelected = viewModel::onNumberSelected,
        onClearSelection = viewModel::clearSelection,
        onUndo = viewModel::undo,
        onErase = viewModel::eraseSelectedCell,
        onToggleNoteMode = viewModel::toggleNoteMode,
        onFillCandidates = viewModel::fillCandidates,
        onShowHint = {
            viewModel.clearHintMessage()
            showHint = true
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )

    if (showHint) {
        fun dismissHint() {
            coroutineScope.launch {
                sheetState.hide()
                showHint = false
                viewModel.clearHintMessage()
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showHint = false
                viewModel.clearHintMessage()
            },
            sheetState = sheetState,
        ) {
            GameHintPanel(
                isHintUnavailable = uiState.isHintUnavailable,
                onDismiss = ::dismissHint,
                onApply = {
                    if (viewModel.applySingleCandidateHint()) {
                        dismissHint()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    boardConfig: BoardConfig,
    originGameId: Long?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onShowHint: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            LoadingGame(modifier = modifier)
        }

        uiState.hasLoadError -> {
            GameLoadError(
                onBack = onBack,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        else -> {
            GameScaffold(
                uiState = uiState,
                boardConfig = boardConfig,
                originGameId = originGameId,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onBack = onBack,
                onCellSelected = onCellSelected,
                onNumberSelected = onNumberSelected,
                onClearSelection = onClearSelection,
                onUndo = onUndo,
                onErase = onErase,
                onToggleNoteMode = onToggleNoteMode,
                onFillCandidates = onFillCandidates,
                onShowHint = onShowHint,
                snackbarHostState = snackbarHostState,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun LoadingGame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.game_loading),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun GameLoadError(
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.game_load_error),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GameScaffold(
    uiState: GameUiState,
    boardConfig: BoardConfig,
    originGameId: Long?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onShowHint: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            GameTopBar(
                difficulty = uiState.difficulty.label(),
                elapsedTime = formatElapsedTime(uiState.timeSpentSeconds),
                onBack = onBack,
            )
        },
        bottomBar = {
            GameBottomBar(
                isNoteMode = uiState.isNoteMode,
                canUndo = uiState.canUndo && !uiState.isComplete,
                enabled = !uiState.isComplete,
                onUndo = onUndo,
                onErase = onErase,
                onToggleNoteMode = onToggleNoteMode,
                onFillCandidates = onFillCandidates,
                onShowHint = onShowHint,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        with(sharedTransitionScope) {
            val containerKey = originGameId?.let(::gameContainerKey) ?: NEW_GAME_CONTAINER_KEY
            val containerModifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = containerKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                )

            val boardModifier = if (originGameId != null) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = thumbnailKey(originGameId)
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            } else {
                Modifier
            }

            AdaptiveGameContent(
                uiState = uiState,
                boardConfig = boardConfig,
                onCellSelected = onCellSelected,
                onNumberSelected = onNumberSelected,
                onClearSelection = onClearSelection,
                boardModifier = boardModifier,
                modifier = containerModifier,
            )
        }
    }
}

@Composable
private fun GameTopBar(
    difficulty: String,
    elapsedTime: String,
    onBack: () -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.game_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = difficulty,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = elapsedTime,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GameBottomBar(
    isNoteMode: Boolean,
    canUndo: Boolean,
    enabled: Boolean,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onShowHint: () -> Unit,
) {
    BottomAppBar {
        ToolbarAction(
            icon = Icons.AutoMirrored.Filled.Undo,
            contentDescription = stringResource(R.string.game_undo),
            enabled = canUndo,
            onClick = onUndo,
            modifier = Modifier.weight(1f),
        )
        ToolbarAction(
            icon = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = stringResource(R.string.game_erase),
            enabled = enabled,
            onClick = onErase,
            modifier = Modifier.weight(1f),
        )
        ToolbarToggleAction(
            icon = Icons.Default.Edit,
            contentDescription = stringResource(R.string.game_note_mode),
            checked = isNoteMode,
            enabled = enabled,
            onCheckedChange = { onToggleNoteMode() },
            modifier = Modifier.weight(1f),
        )
        ToolbarAction(
            icon = Icons.Default.AutoAwesome,
            contentDescription = stringResource(R.string.game_auto_candidates),
            enabled = enabled,
            onClick = onFillCandidates,
            modifier = Modifier.weight(1f),
        )
        ToolbarAction(
            icon = Icons.Default.Lightbulb,
            contentDescription = stringResource(R.string.game_hint),
            enabled = enabled,
            onClick = onShowHint,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ToolbarAction(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun ToolbarToggleAction(
    icon: ImageVector,
    contentDescription: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        FilledIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun AdaptiveGameContent(
    uiState: GameUiState,
    boardConfig: BoardConfig,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
    boardModifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val useSideBySideLayout = maxWidth >= 600.dp || maxWidth > maxHeight

        if (useSideBySideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GameBoard(
                    uiState = uiState,
                    boardConfig = boardConfig,
                    onCellSelected = onCellSelected,
                    modifier = boardModifier
                        .weight(1f)
                        .widthIn(max = 560.dp),
                )
                GameControlArea(
                    uiState = uiState,
                    onNumberSelected = onNumberSelected,
                    onClearSelection = onClearSelection,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                GameBoard(
                    uiState = uiState,
                    boardConfig = boardConfig,
                    onCellSelected = onCellSelected,
                    modifier = boardModifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                GameControlArea(
                    uiState = uiState,
                    onNumberSelected = onNumberSelected,
                    onClearSelection = onClearSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GameBoard(
    uiState: GameUiState,
    boardConfig: BoardConfig,
    onCellSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SudokuBoard(
        sudoku = uiState.sudoku,
        onCellClick = onCellSelected,
        selectedRow = uiState.selectedCell?.row,
        selectedCol = uiState.selectedCell?.col,
        highlightNumber = uiState.highlightedNumber,
        config = boardConfig,
        modifier = modifier,
    )
}

@Composable
private fun GameControlArea(
    uiState: GameUiState,
    onNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.isComplete) {
            CompletedGame(
                elapsedTime = formatElapsedTime(uiState.timeSpentSeconds),
            )
        } else {
            NumberPad(
                selectedNumber = uiState.selectedNumber,
                onNumberClick = onNumberSelected,
                onBackgroundClick = onClearSelection,
                modifier = Modifier.widthIn(max = 360.dp),
            )
        }
    }
}

@Composable
private fun CompletedGame(elapsedTime: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.game_completed),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.game_congratulations),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.game_completion_time, elapsedTime),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        val party = remember {
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(
                    duration = 100,
                    TimeUnit.MILLISECONDS,
                ).max(100),
            )
        }
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(party),
        )
    }
}
