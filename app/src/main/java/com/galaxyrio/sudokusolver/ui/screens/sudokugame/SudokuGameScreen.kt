package com.galaxyrio.sudokusolver.ui.screens.sudokugame

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.galaxyrio.sudokusolver.database.AppDatabase
import com.galaxyrio.sudokusolver.database.SudokuEntity
import com.galaxyrio.sudokusolver.game.Sudoku
import com.galaxyrio.sudokusolver.game.generator.CandidateCalculator
import com.galaxyrio.sudokusolver.game.generator.SudokuGenerator
import com.galaxyrio.sudokusolver.game.validator.SudokuValidator
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.GameHintPanel
import com.galaxyrio.sudokusolver.ui.components.NumberPad
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screens.play.Difficulty
import com.galaxyrio.sudokusolver.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SudokuGameScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    difficulty: Difficulty,
    gameId: Long? = null,
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).sudokuDao() }
    val scope = rememberCoroutineScope()
    var currentGameId by remember { mutableStateOf(gameId) }

    var timeSpentSeconds by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Hint state
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Initialize the board using the generator
    // We use a nullable state to show loading until we check the DB
    var sudokuState by remember { mutableStateOf<Sudoku?>(null) }

    val coloredBoard by viewModel.coloredBoard.collectAsState()
    val showLines by viewModel.positionLines.collectAsState()
    val showBlock by viewModel.positionBlock.collectAsState()
    val altErrorColor by viewModel.alternativeErrorColor.collectAsState()

    LaunchedEffect(key1 = gameId, key2 = difficulty) {
        withContext(Dispatchers.IO) {
            var loadedGame: Sudoku? = null
            if (gameId != null) {
                val savedGame = dao.getGame(gameId)
                if (savedGame != null) {
                    loadedGame = savedGame.sudoku
                    timeSpentSeconds = savedGame.timeSpent
                }
            }

            if (loadedGame != null) {
                sudokuState = loadedGame
            } else {
                // New Game
                val clues = when (difficulty) {
                    Difficulty.EASY -> 80
                    Difficulty.MEDIUM -> 30
                    Difficulty.HARD -> 24
                }
                val newGame = SudokuGenerator().generate(clues)
                sudokuState = newGame
                timeSpentSeconds = 0
                // Save immediately to get an ID
                val newId = dao.saveGame(
                    SudokuEntity(
                        difficulty = difficulty,
                        sudoku = newGame,
                        timeSpent = 0
                    )
                )
                currentGameId = newId
            }
        }
    }

    if (sudokuState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var sudoku by remember(sudokuState) { mutableStateOf(sudokuState!!) }

    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var selectedCol by remember { mutableStateOf<Int?>(null) }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var isNoteMode by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    // Timer Logic
    LaunchedEffect(isTimerRunning, showWinDialog) {
        if (isTimerRunning && !showWinDialog) {
            val startTime = System.currentTimeMillis()
            val startSeconds = timeSpentSeconds
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                timeSpentSeconds = startSeconds + elapsed
            }
        }
    }

    // Lifecycle observer to pause timer when app is backgrounded
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isTimerRunning = false
                // Save on pause
                scope.launch(Dispatchers.IO) {
                    currentGameId?.let { id ->
                        val currentSudoku = sudokuState ?: return@launch
                        dao.saveGame(
                            SudokuEntity(
                                id = id,
                                difficulty = difficulty,
                                sudoku = currentSudoku,
                                timeSpent = timeSpentSeconds
                            )
                        )
                    }
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                isTimerRunning = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Save on exit
            scope.launch(Dispatchers.IO + NonCancellable) {
                currentGameId?.let { id ->
                    val currentSudoku = sudokuState ?: return@launch
                    dao.saveGame(
                        SudokuEntity(
                            id = id,
                            difficulty = difficulty,
                            sudoku = currentSudoku,
                            timeSpent = timeSpentSeconds
                        )
                    )
                }
            }
        }
    }

    // History for Undo
    val history = remember { mutableListOf<Sudoku>() }

    // Hint state
    // Scope is already defined above
    // ScaffoldState moved up for BackHandler access

    fun updateSudoku(newSudoku: Sudoku) {
        history.add(sudoku)
        sudoku = newSudoku
        // Update local state copy if needed for saving
        sudokuState = newSudoku

        // Save to DB asynchronously
        scope.launch(Dispatchers.IO) {
            currentGameId?.let { id ->
                dao.saveGame(
                    SudokuEntity(
                        id = id,
                        difficulty = difficulty,
                        sudoku = newSudoku,
                        timeSpent = timeSpentSeconds
                    )
                )
            }
        }

        // Check win condition
        val isFull = newSudoku.cells.none { it.value == 0 }
        if (isFull && SudokuValidator.isBoardValid(newSudoku)) {
            showWinDialog = true
            // Clear saved game on win
            val idToDelete = currentGameId
            currentGameId = null
            scope.launch(Dispatchers.IO) {
                idToDelete?.let { id ->
                    dao.deleteGame(id)
                }
            }
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            sudoku = history.removeAt(history.lastIndex)
            sudokuState = sudoku
        }
    }

    // Interaction source for the background click to avoid ripple effect if desired
    val interactionSource = remember { MutableInteractionSource() }

    with(sharedTransitionScope) {


        Scaffold(
            modifier = Modifier,
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        title = {
                            Text("Sudoku")
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )

                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = difficulty.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                        Text(
                            text = formatTime(timeSpentSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = { undo() },
                                    icon = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    enabled = !showWinDialog
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        if (selectedRow != null && selectedCol != null) {
                                            val row = selectedRow!!
                                            val col = selectedCol!!
                                            val currentCell = sudoku.getCell(row, col)

                                            if (!currentCell.isFixed) {
                                                if (currentCell.value != 0 || currentCell.candidates.isNotEmpty()) {
                                                    updateSudoku(sudoku.setCell(row, col, 0))
                                                }
                                            }
                                        }
                                    },
                                    icon = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    enabled = !showWinDialog
                                )
                            }
                            // Edit Button
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = { isNoteMode = !isNoteMode },
                                    icon = Icons.Default.Edit,
                                    contentDescription = "Note Mode",
                                    isSelected = isNoteMode,
                                    enabled = !showWinDialog
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        updateSudoku(
                                            CandidateCalculator.calculateAllCandidates(
                                                sudoku
                                            )
                                        )
                                    },
                                    icon = Icons.Default.AutoAwesome,
                                    contentDescription = "Auto Candidates",
                                    enabled = !showWinDialog
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ToolbarActionButton(
                                    onClick = {
                                        showBottomSheet = true
                                    },
                                    icon = Icons.Default.Lightbulb,
                                    contentDescription = "Hint",
                                    enabled = !showWinDialog
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            val backgroundModifier = if (gameId != null) {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .sharedBounds(
                        rememberSharedContentState(key = "game_container_$gameId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .sharedBounds(
                        rememberSharedContentState(key = "game_container"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )

            }

            val boardModifier = if (gameId != null) {
                Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "thumbnail_$gameId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .fillMaxSize()
            } else {
                Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "thumbnail"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .fillMaxSize()
            }

            BoxWithConstraints(
                modifier = backgroundModifier,
                contentAlignment = Alignment.TopCenter
            ) {
                val screenWidth = maxWidth

                val boardHeight = screenWidth - 32.dp

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Board Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardHeight)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SudokuBoard(
                            sudoku = sudoku,
                            onCellClick = { row, col ->
                                if (selectedRow == row && selectedCol == col) {
                                    selectedRow = null
                                    selectedCol = null
                                } else {
                                    selectedRow = row
                                    selectedCol = col
                                }

                                // If a number is selected in the pad, try to fill it
                                val currentCell = sudoku.getCell(row, col)
                                if (selectedNumber != null) {
                                    if (!currentCell.isFixed) {
                                        if (isNoteMode) {
                                            if (currentCell.value == 0) {
                                                updateSudoku(
                                                    sudoku.toggleCandidate(
                                                        row,
                                                        col,
                                                        selectedNumber!!
                                                    )
                                                )
                                            }
                                        } else {
                                            val newValue = selectedNumber!!
                                            // Only update if value is changing
                                            if (currentCell.value != newValue) {
                                                updateSudoku(sudoku.setCell(row, col, newValue))
                                            }
                                        }
                                    } else {
                                        selectedNumber = null
                                    }
                                }
                            },
                            selectedRow = selectedRow,
                            selectedCol = selectedCol,
                            highlightNumber = selectedNumber
                                ?: if (selectedRow != null && selectedCol != null) {
                                    sudoku.getCell(
                                        selectedRow!!,
                                        selectedCol!!
                                    ).value.takeIf { it != 0 }
                                } else null,
                            config = BoardConfig(
                                useColoredBoard = coloredBoard,
                                highlightCross = showLines,     // 是否高亮行列
                                highlightBlock = showBlock,     // 是否高亮九宫格
                                useAltErrorColor = altErrorColor // 是否用替代错误色
                            ),
                            modifier = boardModifier,


                            )
                    }

                    // Number Pad
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                // Deselect cell when clicking empty space
                                selectedRow = null
                                selectedCol = null
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Middle Area: Number Pad
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f), // Take available space
                            contentAlignment = Alignment.Center
                        ) {
                            if (showWinDialog) {

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Game Completed",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Congratulations! You've solved it!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Time: ${formatTime(timeSpentSeconds)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            TimeUnit.MILLISECONDS
                                        ).max(100)
                                    )
                                }

                                KonfettiView(
                                    modifier = Modifier.fillMaxSize(),
                                    parties = listOf(party),
                                )
                            } else {
                                NumberPad(
                                    selectedNumber = selectedNumber,
                                    onNumberClick = { number ->
                                        // Logic update: If a cell is selected (Cell First Mode), fill it and Clear selection
                                        if (selectedRow != null && selectedCol != null) {
                                            val row = selectedRow!!
                                            val col = selectedCol!!
                                            val currentCell = sudoku.getCell(row, col)

                                            if (!currentCell.isFixed) {
                                                if (isNoteMode) {
                                                    if (currentCell.value == 0) {
                                                        updateSudoku(
                                                            sudoku.toggleCandidate(
                                                                row,
                                                                col,
                                                                number
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    if (currentCell.value != number) {
                                                        updateSudoku(
                                                            sudoku.setCell(
                                                                row,
                                                                col,
                                                                number
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            // Cell First detected: clear the number selection so user isn't stuck in "fill mode" for this number
                                            selectedNumber = null
                                        } else {
                                            // Digit First Mode: Toggle number selection
                                            selectedNumber =
                                                if (selectedNumber == number) null else number
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                GameHintPanel(
                    onBackClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onApplyClick = {
                        /* Apply Hint */
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolbarActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        label = "scale"
    )

    val containerColor =
        if (isSelected && enabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor =
        if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = 400.dp
                ),  // Radius larger than screen width ensures ripple effect covers entire button
                onClick = onClick,
                enabled = enabled
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(48.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
