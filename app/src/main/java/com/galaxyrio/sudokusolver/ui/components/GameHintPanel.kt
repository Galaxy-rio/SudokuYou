package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope.PlaceholderSize.Companion.AnimatedSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.game.HintIssue
import com.galaxyrio.sudokusolver.domain.solver.SolveTrace
import com.galaxyrio.sudokusolver.domain.solver.SolveTraceStatus
import com.galaxyrio.sudokusolver.ui.motion.materialQuickCrossfade
import com.galaxyrio.sudokusolver.ui.motion.materialQuickFadeIn
import com.galaxyrio.sudokusolver.ui.motion.materialQuickFadeOut
import com.galaxyrio.sudokusolver.ui.util.cellCoordinateLabel
import com.galaxyrio.sudokusolver.ui.util.localizedAction
import com.galaxyrio.sudokusolver.ui.util.localizedExplanation
import com.galaxyrio.sudokusolver.ui.util.localizedName
import kotlin.math.roundToInt

@Composable
fun GameHintPanel(
    isLoading: Boolean,
    trace: SolveTrace?,
    issue: HintIssue?,
    selectedStepIndex: Int,
    areHintDetailsVisible: Boolean,
    showErrorDetails: Boolean,
    coordinateNotation: CoordinateNotation,
    onStepSelected: (Int) -> Unit,
    onRevealDetails: () -> Unit,
    onApplyNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            isLoading -> HintLoading()
            issue != null -> RecoveryIssue(
                issue = issue,
                showErrorDetails = showErrorDetails,
                coordinateNotation = coordinateNotation,
                onApplyRecovery = onApplyNext,
            )
            trace == null || trace.steps.isEmpty() -> EmptyTrace(trace?.status)
            else -> TraceBrowser(
                trace = trace,
                selectedStepIndex = selectedStepIndex,
                areHintDetailsVisible = areHintDetailsVisible,
                coordinateNotation = coordinateNotation,
                onStepSelected = onStepSelected,
                onRevealDetails = onRevealDetails,
                onApplyNext = onApplyNext,
            )
        }
    }
}

