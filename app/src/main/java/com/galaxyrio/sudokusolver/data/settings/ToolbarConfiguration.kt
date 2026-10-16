package com.galaxyrio.sudokusolver.data.settings

enum class ToolbarTool(val id: String) {
    UNDO("undo"),
    REDO("redo"),
    ERASE("erase"),
    NOTES("notes"),
    HINT("hint"),
    BIVALUE("bivalue"),
    DESELECT("deselect"),
}

data class ToolbarConfiguration(
    val toolCount: Int = DEFAULT_TOOL_COUNT,
    val toolOrder: List<ToolbarTool> = ToolbarTool.entries.toList(),
) {
    val visibleTools: List<ToolbarTool>
        get() = toolOrder.take(toolCount)

    companion object {
        const val MIN_TOOL_COUNT = 3
        const val MAX_TOOL_COUNT = 7
        const val DEFAULT_TOOL_COUNT = 5
    }
}

// Keep every tool exactly once, including tools added after a preference was saved.
internal fun normalizeToolbarToolOrder(order: List<ToolbarTool>): List<ToolbarTool> =
    (order + ToolbarTool.entries).distinct()

internal fun decodeToolbarToolOrder(value: String?): List<ToolbarTool> =
    normalizeToolbarToolOrder(
        value.orEmpty().split(',').mapNotNull { id ->
            ToolbarTool.entries.find { it.id == id }
        }
    )
