package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialLessons
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialStage
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.util.localizedAction
import com.galaxyrio.sudokusolver.ui.util.localizedName

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TutorialDetailScreen(
    techniqueId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    boardConfig: BoardConfig = BoardConfig(),
    coordinateNotation: CoordinateNotation = CoordinateNotation.LOCALIZED,
) {
    val lesson = TutorialLessons.find(techniqueId)
    var showingExamples by rememberSaveable(techniqueId) { mutableStateOf(false) }
    var stepIndex by rememberSaveable(techniqueId) { mutableIntStateOf(0) }
    var exampleIndex by rememberSaveable(techniqueId) { mutableIntStateOf(0) }
    var selectedCell by rememberSaveable(techniqueId) { mutableIntStateOf(-1) }
    val clearSelection = { selectedCell = -1 }
    val stage = if (showingExamples) TutorialStage.DEDUCTION else TutorialStage.entries[stepIndex]
    val example = lesson?.examples?.get(if (showingExamples) exampleIndex else 0)
    val index = if (showingExamples) exampleIndex else stepIndex
    val count = if (showingExamples) lesson?.examples?.size ?: 0 else TutorialStage.entries.size

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_tutorial)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                lesson?.let {
                    Text(
                        text = it.technique.localizedName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
                    )
                }
            }
        },
        bottomBar = {
            if (lesson != null) BottomAppBar {
                TutorialToolbarAction(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(if (showingExamples) R.string.tutorial_previous_example else R.string.tutorial_previous_step),
                    index > 0,
                    onClick = {
                        if (showingExamples) exampleIndex = (exampleIndex - 1).coerceAtLeast(0)
                        else stepIndex = (stepIndex - 1).coerceAtLeast(0)
                        clearSelection()
                    }, modifier = Modifier.weight(1f),
                )
                TutorialToolbarAction(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    stringResource(if (showingExamples) R.string.tutorial_next_example else R.string.tutorial_next_step),
                    index < count - 1,
                    onClick = {
                        if (showingExamples) exampleIndex = (exampleIndex + 1).coerceAtMost(count - 1)
                        else stepIndex = (stepIndex + 1).coerceAtMost(count - 1)
                        clearSelection()
                    }, modifier = Modifier.weight(1f),
                )
                TutorialToolbarAction(
                    if (showingExamples) ImageVector.vectorResource(R.drawable.ic_material_symbol_flowsheet) else Icons.Outlined.Preview,
                    stringResource(if (showingExamples) R.string.tutorial_show_steps else R.string.tutorial_show_examples),
                    true,
                    onClick = { showingExamples = !showingExamples; clearSelection() },
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { padding ->
        if (example == null) {
            Text(stringResource(R.string.tutorial_unavailable), Modifier.padding(padding).padding(20.dp))
            return@Scaffold
        }
        with(sharedTransitionScope) {
            val boardModifier = Modifier.sharedBounds(
                rememberSharedContentState(tutorialBoardKey(techniqueId)),
                animatedVisibilityScope, resizeMode = RemeasureToBounds,
            )
            val onCellClick: (Int, Int) -> Unit = { row, col ->
                val cell = row * 9 + col
                selectedCell = if (selectedCell == cell) -1 else cell
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(padding).sharedBounds(
                    rememberSharedContentState(tutorialContainerKey(techniqueId)), animatedVisibilityScope,
                ),
            ) {
                val sideBySide = maxWidth >= 600.dp || maxWidth > maxHeight
                val boardSize = if (sideBySide) {
                    minOf((maxWidth - 56.dp) / 2, maxHeight - 32.dp, 560.dp)
                } else {
                    minOf(maxWidth - 32.dp, maxHeight * 0.66f)
                }
                val density = LocalDensity.current
                val boardDensity = Density(density.density, minOf(density.fontScale, boardSize.value / 324f))
                val board: @Composable (Modifier) -> Unit = { layoutModifier ->
                    CompositionLocalProvider(LocalDensity provides boardDensity) {
                        TutorialBoard(
                            example = example,
                            stage = stage,
                            selectedCell = selectedCell.takeIf { it >= 0 },
                            onCellClick = onCellClick,
                            config = boardConfig,
                            modifier = layoutModifier.size(boardSize).then(boardModifier).testTag("tutorial_board"),
                        )
                    }
                }
                val text: @Composable (Modifier) -> Unit = { layoutModifier ->
                    key(showingExamples, index) {
                        TutorialExplanation(
                            example = example,
                            stage = stage,
                            showingExamples = showingExamples,
                            index = index,
                            count = count,
                            notation = coordinateNotation,
                            modifier = layoutModifier,
                        )
                    }
                }
                if (sideBySide) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { board(Modifier) }
                        text(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        board(Modifier.padding(top = 4.dp, bottom = 4.dp))
                        text(Modifier.fillMaxWidth().weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialExplanation(
    example: TutorialExample,
    stage: TutorialStage,
    showingExamples: Boolean,
    index: Int,
    count: Int,
    notation: CoordinateNotation,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (showingExamples) R.string.tutorial_example_progress else R.string.tutorial_step_progress,
                index + 1, count,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("tutorial_progress").semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(
            text = if (showingExamples) {
                stringResource(R.string.tutorial_example_house, example.house.tutorialLabel())
            } else stage.title(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        if (stage != TutorialStage.RULE) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TutorialLegend(PatternColor, stringResource(R.string.tutorial_pattern_legend))
                if ((stage == TutorialStage.PATTERN && example.isSingle) || stage == TutorialStage.DEDUCTION) {
                    TutorialLegend(
                        ExclusionColor,
                        stringResource(if (example.isSingle) R.string.tutorial_excluded_legend else R.string.tutorial_removed_legend),
                    )
                }
            }
        }
        Text(
            text = if (showingExamples) example.patternText(notation) else example.stepText(stage, notation),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (showingExamples || stage == TutorialStage.DEDUCTION) {
            Text(
                text = example.deduction.localizedAction(notation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TutorialLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(12.dp).background(color.copy(alpha = 0.65f), RoundedCornerShape(3.dp)))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TutorialToolbarAction(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick, enabled = enabled) { Icon(icon, description) }
    }
}
