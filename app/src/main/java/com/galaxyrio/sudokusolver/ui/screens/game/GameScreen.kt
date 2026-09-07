package com.galaxyrio.sudokusolver.ui.screens.game

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.game.HintIssue
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.AdvancedNumberPad
import com.galaxyrio.sudokusolver.ui.components.GameHintPanel
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.components.toComposeColor
import com.galaxyrio.sudokusolver.ui.guide.GuideStep
import com.galaxyrio.sudokusolver.ui.guide.GuideSeries
import com.galaxyrio.sudokusolver.ui.guide.GuideMotionDurationMillis
import com.galaxyrio.sudokusolver.ui.guide.GuideTarget
import com.galaxyrio.sudokusolver.ui.guide.LocalUsageGuide
import com.galaxyrio.sudokusolver.ui.guide.guideTarget
import com.galaxyrio.sudokusolver.ui.screens.play.gameContainerKey
import com.galaxyrio.sudokusolver.ui.screens.play.thumbnailKey
import com.galaxyrio.sudokusolver.ui.util.formatElapsedTime
import com.galaxyrio.sudokusolver.ui.util.label
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.filterNotNull
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
    val usageGuide = LocalUsageGuide.current
    val isWatchingGuide = usageGuide?.isGameGuide == true
    val guideStep = usageGuide?.step
    // Preview the advanced controls without changing the actual game or its annotations.
    val displayedUiState = if (isWatchingGuide) {
        uiState.copy(
            isAdvancedMode = guideStep?.previewsAdvancedMode == true,
            isAdvancedNoteMode = guideStep?.previewsAdvancedMode == true,
            advancedInputTool = AdvancedInputTool.NONE,
        )
    } else uiState
    // ModalBottomSheet reserves space outside its content for the drag handle and its internal
    // spacing. Keep this value shared by overlap detection and the content height cap so both
    // calculations describe the complete visible sheet.
    val sheetChromeHeight = 80.dp
    var showHint by rememberSaveable { mutableStateOf(false) }
    var handledImmediateHintRequestId by rememberSaveable { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val persistenceErrorMessage = stringResource(R.string.game_save_error)
    val clipboardLabel = stringResource(R.string.game_export_clipboard_label)
    val originalCopiedMessage = stringResource(R.string.game_original_copied)
    val currentCopiedMessage = stringResource(R.string.game_current_copied)
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    var isHintLayoutElevated by remember { mutableStateOf(false) }
    var elevatedBoardBottomPx by remember { mutableFloatStateOf(Float.NaN) }
    var normalNumberPadTopPx by remember { mutableFloatStateOf(Float.NaN) }
    var windowBottomPx by remember { mutableFloatStateOf(Float.NaN) }
    var windowOrigin by remember { mutableStateOf(Offset.Zero) }

    fun copyToClipboard(text: String, confirmation: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(clipboardLabel, text))
        coroutineScope.launch { snackbarHostState.showSnackbar(confirmation) }
    }

    LifecycleResumeEffect(viewModel, isWatchingGuide) {
        if (!isWatchingGuide) viewModel.onResume()
        onPauseOrDispose {
            viewModel.onPause()
        }
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) showHint = false
    }

    LaunchedEffect(uiState.immediateHintRequestId) {
        if (uiState.immediateHintRequestId > handledImmediateHintRequestId) {
            handledImmediateHintRequestId = uiState.immediateHintRequestId
            isHintLayoutElevated = false
            elevatedBoardBottomPx = Float.NaN
            showHint = true
        }
    }

    LaunchedEffect(showHint) {
        if (!showHint) {
            isHintLayoutElevated = false
            elevatedBoardBottomPx = Float.NaN
        }
    }

    LaunchedEffect(showHint, normalNumberPadTopPx) {
        if (showHint && normalNumberPadTopPx.isFinite()) {
            snapshotFlow {
                runCatching { sheetState.requireOffset() }.getOrNull()
            }
                .filterNotNull()
                .collect { sheetTopPx ->
                    if (!isHintLayoutElevated && sheetTopPx < normalNumberPadTopPx) {
                        isHintLayoutElevated = true
                    }
                }
        }
    }

    LaunchedEffect(uiState.hasPersistenceError) {
        if (uiState.hasPersistenceError) {
            viewModel.clearPersistenceError()
            snackbarHostState.showSnackbar(persistenceErrorMessage)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                windowBottomPx = coordinates.boundsInWindow().bottom
                windowOrigin = coordinates.boundsInWindow().topLeft
            },
    ) {
        val density = LocalDensity.current
        val maximumHalfSheetHeight = maxHeight * 0.56f
        val availableHeightBelowBoard = if (
            isHintLayoutElevated && elevatedBoardBottomPx.isFinite()
        ) {
            with(density) {
                ((windowBottomPx.takeIf(Float::isFinite) ?: maxHeight.toPx()) -
                    elevatedBoardBottomPx - 8.dp.toPx())
                    .coerceAtLeast(1f)
                    .toDp()
            }
        } else {
            maximumHalfSheetHeight
        }
        val sheetMaximumHeight = minOf(
            maximumHalfSheetHeight,
            availableHeightBelowBoard,
        )

        GameScreen(
            uiState = displayedUiState,
            boardConfig = boardConfig,
            originGameId = originGameId,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            isHintVisible = showHint,
            isHintLayoutElevated = isHintLayoutElevated,
            onBoardBoundsChanged = { bounds ->
                if (isHintLayoutElevated) elevatedBoardBottomPx = bounds.bottom
            },
            onNumberPadBoundsChanged = { bounds ->
                if (!isHintLayoutElevated) normalNumberPadTopPx = bounds.top
            },
            onBack = onBack,
            onRetry = viewModel::retry,
            onCellSelected = viewModel::onCellSelected,
            onNumberSelected = viewModel::onNumberSelected,
            onAdvancedNumberSelected = viewModel::onAdvancedNumberSelected,
            onClearSelection = viewModel::clearSelection,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onErase = viewModel::eraseSelectedCell,
            onToggleNoteMode = viewModel::toggleNoteMode,
            onFillCandidates = viewModel::fillCandidates,
            onDeleteDraft = viewModel::deleteAdvancedDraft,
            onExportOriginal = {
                copyToClipboard(viewModel.exportOriginalText(), originalCopiedMessage)
            },
            onExportCurrent = {
                copyToClipboard(viewModel.exportCurrentText(), currentCopiedMessage)
            },
            onRestart = viewModel::restartGame,
            onAdvancedModeChange = viewModel::setAdvancedMode,
            onToggleAdvancedNoteMode = viewModel::toggleAdvancedNoteMode,
            onToggleBivalueHighlights = viewModel::toggleBivalueHighlights,
            onTogglePaintTool = viewModel::togglePaintTool,
            onToggleFrameHighlights = viewModel::toggleFrameHighlights,
            onToggleSolidLineTool = {
                viewModel.toggleLineTool(AdvancedNoteLineStyle.SOLID)
            },
            onToggleDashedLineTool = {
                viewModel.toggleLineTool(AdvancedNoteLineStyle.DASHED)
            },
            onAdvancedColorSelected = viewModel::selectAdvancedColor,
            onShowHint = {
                isHintLayoutElevated = false
                elevatedBoardBottomPx = Float.NaN
                viewModel.prepareHintTrace()
                showHint = true
            },
            snackbarHostState = snackbarHostState,
            modifier = Modifier.fillMaxSize(),
        )

        if (guideStep?.series == GuideSeries.MENU) {
            usageGuide.anchors[GuideTarget.MORE]?.bounds?.let { buttonBounds ->
                val localButton = buttonBounds.translate(-windowOrigin)
                val menuWidth = minOf(256.dp, maxWidth - 32.dp)
                val menuTop = with(density) { localButton.bottom.toDp() } + 4.dp
                val menuLeft = (with(density) { localButton.right.toDp() } - menuWidth)
                    .coerceIn(16.dp, (maxWidth - menuWidth - 16.dp).coerceAtLeast(16.dp))
                val reveal by animateFloatAsState(
                    targetValue = if (guideStep == GuideStep.MENU) 1f else 0f,
                    animationSpec = tween(GuideMotionDurationMillis, easing = FastOutSlowInEasing),
                    label = "guide_menu_reveal",
                )
                // An in-window copy shares the real menu contents and stays under the guide.
                // Measure it even when collapsed to keep this series' navigation stationary.
                Surface(
                    modifier = Modifier.offset(menuLeft, menuTop).width(menuWidth)
                        .heightIn(max = (maxHeight - menuTop - 24.dp).coerceAtLeast(1.dp))
                        .guideTarget(GuideTarget.MORE_MENU)
                        .graphicsLayer {
                            alpha = reveal
                            scaleX = 0.85f + 0.15f * reveal
                            scaleY = 0.85f + 0.15f * reveal
                            transformOrigin = TransformOrigin(1f, 0f)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 8.dp,
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                        GameMenuItems(
                            isAdvancedMode = false,
                            hasAdvancedDraft = false,
                            gameActionsEnabled = true,
                            onToggleAdvancedMode = usageGuide::next,
                            onFillCandidates = {},
                            onDeleteDraft = {},
                            onExportOriginal = {},
                            onExportCurrent = {},
                            onRestart = {},
                        )
                    }
                }
            }
        }

        if (showHint) {
            fun dismissHint() {
                coroutineScope.launch {
                    sheetState.hide()
                    showHint = false
                    viewModel.clearHintTrace()
                }
            }

            ModalBottomSheet(
                onDismissRequest = {
                    showHint = false
                    viewModel.clearHintTrace()
                },
                sheetState = sheetState,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f),
                dragHandle = {
                    BottomSheetDefaults.DragHandle(
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                },
            ) {
                GameHintPanel(
                    isLoading = uiState.isHintLoading,
                    trace = uiState.hintTrace,
                    issue = uiState.hintIssue,
                    selectedStepIndex = uiState.selectedHintStepIndex,
                    areHintDetailsVisible = uiState.areHintDetailsVisible,
                    showErrorDetails = uiState.showErrorDetails,
                    coordinateNotation = uiState.coordinateNotation,
                    onStepSelected = viewModel::selectHintStep,
                    onRevealDetails = viewModel::revealHintDetails,
                    onApplyNext = {
                        if (viewModel.applyHintAction()) {
                            dismissHint()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = (sheetMaximumHeight - sheetChromeHeight).coerceAtLeast(1.dp),
                        ),
                )
            }
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
    isHintVisible: Boolean,
    isHintLayoutElevated: Boolean,
    onBoardBoundsChanged: (Rect) -> Unit,
    onNumberPadBoundsChanged: (Rect) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onAdvancedNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onDeleteDraft: () -> Unit,
    onExportOriginal: () -> Unit,
    onExportCurrent: () -> Unit,
    onRestart: () -> Unit,
    onAdvancedModeChange: (Boolean) -> Unit,
    onToggleAdvancedNoteMode: () -> Unit,
    onToggleBivalueHighlights: () -> Unit,
    onTogglePaintTool: () -> Unit,
    onToggleFrameHighlights: () -> Unit,
    onToggleSolidLineTool: () -> Unit,
    onToggleDashedLineTool: () -> Unit,
    onAdvancedColorSelected: (Int) -> Unit,
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
                isHintVisible = isHintVisible,
                isHintLayoutElevated = isHintLayoutElevated,
                onBoardBoundsChanged = onBoardBoundsChanged,
                onNumberPadBoundsChanged = onNumberPadBoundsChanged,
                onBack = onBack,
                onCellSelected = onCellSelected,
                onNumberSelected = onNumberSelected,
                onAdvancedNumberSelected = onAdvancedNumberSelected,
                onClearSelection = onClearSelection,
                onUndo = onUndo,
                onRedo = onRedo,
                onErase = onErase,
                onToggleNoteMode = onToggleNoteMode,
                onFillCandidates = onFillCandidates,
                onDeleteDraft = onDeleteDraft,
                onExportOriginal = onExportOriginal,
                onExportCurrent = onExportCurrent,
                onRestart = onRestart,
                onAdvancedModeChange = onAdvancedModeChange,
                onToggleAdvancedNoteMode = onToggleAdvancedNoteMode,
                onToggleBivalueHighlights = onToggleBivalueHighlights,
                onTogglePaintTool = onTogglePaintTool,
                onToggleFrameHighlights = onToggleFrameHighlights,
                onToggleSolidLineTool = onToggleSolidLineTool,
                onToggleDashedLineTool = onToggleDashedLineTool,
                onAdvancedColorSelected = onAdvancedColorSelected,
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
    isHintVisible: Boolean,
    isHintLayoutElevated: Boolean,
    onBoardBoundsChanged: (Rect) -> Unit,
    onNumberPadBoundsChanged: (Rect) -> Unit,
    onBack: () -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onAdvancedNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onDeleteDraft: () -> Unit,
    onExportOriginal: () -> Unit,
    onExportCurrent: () -> Unit,
    onRestart: () -> Unit,
    onAdvancedModeChange: (Boolean) -> Unit,
    onToggleAdvancedNoteMode: () -> Unit,
    onToggleBivalueHighlights: () -> Unit,
    onTogglePaintTool: () -> Unit,
    onToggleFrameHighlights: () -> Unit,
    onToggleSolidLineTool: () -> Unit,
    onToggleDashedLineTool: () -> Unit,
    onAdvancedColorSelected: (Int) -> Unit,
    onShowHint: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = !isHintLayoutElevated,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                    label = "game_top_bar_visibility",
                ) {
                    GameTopBar(
                        difficulty = uiState.difficulty.label(),
                        elapsedTime = formatElapsedTime(uiState.timeSpentSeconds),
                        isAdvancedMode = uiState.isAdvancedMode,
                        hasAdvancedDraft = uiState.hasAdvancedDraft,
                        gameActionsEnabled = !uiState.isComplete,
                        onBack = onBack,
                        onAdvancedModeChange = onAdvancedModeChange,
                        onFillCandidates = onFillCandidates,
                        onDeleteDraft = onDeleteDraft,
                        onExportOriginal = onExportOriginal,
                        onExportCurrent = onExportCurrent,
                        onRestart = onRestart,
                    )
                }

                // Keep the top-bar slot non-zero throughout its exit animation. Otherwise,
                // Scaffold adds the status-bar inset only after AnimatedVisibility reaches zero,
                // which makes the board first enter the status bar and then snap back down.
                Column {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        },
        bottomBar = {
            GameBottomBar(
                isNoteMode = uiState.isNoteMode,
                isAdvancedColorToolActive = uiState.isAdvancedColorToolActive,
                selectedAdvancedColorIndex = uiState.selectedAdvancedColorIndex,
                canUndo = uiState.canUndo && !uiState.isComplete,
                canRedo = uiState.canRedo && !uiState.isComplete,
                enabled = !uiState.isComplete,
                onUndo = onUndo,
                onRedo = onRedo,
                onErase = onErase,
                onToggleNoteMode = onToggleNoteMode,
                onShowHint = onShowHint,
                onAdvancedColorSelected = onAdvancedColorSelected,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        with(sharedTransitionScope) {
            val baseContainerModifier = Modifier
                .fillMaxSize()
                .padding(padding)
            val containerModifier = if (originGameId != null) {
                baseContainerModifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = gameContainerKey(originGameId)
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            } else {
                baseContainerModifier
            }

            val boardModifier = if (originGameId != null) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = thumbnailKey(originGameId)
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = RemeasureToBounds,
                )
            } else {
                Modifier
            }

            AdaptiveGameContent(
                uiState = uiState,
                boardConfig = boardConfig,
                isHintVisible = isHintVisible,
                isHintLayoutElevated = isHintLayoutElevated,
                onBoardBoundsChanged = onBoardBoundsChanged,
                onNumberPadBoundsChanged = onNumberPadBoundsChanged,
                onCellSelected = onCellSelected,
                onNumberSelected = onNumberSelected,
                onAdvancedNumberSelected = onAdvancedNumberSelected,
                onClearSelection = onClearSelection,
                onToggleAdvancedNoteMode = onToggleAdvancedNoteMode,
                onToggleBivalueHighlights = onToggleBivalueHighlights,
                onTogglePaintTool = onTogglePaintTool,
                onToggleFrameHighlights = onToggleFrameHighlights,
                onToggleSolidLineTool = onToggleSolidLineTool,
                onToggleDashedLineTool = onToggleDashedLineTool,
                boardModifier = boardModifier,
                modifier = containerModifier,
            )
        }
    }
}

