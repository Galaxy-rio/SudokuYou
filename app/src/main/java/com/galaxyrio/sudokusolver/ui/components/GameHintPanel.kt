package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.SolveTrace
import com.galaxyrio.sudokusolver.domain.solver.SolveTraceStatus
import com.galaxyrio.sudokusolver.ui.util.localizedAction
import com.galaxyrio.sudokusolver.ui.util.localizedExplanation
import com.galaxyrio.sudokusolver.ui.util.localizedName
import kotlin.math.roundToInt

@Composable
fun GameHintPanel(
    isLoading: Boolean,
    trace: SolveTrace?,
    selectedStepIndex: Int,
    onStepSelected: (Int) -> Unit,
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
            trace == null || trace.steps.isEmpty() -> EmptyTrace(trace?.status)
            else -> TraceBrowser(
                trace = trace,
                selectedStepIndex = selectedStepIndex,
                onStepSelected = onStepSelected,
                onApplyNext = onApplyNext,
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
    onStepSelected: (Int) -> Unit,
    onApplyNext: () -> Unit,
) {
    val lastIndex = trace.steps.lastIndex
    val index = selectedStepIndex.coerceIn(0, lastIndex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = trace.steps[index].technique.localizedName(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.game_hint_step_count,
                index + 1,
                trace.steps.size,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    AnimatedContent(
        targetState = index,
        label = "hint_step_content",
    ) { targetIndex ->
        val step = trace.steps[targetIndex]
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
                    text = step.localizedExplanation(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = step.localizedAction(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    if (lastIndex > 0) {
        Slider(
            value = index.toFloat(),
            onValueChange = { onStepSelected(it.roundToInt()) },
            valueRange = 0f..lastIndex.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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

    if (trace.status == SolveTraceStatus.STALLED) {
        Text(
            text = stringResource(R.string.game_hint_partial_trace),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
