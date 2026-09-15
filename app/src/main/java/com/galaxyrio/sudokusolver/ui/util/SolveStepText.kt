package com.galaxyrio.sudokusolver.ui.util

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.InferenceContradictionType
import com.galaxyrio.sudokusolver.domain.solver.InferenceTruth
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId

@Composable
internal fun TechniqueId.localizedName(): String = stringResource(nameResourceId())

internal fun TechniqueId.nameResourceId(): Int =
    when (this) {
        TechniqueId.LAST_DIGIT -> R.string.technique_last_digit
        TechniqueId.NAKED_SINGLE -> R.string.technique_naked_single
        TechniqueId.HIDDEN_SINGLE -> R.string.technique_hidden_single
        TechniqueId.NAKED_PAIR -> R.string.technique_naked_pair
        TechniqueId.LOCKED_PAIR -> R.string.technique_locked_pair
        TechniqueId.HIDDEN_PAIR -> R.string.technique_hidden_pair
        TechniqueId.POINTING_PAIR -> R.string.technique_pointing_pair
        TechniqueId.POINTING_TRIPLE -> R.string.technique_pointing_triple
        TechniqueId.CLAIMING_PAIR -> R.string.technique_claiming_pair
        TechniqueId.CLAIMING_TRIPLE -> R.string.technique_claiming_triple
        TechniqueId.NAKED_TRIPLE -> R.string.technique_naked_triple
        TechniqueId.LOCKED_TRIPLE -> R.string.technique_locked_triple
        TechniqueId.HIDDEN_TRIPLE -> R.string.technique_hidden_triple
        TechniqueId.NAKED_QUAD -> R.string.technique_naked_quad
        TechniqueId.HIDDEN_QUAD -> R.string.technique_hidden_quad
        TechniqueId.X_WING -> R.string.technique_x_wing
        TechniqueId.SKYSCRAPER -> R.string.technique_skyscraper
        TechniqueId.TWO_STRING_KITE -> R.string.technique_two_string_kite
        TechniqueId.TURBOT_CRANE -> R.string.technique_turbot_crane
        TechniqueId.XY_WING -> R.string.technique_xy_wing
        TechniqueId.XYZ_WING -> R.string.technique_xyz_wing
        TechniqueId.W_WING -> R.string.technique_w_wing
        TechniqueId.FINNED_X_WING -> R.string.technique_finned_x_wing
        TechniqueId.FINNED_SWORDFISH -> R.string.technique_finned_swordfish
        TechniqueId.FINNED_JELLYFISH -> R.string.technique_finned_jellyfish
        TechniqueId.REMOTE_PAIR -> R.string.technique_remote_pair
        TechniqueId.CHUTE_REMOTE_PAIR_SINGLE -> R.string.technique_chute_remote_pair_single
        TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE -> R.string.technique_chute_remote_pair_double
        TechniqueId.CHUTE_REMOTE_PAIR_BONUS -> R.string.technique_chute_remote_pair_bonus
        TechniqueId.SIMPLE_COLORING_TYPE_1 -> R.string.technique_simple_coloring_type_1
        TechniqueId.SIMPLE_COLORING_TYPE_2 -> R.string.technique_simple_coloring_type_2
        TechniqueId.EMPTY_RECTANGLE -> R.string.technique_empty_rectangle
        TechniqueId.SWORDFISH -> R.string.technique_swordfish
        TechniqueId.X_CHAIN -> R.string.technique_x_chain
        TechniqueId.GROUPED_X_CHAIN -> R.string.technique_grouped_x_chain
        TechniqueId.X_CHAIN_LOOP -> R.string.technique_x_chain_loop
        TechniqueId.X_CHAIN_ONE_ENDPOINT -> R.string.technique_x_chain_one_endpoint
        TechniqueId.XY_CHAIN -> R.string.technique_xy_chain
        TechniqueId.XY_CHAIN_LOOP -> R.string.technique_xy_chain_loop
        TechniqueId.BUG_PLUS_ONE -> R.string.technique_bug_plus_one
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1 -> R.string.technique_unique_rectangle_type_1
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1M -> R.string.technique_unique_rectangle_type_1m
        TechniqueId.UNIQUE_RECTANGLE_TYPE_2 -> R.string.technique_unique_rectangle_type_2
        TechniqueId.UNIQUE_RECTANGLE_TYPE_3 -> R.string.technique_unique_rectangle_type_3
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4 -> R.string.technique_unique_rectangle_type_4
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4M -> R.string.technique_unique_rectangle_type_4m
        TechniqueId.UNIQUE_RECTANGLE_TYPE_5 -> R.string.technique_unique_rectangle_type_5
        TechniqueId.UNIQUE_RECTANGLE_TYPE_5P -> R.string.technique_unique_rectangle_type_5p
        TechniqueId.UNIQUE_RECTANGLE_TYPE_6 -> R.string.technique_unique_rectangle_type_6
        TechniqueId.UNIQUE_RECTANGLE_TYPE_7 -> R.string.technique_unique_rectangle_type_7
        TechniqueId.JELLYFISH -> R.string.technique_jellyfish
        TechniqueId.WXYZ_WING -> R.string.technique_wxyz_wing
        TechniqueId.FIVE_Y_WING -> R.string.technique_five_y_wing
        TechniqueId.SIX_Y_WING -> R.string.technique_six_y_wing
        TechniqueId.SEVEN_Y_WING -> R.string.technique_seven_y_wing
        TechniqueId.EIGHT_Y_WING -> R.string.technique_eight_y_wing
        TechniqueId.NINE_Y_WING -> R.string.technique_nine_y_wing
        TechniqueId.THREE_D_MEDUSA -> R.string.technique_three_d_medusa
        TechniqueId.SUE_DE_COQ_TYPE_1 -> R.string.technique_sue_de_coq_type_1
        TechniqueId.SUE_DE_COQ_TYPE_2 -> R.string.technique_sue_de_coq_type_2
        TechniqueId.STARFISH -> R.string.technique_starfish
        TechniqueId.WHALE -> R.string.technique_whale
        TechniqueId.LEVIATHAN -> R.string.technique_leviathan
        TechniqueId.AIC -> R.string.technique_aic
        TechniqueId.NISHIO_FORCING_CHAIN -> R.string.technique_nishio_forcing_chain
        TechniqueId.NISHIO_FORCING_NET -> R.string.technique_nishio_forcing_net
        TechniqueId.DIGIT_FORCING_CHAIN -> R.string.technique_digit_forcing_chain
        TechniqueId.DIGIT_FORCING_NET -> R.string.technique_digit_forcing_net
        TechniqueId.CELL_FORCING_CHAIN -> R.string.technique_cell_forcing_chain
        TechniqueId.CELL_FORCING_NET -> R.string.technique_cell_forcing_net
        TechniqueId.REGION_FORCING_CHAIN -> R.string.technique_region_forcing_chain
        TechniqueId.REGION_FORCING_NET -> R.string.technique_region_forcing_net
    }

