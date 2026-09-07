package com.galaxyrio.sudokusolver.ui.guide

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.SettingsRepository
import kotlin.math.roundToInt

internal val GuideSpotlightBounds = SemanticsPropertyKey<Rect>("Guide spotlight bounds")

fun Modifier.guideTarget(
    target: GuideTarget,
    label: String? = null,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val guide = LocalUsageGuide.current
    if (guide == null || !guide.isActive) return@composed this
    val owner = remember { Any() }
    val currentOnClick by rememberUpdatedState(onClick)
    val action = remember(owner) { { currentOnClick?.invoke(); Unit } }
    DisposableEffect(guide, target, owner) {
        onDispose {
            if (guide.anchors[target]?.owner === owner) guide.anchors.remove(target)
        }
    }
    SideEffect {
        guide.anchors[target]?.takeIf { it.owner === owner }?.let { anchor ->
            val updated = anchor.copy(label = label, onClick = action.takeIf { onClick != null })
            if (anchor != updated) guide.anchors[target] = updated
        }
    }
    onGloballyPositioned { coordinates ->
        guide.anchors[target] = GuideAnchor(
            owner = owner,
            bounds = coordinates.boundsInWindow(),
            label = label,
            onClick = action.takeIf { onClick != null },
        )
    }
}

@Composable
fun UsageGuideHost(repository: SettingsRepository, content: @Composable () -> Unit) {
    val guide = rememberSaveable(saver = UsageGuideState.Saver) {
        UsageGuideState(if (repository.settings.value.hasSeenUsageGuide) null else GuideStep.START)
    }
    LaunchedEffect(repository) {
        if (!repository.settings.value.hasSeenUsageGuide) repository.markUsageGuideSeen()
    }
    UsageGuideLayout(guide, content)
}

private data class GuidePresentation(
    val step: GuideStep,
    val spotlight: GuideSpotlight?,
    val avoidanceBounds: Rect?,
)

private fun UsageGuideState.presentation(padding: Float): GuidePresentation? {
    val current = step ?: return null
    if (current == GuideStep.FINISH) return GuidePresentation(current, null, null)

    fun spotlightFor(item: GuideStep): GuideSpotlight? {
        val bounds = anchors[item.target]?.bounds?.takeUnless { it.isEmpty } ?: return null
        if (item == GuideStep.NUMBERS) {
            return GuideSpotlight(bounds.inflate(padding), roundness = 0.12f)
        }
        val center = when {
            item.isHomeStep -> anchors[GuideTarget.NEW_GAME]?.bounds?.center
            item.series == GuideSeries.MENU -> anchors[GuideTarget.MORE]?.bounds?.center
            else -> bounds.center
        } ?: return null
        return GuideSpotlight(Rect(center, enclosingRadius(center, bounds) + padding), 1f)
    }

    val spotlight = spotlightFor(current) ?: return null
    val seriesSteps = GuideStep.entries.filter { it.series == current.series }
    val seriesBounds = seriesSteps.map { spotlightFor(it)?.bounds ?: return null }
    return GuidePresentation(current, spotlight, unionGuideBounds(seriesBounds))
}

@Composable
internal fun UsageGuideLayout(guide: UsageGuideState, content: @Composable () -> Unit) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val padding = with(LocalDensity.current) { 10.dp.toPx() }
    val resolved = guide.presentation(padding)
    var lastPresentation by remember { mutableStateOf<GuidePresentation?>(null) }
    SideEffect {
        if (!guide.isActive) lastPresentation = null
        else if (resolved != null) lastPresentation = resolved
    }
    // Keep the same overlay alive during navigation and while the next target is laid out.
    // Its outline can then travel from the previous target instead of disappearing/snapping.
    val presentation = if (guide.isActive) resolved ?: lastPresentation else null
    Box(
        modifier = Modifier.fillMaxSize().onGloballyPositioned {
            origin = it.boundsInWindow().topLeft
        },
    ) {
        CompositionLocalProvider(LocalUsageGuide provides guide) {
            Box(
                modifier = Modifier.fillMaxSize().then(
                    if (presentation != null) Modifier.clearAndSetSemantics { } else Modifier,
                ),
            ) { content() }
        }
        if (presentation != null) {
            GuideOverlay(guide, presentation, origin, ready = resolved != null)
        }
    }
    BackHandler(enabled = guide.isActive, onBack = guide::dismiss)
}

