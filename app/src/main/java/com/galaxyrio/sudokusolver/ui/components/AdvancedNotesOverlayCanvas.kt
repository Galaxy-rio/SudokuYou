package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteEndpoint
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import kotlin.math.min
import kotlin.math.sqrt

/** Draws persistent, user-authored annotations without changing the logical Sudoku state. */
@Composable
internal fun AdvancedNotesOverlayCanvas(
    sudoku: Sudoku,
    notes: AdvancedNotes,
    showDynamicHighlights: Boolean,
    pendingLineStart: AdvancedNoteEndpoint?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val frameColor = MaterialTheme.colorScheme.primary
    val outerInset = with(density) { 2.dp.toPx() }
    val blockGap = with(density) { 2.dp.toPx() }
    val cellGap = with(density) { 1.dp.toPx() }
    val candidateInset = with(density) { 1.dp.toPx() }
    val frameStroke = with(density) { 3.dp.toPx() }
    val lineStroke = with(density) { 2.5.dp.toPx() }
    val dashLength = with(density) { 7.dp.toPx() }
    val dashGap = with(density) { 5.dp.toPx() }

    Canvas(modifier = modifier) {
        val geometry = BoardGeometry(
            boardSize = min(size.width, size.height),
            outerInset = outerInset,
            blockGap = blockGap,
            cellGap = cellGap,
            candidateInset = candidateInset,
        )

        if (showDynamicHighlights && notes.frameHighlightedCells) {
            val framedCells = framedCellIndices(sudoku, notes)
            framedCells.forEach { cellIndex ->
                drawFrameBoundary(
                    geometry = geometry,
                    cell = CellRef.fromIndex(cellIndex),
                    framedCells = framedCells,
                    color = frameColor,
                    strokeWidth = frameStroke,
                )
            }
        }

        notes.lines.forEach { line ->
            drawAdvancedLine(
                geometry = geometry,
                startEndpoint = line.start,
                endEndpoint = line.end,
                color = line.color.toComposeColor(),
                style = line.style,
                strokeWidth = lineStroke,
                dashLength = dashLength,
                dashGap = dashGap,
            )
        }

        pendingLineStart?.let { endpoint ->
            val center = geometry.endpointCenter(endpoint)
            val radius = if (endpoint.candidateDigit == null) {
                geometry.cellSize * 0.16f
            } else {
                geometry.candidateSize * 0.38f
            }
            drawCircle(
                color = frameColor,
                radius = radius,
                center = center,
                style = Stroke(width = lineStroke),
            )
        }
    }
}

internal fun AdvancedNoteColor.toComposeColor(): Color = Color(argb)

private fun framedCellIndices(sudoku: Sudoku, notes: AdvancedNotes): Set<Int> = buildSet {
    sudoku.cells.forEachIndexed { index, cell ->
        val containsHighlightedValue = cell.value in notes.highlightedDigits
        val containsHighlightedCandidate = cell.candidates.any(notes.highlightedDigits::contains)
        val isHighlightedBivalue = notes.highlightBivalueCandidates &&
            !cell.isSolved() && cell.candidates.size == 2
        if (containsHighlightedValue || containsHighlightedCandidate || isHighlightedBivalue) {
            add(index)
        }
    }
}

private fun DrawScope.drawFrameBoundary(
    geometry: BoardGeometry,
    cell: CellRef,
    framedCells: Set<Int>,
    color: Color,
    strokeWidth: Float,
) {
    val bounds = geometry.cellBounds(cell)
    val halfStroke = strokeWidth / 2f
    fun isFramed(row: Int, col: Int): Boolean =
        row in 0 until Sudoku.GRID_SIZE &&
            col in 0 until Sudoku.GRID_SIZE &&
            row * Sudoku.GRID_SIZE + col in framedCells

    if (!isFramed(cell.row - 1, cell.col)) {
        drawLine(
            color = color,
            start = Offset(bounds.left - halfStroke, bounds.top),
            end = Offset(bounds.right + halfStroke, bounds.top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square,
        )
    }
    if (!isFramed(cell.row + 1, cell.col)) {
        drawLine(
            color = color,
            start = Offset(bounds.left - halfStroke, bounds.bottom),
            end = Offset(bounds.right + halfStroke, bounds.bottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square,
        )
    }
    if (!isFramed(cell.row, cell.col - 1)) {
        drawLine(
            color = color,
            start = Offset(bounds.left, bounds.top - halfStroke),
            end = Offset(bounds.left, bounds.bottom + halfStroke),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square,
        )
    }
    if (!isFramed(cell.row, cell.col + 1)) {
        drawLine(
            color = color,
            start = Offset(bounds.right, bounds.top - halfStroke),
            end = Offset(bounds.right, bounds.bottom + halfStroke),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square,
        )
    }
}

private fun DrawScope.drawAdvancedLine(
    geometry: BoardGeometry,
    startEndpoint: AdvancedNoteEndpoint,
    endEndpoint: AdvancedNoteEndpoint,
    color: Color,
    style: AdvancedNoteLineStyle,
    strokeWidth: Float,
    dashLength: Float,
    dashGap: Float,
) {
    val start = geometry.endpointCenter(startEndpoint)
    val end = geometry.endpointCenter(endEndpoint)
    val delta = end - start
    val length = sqrt(delta.x * delta.x + delta.y * delta.y)
    if (length <= 0f) return
    val direction = Offset(delta.x / length, delta.y / length)
    val startPadding = geometry.endpointPadding(startEndpoint).coerceAtMost(length * 0.20f)
    val endPadding = geometry.endpointPadding(endEndpoint).coerceAtMost(length * 0.20f)

    drawLine(
        color = color.copy(alpha = 0.92f),
        start = start + direction * startPadding,
        end = end - direction * endPadding,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = if (style == AdvancedNoteLineStyle.DASHED) {
            PathEffect.dashPathEffect(floatArrayOf(dashLength, dashGap))
        } else {
            null
        },
    )
}

private fun BoardGeometry.endpointCenter(endpoint: AdvancedNoteEndpoint): Offset {
    val cell = CellRef.fromIndex(endpoint.cellIndex)
    return endpoint.candidateDigit?.let { digit ->
        candidateCenter(CandidateRef(cell, digit))
    } ?: cellBounds(cell).center
}

private fun BoardGeometry.endpointPadding(endpoint: AdvancedNoteEndpoint): Float =
    if (endpoint.candidateDigit == null) cellSize * 0.16f else candidateSize * 0.34f
