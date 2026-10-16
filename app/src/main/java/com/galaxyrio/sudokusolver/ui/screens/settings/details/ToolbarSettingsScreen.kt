package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.ToolbarConfiguration
import com.galaxyrio.sudokusolver.data.settings.ToolbarTool
import com.galaxyrio.sudokusolver.ui.components.ToolbarToolIcon
import com.galaxyrio.sudokusolver.ui.components.titleResource
import kotlin.math.roundToInt

@Composable
fun ToolbarSettingsScreen(
    configuration: ToolbarConfiguration,
    onToolCountChange: (Int) -> Unit,
    onToolOrderChange: (List<ToolbarTool>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val saveOrder by rememberUpdatedState(onToolOrderChange)
    val reorder = remember(listState) {
        ToolbarReorderState(
            listState = listState,
            firstToolIndex = 2,
            initialOrder = configuration.toolOrder,
            onOrderChanged = { saveOrder(it) },
        )
    }
    LaunchedEffect(configuration.toolOrder) { reorder.syncOrder(configuration.toolOrder) }
    val density = LocalDensity.current
    LaunchedEffect(reorder.draggedTool, density) {
        val edge = with(density) { 48.dp.toPx() }
        val maxScroll = with(density) { 12.dp.toPx() }
        while (reorder.draggedTool != null) {
            withFrameNanos { }
            val delta = reorder.scrollDelta(edge, maxScroll)
            if (delta != 0f) {
                listState.scrollBy(delta)
                reorder.dragBy(0f)
            }
        }
    }
    val moveUpLabel = stringResource(R.string.toolbar_move_up)
    val moveDownLabel = stringResource(R.string.toolbar_move_down)

    SettingsDetailScaffold(
        titleResource = R.string.toolbar_settings_title,
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
                .testTag("toolbar_tools_list"),
        ) {
            item(key = "tool_count") {
                ToolbarCountSetting(configuration.toolCount, onToolCountChange)
            }
            item(key = "tools_heading") {
                Column {
                    GameSettingsSectionHeading(R.string.toolbar_all_tools)
                    Text(
                        text = stringResource(R.string.toolbar_order_summary, configuration.toolCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
                    )
                }
            }
            itemsIndexed(reorder.order, key = { _, tool -> tool.id }) { index, tool ->
                val label = stringResource(tool.titleResource())
                val isDragging = reorder.draggedTool == tool
                var showMoveMenu by remember { mutableStateOf(false) }
                val inToolbar = index < configuration.toolCount
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index, reorder.order.size),
                    colors = gameSettingsItemColors(),
                    leadingContent = {
                        ToolbarToolIcon(
                            tool,
                            modifier = Modifier.size(24.dp),
                            tint = if (inToolbar) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    content = { Text(label) },
                    supportingContent = {
                        Text(stringResource(
                            if (inToolbar) R.string.toolbar_tool_visible else R.string.toolbar_tool_hidden,
                            index + 1,
                        ))
                    },
                    trailingContent = {
                        Box {
                            IconButton(
                                onClick = { showMoveMenu = true },
                                modifier = Modifier.pointerInput(tool) {
                                    detectDragGestures(
                                        onDragStart = { reorder.startDrag(tool) },
                                        onDragEnd = reorder::finishDrag,
                                        onDragCancel = reorder::cancelDrag,
                                    ) { change, amount ->
                                        change.consume()
                                        reorder.dragBy(amount.y)
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = stringResource(R.string.toolbar_reorder_tool, label),
                                )
                            }
                            DropdownMenu(
                                expanded = showMoveMenu,
                                onDismissRequest = { showMoveMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(moveUpLabel) },
                                    enabled = index > 0,
                                    onClick = { reorder.move(tool, -1); showMoveMenu = false },
                                )
                                DropdownMenuItem(
                                    text = { Text(moveDownLabel) },
                                    enabled = index < reorder.order.lastIndex,
                                    onClick = { reorder.move(tool, 1); showMoveMenu = false },
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("toolbar_tool_" + tool.id)
                        .animateItem(placementSpec = if (isDragging) null else spring())
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = reorder.translationFor(tool)
                            shadowElevation = if (isDragging) 4.dp.toPx() else 0f
                        }
                        .semantics {
                            customActions = buildList {
                                if (index > 0) add(CustomAccessibilityAction(moveUpLabel) { reorder.move(tool, -1) })
                                if (index < reorder.order.lastIndex) {
                                    add(CustomAccessibilityAction(moveDownLabel) { reorder.move(tool, 1) })
                                }
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun ToolbarCountSetting(count: Int, onCountChange: (Int) -> Unit) {
    val label = stringResource(R.string.toolbar_tool_count)
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        colors = gameSettingsItemColors(),
        content = {
            Row(Modifier.fillMaxWidth()) {
                Text(label, modifier = Modifier.weight(1f))
                Text(count.toString(), color = MaterialTheme.colorScheme.primary)
            }
        },
        supportingContent = {
            Column {
                Slider(
                    value = count.toFloat(),
                    onValueChange = { onCountChange(it.roundToInt()) },
                    valueRange = ToolbarConfiguration.MIN_TOOL_COUNT.toFloat()..
                        ToolbarConfiguration.MAX_TOOL_COUNT.toFloat(),
                    steps = ToolbarConfiguration.MAX_TOOL_COUNT - ToolbarConfiguration.MIN_TOOL_COUNT - 1,
                    modifier = Modifier
                        .testTag("toolbar_tool_count")
                        .semantics { contentDescription = label },
                )
                Row(Modifier.fillMaxWidth()) {
                    (ToolbarConfiguration.MIN_TOOL_COUNT..ToolbarConfiguration.MAX_TOOL_COUNT).forEach {
                        Text(
                            it.toString(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    )
}