@Composable
private fun GameMenuItems(
    isAdvancedMode: Boolean,
    hasAdvancedDraft: Boolean,
    gameActionsEnabled: Boolean,
    onToggleAdvancedMode: () -> Unit,
    onFillCandidates: () -> Unit,
    onDeleteDraft: () -> Unit,
    onExportOriginal: () -> Unit,
    onExportCurrent: () -> Unit,
    onRestart: () -> Unit,
) {
    val modeLabel = stringResource(
        if (isAdvancedMode) R.string.game_simple_mode else R.string.game_advanced_mode,
    )
    DropdownMenuItem(
        text = { Text(modeLabel) },
        onClick = onToggleAdvancedMode,
        modifier = Modifier.guideTarget(GuideTarget.ADVANCED_MODE, modeLabel, onToggleAdvancedMode),
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.game_auto_candidates)) },
        enabled = gameActionsEnabled,
        onClick = onFillCandidates,
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.game_delete_draft)) },
        enabled = gameActionsEnabled && hasAdvancedDraft,
        onClick = onDeleteDraft,
    )
    HorizontalDivider()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.game_export_original)) },
        onClick = onExportOriginal,
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.game_export_current)) },
        onClick = onExportCurrent,
    )
    HorizontalDivider()
    DropdownMenuItem(
        text = { Text(stringResource(R.string.game_restart)) },
        enabled = gameActionsEnabled,
        onClick = onRestart,
    )
}

