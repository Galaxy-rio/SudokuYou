package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R

@Composable
fun NumberPad(
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundInteraction = remember { MutableInteractionSource() }
    val clearSelectionLabel = stringResource(R.string.game_clear_selection)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = backgroundInteraction,
                indication = null,
                onClickLabel = clearSelectionLabel,
                onClick = onBackgroundClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // The game content gives the number pad all space left after the board and bottom bar.
        // Fit one square into that area so the 3 x 3 grid can never extend behind the bar.
        val padSize = minOf(maxWidth, maxHeight).coerceAtMost(360.dp)

        Column(
            modifier = Modifier.size(padSize),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            (1..9).chunked(3).forEach { rowNumbers ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowNumbers.forEach { number ->
                        NumberButton(
                            number = number,
                            isSelected = selectedNumber == number,
                            onClick = { onNumberClick(number) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "number_pad_scale",
    )
    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 24 else 50,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "number_pad_shape",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .semantics { selected = isSelected }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(percent = cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
