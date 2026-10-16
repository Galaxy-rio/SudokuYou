package com.galaxyrio.sudokusolver.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.ToolbarTool

@StringRes
internal fun ToolbarTool.titleResource(): Int = when (this) {
    ToolbarTool.UNDO -> R.string.toolbar_tool_undo
    ToolbarTool.REDO -> R.string.game_redo
    ToolbarTool.ERASE -> R.string.game_erase
    ToolbarTool.NOTES -> R.string.toolbar_tool_notes
    ToolbarTool.HINT -> R.string.game_hint
    ToolbarTool.BIVALUE -> R.string.toolbar_tool_bivalue
    ToolbarTool.DESELECT -> R.string.toolbar_tool_deselect
}

@StringRes
internal fun ToolbarTool.actionDescriptionResource(): Int = when (this) {
    ToolbarTool.UNDO -> R.string.game_undo
    ToolbarTool.NOTES -> R.string.game_note_mode
    ToolbarTool.BIVALUE -> R.string.game_advanced_bivalue
    else -> titleResource()
}

@Composable
internal fun ToolbarToolIcon(
    tool: ToolbarTool,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    if (tool == ToolbarTool.BIVALUE) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("XY", style = MaterialTheme.typography.titleSmall, color = tint)
        }
    } else {
        val icon = when (tool) {
            ToolbarTool.UNDO -> ImageVector.vectorResource(R.drawable.ic_material_symbol_undo)
            ToolbarTool.REDO -> ImageVector.vectorResource(R.drawable.ic_material_symbol_redo)
            ToolbarTool.ERASE -> Icons.AutoMirrored.Filled.Backspace
            ToolbarTool.NOTES -> Icons.Default.Edit
            ToolbarTool.HINT -> Icons.Default.Lightbulb
            ToolbarTool.DESELECT -> Icons.Default.Deselect
            ToolbarTool.BIVALUE -> error("Bivalue uses the XY label")
        }
        Icon(icon, contentDescription = null, modifier = modifier, tint = tint)
    }
}