@Composable
private fun GameTopBar(
    difficulty: String,
    elapsedTime: String,
    isAdvancedMode: Boolean,
    hasAdvancedDraft: Boolean,
    gameActionsEnabled: Boolean,
    onBack: () -> Unit,
    onAdvancedModeChange: (Boolean) -> Unit,
    onFillCandidates: () -> Unit,
    onDeleteDraft: () -> Unit,
    onExportOriginal: () -> Unit,
    onExportCurrent: () -> Unit,
    onRestart: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var showRestartConfirmation by rememberSaveable { mutableStateOf(false) }
    val usageGuide = LocalUsageGuide.current
    val moreLabel = stringResource(R.string.game_more_options)
    val openMenu: () -> Unit = {
        if (usageGuide?.isGameGuide == true) {
            usageGuide.onMoreExpanded(usageGuide.step != GuideStep.MENU)
        } else menuExpanded = true
    }

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
            actions = {
                Box {
                    IconButton(
                        onClick = openMenu,
                        modifier = Modifier.guideTarget(GuideTarget.MORE, moreLabel, openMenu),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.game_more_options),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        GameMenuItems(
                            isAdvancedMode = isAdvancedMode,
                            hasAdvancedDraft = hasAdvancedDraft,
                            gameActionsEnabled = gameActionsEnabled,
                            onToggleAdvancedMode = {
                                onAdvancedModeChange(!isAdvancedMode)
                                menuExpanded = false
                            },
                            onFillCandidates = { onFillCandidates(); menuExpanded = false },
                            onDeleteDraft = { onDeleteDraft(); menuExpanded = false },
                            onExportOriginal = { onExportOriginal(); menuExpanded = false },
                            onExportCurrent = { onExportCurrent(); menuExpanded = false },
                            onRestart = { menuExpanded = false; showRestartConfirmation = true },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
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

    if (showRestartConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmation = false },
            title = { Text(stringResource(R.string.game_restart_confirm_title)) },
            text = { Text(stringResource(R.string.game_restart_confirm_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showRestartConfirmation = false
                        onRestart()
                    },
                ) {
                    Text(stringResource(R.string.game_restart))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showRestartConfirmation = false },
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun GameBottomBar(
    isNoteMode: Boolean,
    isAdvancedColorToolActive: Boolean,
    selectedAdvancedColorIndex: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    enabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onErase: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onShowHint: () -> Unit,
    onAdvancedColorSelected: (Int) -> Unit,
) {
    if (isAdvancedColorToolActive) {
        AdvancedColorBottomBar(
            selectedColorIndex = selectedAdvancedColorIndex,
            onColorSelected = onAdvancedColorSelected,
        )
        return
    }

    BottomAppBar {
        ToolbarAction(
            icon = ImageVector.vectorResource(R.drawable.ic_material_symbol_undo),
            contentDescription = stringResource(R.string.game_undo),
            enabled = canUndo,
            onClick = onUndo,
            modifier = Modifier.weight(1f).guideTarget(GuideTarget.UNDO),
        )
        ToolbarAction(
            icon = ImageVector.vectorResource(R.drawable.ic_material_symbol_redo),
            contentDescription = stringResource(R.string.game_redo),
            enabled = canRedo,
            onClick = onRedo,
            modifier = Modifier.weight(1f).guideTarget(GuideTarget.REDO),
        )
        ToolbarAction(
            icon = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = stringResource(R.string.game_erase),
            enabled = enabled,
            onClick = onErase,
            modifier = Modifier.weight(1f).guideTarget(GuideTarget.ERASE),
        )
        ToolbarToggleAction(
            icon = Icons.Default.Edit,
            contentDescription = stringResource(R.string.game_note_mode),
            checked = isNoteMode,
            enabled = enabled,
            onCheckedChange = { onToggleNoteMode() },
            modifier = Modifier.weight(1f).guideTarget(GuideTarget.NOTES),
        )
        ToolbarAction(
            icon = Icons.Default.Lightbulb,
            contentDescription = stringResource(R.string.game_hint),
            enabled = enabled,
            onClick = onShowHint,
            modifier = Modifier.weight(1f).guideTarget(GuideTarget.HINT),
        )
    }
}

@Composable
private fun AdvancedColorBottomBar(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
) {
    BottomAppBar {
        AdvancedNoteColor.entries.forEachIndexed { index, noteColor ->
            AdvancedColorSwatch(
                color = noteColor.toComposeColor(),
                selected = selectedColorIndex == index,
                contentDescription = stringResource(
                    R.string.game_advanced_color_option,
                    index + 1,
                ),
                onClick = { onColorSelected(index) },
                modifier = Modifier.weight(1f),
            )
        }
        AdvancedColorSwatch(
            color = Color.White,
            selected = selectedColorIndex == AdvancedNoteColor.entries.size,
            contentDescription = stringResource(R.string.game_advanced_erase_colors),
            onClick = { onColorSelected(AdvancedNoteColor.entries.size) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AdvancedColorSwatch(
    color: Color,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .semantics {
                this.contentDescription = contentDescription
                this.selected = selected
            }
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 36.dp else 30.dp)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = outlineColor,
                    shape = CircleShape,
                )
                .padding(if (selected) 4.dp else 3.dp)
                .background(color, CircleShape),
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
    ExpressiveToolbarButton(
        icon = icon,
        contentDescription = contentDescription,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    )
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
    ExpressiveToolbarButton(
        icon = icon,
        contentDescription = contentDescription,
        enabled = enabled,
        checked = checked,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
    )
}

@Composable
private fun ExpressiveToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "toolbar_action_scale",
    )
    val actionRipple = ripple(
        bounded = false,
        radius = 400.dp,
    )
    val interactionModifier = if (checked == null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = actionRipple,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier.toggleable(
            value = checked,
            interactionSource = interactionSource,
            indication = actionRipple,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = { onClick() },
        )
    }
    val isSelected = checked == true
    val containerColor = if (isSelected && enabled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(interactionModifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(48.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun AdaptiveGameContent(
    uiState: GameUiState,
    boardConfig: BoardConfig,
    isHintVisible: Boolean,
    isHintLayoutElevated: Boolean,
    onBoardBoundsChanged: (Rect) -> Unit,
    onNumberPadBoundsChanged: (Rect) -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onAdvancedNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onToggleAdvancedNoteMode: () -> Unit,
    onToggleBivalueHighlights: () -> Unit,
    onTogglePaintTool: () -> Unit,
    onToggleFrameHighlights: () -> Unit,
    onToggleSolidLineTool: () -> Unit,
    onToggleDashedLineTool: () -> Unit,
    modifier: Modifier = Modifier,
    boardModifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val useSideBySideLayout = maxWidth >= 600.dp || maxWidth > maxHeight

        if (useSideBySideLayout) {
            // A weighted child otherwise receives a fixed width, forcing its square board
            // beyond the available height and moving the guide target over both app bars.
            val boardSize = minOf((maxWidth - 56.dp) / 2, maxHeight - 32.dp, 560.dp)
                .coerceAtLeast(1.dp)
            val density = LocalDensity.current
            val boardDensity = Density(
                density.density,
                // Keep the fixed digit/candidate typography inside the smaller square cells.
                fontScale = minOf(density.fontScale, boardSize.value / 324f),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = if (isHintLayoutElevated) {
                    Alignment.Top
                } else {
                    Alignment.CenterVertically
                },
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = if (isHintLayoutElevated) Alignment.TopCenter else Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalDensity provides boardDensity) {
                        GameBoard(
                            uiState = uiState,
                            boardConfig = boardConfig,
                            isHintVisible = isHintVisible,
                            onBoardBoundsChanged = onBoardBoundsChanged,
                            onCellSelected = onCellSelected,
                            modifier = Modifier.size(boardSize).then(boardModifier),
                        )
                    }
                }
                GameControlArea(
                    uiState = uiState,
                    isNumberPadVisible = !isHintLayoutElevated,
                    onNumberPadBoundsChanged = onNumberPadBoundsChanged,
                    onNumberSelected = onNumberSelected,
                    onAdvancedNumberSelected = onAdvancedNumberSelected,
                    onClearSelection = onClearSelection,
                    onToggleAdvancedNoteMode = onToggleAdvancedNoteMode,
                    onToggleBivalueHighlights = onToggleBivalueHighlights,
                    onTogglePaintTool = onTogglePaintTool,
                    onToggleFrameHighlights = onToggleFrameHighlights,
                    onToggleSolidLineTool = onToggleSolidLineTool,
                    onToggleDashedLineTool = onToggleDashedLineTool,
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
                    isHintVisible = isHintVisible,
                    onBoardBoundsChanged = onBoardBoundsChanged,
                    onCellSelected = onCellSelected,
                    // The shared bounds must describe the board itself, not this layout's
                    // surrounding spacing. Keeping the padding outside the shared modifier
                    // makes the full-size board line up exactly with the saved-game thumbnail
                    // during a predictive-back container transform.
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 4.dp,
                            end = 16.dp,
                            bottom = 4.dp,
                        )
                        .then(boardModifier),
                )
                GameControlArea(
                    uiState = uiState,
                    isNumberPadVisible = !isHintLayoutElevated,
                    onNumberPadBoundsChanged = onNumberPadBoundsChanged,
                    onNumberSelected = onNumberSelected,
                    onAdvancedNumberSelected = onAdvancedNumberSelected,
                    onClearSelection = onClearSelection,
                    onToggleAdvancedNoteMode = onToggleAdvancedNoteMode,
                    onToggleBivalueHighlights = onToggleBivalueHighlights,
                    onTogglePaintTool = onTogglePaintTool,
                    onToggleFrameHighlights = onToggleFrameHighlights,
                    onToggleSolidLineTool = onToggleSolidLineTool,
                    onToggleDashedLineTool = onToggleDashedLineTool,
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
    isHintVisible: Boolean,
    onBoardBoundsChanged: (Rect) -> Unit,
    onCellSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trace = uiState.hintTrace
    val issue = uiState.hintIssue.takeIf { isHintVisible }
    val areHintDetailsVisible = uiState.areHintDetailsVisible
    val hintIndex = trace?.steps?.takeIf { it.isNotEmpty() }?.let { steps ->
        uiState.selectedHintStepIndex.coerceIn(steps.indices)
    }
    val overlayStep: SolveStep? = if (
        isHintVisible && issue == null && hintIndex != null && areHintDetailsVisible
    ) {
        trace.steps[hintIndex]
    } else {
        null
    }
    val hintState: SolverState? = if (
        isHintVisible && issue == null && hintIndex != null && areHintDetailsVisible
    ) {
        trace.stateBeforeStep(hintIndex)
    } else {
        null
    }
    val displayedSudoku = remember(hintState, uiState.sudoku) {
        hintState?.toDisplaySudoku(uiState.sudoku) ?: uiState.sudoku
    }
    val correctionCells: Set<CellRef> = when (issue) {
        is HintIssue.IncorrectValues -> issue.entries.mapTo(mutableSetOf()) { it.cell }
        is HintIssue.MissingCandidates -> if (uiState.showErrorDetails) {
            emptySet()
        } else {
            issue.candidates.mapTo(mutableSetOf()) { it.cell }
        }
        else -> emptySet()
    }
    val correctionCandidates: Set<CandidateRef> = when (issue) {
        is HintIssue.MissingCandidates -> if (uiState.showErrorDetails) {
            issue.candidates.toSet()
        } else {
            emptySet()
        }
        else -> emptySet()
    }
    val showAdvancedAnnotations = uiState.isAdvancedMode && !isHintVisible
    val showAdvancedHighlights = showAdvancedAnnotations && uiState.isAdvancedNoteMode
    val highlightedNumbers = when {
        isHintVisible -> emptySet()
        showAdvancedHighlights -> uiState.advancedNotes.highlightedDigits
        else -> setOfNotNull(uiState.highlightedNumber)
    }

    SudokuBoard(
        sudoku = displayedSudoku,
        onCellClick = onCellSelected,
        selectedRow = uiState.selectedCell?.row.takeUnless { isHintVisible },
        selectedCol = uiState.selectedCell?.col.takeUnless { isHintVisible },
        highlightNumbers = highlightedNumbers,
        advancedNotes = uiState.advancedNotes.takeIf { showAdvancedAnnotations },
        showAdvancedHighlights = showAdvancedHighlights,
        pendingLineStart = uiState.pendingLineStart.takeIf { showAdvancedHighlights },
        overlayStep = overlayStep,
        correctionCells = correctionCells,
        correctionCandidates = correctionCandidates,
        onBoardBoundsChanged = onBoardBoundsChanged.takeIf { isHintVisible },
        config = boardConfig,
        modifier = modifier,
    )
}

private fun SolverState.toDisplaySudoku(reference: Sudoku): Sudoku = Sudoku(
    cells = List(Sudoku.CELL_COUNT) { index ->
        val cellRef = CellRef.fromIndex(index)
        val value = valueAt(cellRef)
        Cell(
            value = value,
            candidates = if (value == 0) candidatesAt(cellRef) else emptySet(),
            isFixed = value != 0 && reference.cells[index].isFixed,
        )
    }
)

@Composable
private fun GameControlArea(
    uiState: GameUiState,
    isNumberPadVisible: Boolean,
    onNumberPadBoundsChanged: (Rect) -> Unit,
    onNumberSelected: (Int) -> Unit,
    onAdvancedNumberSelected: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onToggleAdvancedNoteMode: () -> Unit,
    onToggleBivalueHighlights: () -> Unit,
    onTogglePaintTool: () -> Unit,
    onToggleFrameHighlights: () -> Unit,
    onToggleSolidLineTool: () -> Unit,
    onToggleDashedLineTool: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.isComplete) {
            CompletedGame(
                elapsedTime = formatElapsedTime(uiState.timeSpentSeconds),
            )
        } else {
            AnimatedVisibility(
                visible = isNumberPadVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
                label = "game_number_pad_visibility",
            ) {
                if (uiState.isAdvancedMode) {
                    AdvancedNumberPad(
                        isAdvancedNoteMode = uiState.isAdvancedNoteMode,
                        selectedNumber = uiState.selectedNumber,
                        highlightedNumbers = uiState.advancedNotes.highlightedDigits,
                        showBivalueHighlights =
                            uiState.advancedNotes.highlightBivalueCandidates,
                        frameHighlights = uiState.advancedNotes.frameHighlightedCells,
                        isPaintSelected =
                            uiState.advancedInputTool == AdvancedInputTool.PAINT,
                        isSolidLineSelected =
                            uiState.advancedInputTool == AdvancedInputTool.SOLID_LINE,
                        isDashedLineSelected =
                            uiState.advancedInputTool == AdvancedInputTool.DASHED_LINE,
                        onNumberClick = onAdvancedNumberSelected,
                        onAdvancedNoteModeClick = onToggleAdvancedNoteMode,
                        onBivalueClick = onToggleBivalueHighlights,
                        onPaintClick = onTogglePaintTool,
                        onFrameClick = onToggleFrameHighlights,
                        onSolidLineClick = onToggleSolidLineTool,
                        onDashedLineClick = onToggleDashedLineTool,
                        onBackgroundClick = onClearSelection,
                        onPadBoundsChanged = onNumberPadBoundsChanged,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    NumberPad(
                        selectedNumber = uiState.selectedNumber,
                        onNumberClick = onNumberSelected,
                        onBackgroundClick = onClearSelection,
                        onPadBoundsChanged = onNumberPadBoundsChanged,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
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