/** Keeps hint wording testable with the same resources used by the Compose UI. */
internal fun interface HintStrings {
    fun getString(resourceId: Int, vararg arguments: Any): String
}

@Composable
internal fun SolveStep.localizedExplanation(
    coordinateNotation: CoordinateNotation = CoordinateNotation.LOCALIZED,
): String {
    val resources = LocalResources.current
    return formatExplanation(
        strings = HintStrings { id, arguments -> resources.getString(id, *arguments) },
        coordinateLabel = { resources.cellCoordinateLabel(it, coordinateNotation) },
        houseLabel = resources::houseLabel,
        techniqueName = technique.localizedName(),
    )
}

internal fun SolveStep.formatExplanation(
    strings: HintStrings,
    coordinateLabel: (CellRef) -> String,
    houseLabel: (HouseRef) -> String,
    techniqueName: String,
): String {
    val resources = strings
    val separator = resources.getString(R.string.game_hint_list_separator)
    fun candidateLabel(candidate: CandidateRef): String = resources.getString(
        R.string.game_hint_candidate_at,
        candidate.digit.toString(),
        coordinateLabel(candidate.cell),
    )
    fun groupLabel(candidates: Set<CandidateRef>): String = candidates
        .sortedWith(compareBy({ it.cell.index }, CandidateRef::digit))
        .joinToString(separator, transform = ::candidateLabel)
    val cells = evidence.causeCells
        .sortedBy(CellRef::index)
        .joinToString(separator) { coordinateLabel(it) }
    val digits = evidence.focusDigits
        .ifEmpty { evidence.causeCandidates.map { it.digit }.toSet() }
        .sorted()
        .joinToString(separator)
    val firstHouse = evidence.houses.getOrNull(0)?.let(houseLabel).orEmpty()
    val secondHouse = evidence.houses.getOrNull(1)?.let(houseLabel).orEmpty()
    val baseHouses = evidence.baseHouses.joinToString(separator, transform = houseLabel)
    val coverHouses = evidence.coverHouses.joinToString(separator, transform = houseLabel)
    val startCandidate = evidence.links.firstOrNull()?.from?.let(::candidateLabel).orEmpty()
    val endCandidate = evidence.links.lastOrNull()?.to?.let(::candidateLabel).orEmpty()
    val eliminationDigit = eliminations.firstOrNull()?.candidate?.digit ?: 0
    val placement = placements.firstOrNull()

    // A forcing proof can refute one assumption, including a FALSE assumption that
    // proves a placement. Only a verity proof actually has converging alternatives.
    if (technique in FORCING_TECHNIQUES) {
        val graph = evidence.inferenceGraph
        val contradiction = graph.contradiction
        val assumption = graph.nodes.firstOrNull { it.isAssumption }
        if (contradiction != null && assumption != null) {
            val contradictionText = when (contradiction.type) {
                InferenceContradictionType.OPPOSITE_TRUTHS -> resources.getString(
                    R.string.game_hint_contradiction_opposite_truths,
                    contradiction.candidate?.let(::candidateLabel).orEmpty(),
                )
                InferenceContradictionType.EMPTY_CELL -> resources.getString(
                    R.string.game_hint_contradiction_empty_cell,
                    contradiction.cell?.let(coordinateLabel).orEmpty(),
                )
                InferenceContradictionType.EMPTY_HOUSE -> resources.getString(
                    R.string.game_hint_contradiction_empty_house,
                    contradiction.house?.let(houseLabel).orEmpty(),
                )
            }
            return resources.getString(
                if (assumption.truth == InferenceTruth.TRUE) {
                    R.string.game_hint_explanation_forcing_true_contradiction
                } else {
                    R.string.game_hint_explanation_forcing_false_contradiction
                },
                candidateLabel(assumption.candidate),
                contradictionText,
            )
        }
    }

    return when (technique) {
        TechniqueId.LAST_DIGIT -> resources.getString(
            R.string.game_hint_explanation_last_digit,
            placement?.cell?.let(coordinateLabel).orEmpty(),
            firstHouse,
            placement?.digit ?: 0,
        )
        TechniqueId.NAKED_SINGLE -> resources.getString(
            R.string.game_hint_explanation_naked_single,
            placement?.cell?.let(coordinateLabel).orEmpty(),
            placement?.digit ?: 0,
        )
        TechniqueId.HIDDEN_SINGLE -> resources.getString(
            R.string.game_hint_explanation_hidden_single,
            (placement?.digit ?: 0).toString(),
            firstHouse,
            placement?.cell?.let(coordinateLabel).orEmpty(),
        )
        TechniqueId.NAKED_PAIR,
        TechniqueId.NAKED_TRIPLE,
        TechniqueId.NAKED_QUAD,
        -> resources.getString(
            R.string.game_hint_explanation_naked_subset,
            firstHouse,
            cells,
            digits,
            evidence.causeCells.size,
        )
        TechniqueId.LOCKED_PAIR,
        TechniqueId.LOCKED_TRIPLE,
        -> resources.getString(
            R.string.game_hint_explanation_locked_subset,
            firstHouse,
            cells,
            digits,
            evidence.causeCells.size,
            secondHouse,
        )
        TechniqueId.HIDDEN_PAIR,
        TechniqueId.HIDDEN_TRIPLE,
        TechniqueId.HIDDEN_QUAD,
        -> resources.getString(
            R.string.game_hint_explanation_hidden_subset,
            firstHouse,
            digits,
            cells,
            evidence.causeCells.size,
        )
        TechniqueId.POINTING_PAIR,
        TechniqueId.POINTING_TRIPLE,
        -> resources.getString(
            R.string.game_hint_explanation_pointing,
            digits,
            firstHouse,
            secondHouse,
        )
        TechniqueId.CLAIMING_PAIR,
        TechniqueId.CLAIMING_TRIPLE,
        -> resources.getString(
            R.string.game_hint_explanation_claiming,
            digits,
            firstHouse,
            secondHouse,
        )
        TechniqueId.X_WING,
        TechniqueId.SWORDFISH,
        TechniqueId.JELLYFISH,
        TechniqueId.STARFISH,
        TechniqueId.WHALE,
        TechniqueId.LEVIATHAN,
        -> resources.getString(
            R.string.game_hint_explanation_fish,
            digits,
            baseHouses,
            coverHouses,
        )
        TechniqueId.FINNED_X_WING,
        TechniqueId.FINNED_SWORDFISH,
        TechniqueId.FINNED_JELLYFISH,
        -> resources.getString(
            R.string.game_hint_explanation_finned_fish,
            digits,
            baseHouses,
            coverHouses,
            evidence.finCandidates.map(CandidateRef::cell).distinct()
                .sortedBy(CellRef::index).joinToString(separator, transform = coordinateLabel),
        )
        TechniqueId.SKYSCRAPER,
        TechniqueId.TWO_STRING_KITE,
        TechniqueId.TURBOT_CRANE,
        TechniqueId.X_CHAIN,
        -> resources.getString(
            R.string.game_hint_explanation_single_digit_chain,
            techniqueName,
            digits,
            startCandidate,
            endCandidate,
        )
        TechniqueId.GROUPED_X_CHAIN -> if (placement != null) {
            resources.getString(
                R.string.game_hint_explanation_x_chain_one_endpoint,
                coordinateLabel(placement.cell),
                placement.digit,
            )
        } else {
            resources.getString(
                R.string.game_hint_explanation_grouped_x_chain,
                digits,
                evidence.groupedLinks.firstOrNull()?.from?.let(::groupLabel).orEmpty(),
                evidence.groupedLinks.lastOrNull()?.to?.let(::groupLabel).orEmpty(),
            )
        }
        TechniqueId.X_CHAIN_LOOP -> resources.getString(
            R.string.game_hint_explanation_x_chain_loop,
            digits,
            cells,
        )
        TechniqueId.X_CHAIN_ONE_ENDPOINT -> resources.getString(
            R.string.game_hint_explanation_x_chain_one_endpoint,
            placement?.cell?.let(coordinateLabel).orEmpty(),
            placement?.digit ?: 0,
        )
        TechniqueId.W_WING -> resources.getString(
            R.string.game_hint_explanation_w_wing,
            evidence.wingCells.sortedBy(CellRef::index)
                .joinToString(separator, transform = coordinateLabel),
            digits,
            evidence.focusDigits.singleOrNull { it != eliminationDigit } ?: 0,
            firstHouse,
            eliminationDigit,
        )
        TechniqueId.XY_WING,
        TechniqueId.XY_CHAIN,
        TechniqueId.XY_CHAIN_LOOP,
        -> resources.getString(
            R.string.game_hint_explanation_multi_digit_chain,
            techniqueName,
            startCandidate,
            endCandidate,
            eliminationDigit,
        )
        TechniqueId.XYZ_WING -> resources.getString(
            R.string.game_hint_explanation_xyz_wing,
            cells,
            eliminationDigit,
        )
        TechniqueId.WXYZ_WING,
        TechniqueId.FIVE_Y_WING,
        TechniqueId.SIX_Y_WING,
        TechniqueId.SEVEN_Y_WING,
        TechniqueId.EIGHT_Y_WING,
        TechniqueId.NINE_Y_WING,
        -> resources.getString(
            R.string.game_hint_explanation_wxyz_wing,
            eliminationDigit,
            cells,
            digits,
            evidence.causeCells.size,
        )
        TechniqueId.AIC -> resources.getString(
            R.string.game_hint_explanation_aic,
            techniqueName,
            startCandidate,
            endCandidate,
        )
        TechniqueId.NISHIO_FORCING_CHAIN,
        TechniqueId.NISHIO_FORCING_NET,
        -> resources.getString(
            R.string.game_hint_explanation_nishio_forcing,
            evidence.inferenceGraph.premise?.candidates?.firstOrNull()
                ?.let(::candidateLabel)
                .orEmpty(),
            techniqueName,
        )
        TechniqueId.DIGIT_FORCING_CHAIN,
        TechniqueId.DIGIT_FORCING_NET,
        TechniqueId.CELL_FORCING_CHAIN,
        TechniqueId.CELL_FORCING_NET,
        TechniqueId.REGION_FORCING_CHAIN,
        TechniqueId.REGION_FORCING_NET,
        -> resources.getString(
            R.string.game_hint_explanation_verity_forcing,
            techniqueName,
            evidence.inferenceGraph.premise?.let { premise ->
                when (premise.type) {
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.NISHIO,
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.DIGIT,
                    -> premise.candidates.firstOrNull()
                        ?.let(::candidateLabel)
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.CELL ->
                        premise.candidates.firstOrNull()?.cell?.let(coordinateLabel)
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.REGION ->
                        resources.getString(
                            R.string.game_hint_forcing_region_premise,
                            premise.candidates.firstOrNull()?.digit ?: 0,
                            premise.house?.let(houseLabel).orEmpty(),
                        )
                }
            }.orEmpty(),
        )
        TechniqueId.REMOTE_PAIR -> resources.getString(
            R.string.game_hint_explanation_remote_pair,
            cells,
            digits,
        )
        TechniqueId.CHUTE_REMOTE_PAIR_SINGLE,
        TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE,
        TechniqueId.CHUTE_REMOTE_PAIR_BONUS,
        -> resources.getString(
            R.string.game_hint_explanation_chute_remote_pair,
            cells,
            digits,
        )
        TechniqueId.SIMPLE_COLORING_TYPE_1 -> resources.getString(
            R.string.game_hint_explanation_coloring_type_1,
            digits,
            cells,
        )
        TechniqueId.SIMPLE_COLORING_TYPE_2 -> resources.getString(
            R.string.game_hint_explanation_coloring_type_2,
            digits,
            cells,
        )
        TechniqueId.THREE_D_MEDUSA -> resources.getString(
            if (evidence.inferenceGraph.contradiction != null) {
                R.string.game_hint_explanation_medusa_contradiction
            } else {
                R.string.game_hint_explanation_medusa_trap
            },
            cells,
        )
        TechniqueId.SUE_DE_COQ_TYPE_1,
        TechniqueId.SUE_DE_COQ_TYPE_2,
        -> {
            val lineCells = evidence.houses.firstOrNull()?.cells().orEmpty()
            val lineWing = evidence.wingCells.firstOrNull { it in lineCells }
            val boxWing = evidence.wingCells.firstOrNull { it != lineWing }
            fun wingDigits(cell: CellRef?): Set<Int> = evidence.causeCandidates
                .filter { it.cell == cell }.map(CandidateRef::digit).toSet()
            val lineDigits = wingDigits(lineWing)
            val boxDigits = wingDigits(boxWing)
            resources.getString(
                if (technique == TechniqueId.SUE_DE_COQ_TYPE_1) {
                    R.string.game_hint_explanation_sue_de_coq_type_1
                } else {
                    R.string.game_hint_explanation_sue_de_coq_type_2
                },
                (evidence.causeCells - evidence.wingCells).sortedBy(CellRef::index)
                    .joinToString(separator, transform = coordinateLabel),
                digits,
                lineWing?.let(coordinateLabel).orEmpty(),
                lineDigits.sorted().joinToString(separator),
                boxWing?.let(coordinateLabel).orEmpty(),
                boxDigits.sorted().joinToString(separator),
                firstHouse,
                secondHouse,
                (evidence.focusDigits - lineDigits - boxDigits).sorted().joinToString(separator),
            )
        }
        TechniqueId.EMPTY_RECTANGLE -> resources.getString(
            R.string.game_hint_explanation_empty_rectangle,
            digits,
            evidence.houses.getOrNull(2)?.let(houseLabel).orEmpty(),
            evidence.houses.getOrNull(1)?.let(houseLabel).orEmpty(),
            evidence.houses.getOrNull(3)?.let(houseLabel).orEmpty(),
            firstHouse,
            evidence.links.firstOrNull()?.from?.cell?.let(coordinateLabel).orEmpty(),
            evidence.links.firstOrNull()?.to?.cell?.let(coordinateLabel).orEmpty(),
            eliminations.firstOrNull()?.candidate?.cell?.let(coordinateLabel).orEmpty(),
        )
        TechniqueId.BUG_PLUS_ONE -> resources.getString(
            R.string.game_hint_explanation_bug_plus_one,
            placement?.cell?.let(coordinateLabel).orEmpty(),
            placement?.digit ?: 0,
        )
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_2,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_5,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_6,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_7,
        -> resources.getString(
            when (technique) {
                TechniqueId.UNIQUE_RECTANGLE_TYPE_1,
                TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
                -> R.string.game_hint_explanation_unique_rectangle_corner
                TechniqueId.UNIQUE_RECTANGLE_TYPE_2,
                TechniqueId.UNIQUE_RECTANGLE_TYPE_5,
                TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
                -> R.string.game_hint_explanation_unique_rectangle_extra
                TechniqueId.UNIQUE_RECTANGLE_TYPE_3 ->
                    R.string.game_hint_explanation_unique_rectangle_subset
                TechniqueId.UNIQUE_RECTANGLE_TYPE_4,
                TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
                -> R.string.game_hint_explanation_unique_rectangle_conjugate
                else -> R.string.game_hint_explanation_unique_rectangle
            },
            techniqueName,
            cells,
            digits,
            eliminationDigit,
        )
    }
}

