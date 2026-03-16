package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxyrio.sudokusolver.game.Sudoku
import com.galaxyrio.sudokusolver.game.validator.SudokuValidator

data class BoardConfig(
    val useColoredBoard: Boolean = false,
    val highlightCross: Boolean = true,
    val highlightBlock: Boolean = true,
    val useAltErrorColor: Boolean = false,
)

@Composable
fun SudokuBoard(
    sudoku: Sudoku,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedRow: Int? = null,
    selectedCol: Int? = null,
    highlightNumber: Int? = null,
    config: BoardConfig = BoardConfig()
) {


    val thickLine = 2.dp
    val thinLine = 1.dp
    val cornerRadius = 8.dp
    val lineColor = if (!config.useColoredBoard) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val boardColor = if (!config.useColoredBoard) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.primary
    }


    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(boardColor, RoundedCornerShape(cornerRadius))
            .border(thickLine, boardColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .padding(thickLine)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(thickLine)
        ) {
            repeat(3) { blockRow ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(thickLine)
                ) {
                    repeat(3) { blockCol ->
                        // Sudoku Block (3x3 Cells)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(lineColor), // Background for thin lines
                            verticalArrangement = Arrangement.spacedBy(thinLine)
                        ) {
                            repeat(3) { cellRowInBlock ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(thinLine)
                                ) {
                                    repeat(3) { cellColInBlock ->
                                        val row = blockRow * 3 + cellRowInBlock
                                        val col = blockCol * 3 + cellColInBlock
                                        val cell = sudoku.getCell(row, col)

                                        val isError =
                                            !cell.isFixed && cell.value != 0 && SudokuValidator.checkContradiction(
                                                sudoku,
                                                row,
                                                col
                                            )
                                        val isSelected = (row == selectedRow && col == selectedCol)
                                        val isSelectedCross =
                                            (selectedRow != null && selectedCol != null) && (row == selectedRow || col == selectedCol)
                                        val isSelectedBoard =
                                            (selectedRow != null && selectedCol != null) && ((row / 3 == selectedRow.div(
                                                3
                                            ) && col / 3 == selectedCol.div(
                                                3
                                            )))

                                        val isValueHighlighted =
                                            (highlightNumber != null && cell.value == highlightNumber)

                                        val errorCandidates = if (cell.value == 0) {
                                            cell.candidates.filter { candidate ->
                                                SudokuValidator.checkContradiction(
                                                    sudoku,
                                                    row,
                                                    col,
                                                    candidate
                                                )
                                            }.toSet()
                                        } else {
                                            emptySet()
                                        }

                                        SudokuCell(
                                            value = if (cell.value == 0) null else cell.value,
                                            candidates = if (cell.value == 0) cell.candidates else emptySet(),
                                            errorCandidates = errorCandidates,
                                            isFixed = cell.isFixed,
                                            isSelected = isSelected,
                                            isError = isError,
                                            highlightNumber = highlightNumber,
                                            isValueHighlighted = isValueHighlighted,
                                            isSelectedCross = isSelectedCross,
                                            isSelectedBoard = isSelectedBoard,
                                            onClick = { onCellClick(row, col) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            config = config
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    value: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    candidates: Set<Int> = emptySet(),
    errorCandidates: Set<Int> = emptySet(),
    isFixed: Boolean,
    isSelected: Boolean,
    isError: Boolean = false,
    highlightNumber: Int? = null,
    isValueHighlighted: Boolean = false,
    isSelectedCross: Boolean = false,
    isSelectedBoard: Boolean = false,
    config: BoardConfig = BoardConfig()
) {
    val errorColor = if (!config.useAltErrorColor) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF4c662b)
    }
    val errorContainerColor = if (!config.useAltErrorColor) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        Color(0xFFcdeda3)
    }
    val onErrorColor = if (!config.useAltErrorColor) {
        MaterialTheme.colorScheme.onError
    } else {
        Color(0xFFffffff)
    }
    val onErrorContainerColor = if (!config.useAltErrorColor) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        Color(0xFF354e16)
    }

    val backgroundColor = when {
        isError && isSelected -> errorColor
        isError -> errorContainerColor
        isSelected && isFixed -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        config.highlightCross && isSelectedCross && !isError -> MaterialTheme.colorScheme.surfaceContainerHighest
        config.highlightBlock && isSelectedBoard && !isError -> MaterialTheme.colorScheme.surfaceContainer
        isValueHighlighted && !isError -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (value != null && value != 0) {
            val textColor = when {
                isError && isSelected -> onErrorColor
                isError -> onErrorContainerColor
                isSelected && isFixed -> MaterialTheme.colorScheme.onPrimary
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isFixed -> MaterialTheme.colorScheme.onSurface
                isValueHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.primary
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Bold, // 改为Normal可以增加对比度
                    fontSize = 30.sp
                ),
                color = textColor
            )
        } else if (candidates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(1.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                (0..2).forEach { rowOffset ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..3).forEach { colOffset ->
                            val candidateNum = rowOffset * 3 + colOffset
                            val isCandidateHighlighted =
                                (highlightNumber != null && candidateNum == highlightNumber && candidates.contains(
                                    candidateNum
                                ))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .then(
                                        if (isCandidateHighlighted) {
                                            val candidateBackground = when {
                                                errorCandidates.contains(candidateNum) -> errorContainerColor
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            }
                                            Modifier.background(
                                                candidateBackground,
                                                androidx.compose.foundation.shape.CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                if (candidates.contains(candidateNum)) {
                                    val candidateColor = when {
                                        errorCandidates.contains(candidateNum) -> errorColor
                                        isCandidateHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.secondary
                                    }

                                    Text(
                                        text = candidateNum.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = candidateColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
