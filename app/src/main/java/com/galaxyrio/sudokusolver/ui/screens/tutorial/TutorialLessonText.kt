package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.domain.solver.InferenceNode
import com.galaxyrio.sudokusolver.domain.solver.InferenceTruth
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialExample
import com.galaxyrio.sudokusolver.domain.tutorial.TutorialStage
import com.galaxyrio.sudokusolver.ui.util.cellCoordinateLabel
import com.galaxyrio.sudokusolver.ui.util.localizedAction
import com.galaxyrio.sudokusolver.ui.util.localizedExplanation

@Composable
internal fun TutorialStage.title(): String = stringResource(when (this) {
    TutorialStage.RULE -> R.string.tutorial_stage_rule
    TutorialStage.PATTERN -> R.string.tutorial_stage_pattern
    TutorialStage.DEDUCTION -> R.string.tutorial_stage_deduction
    TutorialStage.RESULT -> R.string.tutorial_stage_result
    TutorialStage.TAKEAWAY -> R.string.tutorial_stage_takeaway
})

@Composable
internal fun HouseRef.tutorialLabel(): String = stringResource(when (type) {
    HouseType.ROW -> R.string.game_hint_row
    HouseType.COLUMN -> R.string.game_hint_column
    HouseType.BOX -> R.string.game_hint_box
}, index + 1)

@Composable
internal fun TutorialExample.patternText(notation: CoordinateNotation): String {
    val resources = LocalResources.current
    val separator = stringResource(R.string.game_hint_list_separator)
    if (isAdvanced) {
        val groups = wingGroups
        val explanation = deduction.localizedExplanation(notation)
        return if (groups == null) explanation else {
            fun cells(cells: Set<com.galaxyrio.sudokusolver.domain.solver.CellRef>) = cells.sortedBy { it.index }
                .joinToString(separator) { resources.cellCoordinateLabel(it, notation) }
            stringResource(R.string.tutorial_wing_groups, cells(groups.pivot), cells(groups.firstWing), cells(groups.secondWing)) +
                "\n\n" + explanation
        }
    }
    return stringResource(
        when {
            isHidden && isSingle -> R.string.tutorial_find_hidden_single
            isSingle -> R.string.tutorial_find_naked_single
            isHidden -> R.string.tutorial_find_hidden_group
            else -> R.string.tutorial_find_naked_group
        },
        digits.sorted().joinToString(separator),
        house.tutorialLabel(),
        patternCells.sortedBy { it.index }.joinToString(separator) { resources.cellCoordinateLabel(it, notation) },
        digits.size,
    )
}

@Composable
internal fun TutorialExample.stepText(stage: TutorialStage, notation: CoordinateNotation): String {
    if (isAdvanced) {
        val copy = advancedLessonCopy(technique)
        return when (stage) {
            TutorialStage.RULE -> stringResource(copy.rule) + "\n\n" + stringResource(R.string.tutorial_candidate_context)
            TutorialStage.PATTERN -> patternText(notation)
            TutorialStage.DEDUCTION -> stringResource(copy.reason) + "\n\n" + patternText(notation)
            TutorialStage.RESULT -> deduction.localizedAction(notation) + "\n\n" + stringResource(R.string.tutorial_result_advanced)
            TutorialStage.TAKEAWAY -> stringResource(copy.tip)
        }
    }
    val copy = lessonCopy(technique)
    return when (stage) {
        TutorialStage.RULE -> stringResource(copy.first)
        TutorialStage.PATTERN -> patternText(notation)
        TutorialStage.DEDUCTION -> if (isSingle) {
            stringResource(if (isHidden) R.string.tutorial_reason_hidden_single else R.string.tutorial_reason_naked_single)
        } else {
            stringResource(
                if (isHidden) R.string.tutorial_reason_hidden_group else R.string.tutorial_reason_naked_group,
                digits.size,
                pluralStringResource(
                    if (isHidden) R.plurals.tutorial_remaining_cells else R.plurals.tutorial_remaining_digits,
                    digits.size - 1, digits.size - 1,
                ),
            )
        }
        TutorialStage.RESULT -> stringResource(
            when {
                isSingle -> R.string.tutorial_result_single
                isHidden -> R.string.tutorial_result_hidden_group
                else -> R.string.tutorial_result_naked_group
            }, digits.sorted().joinToString(stringResource(R.string.game_hint_list_separator)),
        )
        TutorialStage.TAKEAWAY -> stringResource(copy.second)
    }
}

/** A net joins facts within one assumption, never across different alternative branches. */
@Composable
internal fun TutorialExample.forcingProofText(notation: CoordinateNotation): String {
    val graph = deduction.evidence.inferenceGraph
    if (graph.premise == null) return ""
    val resources = LocalResources.current
    val and = stringResource(R.string.tutorial_fact_and)
    fun fact(node: InferenceNode): String {
        val candidate = resources.getString(R.string.game_hint_candidate_at,
            node.candidate.digit.toString(), resources.cellCoordinateLabel(node.candidate.cell, notation))
        return resources.getString(if (node.truth == InferenceTruth.TRUE) R.string.tutorial_truth_true else R.string.tutorial_truth_false, candidate)
    }
    val byId = graph.nodes.associateBy { it.id }
    return graph.nodes.groupBy { it.branchId }.toSortedMap().entries.mapIndexed { index, (_, nodes) ->
        buildList {
            add(resources.getString(R.string.tutorial_branch, index + 1))
            nodes.forEach { node ->
                if (node.isAssumption) {
                    add(resources.getString(R.string.tutorial_assume, fact(node)))
                } else {
                    val premises = graph.edges.filter { it.toNodeId == node.id }
                        .map { fact(byId.getValue(it.fromNodeId)) }.distinct()
                    if (premises.isNotEmpty()) add(resources.getString(R.string.tutorial_implies,
                        premises.joinToString(and), fact(node)))
                }
            }
        }.joinToString("\n")
    }.joinToString("\n\n")
}

// Original explanations of the rules described at sudoku.coach/en/learn/:
// hidden-single, hidden-pair, hidden-groups, naked-single, naked-pair, disjoint-groups.
// Example boards are generated locally; no article text or external images are bundled.
private fun lessonCopy(technique: TechniqueId): Pair<Int, Int> = when (technique) {
    TechniqueId.HIDDEN_SINGLE -> R.string.tutorial_rule_hidden_single to R.string.tutorial_tip_hidden_single
    TechniqueId.HIDDEN_PAIR -> R.string.tutorial_rule_hidden_pair to R.string.tutorial_tip_hidden_pair
    TechniqueId.HIDDEN_TRIPLE -> R.string.tutorial_rule_hidden_triple to R.string.tutorial_tip_hidden_triple
    TechniqueId.HIDDEN_QUAD -> R.string.tutorial_rule_hidden_quad to R.string.tutorial_tip_hidden_quad
    TechniqueId.NAKED_SINGLE -> R.string.tutorial_rule_naked_single to R.string.tutorial_tip_naked_single
    TechniqueId.NAKED_PAIR -> R.string.tutorial_rule_naked_pair to R.string.tutorial_tip_naked_pair
    TechniqueId.NAKED_TRIPLE -> R.string.tutorial_rule_naked_triple to R.string.tutorial_tip_naked_triple
    TechniqueId.NAKED_QUAD -> R.string.tutorial_rule_naked_quad to R.string.tutorial_tip_naked_quad
    else -> error("No lesson text for $technique")
}
