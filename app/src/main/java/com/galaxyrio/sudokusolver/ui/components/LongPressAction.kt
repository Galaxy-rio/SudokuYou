package com.galaxyrio.sudokusolver.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import com.galaxyrio.sudokusolver.R

/**
 * Adds a long action to an existing clickable without replacing its press interactions or ripple.
 * Observe the initial pass, then consume the release only after a successful long press so the
 * component's normal click cannot also run.
 */
@Composable
internal fun Modifier.onLongPressAction(
    action: (() -> Unit)?,
    enabled: Boolean = true,
): Modifier {
    if (!enabled || action == null) return this

    val currentAction by rememberUpdatedState(action)
    val hapticFeedback = LocalHapticFeedback.current
    val actionLabel = stringResource(R.string.game_alternate_note_mode_action)
    return this
        .semantics {
            onLongClick(label = actionLabel) {
                currentAction()
                true
            }
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                val finishedBeforeTimeout = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    true
                }
                if (finishedBeforeTimeout == null) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentAction()
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            }
        }
}
