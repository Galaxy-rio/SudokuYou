package com.galaxyrio.sudokusolver.ui.guide

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.model.Difficulty

enum class GuideTarget {
    NEW_GAME, DIFFICULTIES, EASY, MEDIUM, HARD, BRUTAL,
    NUMBERS, UNDO, REDO, ERASE, NOTES, HINT, MORE, MORE_MENU, ADVANCED_MODE,
    ADVANCED_NOTES, BIVALUE, PAINT, FRAME, SOLID_LINE, DASHED_LINE;

    companion object {
        fun difficulty(difficulty: Difficulty): GuideTarget = when (difficulty) {
            Difficulty.EASY -> EASY
            Difficulty.MEDIUM -> MEDIUM
            Difficulty.HARD -> HARD
            Difficulty.BRUTAL -> BRUTAL
        }
    }
}

enum class GuideSeries { START, DIFFICULTY, KEYBOARD, TOOLBAR, MENU, ADVANCED, FINISH }

enum class GuideStep(
    val target: GuideTarget?,
    @param:StringRes val title: Int,
    @param:StringRes val message: Int? = null,
    val series: GuideSeries,
) {
    START(GuideTarget.NEW_GAME, R.string.guide_welcome, R.string.guide_start, GuideSeries.START),
    DIFFICULTY(GuideTarget.DIFFICULTIES, R.string.guide_difficulty_title, R.string.guide_difficulty, GuideSeries.DIFFICULTY),
    NUMBERS(GuideTarget.NUMBERS, R.string.guide_numbers_title, R.string.guide_numbers, GuideSeries.KEYBOARD),
    UNDO(GuideTarget.UNDO, R.string.game_undo, series = GuideSeries.TOOLBAR),
    REDO(GuideTarget.REDO, R.string.game_redo, series = GuideSeries.TOOLBAR),
    ERASE(GuideTarget.ERASE, R.string.game_erase, series = GuideSeries.TOOLBAR),
    NOTES(GuideTarget.NOTES, R.string.game_note_mode, series = GuideSeries.TOOLBAR),
    HINT(GuideTarget.HINT, R.string.game_hint, series = GuideSeries.TOOLBAR),
    MORE(GuideTarget.MORE, R.string.game_more_options, series = GuideSeries.MENU),
    MENU(GuideTarget.MORE_MENU, R.string.game_more_options, series = GuideSeries.MENU),
    ADVANCED_NOTES(GuideTarget.ADVANCED_NOTES, R.string.game_advanced_note_mode, R.string.guide_advanced_notes, GuideSeries.ADVANCED),
    BIVALUE(GuideTarget.BIVALUE, R.string.game_advanced_bivalue, series = GuideSeries.ADVANCED),
    PAINT(GuideTarget.PAINT, R.string.game_advanced_paint, series = GuideSeries.ADVANCED),
    FRAME(GuideTarget.FRAME, R.string.game_advanced_frame, series = GuideSeries.ADVANCED),
    SOLID_LINE(GuideTarget.SOLID_LINE, R.string.game_advanced_solid_line, series = GuideSeries.ADVANCED),
    DASHED_LINE(GuideTarget.DASHED_LINE, R.string.game_advanced_dashed_line, series = GuideSeries.ADVANCED),
    FINISH(null, R.string.guide_finish_title, R.string.guide_finish_message, GuideSeries.FINISH);

    val isHomeStep: Boolean get() = this == START || this == DIFFICULTY
    val previewsAdvancedMode: Boolean get() = series == GuideSeries.ADVANCED || this == FINISH
}

internal data class GuideAnchor(
    val owner: Any,
    val bounds: Rect,
    val label: String?,
    val onClick: (() -> Unit)?,
)

@Stable
class UsageGuideState(initialStep: GuideStep? = null, initialRunId: Int = 0) {
    var step by mutableStateOf(initialStep)
        private set
    var runId by mutableIntStateOf(initialRunId)
        private set
    internal val anchors = mutableStateMapOf<GuideTarget, GuideAnchor>()

    val isActive: Boolean get() = step != null
    val isHomeGuide: Boolean get() = step?.isHomeStep == true
    val isGameGuide: Boolean get() = step?.isHomeStep == false

    fun replay() {
        runId++
        step = GuideStep.START
    }

    fun onFabExpanded(expanded: Boolean) {
        if (isHomeGuide) step = if (expanded) GuideStep.DIFFICULTY else GuideStep.START
    }

    fun onGameStarted() {
        if (isHomeGuide) step = GuideStep.NUMBERS
    }

    fun onMoreExpanded(expanded: Boolean) {
        if (step?.series == GuideSeries.MENU) {
            step = if (expanded) GuideStep.MENU else GuideStep.MORE
        }
    }

    fun next() {
        val current = step ?: return
        // The first two steps advance only by using the real FAB/menu actions.
        if (!current.isHomeStep) step = GuideStep.entries.getOrNull(current.ordinal + 1)
    }

    fun previous() {
        val current = step ?: return
        if (current == GuideStep.DIFFICULTY) {
            anchors[GuideTarget.NEW_GAME]?.onClick?.invoke()
        } else if (current.ordinal > GuideStep.NUMBERS.ordinal) {
            step = GuideStep.entries[current.ordinal - 1]
        }
    }

    fun dismiss() {
        if (step == GuideStep.DIFFICULTY) anchors[GuideTarget.NEW_GAME]?.onClick?.invoke()
        step = null
    }

    companion object {
        val Saver = listSaver<UsageGuideState, Any>(
            save = { listOf(it.step?.name.orEmpty(), it.runId) },
            restore = { values ->
                UsageGuideState(
                    initialStep = GuideStep.entries.firstOrNull { it.name == values[0] },
                    initialRunId = values[1] as Int,
                )
            },
        )
    }
}

val LocalUsageGuide = staticCompositionLocalOf<UsageGuideState?> { null }
