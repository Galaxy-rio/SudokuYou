package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialStage
import com.galaxyrio.sudokusolver.ui.components.BoardConfig
import com.galaxyrio.sudokusolver.ui.components.BoardGeometry
import com.galaxyrio.sudokusolver.ui.components.SudokuBoard
import com.galaxyrio.sudokusolver.ui.screens.play.SudokuThumbnail

@Composable
internal fun TutorialBoard(
    example: TutorialExample,
    stage: TutorialStage,
    selectedCell: Int?,
    onCellClick: (row: Int, col: Int) -> Unit,
    config: BoardConfig,
    modifier: Modifier = Modifier,
) {
    val board = example.boardAt(stage)
    val notes = remember(example, stage) { example.annotations(stage) }
    val primary = MaterialTheme.colorScheme.primary
    Box(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
        SudokuBoard(
            sudoku = board,
            onCellClick = onCellClick,
            readOnly = true,
            selectedRow = selectedCell?.div(9),
            selectedCol = selectedCell?.rem(9),
            highlightNumbers = setOfNotNull(selectedCell?.let { board.cells[it].value.takeIf { value -> value != 0 } }),
            advancedNotes = notes,
            config = config,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            val geometry = BoardGeometry(size.minDimension, 2.dp.toPx(), 2.dp.toPx(), 0.6.dp.toPx(), 1.dp.toPx())
            val bounds = geometry.houseBounds(example.house)
            drawRect(primary.copy(alpha = 0.65f), bounds.topLeft, bounds.size, style = Stroke(1.25.dp.toPx()))
            if (stage != TutorialStage.RULE) {
                example.patternCells.forEach { cell ->
                    val cellBounds = geometry.cellBounds(cell).deflate(1.dp.toPx())
                    drawRect(PatternColor, cellBounds.topLeft, cellBounds.size, style = Stroke(1.dp.toPx()))
                }
                example.patternCandidates.filter { it.digit in board.cells[it.cell.index].candidates }.forEach { candidate ->
                    val candidateBounds = geometry.candidateBounds(candidate).deflate(0.3.dp.toPx())
                    drawRect(PatternColor, candidateBounds.topLeft, candidateBounds.size, style = Stroke(1.dp.toPx()))
                }
            }
            if (stage == TutorialStage.DEDUCTION) {
                example.deduction.eliminations.forEach { elimination ->
                    val candidateBounds = geometry.candidateBounds(elimination.candidate).deflate(1.dp.toPx())
                    drawLine(ExclusionColor, candidateBounds.topLeft, candidateBounds.bottomRight, 1.dp.toPx())
                    drawLine(ExclusionColor, candidateBounds.topRight, candidateBounds.bottomLeft, 1.dp.toPx())
                }
            }
        }
    }
}

/** The first example rendered with the home page's grid and solid cell silhouettes. */
@Composable
internal fun TutorialThumbnail(example: TutorialExample, modifier: Modifier = Modifier) {
    val board = remember(example) { example.before.cells.map { it.value } }
    val highlights = remember(example) {
        val patterns = example.patternCells.mapTo(mutableSetOf()) { it.index }
        val exclusions = (example.excludedCells + example.blockingGivens)
            .mapTo(mutableSetOf()) { it.index }
        example.deduction.eliminations.forEach { exclusions += it.candidate.cell.index }
        (patterns + exclusions).associateWith { index ->
            buildList {
                if (index in patterns) add(PatternColor)
                if (index in exclusions) add(ExclusionColor)
            }
        }
    }
    SudokuThumbnail(board = board, modifier = modifier, cellHighlights = highlights)
}

private fun TutorialExample.annotations(stage: TutorialStage): AdvancedNotes {
    if (stage == TutorialStage.RULE) return AdvancedNotes()
    val showExclusions = stage == TutorialStage.PATTERN || stage == TutorialStage.DEDUCTION
    return AdvancedNotes(
        cellColors = buildMap {
            if (showExclusions) (excludedCells + blockingGivens).forEach { put(it.index, AdvancedNoteColor.RED) }
            patternCells.forEach { put(it.index, AdvancedNoteColor.GREEN) }
        },
        candidateColors = buildMap {
            patternCandidates.forEach { put(AdvancedNotes.candidateKey(it.cell.index, it.digit), AdvancedNoteColor.GREEN) }
            if (stage == TutorialStage.DEDUCTION) deduction.eliminations.forEach {
                val candidate = it.candidate
                put(AdvancedNotes.candidateKey(candidate.cell.index, candidate.digit), AdvancedNoteColor.RED)
            }
        },
    )
}

internal val PatternColor = Color(AdvancedNoteColor.GREEN.argb)
internal val ExclusionColor = Color(AdvancedNoteColor.RED.argb)
internal fun tutorialContainerKey(id: String) = "tutorial_container_$id"
internal fun tutorialBoardKey(id: String) = "tutorial_board_$id"
