package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle

@Composable
fun NumberPad(
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPadBoundsChanged: ((Rect) -> Unit)? = null,
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
            modifier = Modifier
                .size(padSize)
                .onGloballyPositioned { coordinates ->
                    onPadBoundsChanged?.invoke(coordinates.boundsInWindow())
                },
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
fun AdvancedNumberPad(
    isAdvancedNoteMode: Boolean,
    selectedNumber: Int?,
    highlightedNumbers: Set<Int>,
    showBivalueHighlights: Boolean,
    frameHighlights: Boolean,
    isPaintSelected: Boolean,
    isSolidLineSelected: Boolean,
    isDashedLineSelected: Boolean,
    onNumberClick: (Int) -> Unit,
    onAdvancedNoteModeClick: () -> Unit,
    onBivalueClick: () -> Unit,
    onPaintClick: () -> Unit,
    onFrameClick: () -> Unit,
    onSolidLineClick: () -> Unit,
    onDashedLineClick: () -> Unit,
    onBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPadBoundsChanged: ((Rect) -> Unit)? = null,
) {
    val backgroundInteraction = remember { MutableInteractionSource() }
    val clearSelectionLabel = stringResource(R.string.game_clear_selection)
    val digitSelection = if (isAdvancedNoteMode) {
        highlightedNumbers
    } else {
        setOfNotNull(selectedNumber)
    }

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
        val padWidth = minOf(maxWidth, 480.dp)
        val padHeight = minOf(maxHeight, padWidth * 0.60f, 264.dp)
        val rowSpacing = 8.dp
        val columnSpacing = 8.dp

        Column(
            modifier = Modifier
                .width(padWidth)
                .height(padHeight)
                .onGloballyPositioned { coordinates ->
                    onPadBoundsChanged?.invoke(coordinates.boundsInWindow())
                },
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
        ) {
            AdvancedPadRow(
                modifier = Modifier.weight(1f),
                spacing = columnSpacing,
            ) {
                (1..5).forEach { number ->
                    AdvancedPadButton(
                        isSelected = number in digitSelection,
                        contentDescription = number.toString(),
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            AdvancedPadRow(
                modifier = Modifier.weight(1f),
                spacing = columnSpacing,
            ) {
                (6..9).forEach { number ->
                    AdvancedPadButton(
                        isSelected = number in digitSelection,
                        contentDescription = number.toString(),
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
                AdvancedPadButton(
                    isSelected = isAdvancedNoteMode,
                    contentDescription = stringResource(R.string.game_advanced_note_mode),
                    onClick = onAdvancedNoteModeClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("AN", style = MaterialTheme.typography.titleMedium)
                }
            }
            AdvancedPadRow(
                modifier = Modifier.weight(1f),
                spacing = columnSpacing,
            ) {
                AdvancedPadButton(
                    isSelected = showBivalueHighlights,
                    contentDescription = stringResource(R.string.game_advanced_bivalue),
                    onClick = onBivalueClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("XY", style = MaterialTheme.typography.titleMedium)
                }
                AdvancedPadButton(
                    isSelected = isPaintSelected,
                    contentDescription = stringResource(R.string.game_advanced_paint),
                    onClick = onPaintClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatColorFill,
                        contentDescription = null,
                    )
                }
                AdvancedPadButton(
                    isSelected = frameHighlights,
                    contentDescription = stringResource(R.string.game_advanced_frame),
                    onClick = onFrameClick,
                    modifier = Modifier.weight(1f),
                ) {
                    FrameToolGlyph()
                }
                AdvancedPadButton(
                    isSelected = isSolidLineSelected,
                    contentDescription = stringResource(R.string.game_advanced_solid_line),
                    onClick = onSolidLineClick,
                    modifier = Modifier.weight(1f),
                ) {
                    LineToolGlyph(style = AdvancedNoteLineStyle.SOLID)
                }
                AdvancedPadButton(
                    isSelected = isDashedLineSelected,
                    contentDescription = stringResource(R.string.game_advanced_dashed_line),
                    onClick = onDashedLineClick,
                    modifier = Modifier.weight(1f),
                ) {
                    LineToolGlyph(style = AdvancedNoteLineStyle.DASHED)
                }
            }
        }
    }
}

@Composable
private fun AdvancedPadRow(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@Composable
private fun AdvancedPadButton(
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "advanced_pad_scale",
    )
    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 24 else 50,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "advanced_pad_shape",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .semantics {
                selected = isSelected
                this.contentDescription = contentDescription
            }
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
        content()
    }
}

@Composable
private fun FrameToolGlyph(modifier: Modifier = Modifier) {
    val color = androidx.compose.material3.LocalContentColor.current
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = 2.4.dp.toPx()
        val short = size.minDimension * 0.30f
        val far = size.minDimension
        drawLine(color, Offset.Zero, Offset(short, 0f), stroke, StrokeCap.Square)
        drawLine(color, Offset.Zero, Offset(0f, short), stroke, StrokeCap.Square)
        drawLine(color, Offset(far, 0f), Offset(far - short, 0f), stroke, StrokeCap.Square)
        drawLine(color, Offset(far, 0f), Offset(far, short), stroke, StrokeCap.Square)
        drawLine(color, Offset(0f, far), Offset(short, far), stroke, StrokeCap.Square)
        drawLine(color, Offset(0f, far), Offset(0f, far - short), stroke, StrokeCap.Square)
        drawLine(color, Offset(far, far), Offset(far - short, far), stroke, StrokeCap.Square)
        drawLine(color, Offset(far, far), Offset(far, far - short), stroke, StrokeCap.Square)
    }
}

@Composable
private fun LineToolGlyph(
    style: AdvancedNoteLineStyle,
    modifier: Modifier = Modifier,
) {
    val color = androidx.compose.material3.LocalContentColor.current
    Canvas(modifier = modifier.size(26.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.12f, size.height * 0.76f),
            end = Offset(size.width * 0.88f, size.height * 0.24f),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = if (style == AdvancedNoteLineStyle.DASHED) {
                PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
            } else {
                null
            },
        )
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
