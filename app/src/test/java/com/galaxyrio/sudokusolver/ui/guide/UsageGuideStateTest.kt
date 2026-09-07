package com.galaxyrio.sudokusolver.ui.guide

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageGuideStateTest {
    @Test
    fun gameGuideRequiresStartingGameAndCanBeCompletedAndReplayed() {
        val guide = UsageGuideState(GuideStep.START)
        guide.next()
        assertEquals(GuideStep.START, guide.step)
        guide.onFabExpanded(true)
        guide.next()
        assertEquals(GuideStep.DIFFICULTY, guide.step)
        guide.onGameStarted()
        guide.previous()
        assertEquals(GuideStep.NUMBERS, guide.step)
        assertTrue(guide.isGameGuide)
        while (guide.isActive) guide.next()
        assertNull(guide.step)
        guide.onGameStarted()
        assertFalse(guide.isActive)
        guide.replay()
        assertEquals(GuideStep.START, guide.step)
        assertEquals(1, guide.runId)
    }

    @Test
    fun menuCanBeExpandedByNextOrByTappingAndPreviousCollapsesIt() {
        val guide = UsageGuideState(GuideStep.MORE)
        guide.next()
        assertEquals(GuideStep.MENU, guide.step)
        guide.previous()
        assertEquals(GuideStep.MORE, guide.step)
        guide.onMoreExpanded(true)
        assertEquals(GuideStep.MENU, guide.step)
        guide.next()
        assertEquals(GuideStep.ADVANCED_NOTES, guide.step)
        guide.previous()
        assertEquals(GuideStep.MENU, guide.step)
        guide.onMoreExpanded(false)
        assertEquals(GuideStep.MORE, guide.step)
    }

    @Test
    fun onlyAdvancedNotesNeedDetailsAmongTheElevenTools() {
        val toolbar = GuideStep.entries.filter { it.series == GuideSeries.TOOLBAR }
        val advanced = GuideStep.entries.filter { it.series == GuideSeries.ADVANCED }
        assertEquals(5, toolbar.size)
        assertEquals(6, advanced.size)
        assertTrue(toolbar.all { it.message == null })
        assertEquals(listOf(GuideStep.ADVANCED_NOTES), advanced.filter { it.message != null })
        val finish = UsageGuideState(GuideStep.FINISH)
        assertNull(finish.step!!.target)
        finish.next()
        assertFalse(finish.isActive)
    }

    @Test
    fun savedGuideRestoresCurrentToolAndDismissal() {
        val scope = object : SaverScope {
            override fun canBeSaved(value: Any): Boolean = true
        }
        val guide = UsageGuideState(GuideStep.PAINT, initialRunId = 2)
        fun restore(): UsageGuideState {
            val saved = with(UsageGuideState.Saver) { scope.save(guide) }
            return UsageGuideState.Saver.restore(requireNotNull(saved))!!
        }
        assertEquals(GuideStep.PAINT, restore().step)
        assertEquals(2, restore().runId)
        guide.dismiss()
        assertFalse(restore().isActive)
    }
}
