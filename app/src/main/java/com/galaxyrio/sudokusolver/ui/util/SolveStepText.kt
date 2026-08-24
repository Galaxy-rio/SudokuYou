package com.galaxyrio.sudokusolver.ui.util

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId

@Composable
internal fun TechniqueId.localizedName(): String = stringResource(
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
        TechniqueId.REMOTE_PAIR -> R.string.technique_remote_pair
        TechniqueId.CHUTE_REMOTE_PAIR_SINGLE -> R.string.technique_chute_remote_pair_single
        TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE -> R.string.technique_chute_remote_pair_double
        TechniqueId.CHUTE_REMOTE_PAIR_BONUS -> R.string.technique_chute_remote_pair_bonus
        TechniqueId.SIMPLE_COLORING_TYPE_1 -> R.string.technique_simple_coloring_type_1
        TechniqueId.SIMPLE_COLORING_TYPE_2 -> R.string.technique_simple_coloring_type_2
        TechniqueId.EMPTY_RECTANGLE -> R.string.technique_empty_rectangle
        TechniqueId.SWORDFISH -> R.string.technique_swordfish
        TechniqueId.X_CHAIN -> R.string.technique_x_chain
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
)

@Composable
internal fun SolveStep.localizedExplanation(
    coordinateNotation: CoordinateNotation = CoordinateNotation.LOCALIZED,
): String {
    val resources = LocalResources.current
    val coordinateLabel: (CellRef) -> String = { cell ->
        resources.cellCoordinateLabel(cell, coordinateNotation)
    }
    val cells = evidence.causeCells
        .sortedBy(CellRef::index)
        .joinToString { coordinateLabel(it) }
    val digits = evidence.focusDigits
        .ifEmpty { evidence.causeCandidates.map { it.digit }.toSet() }
        .sorted()
        .joinToString(", ")
    val firstHouse = evidence.houses.getOrNull(0)?.let(resources::houseLabel).orEmpty()
    val secondHouse = evidence.houses.getOrNull(1)?.let(resources::houseLabel).orEmpty()
    val baseHouses = evidence.baseHouses.joinToString { resources.houseLabel(it) }
    val coverHouses = evidence.coverHouses.joinToString { resources.houseLabel(it) }
    val placement = placements.firstOrNull()

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
        TechniqueId.LOCKED_PAIR,
        TechniqueId.NAKED_TRIPLE,
        TechniqueId.LOCKED_TRIPLE,
        TechniqueId.NAKED_QUAD,
        -> resources.getString(
            R.string.game_hint_explanation_naked_subset,
            firstHouse,
            cells,
            digits,
        )
        TechniqueId.HIDDEN_PAIR,
        TechniqueId.HIDDEN_TRIPLE,
        TechniqueId.HIDDEN_QUAD,
        -> resources.getString(
            R.string.game_hint_explanation_hidden_subset,
            firstHouse,
            digits,
            cells,
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
        TechniqueId.SKYSCRAPER,
        TechniqueId.TWO_STRING_KITE,
        TechniqueId.TURBOT_CRANE,
        TechniqueId.X_CHAIN,
        TechniqueId.X_CHAIN_LOOP,
        TechniqueId.X_CHAIN_ONE_ENDPOINT,
        -> resources.getString(
            R.string.game_hint_explanation_single_digit_chain,
            technique.localizedName(),
            digits,
            cells,
        )
        TechniqueId.XY_WING,
        TechniqueId.XYZ_WING,
        TechniqueId.XY_CHAIN,
        TechniqueId.XY_CHAIN_LOOP,
        -> resources.getString(
            R.string.game_hint_explanation_multi_digit_chain,
            technique.localizedName(),
            cells,
            digits,
        )
        TechniqueId.WXYZ_WING -> resources.getString(
            R.string.game_hint_explanation_wxyz_wing,
            eliminations.firstOrNull()?.candidate?.digit ?: 0,
            cells,
            digits,
        )
        TechniqueId.AIC -> resources.getString(
            R.string.game_hint_explanation_aic,
            technique.localizedName(),
            cells,
            digits,
        )
        TechniqueId.NISHIO_FORCING_CHAIN,
        TechniqueId.NISHIO_FORCING_NET,
        -> resources.getString(
            R.string.game_hint_explanation_nishio_forcing,
            evidence.inferenceGraph.premise?.candidates?.firstOrNull()
                ?.let { resources.candidateLabel(it, coordinateNotation) }
                .orEmpty(),
            technique.localizedName(),
        )
        TechniqueId.DIGIT_FORCING_CHAIN,
        TechniqueId.DIGIT_FORCING_NET,
        TechniqueId.CELL_FORCING_CHAIN,
        TechniqueId.CELL_FORCING_NET,
        TechniqueId.REGION_FORCING_CHAIN,
        TechniqueId.REGION_FORCING_NET,
        -> resources.getString(
            R.string.game_hint_explanation_verity_forcing,
            technique.localizedName(),
            evidence.inferenceGraph.premise?.let { premise ->
                when (premise.type) {
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.NISHIO,
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.DIGIT,
                    -> premise.candidates.firstOrNull()
                        ?.let { resources.candidateLabel(it, coordinateNotation) }
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.CELL ->
                        premise.candidates.firstOrNull()?.cell?.let(coordinateLabel)
                    com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType.REGION ->
                        resources.getString(
                            R.string.game_hint_forcing_region_premise,
                            premise.candidates.firstOrNull()?.digit ?: 0,
                            premise.house?.let(resources::houseLabel).orEmpty(),
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
        TechniqueId.EMPTY_RECTANGLE -> resources.getString(
            R.string.game_hint_explanation_empty_rectangle,
            digits,
            cells,
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
            R.string.game_hint_explanation_unique_rectangle,
            technique.localizedName(),
            cells,
            digits,
        )
    }
}

@Composable
internal fun SolveStep.localizedAction(
    coordinateNotation: CoordinateNotation = CoordinateNotation.LOCALIZED,
): String {
    val resources = LocalResources.current
    return buildList {
        placements.forEach { placement ->
            add(
                resources.getString(
                    R.string.game_hint_action_place,
                    placement.digit,
                    resources.cellCoordinateLabel(placement.cell, coordinateNotation),
                )
            )
        }
        if (eliminations.isNotEmpty()) {
            val candidates = eliminations.joinToString { elimination ->
                val candidate = elimination.candidate
                resources.getString(
                    R.string.game_hint_candidate_at,
                    candidate.digit.toString(),
                    resources.cellCoordinateLabel(candidate.cell, coordinateNotation),
                )
            }
            add(resources.getString(R.string.game_hint_action_remove, candidates))
        }
    }.joinToString(separator = "\n")
}

private fun Resources.candidateLabel(
    candidate: com.galaxyrio.sudokusolver.domain.solver.CandidateRef,
    coordinateNotation: CoordinateNotation,
): String = getString(
    R.string.game_hint_candidate_at,
    candidate.digit.toString(),
    cellCoordinateLabel(candidate.cell, coordinateNotation),
)

private fun Resources.houseLabel(house: HouseRef): String = when (house.type) {
    HouseType.ROW -> getString(R.string.game_hint_row, house.index + 1)
    HouseType.COLUMN -> getString(R.string.game_hint_column, house.index + 1)
    HouseType.BOX -> getString(R.string.game_hint_box, house.index + 1)
}
