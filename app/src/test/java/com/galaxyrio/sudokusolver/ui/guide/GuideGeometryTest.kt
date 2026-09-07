package com.galaxyrio.sudokusolver.ui.guide

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideGeometryTest {
    @Test
    fun expandedCircleEnclosesMenuWithoutMovingButtonCenter() {
        val center = Offset(350f, 730f)
        val fab = Rect(320f, 700f, 380f, 760f)
        val menu = Rect(140f, 300f, 390f, 770f)
        val radius = enclosingRadius(center, menu)
        assertTrue(radius > enclosingRadius(center, fab))
        listOf(menu.topLeft, menu.topRight, menu.bottomLeft, menu.bottomRight).forEach {
            assertTrue((it - center).getDistance() <= radius)
        }
        assertEquals((menu.topLeft - center).getDistance(), radius, 0.001f)
    }

    @Test
    fun firstCaptionSitsImmediatelyAboveFabCutout() {
        val safe = Rect(20f, 40f, 380f, 820f)
        val hole = Rect(Offset(350f, 760f), 48f)
        val placement = guideCaptionPlacement(safe, hole, 16f, 260f, 152f)
        val position = placement.position(360f, 132f)
        assertEquals(CaptionSide.ABOVE, placement.side)
        assertEquals(hole.top - 16f, position.y + 132f, 0.001f)
        assertEquals(safe.left, position.x, 0.001f)
    }

    @Test
    fun captionAvoidsPortraitToolsAndLandscapeMenu() {
        val cases = listOf(
            Rect(20f, 40f, 380f, 820f) to Rect(20f, 710f, 380f, 800f),
            Rect(20f, 40f, 380f, 820f) to Rect(-20f, -200f, 720f, 470f),
            Rect(40f, 30f, 820f, 390f) to Rect(470f, -250f, 1070f, 430f),
        )
        cases.forEach { (safe, target) ->
            val placement = guideCaptionPlacement(safe, target, 16f, 260f, 112f)
            val width = minOf(380f, placement.area.width)
            val position = placement.position(width, 112f)
            val caption = Rect(position.x, position.y, position.x + width, position.y + 112f)
            assertTrue(safe.contains(caption.topLeft))
            assertTrue(caption.right <= safe.right && caption.bottom <= safe.bottom)
            assertFalse(caption.overlaps(target))
        }
    }

    @Test
    fun oneGroupUsesSameAvoidanceAreaForEveryTarget() {
        val targets = (0..4).map { Rect(Offset(45f + it * 76f, 760f), 42f) }
        val union = unionGuideBounds(targets)
        assertEquals(Rect(3f, 718f, 391f, 802f), union)
        val placement = guideCaptionPlacement(Rect(20f, 40f, 380f, 820f), union, 16f, 260f, 112f)
        assertEquals(590f, placement.position(360f, 112f).y, 0.001f)
    }

    @Test
    fun finishIsCenteredWithoutACutout() {
        val safe = Rect(20f, 40f, 380f, 820f)
        val placement = guideCaptionPlacement(safe, null, 16f, 260f, 152f)
        assertEquals(Offset(50f, 354f), placement.position(300f, 152f))
    }
}
