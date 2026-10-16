package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.galaxyrio.sudokusolver.data.settings.ToolbarTool

internal class ToolbarReorderState(
    private val listState: LazyListState,
    private val firstToolIndex: Int,
    initialOrder: List<ToolbarTool>,
    private val onOrderChanged: (List<ToolbarTool>) -> Unit,
) {
    var order by mutableStateOf(initialOrder)
        private set
    var draggedTool by mutableStateOf<ToolbarTool?>(null)
        private set
    private var draggedTop by mutableFloatStateOf(0f)
    private var draggedHeight = 0
    private var orderBeforeDrag = initialOrder

    fun syncOrder(value: List<ToolbarTool>) {
        if (draggedTool == null) order = value
    }

    fun startDrag(tool: ToolbarTool) {
        val item = listState.layoutInfo.visibleItemsInfo.find { it.key == tool.id } ?: return
        orderBeforeDrag = order
        draggedTool = tool
        draggedTop = item.offset.toFloat()
        draggedHeight = item.size
    }

    fun dragBy(delta: Float) {
        val tool = draggedTool ?: return
        draggedTop += delta
        val items = listState.layoutInfo.visibleItemsInfo
        val draggedItem = items.find { it.key == tool.id } ?: return
        val from = order.indexOf(tool)
        // Wait for the previous move to be laid out before considering another move.
        if (draggedItem.index != firstToolIndex + from) return
        val center = draggedTop + draggedHeight / 2f
        val target = items.find {
            it.key != tool.id && order.any { tool -> tool.id == it.key } &&
                center >= it.offset && center < it.offset + it.size
        } ?: return
        val to = order.indexOfFirst { it.id == target.key }
        order = order.toMutableList().apply { add(to, removeAt(from)) }
    }

    fun translationFor(tool: ToolbarTool): Float {
        if (draggedTool != tool) return 0f
        val item = listState.layoutInfo.visibleItemsInfo.find { it.key == tool.id } ?: return 0f
        return draggedTop - item.offset
    }

    fun scrollDelta(edge: Float, maxPerFrame: Float): Float {
        if (draggedTool == null) return 0f
        val layout = listState.layoutInfo
        val above = draggedTop - (layout.viewportStartOffset + edge)
        val below = draggedTop + draggedHeight - (layout.viewportEndOffset - edge)
        return when {
            above < 0 -> (above * 0.2f).coerceAtLeast(-maxPerFrame)
            below > 0 -> (below * 0.2f).coerceAtMost(maxPerFrame)
            else -> 0f
        }
    }

    fun finishDrag() {
        draggedTool = null
        if (order != orderBeforeDrag) onOrderChanged(order)
    }

    fun cancelDrag() {
        order = orderBeforeDrag
        draggedTool = null
    }

    fun move(tool: ToolbarTool, delta: Int): Boolean {
        val from = order.indexOf(tool)
        val to = from + delta
        if (from < 0 || to !in order.indices) return false
        order = order.toMutableList().apply { add(to, removeAt(from)) }
        onOrderChanged(order)
        return true
    }
}
