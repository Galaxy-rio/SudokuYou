package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NumberPad(
    modifier: Modifier = Modifier,
    selectedNumber: Int? = null,
    onNumberClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = (1..9).chunked(3) // Split 1..9 into [[1,2,3], [4,5,6], [7,8,9]]
        rows.forEach { rowNumbers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                rowNumbers.forEach { number ->
                    NumberButton(
                        number = number,
                        isSelected = selectedNumber == number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }
    }
}


@Composable
fun NumberButton(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "scaleAnimation"
    )

    val cornerPercent by animateIntAsState(
        targetValue = if (isSelected) 20 else 50,
        label = "cornerAnimation"
    )

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSecondaryContainer

    Button(
        onClick = onClick,
        modifier = modifier
            .size(75.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number.toString(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
