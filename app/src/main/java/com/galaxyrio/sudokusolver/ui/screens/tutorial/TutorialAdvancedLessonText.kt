package com.galaxyrio.sudokusolver.ui.screens.tutorial

import androidx.annotation.StringRes
import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId

internal data class AdvancedLessonCopy(@param:StringRes val rule: Int, @param:StringRes val reason: Int, @param:StringRes val tip: Int)

/** Original wording of the rules in the reference lessons listed in NOTICE. */
internal fun advancedLessonCopy(technique: TechniqueId): AdvancedLessonCopy = when (technique) {
    TechniqueId.POINTING_PAIR, TechniqueId.POINTING_TRIPLE, TechniqueId.CLAIMING_PAIR, TechniqueId.CLAIMING_TRIPLE ->
        AdvancedLessonCopy(R.string.tutorial_rule_locked, R.string.tutorial_reason_locked, R.string.tutorial_tip_locked)
    TechniqueId.X_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_x_wing, R.string.tutorial_reason_x_wing, R.string.tutorial_tip_x_wing)
    TechniqueId.SWORDFISH ->
        AdvancedLessonCopy(R.string.tutorial_rule_swordfish, R.string.tutorial_reason_swordfish, R.string.tutorial_tip_swordfish)
    TechniqueId.JELLYFISH ->
        AdvancedLessonCopy(R.string.tutorial_rule_jellyfish, R.string.tutorial_reason_jellyfish, R.string.tutorial_tip_jellyfish)
    TechniqueId.FINNED_X_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_finned_x_wing, R.string.tutorial_reason_finned_x_wing, R.string.tutorial_tip_finned_x_wing)
    TechniqueId.FINNED_SWORDFISH ->
        AdvancedLessonCopy(R.string.tutorial_rule_finned_swordfish, R.string.tutorial_reason_finned_swordfish, R.string.tutorial_tip_finned_swordfish)
    TechniqueId.FINNED_JELLYFISH ->
        AdvancedLessonCopy(R.string.tutorial_rule_finned_jellyfish, R.string.tutorial_reason_finned_jellyfish, R.string.tutorial_tip_finned_jellyfish)
    TechniqueId.SKYSCRAPER ->
        AdvancedLessonCopy(R.string.tutorial_rule_skyscraper, R.string.tutorial_reason_skyscraper, R.string.tutorial_tip_skyscraper)
    TechniqueId.TWO_STRING_KITE ->
        AdvancedLessonCopy(R.string.tutorial_rule_two_string_kite, R.string.tutorial_reason_two_string_kite, R.string.tutorial_tip_two_string_kite)
    TechniqueId.TURBOT_CRANE ->
        AdvancedLessonCopy(R.string.tutorial_rule_crane, R.string.tutorial_reason_crane, R.string.tutorial_tip_crane)
    TechniqueId.EMPTY_RECTANGLE ->
        AdvancedLessonCopy(R.string.tutorial_rule_empty_rectangle, R.string.tutorial_reason_empty_rectangle, R.string.tutorial_tip_empty_rectangle)
    TechniqueId.XY_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_xy_wing, R.string.tutorial_reason_xy_wing, R.string.tutorial_tip_xy_wing)
    TechniqueId.XYZ_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_xyz_wing, R.string.tutorial_reason_xyz_wing, R.string.tutorial_tip_xyz_wing)
    TechniqueId.WXYZ_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_wxyz_wing, R.string.tutorial_reason_wxyz_wing, R.string.tutorial_tip_wxyz_wing)
    TechniqueId.FIVE_Y_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_five_y_wing, R.string.tutorial_reason_five_y_wing, R.string.tutorial_tip_five_y_wing)
    TechniqueId.SIX_Y_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_six_y_wing, R.string.tutorial_reason_six_y_wing, R.string.tutorial_tip_six_y_wing)
    TechniqueId.SEVEN_Y_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_seven_y_wing, R.string.tutorial_reason_seven_y_wing, R.string.tutorial_tip_seven_y_wing)
    TechniqueId.W_WING ->
        AdvancedLessonCopy(R.string.tutorial_rule_w_wing, R.string.tutorial_reason_w_wing, R.string.tutorial_tip_w_wing)
    TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2 ->
        AdvancedLessonCopy(R.string.tutorial_rule_simple_coloring, R.string.tutorial_reason_simple_coloring, R.string.tutorial_tip_simple_coloring)
    TechniqueId.X_CHAIN, TechniqueId.X_CHAIN_LOOP, TechniqueId.X_CHAIN_ONE_ENDPOINT ->
        AdvancedLessonCopy(R.string.tutorial_rule_x_chain, R.string.tutorial_reason_x_chain, R.string.tutorial_tip_x_chain)
    TechniqueId.GROUPED_X_CHAIN ->
        AdvancedLessonCopy(R.string.tutorial_rule_grouped_x_chain, R.string.tutorial_reason_grouped_x_chain, R.string.tutorial_tip_grouped_x_chain)
    TechniqueId.THREE_D_MEDUSA ->
        AdvancedLessonCopy(R.string.tutorial_rule_medusa, R.string.tutorial_reason_medusa, R.string.tutorial_tip_medusa)
    TechniqueId.XY_CHAIN, TechniqueId.XY_CHAIN_LOOP ->
        AdvancedLessonCopy(R.string.tutorial_rule_xy_chain, R.string.tutorial_reason_xy_chain, R.string.tutorial_tip_xy_chain)
    TechniqueId.AIC ->
        AdvancedLessonCopy(R.string.tutorial_rule_aic, R.string.tutorial_reason_aic, R.string.tutorial_tip_aic)
    TechniqueId.NISHIO_FORCING_CHAIN ->
        AdvancedLessonCopy(R.string.tutorial_rule_nishio, R.string.tutorial_reason_nishio, R.string.tutorial_tip_nishio)
    TechniqueId.CELL_FORCING_CHAIN, TechniqueId.REGION_FORCING_CHAIN ->
        AdvancedLessonCopy(R.string.tutorial_rule_forcing_chain, R.string.tutorial_reason_forcing_chain, R.string.tutorial_tip_forcing_chain)
    TechniqueId.CELL_FORCING_NET, TechniqueId.REGION_FORCING_NET ->
        AdvancedLessonCopy(R.string.tutorial_rule_forcing_net, R.string.tutorial_reason_forcing_net, R.string.tutorial_tip_forcing_net)
    TechniqueId.UNIQUE_RECTANGLE_TYPE_1 ->
        AdvancedLessonCopy(R.string.tutorial_rule_ur1, R.string.tutorial_reason_ur1, R.string.tutorial_tip_ur1)
    TechniqueId.UNIQUE_RECTANGLE_TYPE_2 ->
        AdvancedLessonCopy(R.string.tutorial_rule_ur2, R.string.tutorial_reason_ur2, R.string.tutorial_tip_ur2)
    TechniqueId.UNIQUE_RECTANGLE_TYPE_3 ->
        AdvancedLessonCopy(R.string.tutorial_rule_ur3, R.string.tutorial_reason_ur3, R.string.tutorial_tip_ur3)
    TechniqueId.UNIQUE_RECTANGLE_TYPE_4 ->
        AdvancedLessonCopy(R.string.tutorial_rule_ur4, R.string.tutorial_reason_ur4, R.string.tutorial_tip_ur4)
    TechniqueId.UNIQUE_RECTANGLE_TYPE_5 ->
        AdvancedLessonCopy(R.string.tutorial_rule_ur5, R.string.tutorial_reason_ur5, R.string.tutorial_tip_ur5)
    TechniqueId.BUG_PLUS_ONE ->
        AdvancedLessonCopy(R.string.tutorial_rule_bug, R.string.tutorial_reason_bug, R.string.tutorial_tip_bug)
    else -> error("No advanced tutorial text for $technique")
}
