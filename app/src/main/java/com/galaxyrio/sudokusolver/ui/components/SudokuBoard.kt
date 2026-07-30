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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.game.SudokuValidator
import com.galaxyrio.sudokusolver.domain.model.Sudoku

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
    config: BoardConfig = BoardConfig(),
) {
    val thickLine = 2.dp
    val thinLine = 1.dp
    val cornerRadius = 12.dp
    val lineColor = if (config.useColoredBoard) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val boardColor = if (config.useColoredBoard) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val conflictingCells = remember(sudoku) {
        buildSet {
            repeat(Sudoku.GRID_SIZE) { row ->
                repeat(Sudoku.GRID_SIZE) { col ->
                    if (SudokuValidator.hasConflict(sudoku, row, col)) {
                        add(row * Sudoku.GRID_SIZE + col)
                    }
                }
            }
        }
    }
    val invalidCandidates = remember(sudoku) {
        List(Sudoku.CELL_COUNT) { index ->
            val row = index / Sudoku.GRID_SIZE
            val col = index % Sudoku.GRID_SIZE
            sudoku.cells[index].candidates.filterTo(mutableSetOf()) { candidate ->
                SudokuValidator.hasConflict(sudoku, row, col, candidate)
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(boardColor, RoundedCornerShape(cornerRadius))
            .border(thickLine, boardColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .padding(thickLine),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(thickLine),
        ) {
            repeat(3) { blockRow ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(thickLine),
                ) {
                    repeat(3) { blockCol ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(lineColor),
                            verticalArrangement = Arrangement.spacedBy(thinLine),
                        ) {
                            repeat(3) { cellRowInBlock ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(thinLine),
                                ) {
                                    repeat(3) { cellColInBlock ->
                                        val row = blockRow * 3 + cellRowInBlock
                                        val col = blockCol * 3 + cellColInBlock
                                        val index = row * Sudoku.GRID_SIZE + col
                                        val cell = sudoku.cells[index]
                                        val isSelected = row == selectedRow && col == selectedCol
                                        val hasSelection = selectedRow != null && selectedCol != null
                                        val isSelectedCross = hasSelection &&
                                            (row == selectedRow || col == selectedCol)
                                        val isSelectedBlock =
                                            selectedRow != null && selectedCol != null &&
                                            row / 3 == selectedRow / 3 &&
                                            col / 3 == selectedCol / 3
                                        val isValueHighlighted =
                                            highlightNumber != null && cell.value == highlightNumber
                                        val cellDescription = if (cell.isSolved()) {
                                            stringResource(
                                                R.string.game_cell_value,
                                                row + 1,
                                                col + 1,
                                                cell.value,
                                            )
                                        } else {
                                            stringResource(
                                                R.string.game_cell_empty,
                                                row + 1,
                                                col + 1,
                                            )
                                        } + ", " + stringResource(
                                            if (cell.isFixed) {
                                                R.string.game_cell_fixed
                                            } else {
                                                R.string.game_cell_editable
                                            }
                                        )

                                        SudokuCell(
                                            value = cell.value.takeIf { it != 0 },
                                            candidates = cell.candidates,
                                            errorCandidates = invalidCandidates[index],
                                            isFixed = cell.isFixed,
                                            isSelected = isSelected,
                                            isError = !cell.isFixed && index in conflictingCells,
                                            highlightNumber = highlightNumber,
                                            isValueHighlighted = isValueHighlighted,
                                            isSelectedCross = isSelectedCross,
                                            isSelectedBlock = isSelectedBlock,
                                            contentDescription = cellDescription,
                                            onClick = { onCellClick(row, col) },
                                            config = config,
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
            }
        }
    }
}

@Composable
private fun SudokuCell(
    value: Int?,
    candidates: Set<Int>,
    errorCandidates: Set<Int>,
    isFixed: Boolean,
    isSelected: Boolean,
    isError: Boolean,
    highlightNumber: Int?,
    isValueHighlighted: Boolean,
    isSelectedCross: Boolean,
    isSelectedBlock: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    config: BoardConfig,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val errorColor = if (config.useAltErrorColor) colorScheme.tertiary else colorScheme.error
    val errorContainerColor =
        if (config.useAltErrorColor) colorScheme.tertiaryContainer else colorScheme.errorContainer
    val onErrorColor =
        if (config.useAltErrorColor) colorScheme.onTertiary else colorScheme.onError
    val onErrorContainerColor =
        if (config.useAltErrorColor) colorScheme.onTertiaryContainer else colorScheme.onErrorContainer

    val backgroundColor = when {
        isError && isSelected -> errorColor
        isError -> errorContainerColor
        isSelected && isFixed -> colorScheme.primary
        isSelected -> colorScheme.primaryContainer
        config.highlightCross && isSelectedCross -> colorScheme.surfaceContainerHighest
        config.highlightBlock && isSelectedBlock -> colorScheme.surfaceContainer
        isValueHighlighted -> colorScheme.secondaryContainer
        else -> colorScheme.surface
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .semantics {
                this.contentDescription = contentDescription
                this.selected = isSelected
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            val textColor = when {
                isError && isSelected -> onErrorColor
                isError -> onErrorContainerColor
                isSelected && isFixed -> colorScheme.onPrimary
                isSelected -> colorScheme.onPrimaryContainer
                isFixed -> colorScheme.onSurface
                isValueHighlighted -> colorScheme.onSecondaryContainer
                else -> colorScheme.primary
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        } else if (candidates.isNotEmpty()) {
            CandidateGrid(
                candidates = candidates,
                errorCandidates = errorCandidates,
                highlightNumber = highlightNumber,
                isSelected = isSelected,
                errorColor = errorColor,
                errorContainerColor = errorContainerColor,
            )
        }
    }
}

@Composable
private fun CandidateGrid(
    candidates: Set<Int>,
    errorCandidates: Set<Int>,
    highlightNumber: Int?,
    isSelected: Boolean,
    errorColor: androidx.compose.ui.graphics.Color,
    errorContainerColor: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(1.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(3) { col ->
                    val candidate = row * 3 + col + 1
                    val isHighlighted = candidate == highlightNumber && candidate in candidates
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(
                                if (isHighlighted) {
                                    Modifier.background(
                                        color = if (candidate in errorCandidates) {
                                            errorContainerColor
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        shape = CircleShape,
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                    ) {
                        if (candidate in candidates) {
                            Text(
                                text = candidate.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    candidate in errorCandidates -> errorColor
                                    isHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
