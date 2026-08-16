package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.InferenceLink
import com.galaxyrio.sudokusolver.domain.solver.InferenceLinkType
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import kotlin.math.min
import kotlin.math.sqrt

/** Read-only semantic layer for the currently selected logical solve step. */
@Composable
internal fun StepOverlayCanvas(
    step: SolveStep,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val outerInset = with(density) { 2.dp.toPx() }
    val blockGap = with(density) { 2.dp.toPx() }
    val cellGap = with(density) { 1.dp.toPx() }
    val candidateInset = with(density) { 1.dp.toPx() }
    val thinStroke = with(density) { 1.25.dp.toPx() }
    val strongStroke = with(density) { 2.dp.toPx() }
    val dualStroke = with(density) { 2.75.dp.toPx() }
    val dashLength = with(density) { 5.dp.toPx() }
    val dashGap = with(density) { 4.dp.toPx() }
    val arrowLength = with(density) { 5.dp.toPx() }
    val colors = StepOverlayColors(
        genericHouse = colorScheme.tertiaryContainer,
        baseHouse = colorScheme.primaryContainer,
        coverHouse = colorScheme.secondaryContainer,
        causeCell = colorScheme.tertiaryContainer,
        targetCell = colorScheme.errorContainer,
        causeCandidate = colorScheme.tertiary,
        targetCandidate = colorScheme.error,
        placementCandidate = colorScheme.primary,
        strongLink = colorScheme.error,
        weakLink = colorScheme.primary,
        dualLink = colorScheme.tertiary,
    )

    Canvas(modifier = modifier) {
        val geometry = BoardGeometry(
            boardSize = min(size.width, size.height),
            outerInset = outerInset,
            blockGap = blockGap,
            cellGap = cellGap,
            candidateInset = candidateInset,
        )
        val evidence = step.evidence
        val baseHouses = evidence.baseHouses.toSet()
        val coverHouses = evidence.coverHouses.toSet()

        evidence.houses
            .filterNot { it in baseHouses || it in coverHouses }
            .forEach { house ->
                drawHouseHighlight(geometry, house, colors.genericHouse.copy(alpha = 0.14f))
            }
        baseHouses.forEach { house ->
            drawHouseHighlight(geometry, house, colors.baseHouse.copy(alpha = 0.20f))
        }
        coverHouses.forEach { house ->
            drawHouseHighlight(geometry, house, colors.coverHouse.copy(alpha = 0.18f))
        }

        evidence.causeCells.forEach { cell ->
            val bounds = geometry.cellBounds(cell)
            drawRoundRect(
                color = colors.causeCell.copy(alpha = 0.34f),
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(geometry.cellSize * 0.08f),
            )
        }
        val targetCells = buildSet {
            step.placements.forEach { add(it.cell) }
            step.eliminations.forEach { add(it.candidate.cell) }
        }
        targetCells.forEach { cell ->
            val bounds = geometry.cellBounds(cell)
            drawRoundRect(
                color = colors.targetCell.copy(alpha = 0.34f),
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(geometry.cellSize * 0.08f),
            )
        }

        // The graph may contain several branches that split and converge. Legacy techniques are
        // automatically represented as a graph by StepEvidence, so the canvas has one code path.
        evidence.inferenceGraph.links.forEach { link ->
            drawInferenceLink(
                geometry = geometry,
                link = link,
                colors = colors,
                strongStroke = strongStroke,
                dualStroke = dualStroke,
                dashLength = dashLength,
                dashGap = dashGap,
                arrowLength = arrowLength,
            )
        }

        evidence.causeCandidates.forEach { candidate ->
            drawCandidateMarker(
                geometry = geometry,
                candidate = candidate,
                fill = colors.causeCandidate.copy(alpha = 0.22f),
                outline = colors.causeCandidate,
                strokeWidth = thinStroke,
            )
        }
        step.placements.forEach { placement ->
            drawCandidateMarker(
                geometry = geometry,
                candidate = CandidateRef(placement.cell, placement.digit),
                fill = colors.placementCandidate.copy(alpha = 0.24f),
                outline = colors.placementCandidate,
                strokeWidth = strongStroke,
            )
        }
        step.eliminations.forEach { elimination ->
            val candidate = elimination.candidate
            drawCandidateMarker(
                geometry = geometry,
                candidate = candidate,
                fill = colors.targetCell.copy(alpha = 0.80f),
                outline = colors.targetCandidate,
                strokeWidth = strongStroke,
            )
            drawCandidateCross(
                geometry = geometry,
                candidate = candidate,
                color = colors.targetCandidate,
                strokeWidth = thinStroke,
            )
        }
    }
}

