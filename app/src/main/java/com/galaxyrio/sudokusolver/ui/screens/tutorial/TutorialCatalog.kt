package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.annotation.StringRes
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.domain.solver.TechniqueLevel
import com.galaxyrio.sudokusolver.domain.tutorial.tutorialId
import com.galaxyrio.sudokusolver.ui.util.nameResourceId

internal data class TutorialCategory(
    val id: String,
    @param:StringRes val titleResource: Int,
    val techniques: List<TutorialTechnique>,
)

internal data class TutorialTechnique(
    val technique: TechniqueId,
    @param:StringRes val aliasesResource: Int? = null,
    @param:StringRes val relationshipResource: Int? = null,
) {
    val id: String get() = technique.tutorialId
    val titleResource: Int get() = technique.nameResourceId()
    val solverTechniques: List<TechniqueId> get() = listOf(technique)
    val level: TechniqueLevel get() = technique.level
}

/** Every hint has its own discoverable entry. Families are headings; aliases never duplicate lessons. */
internal val tutorialCategories: List<TutorialCategory> = listOf(
    category("singles", R.string.tutorial_category_singles,
        TechniqueId.LAST_DIGIT, TechniqueId.HIDDEN_SINGLE, TechniqueId.NAKED_SINGLE),
    category("hidden", R.string.tutorial_category_hidden,
        TechniqueId.HIDDEN_PAIR, TechniqueId.HIDDEN_TRIPLE, TechniqueId.HIDDEN_QUAD),
    category("naked", R.string.tutorial_category_naked,
        TechniqueId.NAKED_PAIR, TechniqueId.LOCKED_PAIR, TechniqueId.NAKED_TRIPLE,
        TechniqueId.LOCKED_TRIPLE, TechniqueId.NAKED_QUAD),
    category("locked_candidates", R.string.tutorial_category_locked_candidates,
        TechniqueId.POINTING_PAIR, TechniqueId.POINTING_TRIPLE,
        TechniqueId.CLAIMING_PAIR, TechniqueId.CLAIMING_TRIPLE),
    category("fish", R.string.tutorial_category_fish,
        TechniqueId.X_WING, TechniqueId.SWORDFISH, TechniqueId.JELLYFISH,
        TechniqueId.STARFISH, TechniqueId.WHALE, TechniqueId.LEVIATHAN,
        TechniqueId.FINNED_X_WING, TechniqueId.FINNED_SWORDFISH, TechniqueId.FINNED_JELLYFISH),
    category("single_digit", R.string.tutorial_category_single_digit,
        TechniqueId.SKYSCRAPER, TechniqueId.TWO_STRING_KITE, TechniqueId.TURBOT_CRANE, TechniqueId.EMPTY_RECTANGLE),
    category("wings", R.string.tutorial_category_wings,
        TechniqueId.XY_WING, TechniqueId.XYZ_WING, TechniqueId.WXYZ_WING,
        TechniqueId.FIVE_Y_WING, TechniqueId.SIX_Y_WING, TechniqueId.SEVEN_Y_WING,
        TechniqueId.EIGHT_Y_WING, TechniqueId.NINE_Y_WING, TechniqueId.W_WING),
    category("remote_pairs", R.string.tutorial_category_remote_pairs,
        TechniqueId.REMOTE_PAIR, TechniqueId.CHUTE_REMOTE_PAIR_SINGLE,
        TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE, TechniqueId.CHUTE_REMOTE_PAIR_BONUS),
    category("coloring", R.string.tutorial_category_coloring,
        TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2, TechniqueId.THREE_D_MEDUSA),
    category("chains", R.string.tutorial_category_chains,
        TechniqueId.X_CHAIN, TechniqueId.X_CHAIN_LOOP, TechniqueId.X_CHAIN_ONE_ENDPOINT,
        TechniqueId.GROUPED_X_CHAIN, TechniqueId.XY_CHAIN, TechniqueId.XY_CHAIN_LOOP, TechniqueId.AIC),
    category("forcing", R.string.tutorial_category_forcing,
        TechniqueId.NISHIO_FORCING_CHAIN, TechniqueId.NISHIO_FORCING_NET,
        TechniqueId.DIGIT_FORCING_CHAIN, TechniqueId.DIGIT_FORCING_NET,
        TechniqueId.CELL_FORCING_CHAIN, TechniqueId.CELL_FORCING_NET,
        TechniqueId.REGION_FORCING_CHAIN, TechniqueId.REGION_FORCING_NET),
    category("sue_de_coq", R.string.tutorial_category_sue_de_coq,
        TechniqueId.SUE_DE_COQ_TYPE_1, TechniqueId.SUE_DE_COQ_TYPE_2),
    category("uniqueness", R.string.tutorial_category_uniqueness,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1, TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_2, TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4, TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_5, TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_6, TechniqueId.UNIQUE_RECTANGLE_TYPE_7, TechniqueId.BUG_PLUS_ONE),
)