@Composable
private fun RecoveryIssue(
    issue: HintIssue,
    showErrorDetails: Boolean,
    coordinateNotation: CoordinateNotation,
    onApplyRecovery: () -> Unit,
) {
    val resources = LocalResources.current
    val title = when (issue) {
        is HintIssue.IncorrectValues -> R.string.game_hint_incorrect_values_title
        is HintIssue.MissingCandidates -> R.string.game_hint_missing_candidates_title
    }
    val explanation = when {
        showErrorDetails && issue is HintIssue.IncorrectValues ->
            R.string.game_hint_incorrect_values_explanation
        showErrorDetails && issue is HintIssue.MissingCandidates ->
            R.string.game_hint_missing_candidates_explanation
        issue is HintIssue.IncorrectValues ->
            R.string.game_hint_incorrect_values_private_explanation
        else -> R.string.game_hint_missing_candidates_private_explanation
    }

    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                !showErrorDetails -> issue.cells().forEach { cell ->
                    Text(
                        text = stringResource(
                            R.string.game_hint_error_cell_item,
                            resources.cellCoordinateLabel(cell, coordinateNotation),
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                issue is HintIssue.IncorrectValues -> issue.entries.forEach { entry ->
                    Text(
                        text = stringResource(
                            R.string.game_hint_incorrect_value_item,
                            resources.cellCoordinateLabel(entry.cell, coordinateNotation),
                            entry.enteredDigit,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                issue is HintIssue.MissingCandidates -> issue.candidates.forEach { candidate ->
                    Text(
                        text = stringResource(
                            R.string.game_hint_missing_candidate_item,
                            candidate.digit,
                            resources.cellCoordinateLabel(candidate.cell, coordinateNotation),
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        FilledIconButton(
            onClick = onApplyRecovery,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(
                    if (issue is HintIssue.MissingCandidates && !showErrorDetails) {
                        R.string.game_hint_dismiss_error
                    } else {
                        R.string.game_hint_apply_recovery
                    }
                ),
            )
        }
    }
}

@Composable
private fun HintLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.game_hint_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyTrace(status: SolveTraceStatus?) {
    val message = when (status) {
        SolveTraceStatus.SOLVED -> R.string.game_hint_already_solved
        SolveTraceStatus.INVALID -> R.string.game_hint_invalid
        SolveTraceStatus.STALLED, null -> R.string.game_hint_no_supported_step
    }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun TraceBrowser(
    trace: SolveTrace,
    selectedStepIndex: Int,
    areHintDetailsVisible: Boolean,
    coordinateNotation: CoordinateNotation,
    onStepSelected: (Int) -> Unit,
    onRevealDetails: () -> Unit,
    onApplyNext: () -> Unit,
) {
    val lastIndex = trace.steps.lastIndex
    val index = selectedStepIndex.coerceIn(0, lastIndex)
    val step = trace.steps[index]

    HintStepHeader(
        techniqueName = step.technique.localizedName(),
        stepNumber = if (areHintDetailsVisible) index + 1 else null,
        totalSteps = trace.steps.size,
    )

    if (areHintDetailsVisible) {
        HintStepDetails(
            trace = trace,
            selectedStepIndex = index,
            coordinateNotation = coordinateNotation,
        )
    } else {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                FilledTonalButton(onClick = onRevealDetails) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.game_hint_reveal_details),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }

    if (areHintDetailsVisible && lastIndex > 0) {
        Slider(
            value = index.toFloat(),
            onValueChange = { onStepSelected(it.roundToInt()) },
            valueRange = 0f..lastIndex.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (areHintDetailsVisible) {
            Arrangement.SpaceBetween
        } else {
            Arrangement.End
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (areHintDetailsVisible) {
            HintNavigationButton(
                icon = Icons.Default.FirstPage,
                contentDescription = stringResource(R.string.game_hint_first_step),
                enabled = index > 0,
                onClick = { onStepSelected(0) },
            )
            HintNavigationButton(
                icon = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = stringResource(R.string.game_hint_previous_step),
                enabled = index > 0,
                onClick = { onStepSelected(index - 1) },
            )
            HintNavigationButton(
                icon = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = stringResource(R.string.game_hint_next_step),
                enabled = index < lastIndex,
                onClick = { onStepSelected(index + 1) },
            )
            HintNavigationButton(
                icon = Icons.AutoMirrored.Filled.LastPage,
                contentDescription = stringResource(R.string.game_hint_last_step),
                enabled = index < lastIndex,
                onClick = { onStepSelected(lastIndex) },
            )
        }
        FilledIconButton(
            onClick = onApplyNext,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.game_hint_apply_next),
            )
        }
    }

    if (areHintDetailsVisible && trace.status == SolveTraceStatus.STALLED) {
        Text(
            text = stringResource(R.string.game_hint_partial_trace),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HintStepDetails(
    trace: SolveTrace,
    selectedStepIndex: Int,
    coordinateNotation: CoordinateNotation,
) {
    SharedTransitionLayout {
        AnimatedContent(
            targetState = selectedStepIndex,
            transitionSpec = {
                EnterTransition.None.togetherWith(ExitTransition.None)
            },
            contentAlignment = Alignment.TopStart,
            label = "hint_step_details",
        ) { targetIndex ->
            val animatedVisibilityScope = this
            val step = trace.steps[targetIndex]

            with(this@SharedTransitionLayout) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = HintStepDetailsSharedKey,
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            enter = materialQuickFadeIn(),
                            exit = materialQuickFadeOut(),
                            boundsTransform = { _, _ ->
                                tween(
                                    durationMillis = HintStepBoundsDurationMillis,
                                    easing = FastOutSlowInEasing,
                                )
                            },
                            resizeMode = RemeasureToBounds,
                            placeholderSize = AnimatedSize,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = step.localizedExplanation(coordinateNotation),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = step.localizedAction(coordinateNotation),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HintStepHeader(
    techniqueName: String,
    stepNumber: Int?,
    totalSteps: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = techniqueName,
            transitionSpec = { materialQuickCrossfade() },
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.weight(1f),
            label = "hint_technique_title",
        ) { targetTechniqueName ->
            Text(
                text = targetTechniqueName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (stepNumber != null) {
            AnimatedContent(
                targetState = stepNumber,
                transitionSpec = { materialQuickCrossfade() },
                contentAlignment = Alignment.CenterEnd,
                label = "hint_step_number",
            ) { targetStepNumber ->
                Text(
                    text = stringResource(
                        R.string.game_hint_step_count,
                        targetStepNumber,
                        totalSteps,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun HintIssue.cells() = when (this) {
    is HintIssue.IncorrectValues -> entries.map { it.cell }
    is HintIssue.MissingCandidates -> candidates.map { it.cell }
}.distinct()

private const val HintStepDetailsSharedKey = "hint_step_details_container"
private const val HintStepBoundsDurationMillis = 220

@Composable
private fun HintNavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