/** Highlights invalid player entries or candidates that must be restored to make Sukaku valid. */
@Composable
internal fun CorrectionOverlayCanvas(
    cells: Set<com.galaxyrio.sudokusolver.domain.solver.CellRef>,
    candidates: Set<CandidateRef>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val outerInset = with(density) { 2.dp.toPx() }
    val blockGap = with(density) { 2.dp.toPx() }
    val cellGap = with(density) { 1.dp.toPx() }
    val candidateInset = with(density) { 1.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }

    Canvas(modifier = modifier) {
        val geometry = BoardGeometry(
            boardSize = min(size.width, size.height),
            outerInset = outerInset,
            blockGap = blockGap,
            cellGap = cellGap,
            candidateInset = candidateInset,
        )
        cells.forEach { cell ->
            val bounds = geometry.cellBounds(cell)
            drawRoundRect(
                color = colorScheme.errorContainer.copy(alpha = 0.52f),
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(geometry.cellSize * 0.08f),
            )
        }
        candidates.forEach { candidate ->
            drawCandidateMarker(
                geometry = geometry,
                candidate = candidate,
                fill = colorScheme.errorContainer.copy(alpha = 0.72f),
                outline = colorScheme.error,
                strokeWidth = strokeWidth,
            )
        }
    }
}

private data class StepOverlayColors(
    val genericHouse: Color,
    val baseHouse: Color,
    val coverHouse: Color,
    val causeCell: Color,
    val targetCell: Color,
    val causeCandidate: Color,
    val targetCandidate: Color,
    val placementCandidate: Color,
    val strongLink: Color,
    val weakLink: Color,
    val dualLink: Color,
)

private fun DrawScope.drawHouseHighlight(
    geometry: BoardGeometry,
    house: HouseRef,
    color: Color,
) {
    house.cells().forEach { cell ->
        val bounds = geometry.cellBounds(cell)
        drawRect(color = color, topLeft = bounds.topLeft, size = bounds.size)
    }
}

private fun DrawScope.drawCandidateMarker(
    geometry: BoardGeometry,
    candidate: CandidateRef,
    fill: Color,
    outline: Color,
    strokeWidth: Float,
) {
    val bounds = geometry.candidateBounds(candidate)
    val radius = min(bounds.width, bounds.height) * 0.43f
    drawCircle(color = fill, radius = radius, center = bounds.center)
    drawCircle(
        color = outline,
        radius = radius,
        center = bounds.center,
        style = Stroke(width = strokeWidth),
    )
}

private fun DrawScope.drawCandidateCross(
    geometry: BoardGeometry,
    candidate: CandidateRef,
    color: Color,
    strokeWidth: Float,
) {
    val bounds = geometry.candidateBounds(candidate)
    val radius = min(bounds.width, bounds.height) * 0.25f
    drawLine(
        color = color,
        start = bounds.center + Offset(-radius, -radius),
        end = bounds.center + Offset(radius, radius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = bounds.center + Offset(-radius, radius),
        end = bounds.center + Offset(radius, -radius),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawInferenceLink(
    geometry: BoardGeometry,
    link: InferenceLink,
    colors: StepOverlayColors,
    strongStroke: Float,
    dualStroke: Float,
    dashLength: Float,
    dashGap: Float,
    arrowLength: Float,
) {
    val from = geometry.candidateCenter(link.from)
    val to = geometry.candidateCenter(link.to)
    val delta = to - from
    val length = sqrt(delta.x * delta.x + delta.y * delta.y)
    if (length <= 0f) return

    val direction = Offset(delta.x / length, delta.y / length)
    val endpointPadding = min(geometry.candidateSize * 0.30f, length * 0.18f)
    val lineStart = from + direction * endpointPadding
    val lineEnd = to - direction * endpointPadding
    val color = when (link.type) {
        InferenceLinkType.STRONG -> colors.strongLink
        InferenceLinkType.WEAK -> colors.weakLink
        InferenceLinkType.DUAL -> colors.dualLink
    }
    val strokeWidth = if (link.type == InferenceLinkType.DUAL) dualStroke else strongStroke
    val pathEffect = if (link.type == InferenceLinkType.WEAK) {
        PathEffect.dashPathEffect(floatArrayOf(dashLength, dashGap))
    } else {
        null
    }

    drawLine(
        color = color,
        start = lineStart,
        end = lineEnd,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        pathEffect = pathEffect,
    )
    drawArrowHead(
        tip = lineEnd,
        direction = direction,
        color = color,
        length = min(arrowLength, length * 0.22f),
    )
}

private fun DrawScope.drawArrowHead(
    tip: Offset,
    direction: Offset,
    color: Color,
    length: Float,
) {
    val perpendicular = Offset(-direction.y, direction.x)
    val base = tip - direction * length
    val halfWidth = length * 0.55f
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(base.x + perpendicular.x * halfWidth, base.y + perpendicular.y * halfWidth)
        lineTo(base.x - perpendicular.x * halfWidth, base.y - perpendicular.y * halfWidth)
        close()
    }
    drawPath(path = path, color = color)
}