internal fun tutorialEntry(id: String): TutorialTechnique? =
    tutorialCategories.asSequence().flatMap { it.techniques }.firstOrNull { it.id == id }

private fun category(id: String, @StringRes title: Int, vararg techniques: TechniqueId) =
    TutorialCategory(id, title, techniques.map { technique ->
        TutorialTechnique(technique, aliases(technique), relationship(technique))
    })

private fun aliases(technique: TechniqueId): Int? = when (technique) {
    TechniqueId.LAST_DIGIT -> R.string.tutorial_alias_last_digit
    TechniqueId.NAKED_QUAD -> R.string.tutorial_alias_naked_quad
    TechniqueId.HIDDEN_QUAD -> R.string.tutorial_alias_hidden_quad
    TechniqueId.XY_WING -> R.string.tutorial_alias_xy_wing
    TechniqueId.WXYZ_WING -> R.string.tutorial_alias_wxyz_wing
    TechniqueId.TURBOT_CRANE -> R.string.tutorial_alias_crane
    TechniqueId.FINNED_X_WING -> R.string.tutorial_alias_finned_x_wing
    TechniqueId.AIC -> R.string.tutorial_alias_aic
    TechniqueId.REGION_FORCING_CHAIN -> R.string.tutorial_alias_region_chain
    TechniqueId.REGION_FORCING_NET -> R.string.tutorial_alias_region_net
    else -> null
}

private fun relationship(technique: TechniqueId): Int? = when (technique) {
    TechniqueId.LAST_DIGIT -> R.string.tutorial_relation_last_digit
    TechniqueId.HIDDEN_SINGLE -> R.string.tutorial_relation_hidden_single
    TechniqueId.LOCKED_PAIR -> R.string.tutorial_relation_locked_pair
    TechniqueId.LOCKED_TRIPLE -> R.string.tutorial_relation_locked_triple
    TechniqueId.POINTING_PAIR, TechniqueId.POINTING_TRIPLE -> R.string.tutorial_relation_pointing
    TechniqueId.CLAIMING_PAIR, TechniqueId.CLAIMING_TRIPLE -> R.string.tutorial_relation_claiming
    TechniqueId.SIMPLE_COLORING_TYPE_1 -> R.string.tutorial_relation_coloring_1
    TechniqueId.SIMPLE_COLORING_TYPE_2 -> R.string.tutorial_relation_coloring_2
    TechniqueId.THREE_D_MEDUSA -> R.string.tutorial_relation_medusa
    TechniqueId.XY_WING -> R.string.tutorial_relation_xy_wing
    TechniqueId.XYZ_WING -> R.string.tutorial_relation_xyz_wing
    TechniqueId.FIVE_Y_WING, TechniqueId.SIX_Y_WING, TechniqueId.SEVEN_Y_WING,
    TechniqueId.EIGHT_Y_WING, TechniqueId.NINE_Y_WING -> R.string.tutorial_relation_higher_wing
    TechniqueId.X_CHAIN_LOOP, TechniqueId.X_CHAIN_ONE_ENDPOINT -> R.string.tutorial_relation_x_chain
    TechniqueId.XY_CHAIN_LOOP -> R.string.tutorial_relation_xy_chain
    TechniqueId.GROUPED_X_CHAIN -> R.string.tutorial_relation_grouped_x_chain
    TechniqueId.BUG_PLUS_ONE -> R.string.tutorial_relation_bug
    else -> null
}
