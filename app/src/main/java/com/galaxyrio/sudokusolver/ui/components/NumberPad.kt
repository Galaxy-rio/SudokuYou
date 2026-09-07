package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.ui.guide.GuideTarget
import com.galaxyrio.sudokusolver.ui.guide.guideTarget

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
                .guideTarget(GuideTarget.NUMBERS)
                .onGloballyPositioned { coordinates ->
                    onPadBoundsChanged?.invoke(coordinates.boundsInWindow())
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            (1..9).chunked(3).forEach { rowNumbers ->
                NumberButtonGroup(
                    numbers = rowNumbers,
                    selectedNumber = selectedNumber,
                    onNumberClick = onNumberClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NumberButtonGroup(
    numbers: List<Int>,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSources = remember(numbers) {
        List(numbers.size) { MutableInteractionSource() }
    }
    val contentPadding = ButtonDefaults.ContentPadding
    val layoutDirection = LocalLayoutDirection.current
    val compressionLimit = contentPadding.calculateEndPadding(layoutDirection)

    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        numbers.forEachIndexed { index, number ->
            val interactionSource = interactionSources[index]
            customItem(
                buttonGroupContent = {
                    NumberButton(
                        number = number,
                        isSelected = selectedNumber == number,
                        onClick = { onNumberClick(number) },
                        interactionSource = interactionSource,
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(
                                interactionSource = interactionSource,
                                compressionLimit = compressionLimit,
                            )
                            .fillMaxHeight(),
                    )
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(number.toString()) },
                        onClick = {
                            onNumberClick(number)
                            menuState.dismiss()
                        },
                    )
                },
            )
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
    val advancedNoteModeDescription = stringResource(R.string.game_advanced_note_mode)
    val bivalueDescription = stringResource(R.string.game_advanced_bivalue)
    val paintDescription = stringResource(R.string.game_advanced_paint)
    val frameDescription = stringResource(R.string.game_advanced_frame)
    val solidLineDescription = stringResource(R.string.game_advanced_solid_line)
    val dashedLineDescription = stringResource(R.string.game_advanced_dashed_line)

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
            ) {
                (1..5).forEach { number ->
                    advancedPadItem(
                        isSelected = number in digitSelection,
                        contentDescription = number.toString(),
                        onClick = { onNumberClick(number) },
                    ) {
                        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            AdvancedPadRow(
                modifier = Modifier.weight(1f),
            ) {
                (6..9).forEach { number ->
                    advancedPadItem(
                        isSelected = number in digitSelection,
                        contentDescription = number.toString(),
                        onClick = { onNumberClick(number) },
                    ) {
                        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
                advancedPadItem(
                    isSelected = isAdvancedNoteMode,
                    contentDescription = advancedNoteModeDescription,
                    guideTarget = GuideTarget.ADVANCED_NOTES,
                    onClick = onAdvancedNoteModeClick,
                ) {
                    Text("AN", style = MaterialTheme.typography.titleMedium)
                }
            }
            AdvancedPadRow(
                modifier = Modifier.weight(1f),
            ) {
                advancedPadItem(
                    isSelected = showBivalueHighlights,
                    contentDescription = bivalueDescription,
                    guideTarget = GuideTarget.BIVALUE,
                    onClick = onBivalueClick,
                ) {
                    Text("XY", style = MaterialTheme.typography.titleMedium)
                }
                advancedPadItem(
                    isSelected = isPaintSelected,
                    contentDescription = paintDescription,
                    guideTarget = GuideTarget.PAINT,
                    onClick = onPaintClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatColorFill,
                        contentDescription = null,
                    )
                }
                advancedPadItem(
                    isSelected = frameHighlights,
                    contentDescription = frameDescription,
                    guideTarget = GuideTarget.FRAME,
                    onClick = onFrameClick,
                ) {
                    FrameToolGlyph()
                }
                advancedPadItem(
                    isSelected = isSolidLineSelected,
                    contentDescription = solidLineDescription,
                    guideTarget = GuideTarget.SOLID_LINE,
                    onClick = onSolidLineClick,
                ) {
                    LineToolGlyph(style = AdvancedNoteLineStyle.SOLID)
                }
                advancedPadItem(
                    isSelected = isDashedLineSelected,
                    contentDescription = dashedLineDescription,
                    guideTarget = GuideTarget.DASHED_LINE,
                    onClick = onDashedLineClick,
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
    content: ButtonGroupScope.() -> Unit,
) {
    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

private fun ButtonGroupScope.advancedPadItem(
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    guideTarget: GuideTarget? = null,
    content: @Composable () -> Unit,
) {
    customItem(
        buttonGroupContent = {
            val interactionSource = remember { MutableInteractionSource() }
            AdvancedPadButton(
                isSelected = isSelected,
                contentDescription = contentDescription,
                onClick = onClick,
                interactionSource = interactionSource,
                modifier = Modifier
                    .weight(1f)
                    .then(if (guideTarget != null) Modifier.guideTarget(guideTarget) else Modifier)
                    .animateWidth(
                        interactionSource = interactionSource,
                        compressionLimit = AdvancedPadButtonCompressionLimit,
                    )
                    .fillMaxHeight(),
                content = content,
            )
        },
        menuContent = { menuState ->
            DropdownMenuItem(
                text = { Text(contentDescription) },
                onClick = {
                    onClick()
                    menuState.dismiss()
                },
            )
        },
    )
}

@Composable
private fun AdvancedPadButton(
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
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
        contentPadding = PaddingValues(horizontal = AdvancedPadButtonCompressionLimit),
    ) {
        content()
    }
}

private val AdvancedPadButtonCompressionLimit = 12.dp

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
    interactionSource: MutableInteractionSource,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme
    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 24 else 50,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "number_pad_shape",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .semantics { selected = isSelected },
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
        contentPadding = contentPadding,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