@Composable
private fun GuideOverlay(
    guide: UsageGuideState,
    presentation: GuidePresentation,
    origin: Offset,
    ready: Boolean,
) {
    val step = presentation.step
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insets = WindowInsets.safeDrawing
    val paneLabel = stringResource(R.string.settings_usage_guide)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().testTag("usage_guide").semantics { paneTitle = paneLabel },
    ) {
        val safeBounds = with(density) {
            Rect(
                insets.getLeft(this, layoutDirection) + 20.dp.toPx(),
                insets.getTop(this) + 20.dp.toPx(),
                maxWidth.toPx() - insets.getRight(this, layoutDirection) - 20.dp.toPx(),
                maxHeight.toPx() - insets.getBottom(this) - 20.dp.toPx(),
            )
        }
        val destination = presentation.spotlight?.let {
            it.copy(bounds = it.bounds.translate(-origin))
        } ?: GuideSpotlight(Rect(safeBounds.center, 0f), 1f)
        val animatedBounds by animateRectAsState(
            targetValue = destination.bounds,
            animationSpec = tween(GuideMotionDurationMillis, easing = FastOutSlowInEasing),
            label = "guide_hero_bounds",
        )
        val roundness by animateFloatAsState(
            targetValue = destination.roundness,
            animationSpec = tween(GuideMotionDurationMillis, easing = FastOutSlowInEasing),
            label = "guide_hero_roundness",
        )
        val reservedHeight = when (step.series) {
            GuideSeries.TOOLBAR, GuideSeries.MENU -> 112.dp
            GuideSeries.ADVANCED -> 240.dp * density.fontScale.coerceAtLeast(1f)
            else -> null
        }
        val placement = with(density) {
            guideCaptionPlacement(
                safeBounds = safeBounds,
                avoidanceBounds = presentation.avoidanceBounds?.translate(-origin),
                gap = 16.dp.toPx(),
                minimumWidth = 260.dp.toPx(),
                preferredHeight = (reservedHeight ?: 152.dp).toPx(),
            )
        }
        Canvas(
            modifier = Modifier.fillMaxSize().testTag("guide_spotlight")
                .semantics { this[GuideSpotlightBounds] = animatedBounds }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false).consume()
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                },
        ) {
            val corner = animatedBounds.size.minDimension.coerceAtLeast(0f) * roundness / 2
            val hole = Path().apply {
                addRoundRect(RoundRect(animatedBounds, corner, corner))
            }
            val scrim = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addPath(hole)
            }
            drawPath(scrim, Color.Black.copy(alpha = 0.88f))
            if (!animatedBounds.isEmpty) {
                drawPath(hole, Color.White.copy(alpha = 0.65f), style = Stroke(1.25.dp.toPx()))
            }
        }

        val actionTargets = if (!ready) emptyList() else when (step) {
            GuideStep.START -> listOf(GuideTarget.NEW_GAME)
            GuideStep.DIFFICULTY -> listOf(
                GuideTarget.NEW_GAME, GuideTarget.EASY, GuideTarget.MEDIUM,
                GuideTarget.HARD, GuideTarget.BRUTAL,
            )
            GuideStep.MORE -> listOf(GuideTarget.MORE)
            GuideStep.MENU -> listOf(GuideTarget.MORE, GuideTarget.ADVANCED_MODE)
            else -> emptyList()
        }
        actionTargets.forEach { id ->
            guide.anchors[id]?.takeIf { !it.bounds.isEmpty && it.onClick != null }?.let { anchor ->
                val bounds = anchor.bounds.translate(-origin)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
                        .size(with(density) { bounds.width.toDp() }, with(density) { bounds.height.toDp() })
                        .testTag("guide_action_${id.name}")
                        .semantics { contentDescription = anchor.label.orEmpty() }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { anchor.onClick?.invoke() },
                        ),
                )
            }
        }

        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                GuideCaption(
                    guide = guide,
                    step = step,
                    ready = ready,
                    fixedHeight = reservedHeight != null,
                    modifier = if (reservedHeight == null) Modifier else Modifier.height(reservedHeight),
                )
            },
        ) { measurables, constraints ->
            val caption = measurables.single().measure(
                Constraints(
                    maxWidth = minOf(380.dp.roundToPx(), placement.area.width.roundToInt()).coerceAtLeast(1),
                    maxHeight = placement.area.height.roundToInt().coerceAtLeast(1),
                ),
            )
            val position = placement.position(caption.width.toFloat(), caption.height.toFloat())
            layout(constraints.maxWidth, constraints.maxHeight) {
                caption.place(position.x.roundToInt(), position.y.roundToInt())
            }
        }
    }
}

@Composable
private fun GuideCaption(
    guide: UsageGuideState,
    step: GuideStep,
    ready: Boolean,
    fixedHeight: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().testTag("guide_caption")) {
        Crossfade(
            targetState = step,
            animationSpec = tween(150),
            label = "guide_caption_text",
            modifier = Modifier.weight(1f, fill = fixedHeight).fillMaxWidth(),
        ) { displayedStep ->
            Box(
                modifier = if (fixedHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(displayedStep.title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    displayedStep.message?.let { message ->
                        Text(
                            stringResource(message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.94f),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().testTag("guide_navigation"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val secondaryColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (step != GuideStep.FINISH) {
                Button(
                    onClick = guide::dismiss,
                    colors = secondaryColors,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).testTag("guide_skip"),
                ) { Text(stringResource(R.string.guide_skip), maxLines = 1) }
            } else Spacer(Modifier.weight(1f))
            if (step != GuideStep.START) {
                Button(
                    onClick = guide::previous,
                    enabled = ready && step != GuideStep.NUMBERS,
                    colors = secondaryColors,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).testTag("guide_previous"),
                ) { Text(stringResource(R.string.guide_previous), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            } else Spacer(Modifier.weight(1f))
            if (!step.isHomeStep) {
                Button(
                    onClick = guide::next,
                    enabled = ready,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).testTag("guide_next"),
                ) {
                    Text(
                        stringResource(if (step == GuideStep.FINISH) R.string.guide_done else R.string.guide_next),
                        maxLines = 1,
                    )
                }
            } else Spacer(Modifier.weight(1f))
        }
    }
}
