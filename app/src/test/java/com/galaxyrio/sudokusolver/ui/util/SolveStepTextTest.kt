package com.galaxyrio.sudokusolver.ui.util

import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.CandidateElimination
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.EmptyRectangleDetector
import com.galaxyrio.sudokusolver.domain.solver.ForcingChainDetector
import com.galaxyrio.sudokusolver.domain.solver.HouseRef
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.InferenceContradiction
import com.galaxyrio.sudokusolver.domain.solver.InferenceContradictionType
import com.galaxyrio.sudokusolver.domain.solver.InferenceGraph
import com.galaxyrio.sudokusolver.domain.solver.InferenceLink
import com.galaxyrio.sudokusolver.domain.solver.InferenceLinkType
import com.galaxyrio.sudokusolver.domain.solver.InferencePremiseType
import com.galaxyrio.sudokusolver.domain.solver.InferenceTruth
import com.galaxyrio.sudokusolver.domain.solver.Placement
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.domain.solver.StepEvidence
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolveStepTextTest {
    private val english = XmlHintStrings("values", Locale.ENGLISH)
    private val chinese = XmlHintStrings("values-zh-rCN", Locale.SIMPLIFIED_CHINESE)

    @Test
    fun everyTechniqueHasNamesAndFormattableExplanationsInBothLanguages() {
        for (strings in listOf(english, chinese)) {
            for (technique in TechniqueId.entries) {
                assertTrue(strings.getString(technique.nameResourceId()).isNotBlank())
                val step = sampleStep(technique)
                val explanation = step.explanation(strings)
                assertTrue("Missing explanation for $technique", explanation.isNotBlank())
                assertFalse("Unexpanded placeholder for $technique", PLACEHOLDER.containsMatchIn(explanation))
            }
        }
    }

    @Test
    fun allHintResourcesHaveMatchingTranslationAndPlaceholderTypes() {
        val englishHints = english.values.filterKeys(::isHintResource)
        val chineseHints = chinese.values.filterKeys(::isHintResource)
        assertEquals(englishHints.keys, chineseHints.keys)
        for ((key, value) in englishHints) {
            val expected = PLACEHOLDER.findAll(value).map { it.value }.toSet()
            val translated = PLACEHOLDER.findAll(chineseHints.getValue(key)).map { it.value }.toSet()
            assertEquals("Placeholder mismatch: $key", expected, translated)
        }
    }

    @Test
    fun actionsGroupRemovalsByCellAndUseChinesePunctuation() {
        val step = SolveStep(
            technique = TechniqueId.NAKED_PAIR,
            eliminations = listOf(
                elimination(0, 4, 7),
                elimination(0, 2, 7),
                elimination(0, 2, 2),
            ),
        )
        assertEquals(
            "Remove 2, 7 from R1C3.\nRemove 7 from R1C5.",
            step.formatAction(english, ::coordinate),
        )
        assertEquals(
            "从R1C3删除候选数 2、7。\n从R1C5删除候选数 7。",
            step.formatAction(chinese, ::coordinate),
        )
    }

    @Test
    fun openChainNamesTheActualEndpointsInsteadOfSortedCauseCells() {
        val start = CandidateRef(CellRef(7, 8), 5)
        val middle = CandidateRef(CellRef(0, 8), 5)
        val otherMiddle = CandidateRef(CellRef(0, 2), 5)
        val end = CandidateRef(CellRef(5, 2), 5)
        val step = SolveStep(
            technique = TechniqueId.X_CHAIN,
            eliminations = listOf(elimination(7, 2, 5)),
            evidence = StepEvidence(
                causeCells = setOf(end.cell, middle.cell, start.cell, otherMiddle.cell),
                focusDigits = setOf(5),
                links = listOf(
                    InferenceLink(start, middle, InferenceLinkType.STRONG),
                    InferenceLink(middle, otherMiddle, InferenceLinkType.WEAK),
                    InferenceLink(otherMiddle, end, InferenceLinkType.STRONG),
                ),
            ),
        )
        assertTrue(step.explanation(english).contains("If 5 in R8C9 is false, 5 in R6C3 must be true"))
        assertTrue(step.explanation(chinese).contains("如果R8C9中的 5不成立，R6C3中的 5就必须成立"))
    }

    @Test
    fun placementLoopDoesNotDescribeAnElimination() {
        val step = sampleStep(TechniqueId.X_CHAIN_ONE_ENDPOINT)
        assertEquals(
            "If R1C1 were not 2, following the chain would force it to be 2. That contradiction means R1C1 must be 2.",
            step.explanation(english),
        )
        assertFalse(step.explanation(chinese).contains("删除"))
    }

    @Test
    fun groupedPlacementLoopExplainsItsContradictionInsteadOfEndGroupElimination() {
        val step = sampleStep(TechniqueId.GROUPED_X_CHAIN)
        assertEquals(
            sampleStep(TechniqueId.X_CHAIN_ONE_ENDPOINT).explanation(english),
            step.explanation(english),
        )
        assertFalse(step.explanation(chinese).contains("删除"))
    }

    @Test
    fun digitForcingPlacementRefutesTheActualFalseAssumption() {
        val step = requireNotNull(ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.DIGIT),
        ).find(forcingContradictionState()))
        val assumption = step.evidence.inferenceGraph.nodes.single { it.isAssumption }
        assertEquals(InferenceTruth.FALSE, assumption.truth)
        assertTrue(step.placements.isNotEmpty())
        assertTrue(step.explanation(english).startsWith("Assume 2 in R1C1 is false."))
        assertTrue(step.explanation(english).contains("fill in this digit"))
        assertFalse(step.explanation(english).contains("All branches"))
        assertTrue(step.explanation(chinese).contains("应当填入这个数字"))
        assertFalse(step.explanation(chinese).contains("每个分支"))
    }

    @Test
    fun nishioEliminationRefutesTheActualTrueAssumption() {
        val step = requireNotNull(ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.NISHIO),
        ).find(forcingContradictionState()))
        assertEquals(InferenceTruth.TRUE, step.evidence.inferenceGraph.nodes.single { it.isAssumption }.truth)
        assertTrue(step.explanation(english).startsWith("Assume 1 in R1C1 is true."))
        assertTrue(step.explanation(english).contains("can be removed"))
        assertTrue(step.explanation(chinese).contains("该候选数不成立，可以删除"))
    }

    @Test
    fun forcingContradictionsIdentifyTheActualConflictCellOrRegion() {
        val step = requireNotNull(ForcingChainDetector(
            enabledPremises = setOf(InferencePremiseType.DIGIT),
        ).find(forcingContradictionState()))
        val cases = listOf(
            Triple(
                InferenceContradiction(InferenceContradictionType.EMPTY_CELL, cell = CellRef(2, 3)),
                "R3C4 would have no candidate left",
                "R3C4没有可填的候选数",
            ),
            Triple(
                InferenceContradiction(InferenceContradictionType.EMPTY_HOUSE, house = HouseRef(HouseType.COLUMN, 4)),
                "column 5 would have no position left",
                "第 5 列中的某个必需数字没有可填的位置",
            ),
            Triple(
                InferenceContradiction(InferenceContradictionType.OPPOSITE_TRUTHS, candidate = CandidateRef(CellRef(2, 3), 8)),
                "8 in R3C4 both true and false",
                "R3C4中的 8既成立又不成立",
            ),
        )
        for ((contradiction, expectedEnglish, expectedChinese) in cases) {
            val variant = step.copy(evidence = step.evidence.copy(
                inferenceGraph = step.evidence.inferenceGraph.copy(contradiction = contradiction),
            ))
            assertTrue(variant.explanation(english).contains(expectedEnglish))
            assertTrue(variant.explanation(chinese).contains(expectedChinese))
        }
    }

    @Test
    fun emptyRectangleExplainsBothActualPositionsAndTheTarget() {
        val masks = IntArray(81)
        listOf(CellRef(0, 0), CellRef(0, 4), CellRef(3, 0), CellRef(4, 1), CellRef(4, 4))
            .forEach { masks[it.index] = 1 shl 4 }
        masks[CellRef(4, 4).index] = masks[CellRef(4, 4).index] or (1 shl 1)
        val step = requireNotNull(EmptyRectangleDetector().find(SolverState.fromValuesAndCandidates(
            values = IntArray(81), candidateMasks = masks,
        )))
        assertTrue(step.explanation(english).contains("its only positions are R1C1 and R1C5"))
        assertTrue(step.explanation(english).contains("Otherwise R1C1 must be 5"))
        assertTrue(step.explanation(chinese).contains("同样排除R5C5中的 5"))
    }

    @Test
    fun medusaDistinguishesColorContradictionFromSeeingBothColors() {
        val trap = sampleStep(TechniqueId.THREE_D_MEDUSA)
        val contradiction = trap.copy(evidence = trap.evidence.copy(
            inferenceGraph = InferenceGraph(
                nodes = emptyList(),
                edges = emptyList(),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_CELL,
                    cell = CellRef(2, 2),
                ),
            ),
        ))
        for (strings in listOf(english, chinese)) {
            assertNotEquals(trap.explanation(strings), contradiction.explanation(strings))
        }
        assertTrue(trap.explanation(english).contains("conflicts with both colors"))
        assertTrue(contradiction.explanation(english).contains("That color is false"))
    }

    @Test
    fun lockedSubsetExplainsEliminationsInBothSharedRegions() {
        val step = sampleStep(TechniqueId.LOCKED_PAIR)
        assertTrue(step.explanation(english).contains("both regions"))
        assertTrue(step.explanation(chinese).contains("两个区域"))
    }

    @Test
    fun uniquenessHintsStateTheirRequiredAssumptionInBothLanguages() {
        for (technique in listOf(TechniqueId.BUG_PLUS_ONE, TechniqueId.UNIQUE_RECTANGLE_TYPE_1)) {
            val step = sampleStep(technique)
            assertTrue(step.explanation(english).contains("one solution"))
            assertTrue(step.explanation(chinese).contains("唯一解"))
        }
    }

    private fun sampleStep(technique: TechniqueId) = SolveStep(
        technique = technique,
        placements = listOf(Placement(CellRef(0, 0), 2)),
        eliminations = listOf(elimination(0, 2, 7)),
        evidence = StepEvidence(
            causeCells = setOf(CellRef(0, 0), CellRef(0, 1)),
            causeCandidates = setOf(CandidateRef(CellRef(0, 0), 2), CandidateRef(CellRef(0, 1), 7)),
            focusDigits = setOf(2, 7),
            houses = listOf(HouseRef(HouseType.ROW, 0), HouseRef(HouseType.BOX, 0)),
        ),
    )

    private fun SolveStep.explanation(strings: XmlHintStrings): String = formatExplanation(
        strings = strings,
        coordinateLabel = ::coordinate,
        houseLabel = { house ->
            strings.getString(when (house.type) {
                HouseType.ROW -> R.string.game_hint_row
                HouseType.COLUMN -> R.string.game_hint_column
                HouseType.BOX -> R.string.game_hint_box
            }, house.index + 1)
        },
        techniqueName = strings.getString(technique.nameResourceId()),
    )

    private fun elimination(row: Int, col: Int, digit: Int) =
        CandidateElimination(CandidateRef(CellRef(row, col), digit))

    private fun forcingContradictionState(): SolverState {
        val masks = IntArray(81) { SolverState.FULL_CANDIDATE_MASK }
        for ((cell, digits) in listOf(
            CellRef(0, 0) to listOf(1, 2),
            CellRef(0, 4) to listOf(1, 2),
            CellRef(4, 4) to listOf(2, 3),
            CellRef(4, 0) to listOf(1, 3),
        )) {
            masks[cell.index] = digits.fold(0) { mask, digit -> mask or (1 shl (digit - 1)) }
        }
        return SolverState.fromValuesAndCandidates(values = IntArray(81), candidateMasks = masks)
    }

    private fun coordinate(cell: CellRef) = "R${cell.row + 1}C${cell.col + 1}"

    private fun isHintResource(name: String) =
        name.startsWith("game_hint_") || name.startsWith("technique_")

    private class XmlHintStrings(directory: String, private val locale: Locale) : HintStrings {
        val values: Map<String, String>

        init {
            val relativePath = "src/main/res/$directory/strings.xml"
            val file = listOf(File(relativePath), File("app/$relativePath")).first { it.isFile }
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val nodes = document.getElementsByTagName("string")
            values = (0 until nodes.length).associate { index ->
                val node = nodes.item(index)
                node.attributes.getNamedItem("name").nodeValue to
                    node.textContent.removeSurrounding("\"").replace("\\'", "'")
            }
        }

        override fun getString(resourceId: Int, vararg arguments: Any): String {
            val name = RESOURCE_NAMES.getValue(resourceId)
            val template = values.getValue(name)
            return String.format(locale, template, *arguments)
        }
    }

    companion object {
        private val PLACEHOLDER = Regex("%[0-9]+\\$[sd]")
        private val RESOURCE_NAMES = R.string::class.java.fields.associate { it.getInt(null) to it.name }
    }
}