@Composable
internal fun SolveStep.localizedAction(
    coordinateNotation: CoordinateNotation = CoordinateNotation.LOCALIZED,
): String {
    val resources = LocalResources.current
    return formatAction(
        strings = HintStrings { id, arguments -> resources.getString(id, *arguments) },
        coordinateLabel = { resources.cellCoordinateLabel(it, coordinateNotation) },
    )
}

internal fun SolveStep.formatAction(
    strings: HintStrings,
    coordinateLabel: (CellRef) -> String,
): String {
    val separator = strings.getString(R.string.game_hint_list_separator)
    return buildList {
        placements.forEach { placement ->
            add(
                strings.getString(
                    R.string.game_hint_action_place,
                    placement.digit,
                    coordinateLabel(placement.cell),
                )
            )
        }
        eliminations.groupBy { it.candidate.cell }.toList().sortedBy { it.first.index }
            .forEach { (cell, removals) ->
                add(strings.getString(
                    R.string.game_hint_action_remove,
                    removals.map { it.candidate.digit }.sorted().joinToString(separator),
                    coordinateLabel(cell),
                ))
            }
    }.joinToString(separator = "\n")
}

private fun Resources.houseLabel(house: HouseRef): String = when (house.type) {
    HouseType.ROW -> getString(R.string.game_hint_row, house.index + 1)
    HouseType.COLUMN -> getString(R.string.game_hint_column, house.index + 1)
    HouseType.BOX -> getString(R.string.game_hint_box, house.index + 1)
}

private val FORCING_TECHNIQUES = setOf(
    TechniqueId.NISHIO_FORCING_CHAIN,
    TechniqueId.NISHIO_FORCING_NET,
    TechniqueId.DIGIT_FORCING_CHAIN,
    TechniqueId.DIGIT_FORCING_NET,
    TechniqueId.CELL_FORCING_CHAIN,
    TechniqueId.CELL_FORCING_NET,
    TechniqueId.REGION_FORCING_CHAIN,
    TechniqueId.REGION_FORCING_NET,
)
