package com.galaxyrio.sudokusolver.ui.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

internal const val GuideMotionDurationMillis = 360

/** A circle and a rounded rectangle share one continuously animatable outline. */
internal data class GuideSpotlight(val bounds: Rect, val roundness: Float)

internal fun enclosingRadius(center: Offset, bounds: Rect): Float = maxOf(
    (bounds.topLeft - center).getDistance(),
    (bounds.topRight - center).getDistance(),
    (bounds.bottomLeft - center).getDistance(),
    (bounds.bottomRight - center).getDistance(),
)

internal fun unionGuideBounds(bounds: List<Rect>): Rect = bounds.reduce { first, second ->
    Rect(
        minOf(first.left, second.left), minOf(first.top, second.top),
        maxOf(first.right, second.right), maxOf(first.bottom, second.bottom),
    )
}

internal enum class CaptionSide { ABOVE, BELOW, LEFT, RIGHT, CENTER }

internal data class GuideCaptionPlacement(
    val area: Rect,
    val side: CaptionSide,
    val targetCenter: Offset,
) {
    fun position(width: Float, height: Float): Offset {
        val x = when (side) {
            CaptionSide.LEFT -> area.right - width
            CaptionSide.RIGHT -> area.left
            else -> targetCenter.x - width / 2
        }.coerceIn(area.left, (area.right - width).coerceAtLeast(area.left))
        val y = when (side) {
            CaptionSide.ABOVE -> area.bottom - height
            CaptionSide.BELOW -> area.top
            else -> targetCenter.y - height / 2
        }.coerceIn(area.top, (area.bottom - height).coerceAtLeast(area.top))
        return Offset(x, y)
    }
}

/** Place measured text next to the cutout, rather than centering it in unused space. */
internal fun guideCaptionPlacement(
    safeBounds: Rect,
    avoidanceBounds: Rect?,
    gap: Float,
    minimumWidth: Float,
    preferredHeight: Float,
): GuideCaptionPlacement {
    if (avoidanceBounds == null) {
        return GuideCaptionPlacement(safeBounds, CaptionSide.CENTER, safeBounds.center)
    }
    val target = avoidanceBounds
    val regions = listOf(
        CaptionSide.ABOVE to Rect(
            safeBounds.left, safeBounds.top, safeBounds.right,
            (target.top - gap).coerceIn(safeBounds.top, safeBounds.bottom),
        ),
        CaptionSide.BELOW to Rect(
            safeBounds.left, (target.bottom + gap).coerceIn(safeBounds.top, safeBounds.bottom),
            safeBounds.right, safeBounds.bottom,
        ),
        CaptionSide.LEFT to Rect(
            safeBounds.left, safeBounds.top,
            (target.left - gap).coerceIn(safeBounds.left, safeBounds.right), safeBounds.bottom,
        ),
        CaptionSide.RIGHT to Rect(
            (target.right + gap).coerceIn(safeBounds.left, safeBounds.right), safeBounds.top,
            safeBounds.right, safeBounds.bottom,
        ),
    )
    val preferredSide = if (target.center.y >= safeBounds.center.y) CaptionSide.ABOVE else CaptionSide.BELOW
    val usable = regions.filter { (_, area) ->
        area.width >= minOf(minimumWidth, safeBounds.width) && area.height >= preferredHeight
    }
    val chosen = usable.firstOrNull { it.first == preferredSide }
        ?: usable.maxByOrNull { (_, area) -> minOf(area.width, minimumWidth) * area.height }
        ?: regions.maxBy { (_, area) -> area.width * area.height }
    return GuideCaptionPlacement(chosen.second, chosen.first, target.center)
}
