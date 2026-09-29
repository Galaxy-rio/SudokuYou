package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.annotation.StringRes
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.domain.solver.TechniqueLevel
import com.galaxyrio.sudokusolver.ui.util.nameResourceId

internal data class TutorialCategory(
    val id: String,
    @param:StringRes val titleResource: Int,
    val techniques: List<TutorialTechnique>,
)

internal data class TutorialTechnique(
    val id: String,
    @param:StringRes val titleResource: Int,
    val solverTechniques: List<TechniqueId>,
) {
    val level: TechniqueLevel?
        get() = solverTechniques.maxOfOrNull { it.level }
}

/**
 * Category and entry order follow https://sudoku.coach/en/learn/technique-overview.
 * Names and difficulty levels come from the app's solver; available lessons live in TutorialLessons.
 * An empty solverTechniques list reserves an entry for a technique not yet implemented.
 */
internal val tutorialCategories: List<TutorialCategory> = listOf(
    TutorialCategory(
        id = "hidden",
        titleResource = R.string.tutorial_category_hidden,
        techniques = listOf(
            technique(TechniqueId.HIDDEN_SINGLE),
            technique(TechniqueId.HIDDEN_PAIR),
            technique(TechniqueId.HIDDEN_TRIPLE),
            technique(TechniqueId.HIDDEN_QUAD),
        ),
    ),
    TutorialCategory(
        id = "naked",
        titleResource = R.string.tutorial_category_naked,
        techniques = listOf(
            technique(TechniqueId.NAKED_SINGLE),
            technique(TechniqueId.NAKED_PAIR, TechniqueId.LOCKED_PAIR),
            technique(TechniqueId.NAKED_TRIPLE, TechniqueId.LOCKED_TRIPLE),
            technique(TechniqueId.NAKED_QUAD),
        ),
    ),
    TutorialCategory(
        id = "locked_candidates",
        titleResource = R.string.tutorial_category_locked_candidates,
        techniques = listOf(
            TutorialTechnique(
                id = "locked_candidate",
                titleResource = R.string.tutorial_technique_locked_candidate,
                solverTechniques = listOf(
                    TechniqueId.POINTING_PAIR,
                    TechniqueId.POINTING_TRIPLE,
                    TechniqueId.CLAIMING_PAIR,
                    TechniqueId.CLAIMING_TRIPLE,
                ),
            ),
        ),
    ),
    TutorialCategory(
        id = "fish",
        titleResource = R.string.tutorial_category_fish,
        techniques = listOf(
            technique(TechniqueId.X_WING),
            technique(TechniqueId.SWORDFISH),
            technique(TechniqueId.JELLYFISH),
            technique(TechniqueId.FINNED_X_WING),
            technique(TechniqueId.FINNED_SWORDFISH),
            technique(TechniqueId.FINNED_JELLYFISH),
        ),
    ),
    TutorialCategory(
        id = "single_digit",
        titleResource = R.string.tutorial_category_single_digit,
        techniques = listOf(
            technique(TechniqueId.SKYSCRAPER),
            technique(TechniqueId.TWO_STRING_KITE),
            technique(TechniqueId.TURBOT_CRANE),
            technique(TechniqueId.EMPTY_RECTANGLE),
        ),
    ),
    TutorialCategory(
        id = "y_wings",
        titleResource = R.string.tutorial_category_y_wings,
        techniques = listOf(
            technique(TechniqueId.XY_WING),
            technique(TechniqueId.XYZ_WING),
            technique(TechniqueId.WXYZ_WING),
            technique(TechniqueId.FIVE_Y_WING),
            technique(TechniqueId.SIX_Y_WING),
            technique(TechniqueId.SEVEN_Y_WING),
        ),
    ),
    TutorialCategory(
        id = "w_wing",
        titleResource = R.string.tutorial_category_w_wing,
        techniques = listOf(technique(TechniqueId.W_WING)),
    ),
    TutorialCategory(
        id = "chains",
        titleResource = R.string.tutorial_category_chains,
        techniques = listOf(
            TutorialTechnique(
                id = "simple_coloring",
                titleResource = R.string.tutorial_technique_simple_coloring,
                solverTechniques = listOf(
                    TechniqueId.SIMPLE_COLORING_TYPE_1,
                    TechniqueId.SIMPLE_COLORING_TYPE_2,
                ),
            ),
            technique(
                TechniqueId.X_CHAIN,
                TechniqueId.X_CHAIN_LOOP,
                TechniqueId.X_CHAIN_ONE_ENDPOINT,
            ),
            technique(TechniqueId.GROUPED_X_CHAIN),
            technique(TechniqueId.THREE_D_MEDUSA),
            technique(TechniqueId.XY_CHAIN, TechniqueId.XY_CHAIN_LOOP),
            technique(TechniqueId.AIC),
            technique(TechniqueId.NISHIO_FORCING_CHAIN),
            TutorialTechnique(
                id = "cell_region_forcing_chain",
                titleResource = R.string.tutorial_technique_cell_region_forcing_chain,
                solverTechniques = listOf(
                    TechniqueId.CELL_FORCING_CHAIN,
                    TechniqueId.REGION_FORCING_CHAIN,
                ),
            ),
            TutorialTechnique(
                id = "cell_region_forcing_net",
                titleResource = R.string.tutorial_technique_cell_region_forcing_net,
                solverTechniques = listOf(
                    TechniqueId.CELL_FORCING_NET,
                    TechniqueId.REGION_FORCING_NET,
                ),
            ),
        ),
    ),
    TutorialCategory(
        id = "uniqueness",
        titleResource = R.string.tutorial_category_uniqueness,
        techniques = listOf(
            technique(TechniqueId.UNIQUE_RECTANGLE_TYPE_1),
            technique(TechniqueId.UNIQUE_RECTANGLE_TYPE_2),
            technique(TechniqueId.UNIQUE_RECTANGLE_TYPE_3),
            technique(TechniqueId.UNIQUE_RECTANGLE_TYPE_4),
            technique(TechniqueId.UNIQUE_RECTANGLE_TYPE_5),
            technique(TechniqueId.BUG_PLUS_ONE),
        ),
    ),
)

private fun technique(
    technique: TechniqueId,
    vararg variants: TechniqueId,
): TutorialTechnique = TutorialTechnique(
    id = technique.name,
    titleResource = technique.nameResourceId(),
    solverTechniques = listOf(technique) + variants,
)
