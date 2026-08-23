package com.galaxyrio.sudokusolver.ui.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

/**
 * Material 3 top-level transition for destinations owned by a navigation rail or drawer.
 *
 * The outgoing destination disappears first, then the incoming destination appears. Keeping the
 * two fades sequential avoids implying that these peer destinations share a spatial direction.
 */
fun materialTopLevelTransition(): ContentTransform =
    materialContainerEnter().togetherWith(materialContainerExit())

/** Supporting fade for non-shared content around a container transform. */
fun materialContainerEnter(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = IncomingFadeDurationMillis,
        delayMillis = OutgoingFadeDurationMillis,
        easing = LinearEasing,
    ),
)

fun materialContainerExit(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = OutgoingFadeDurationMillis,
        easing = LinearEasing,
    ),
)

/** A compact crossfade for text or other small state changes that should not imply direction. */
fun materialQuickCrossfade(): ContentTransform =
    materialQuickFadeIn().togetherWith(materialQuickFadeOut())

fun materialQuickFadeIn(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = QuickFadeDurationMillis,
        easing = LinearEasing,
    ),
)

fun materialQuickFadeOut(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = QuickFadeDurationMillis,
        easing = LinearEasing,
    ),
)

/** A hierarchy step that moves toward the logical start edge (forward in LTR and RTL). */
fun <S> AnimatedContentTransitionScope<S>.materialForwardEnter(
    maxDistancePx: Int,
): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = spatialTween(),
        initialOffset = { it.limitedTo(maxDistancePx) },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = IncomingFadeDurationMillis,
            delayMillis = OutgoingFadeDurationMillis,
            easing = LinearEasing,
        ),
    )

fun <S> AnimatedContentTransitionScope<S>.materialForwardExit(
    maxDistancePx: Int,
): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = spatialTween(),
        targetOffset = { it.limitedTo(maxDistancePx) },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = OutgoingFadeDurationMillis,
            easing = LinearEasing,
        ),
    )

/** The reverse hierarchy step, including predictive-back progress. */
fun <S> AnimatedContentTransitionScope<S>.materialBackwardEnter(
    maxDistancePx: Int,
): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = spatialTween(),
        initialOffset = { it.limitedTo(maxDistancePx) },
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = IncomingFadeDurationMillis,
            delayMillis = OutgoingFadeDurationMillis,
            easing = LinearEasing,
        ),
    )

fun <S> AnimatedContentTransitionScope<S>.materialBackwardExit(
    maxDistancePx: Int,
): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = spatialTween(),
        targetOffset = { it.limitedTo(maxDistancePx) },
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = OutgoingFadeDurationMillis,
            easing = LinearEasing,
        ),
    )

private fun <T> spatialTween() = tween<T>(
    durationMillis = TransitionDurationMillis,
    easing = FastOutSlowInEasing,
)

private fun Int.limitedTo(maxDistancePx: Int): Int =
    coerceIn(-maxDistancePx, maxDistancePx)

private const val TransitionDurationMillis = 300
private const val OutgoingFadeDurationMillis = 90
private const val IncomingFadeDurationMillis =
    TransitionDurationMillis - OutgoingFadeDurationMillis
private const val QuickFadeDurationMillis = 120
