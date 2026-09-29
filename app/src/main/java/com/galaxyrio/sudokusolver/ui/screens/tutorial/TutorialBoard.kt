package com.galaxyrio.sudokusolver.ui.screens.tutorial

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialStage
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.BoardGeometry
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.components.toComposeColor

@Composable
internal fun TutorialBoard(
    example: TutorialExample,
    stage: TutorialStage,
    selectedCell: Int?,
    onCellClick: (row: Int, col: Int) -> Unit,
    config: BoardConfig,
    modifier: Modifier = Modifier,
) {
    val board = example.boardAt(stage)
    val notes = remember(example, stage) { example.annotations(stage) }
    val primary = MaterialTheme.colorScheme.primary
    Box(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
        SudokuBoard(
            sudoku = board,
            onCellClick = onCellClick,
            readOnly = true,
            selectedRow = selectedCell?.div(9),
            selectedCol = selectedCell?.rem(9),
            highlightNumbers = setOfNotNull(selectedCell?.let { board.cells[it].value.takeIf { value -> value != 0 } }),
            advancedNotes = notes,
            config = config,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            val geometry = BoardGeometry(size.minDimension, 2.dp.toPx(), 2.dp.toPx(), 0.6.dp.toPx(), 1.dp.toPx())
            val bounds = geometry.houseBounds(example.house)
            drawRect(primary.copy(alpha = 0.65f), bounds.topLeft, bounds.size, style = Stroke(1.25.dp.toPx()))
            if (stage != TutorialStage.RULE) {
                example.patternCells.forEach { cell ->
                    val cellBounds = geometry.cellBounds(cell).deflate(1.dp.toPx())
                    drawRect(PatternColor, cellBounds.topLeft, cellBounds.size, style = Stroke(1.dp.toPx()))
                }
                example.patternCandidates.filter { it.digit in board.cells[it.cell.index].candidates }.forEach { candidate ->
                    val candidateBounds = geometry.candidateBounds(candidate).deflate(0.3.dp.toPx())
                    drawRect(PatternColor, candidateBounds.topLeft, candidateBounds.size, style = Stroke(1.dp.toPx()))
                }
            }
            if (stage == TutorialStage.DEDUCTION) {
                example.deduction.eliminations.forEach { elimination ->
                    val candidateBounds = geometry.candidateBounds(elimination.candidate).deflate(1.dp.toPx())
                    drawLine(ExclusionColor, candidateBounds.topLeft, candidateBounds.bottomRight, 1.dp.toPx())
                    drawLine(ExclusionColor, candidateBounds.topRight, candidateBounds.bottomLeft, 1.dp.toPx())
                }
            }
        }
    }
}

/** A miniature of the actual first example, including its candidates and teaching marks. */
@Composable
internal fun TutorialThumbnail(example: TutorialExample, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val board = example.before
    val notes = remember(example) { example.annotations(TutorialStage.DEDUCTION) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER } }
    Canvas(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(4.dp))) {
        drawRect(colors.surface)
        val line = size.width / 81f
        val cellSize = (size.width - line) / 9f
        board.cells.forEachIndexed { index, cell ->
            val left = line / 2 + index % 9 * cellSize
            val top = line / 2 + index / 9 * cellSize
            notes.cellColors[index]?.let {
                drawRect(it.toComposeColor().copy(alpha = 0.30f), Offset(left, top), Size(cellSize, cellSize))
            }
            if (cell.value != 0) {
                paint.color = colors.onSurface.toArgb()
                paint.textSize = cellSize * 0.72f
                paint.typeface = Typeface.DEFAULT_BOLD
                drawContext.canvas.nativeCanvas.drawText(cell.value.toString(), left + cellSize / 2,
                    top + cellSize / 2 - (paint.ascent() + paint.descent()) / 2, paint)
            } else {
                paint.textSize = cellSize * 0.27f
                paint.typeface = Typeface.DEFAULT
                cell.candidates.forEach { digit ->
                    val candidateSize = cellSize / 3
                    val x = left + (digit - 1) % 3 * candidateSize
                    val y = top + (digit - 1) / 3 * candidateSize
                    notes.candidateColor(index, digit)?.let {
                        drawRect(it.toComposeColor().copy(alpha = 0.48f), Offset(x, y), Size(candidateSize, candidateSize))
                    }
                    paint.color = colors.onSurface.toArgb()
                    drawContext.canvas.nativeCanvas.drawText(digit.toString(), x + candidateSize / 2,
                        y + candidateSize / 2 - (paint.ascent() + paint.descent()) / 2, paint)
                }
            }
        }
        example.patternCandidates.forEach { candidate ->
            val candidateSize = cellSize / 3
            val left = line / 2 + candidate.cell.col * cellSize + (candidate.digit - 1) % 3 * candidateSize
            val top = line / 2 + candidate.cell.row * cellSize + (candidate.digit - 1) / 3 * candidateSize
            drawRect(PatternColor, Offset(left, top), Size(candidateSize, candidateSize), style = Stroke(line * 0.35f))
        }
        example.deduction.eliminations.forEach { elimination ->
            val candidate = elimination.candidate
            val candidateSize = cellSize / 3
            val left = line / 2 + candidate.cell.col * cellSize + (candidate.digit - 1) % 3 * candidateSize
            val top = line / 2 + candidate.cell.row * cellSize + (candidate.digit - 1) / 3 * candidateSize
            drawLine(ExclusionColor, Offset(left, top), Offset(left + candidateSize, top + candidateSize), line * 0.35f)
            drawLine(ExclusionColor, Offset(left + candidateSize, top), Offset(left, top + candidateSize), line * 0.35f)
        }
        repeat(8) { index ->
            val position = line / 2 + (index + 1) * cellSize
            val major = (index + 1) % 3 == 0
            val color = colors.primary.copy(alpha = if (major) 1f else 0.35f)
            val width = if (major) line else line * 0.4f
            drawLine(color, Offset(position, 0f), Offset(position, size.height), width)
            drawLine(color, Offset(0f, position), Offset(size.width, position), width)
        }
        drawRoundRect(colors.primary, Offset(line / 2, line / 2),
            Size(size.width - line, size.height - line),
            CornerRadius(4.dp.toPx()), style = Stroke(line))
    }
}

private fun TutorialExample.annotations(stage: TutorialStage): AdvancedNotes {
    if (stage == TutorialStage.RULE) return AdvancedNotes()
    val showExclusions = stage == TutorialStage.PATTERN || stage == TutorialStage.DEDUCTION
    return AdvancedNotes(
        cellColors = buildMap {
            if (showExclusions) (excludedCells + blockingGivens).forEach { put(it.index, AdvancedNoteColor.RED) }
            patternCells.forEach { put(it.index, AdvancedNoteColor.GREEN) }
        },
        candidateColors = buildMap {
            patternCandidates.forEach { put(AdvancedNotes.candidateKey(it.cell.index, it.digit), AdvancedNoteColor.GREEN) }
            if (stage == TutorialStage.DEDUCTION) deduction.eliminations.forEach {
                val candidate = it.candidate
                put(AdvancedNotes.candidateKey(candidate.cell.index, candidate.digit), AdvancedNoteColor.RED)
            }
        },
    )
}

internal val PatternColor = Color(AdvancedNoteColor.GREEN.argb)
internal val ExclusionColor = Color(AdvancedNoteColor.RED.argb)
internal fun tutorialContainerKey(id: String) = "tutorial_container_$id"
internal fun tutorialBoardKey(id: String) = "tutorial_board_$id"
