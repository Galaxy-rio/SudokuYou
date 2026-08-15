package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedTechniqueDetectorsTest {

    @Test
    fun detectsEveryBasicFishSizeInRows() {
        val expectedTechniques = mapOf(
            2 to TechniqueId.X_WING,
            3 to TechniqueId.SWORDFISH,
            4 to TechniqueId.JELLYFISH,
            5 to TechniqueId.STARFISH,
            6 to TechniqueId.WHALE,
            7 to TechniqueId.LEVIATHAN,
        )

        expectedTechniques.forEach { (size, technique) ->
            val entries = buildList {
                repeat(size) { row ->
                    repeat(size) { column -> add(cell(row, column).withCandidates(9)) }
                }
                add(cell(8, 0).withCandidates(1, 9))
            }
            val step = BasicFishDetector(size).find(stateOf(entries))

            assertEquals(technique, step?.technique)
            assertTrue(step!!.removes(cell(8, 0), 9))
            assertEquals(size, step.evidence.baseHouses.size)
            assertEquals(size, step.evidence.coverHouses.size)
        }
    }

    @Test
    fun detectsColumnBasedXWing() {
        val state = stateOf(
            cell(0, 0).withCandidates(5),
            cell(4, 0).withCandidates(5),
            cell(0, 3).withCandidates(5),
            cell(4, 3).withCandidates(5),
            cell(0, 8).withCandidates(2, 5),
        )

        val step = BasicFishDetector(2).find(state)

        assertEquals(TechniqueId.X_WING, step?.technique)
        assertTrue(step!!.removes(cell(0, 8), 5))
        assertTrue(step.evidence.baseHouses.all { it.type == HouseType.COLUMN })
    }

    @Test
    fun defaultHumanSolverReachesXWingAfterBasicTechniques() {
        val masks = IntArray(Sudoku.CELL_COUNT) { SolverState.FULL_CANDIDATE_MASK }
        mapOf(
            0 to setOf(0, 3),
            4 to setOf(0, 3),
            8 to setOf(0, 8),
        ).forEach { (row, columnsWithFive) ->
            repeat(Sudoku.GRID_SIZE) { column ->
                if (column !in columnsWithFive) {
                    masks[cell(row, column).index] =
                        masks[cell(row, column).index] and digitMask(5).inv()
                }
            }
        }
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = HumanSolver().nextStep(state)

        assertEquals(TechniqueId.X_WING, step?.technique)
        assertTrue(step!!.removes(cell(8, 0), 5))
    }

    @Test
    fun doesNotReportDegenerateFish() {
        val state = stateOf(
            cell(0, 0).withCandidates(5),
            cell(0, 3).withCandidates(5),
            cell(4, 0).withCandidates(5),
            cell(4, 4).withCandidates(5),
            cell(8, 0).withCandidates(2, 5),
        )

        assertEquals(null, BasicFishDetector(2).find(state))
    }

    @Test
    fun detectsXYXYZAndWXYZWings() {
        val xy = XYWingDetector().find(
            stateOf(
                cell(1, 1).withCandidates(1, 2),
                cell(1, 4).withCandidates(1, 3),
                cell(4, 1).withCandidates(2, 3),
                cell(4, 4).withCandidates(3, 4, 5),
            )
        )
        assertEquals(TechniqueId.XY_WING, xy?.technique)
        assertTrue(xy!!.removes(cell(4, 4), 3))
        assertFalse(xy.evidence.links.isEmpty())

        val xyz = XYZWingDetector().find(
            stateOf(
                cell(1, 1).withCandidates(1, 2, 3),
                cell(1, 4).withCandidates(1, 3),
                cell(2, 2).withCandidates(2, 3),
                cell(1, 2).withCandidates(3, 4, 5),
            )
        )
        assertEquals(TechniqueId.XYZ_WING, xyz?.technique)
        assertTrue(xyz!!.removes(cell(1, 2), 3))

        val wxyz = WXYZWingDetector().find(
            stateOf(
                cell(1, 1).withCandidates(1, 2, 3, 4),
                cell(2, 2).withCandidates(1, 4),
                cell(1, 4).withCandidates(2, 4),
                cell(1, 7).withCandidates(3, 4),
                cell(1, 2).withCandidates(4, 5, 6),
            )
        )
        assertEquals(TechniqueId.WXYZ_WING, wxyz?.technique)
        assertTrue(wxyz!!.removes(cell(1, 2), 4))
    }

    @Test
    fun detectsFourYWingWithoutFourCandidatePivot() {
        val step = WXYZWingDetector().find(
            stateOf(
                // Screenshot pattern: r1c7 is the pivot; the other three cells form two wings.
                cell(0, 6).withCandidates(2, 3, 4),
                cell(0, 3).withCandidates(4, 7),
                cell(0, 4).withCandidates(3, 4, 7),
                cell(1, 8).withCandidates(2, 7),
                cell(0, 7).withCandidates(5, 7),
                cell(1, 4).withCandidates(3, 4, 7),
            )
        )

        assertEquals(TechniqueId.WXYZ_WING, step?.technique)
        assertTrue(step!!.removes(cell(0, 7), 7))
        assertTrue(step.removes(cell(1, 4), 7))
        assertEquals(
            setOf(cell(0, 3), cell(0, 4), cell(0, 6), cell(1, 8)),
            step.evidence.causeCells,
        )
        assertEquals(setOf(2, 3, 4, 7), step.evidence.focusDigits)
    }

    @Test
    fun unrestrictedFourYWingOnlyRemovesFromCellsThatSeeThePivotToo() {
        val step = WXYZWingDetector().find(
            stateOf(
                cell(0, 6).withCandidates(2, 3, 4, 7),
                cell(0, 3).withCandidates(4, 7),
                cell(0, 4).withCandidates(3, 4, 7),
                cell(1, 8).withCandidates(2, 7),
                cell(0, 7).withCandidates(5, 7),
                cell(1, 4).withCandidates(3, 4, 7),
            )
        )

        assertEquals(TechniqueId.WXYZ_WING, step?.technique)
        assertTrue(step!!.removes(cell(0, 7), 7))
        assertFalse(step.removes(cell(1, 4), 7))
    }

    @Test
    fun defaultHumanSolverReachesReportedFourYWing() {
        val trace = HumanSolver().solveTrace(
            Sudoku.fromGridString(
                "9....6..1" +
                    "..69.1.8." +
                    ".1.852..." +
                    ".3..9...." +
                    "..92.7.34" +
                    "8....3..." +
                    "5....914." +
                    "7913.4..5" +
                    "......79."
            )
        )
        val reportedStep = trace.steps.firstOrNull { step ->
            step.technique == TechniqueId.WXYZ_WING &&
                step.removes(cell(0, 7), 7) &&
                step.removes(cell(1, 4), 7)
        }

        assertNotNull(
            "Trace did not reach the reported 4-Y-Wing: " +
                trace.steps.joinToString { it.technique.name },
            reportedStep,
        )
    }

    @Test
    fun detectsAllThreeTurbotForms() {
        val skyscraper = SkyscraperDetector().find(
            stateOf(
                cell(0, 0).withCandidates(5),
                cell(0, 4).withCandidates(5),
                cell(4, 0).withCandidates(5),
                cell(4, 5).withCandidates(5),
                cell(1, 5).withCandidates(2, 5),
            )
        )
        assertEquals(TechniqueId.SKYSCRAPER, skyscraper?.technique)
        assertTrue(skyscraper!!.removes(cell(1, 5), 5))
        assertEquals(3, skyscraper.evidence.links.size)

        val kite = TwoStringKiteDetector().find(
            stateOf(
                cell(0, 0).withCandidates(6),
                cell(0, 4).withCandidates(6),
                cell(1, 1).withCandidates(6),
                cell(4, 1).withCandidates(6),
                cell(4, 4).withCandidates(2, 6),
            )
        )
        assertEquals(TechniqueId.TWO_STRING_KITE, kite?.technique)
        assertTrue(kite!!.removes(cell(4, 4), 6))

        val crane = TurbotCraneDetector().find(
            stateOf(
                cell(0, 0).withCandidates(7),
                cell(1, 1).withCandidates(7),
                cell(1, 3).withCandidates(7),
                cell(4, 3).withCandidates(7),
                cell(4, 0).withCandidates(2, 7),
            )
        )
        assertEquals(TechniqueId.TURBOT_CRANE, crane?.technique)
        assertTrue(crane!!.removes(cell(4, 0), 7))
    }

    @Test
    fun detectsRemotePairAndEveryChuteResult() {
        val remotePair = RemotePairDetector().find(
            stateOf(
                cell(0, 0).withCandidates(1, 2),
                cell(0, 4).withCandidates(1, 2),
                cell(4, 4).withCandidates(1, 2),
                cell(4, 8).withCandidates(1, 2),
                cell(0, 8).withCandidates(1, 2, 3),
            )
        )
        assertEquals(TechniqueId.REMOTE_PAIR, remotePair?.technique)
        assertTrue(remotePair!!.removes(cell(0, 8), 1))
        assertTrue(remotePair.removes(cell(0, 8), 2))

        val commonEntries = listOf(
            cell(0, 0).withCandidates(1, 2),
            cell(1, 4).withCandidates(1, 2),
            cell(0, 4).withCandidates(1, 2, 3),
        )
        val single = ChuteRemotePairDetector().find(
            stateOf(commonEntries + cell(2, 6).withCandidates(1, 4))
        )
        assertEquals(TechniqueId.CHUTE_REMOTE_PAIR_SINGLE, single?.technique)
        assertTrue(single!!.removes(cell(0, 4), 1))

        val double = ChuteRemotePairDetector().find(stateOf(commonEntries))
        assertEquals(TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE, double?.technique)
        assertTrue(double!!.removes(cell(0, 4), 1))
        assertTrue(double.removes(cell(0, 4), 2))

        val bonus = ChuteRemotePairDetector().find(
            stateOf(commonEntries + cell(0, 1).withCandidates(1, 4, 5))
        )
        assertEquals(TechniqueId.CHUTE_REMOTE_PAIR_BONUS, bonus?.technique)
        assertTrue(bonus!!.removes(cell(0, 1), 1))
    }

    @Test
    fun detectsBothSimpleColoringRules() {
        val type1 = SimpleColoringDetector().find(
            stateOf(
                cell(0, 0).withCandidates(8),
                cell(0, 4).withCandidates(8),
                cell(4, 4).withCandidates(8),
                cell(4, 1).withCandidates(8),
                cell(1, 1).withCandidates(2, 8),
                cell(2, 2).withCandidates(3, 8),
                cell(7, 1).withCandidates(4, 8),
            )
        )
        assertEquals(TechniqueId.SIMPLE_COLORING_TYPE_1, type1?.technique)
        assertTrue(type1!!.removes(cell(1, 1), 8))

        val type2 = SimpleColoringDetector().find(
            stateOf(
                cell(0, 0).withCandidates(8),
                cell(0, 1).withCandidates(8),
                cell(1, 1).withCandidates(8),
            )
        )
        assertEquals(TechniqueId.SIMPLE_COLORING_TYPE_2, type2?.technique)
        assertEquals(listOf(Placement(cell(0, 1), 8)), type2?.placements)
        assertTrue(type2!!.removes(cell(0, 0), 8))
        assertTrue(type2.removes(cell(1, 1), 8))
    }

    @Test
    fun detectsEmptyRectangle() {
        val step = EmptyRectangleDetector().find(
            stateOf(
                cell(0, 0).withCandidates(5),
                cell(0, 4).withCandidates(5),
                cell(3, 0).withCandidates(5),
                cell(4, 1).withCandidates(5),
                cell(4, 4).withCandidates(2, 5),
            )
        )

        assertEquals(TechniqueId.EMPTY_RECTANGLE, step?.technique)
        assertTrue(step!!.removes(cell(4, 4), 5))
    }

    @Test
    fun detectsXYChainAndLoop() {
        val chain = XYChainDetector().find(
            stateOf(
                cell(0, 0).withCandidates(1, 2),
                cell(0, 4).withCandidates(2, 3),
                cell(4, 4).withCandidates(3, 4),
                cell(4, 8).withCandidates(1, 4),
                cell(0, 8).withCandidates(1, 5, 6),
            )
        )
        assertEquals(TechniqueId.XY_CHAIN, chain?.technique)
        assertTrue(chain!!.removes(cell(0, 8), 1))
        assertTrue(chain.evidence.links.isNotEmpty())

        val loop = XYChainDetector().find(
            stateOf(
                cell(0, 0).withCandidates(1, 2),
                cell(0, 4).withCandidates(2, 3),
                cell(4, 4).withCandidates(3, 4),
                cell(4, 0).withCandidates(1, 4),
                cell(2, 0).withCandidates(1, 5, 6),
            )
        )
        assertEquals(TechniqueId.XY_CHAIN_LOOP, loop?.technique)
        assertTrue(loop!!.removes(cell(2, 0), 1))
    }

    @Test
    fun detectsXChainLoopAndOneEndpoint() {
        val chain = XChainDetector().find(
            stateOf(
                cell(0, 0).withCandidates(9),
                cell(0, 4).withCandidates(9),
                cell(3, 4).withCandidates(9),
                cell(3, 7).withCandidates(9),
                cell(6, 7).withCandidates(9),
                cell(6, 2).withCandidates(9),
                cell(1, 2).withCandidates(1, 9),
                cell(2, 1).withCandidates(2, 9),
                cell(8, 2).withCandidates(3, 9),
            )
        )
        assertEquals(TechniqueId.X_CHAIN, chain?.technique)
        assertTrue(chain!!.removes(cell(1, 2), 9))

        val loop = XChainDetector().find(
            stateOf(
                cell(0, 0).withCandidates(9),
                cell(0, 4).withCandidates(9),
                cell(3, 4).withCandidates(9),
                cell(3, 7).withCandidates(9),
                cell(6, 7).withCandidates(9),
                cell(6, 0).withCandidates(9),
                cell(8, 0).withCandidates(1, 9),
                cell(7, 1).withCandidates(2, 9),
            )
        )
        assertEquals(TechniqueId.X_CHAIN_LOOP, loop?.technique)
        assertTrue(loop!!.removes(cell(8, 0), 9))

        val oneEndpoint = XChainDetector().find(
            stateOf(
                cell(0, 0).withCandidates(9),
                cell(0, 4).withCandidates(9),
                cell(3, 4).withCandidates(9),
                cell(3, 1).withCandidates(9),
                cell(1, 1).withCandidates(9),
            )
        )
        assertEquals(TechniqueId.X_CHAIN_ONE_ENDPOINT, oneEndpoint?.technique)
        assertEquals(listOf(Placement(cell(0, 0), 9)), oneEndpoint?.placements)
    }

    @Test
    fun detectsOrdinaryAlternatingInferenceChain() {
        val step = AicDetector().find(
            stateOf(
                cell(0, 0).withCandidates(1, 2),
                cell(0, 4).withCandidates(2, 3),
                cell(4, 4).withCandidates(3, 4),
                cell(4, 0).withCandidates(1, 4),
                cell(8, 0).withCandidates(1, 5, 6),
            )
        )

        assertEquals(TechniqueId.AIC, step?.technique)
        assertTrue(step!!.removes(cell(8, 0), 1))
        assertTrue(step.evidence.links.size >= 3)
        assertEquals(InferenceLinkType.STRONG, step.evidence.links.first().type)
        assertEquals(InferenceLinkType.STRONG, step.evidence.links.last().type)
        assertTrue(
            step.evidence.links.zipWithNext().all { (first, second) ->
                first.type != second.type
            }
        )
    }

    @Test
    fun detectsBugPlusOneFromCandidateParity() {
        val solution = SOLUTION.map(Char::digitToInt)
        val masks = IntArray(Sudoku.CELL_COUNT) { index ->
            val first = solution[index]
            val second = first % Sudoku.GRID_SIZE + 1
            candidateMaskOf(first, second)
        }
        masks[cell(0, 0).index] = candidateMaskOf(5, 6, 7)
        val state = SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )

        val step = BugPlusOneDetector().find(state)

        assertEquals(TechniqueId.BUG_PLUS_ONE, step?.technique)
        assertEquals(listOf(Placement(cell(0, 0), 7)), step?.placements)
    }

    @Test
    fun advancedStepsAlwaysCarryUiIndependentEvidence() {
        val step = BasicFishDetector(2).find(
            stateOf(
                cell(0, 0).withCandidates(9),
                cell(0, 3).withCandidates(9),
                cell(4, 0).withCandidates(9),
                cell(4, 3).withCandidates(9),
                cell(8, 0).withCandidates(1, 9),
            )
        )

        assertNotNull(step)
        assertFalse(step!!.evidence.causeCells.isEmpty())
        assertFalse(step.evidence.causeCandidates.isEmpty())
        assertFalse(step.evidence.houses.isEmpty())
    }

    private fun stateOf(vararg entries: Pair<CellRef, Set<Int>>): SolverState =
        stateOf(entries.toList())

    private fun stateOf(entries: List<Pair<CellRef, Set<Int>>>): SolverState {
        val masks = IntArray(Sudoku.CELL_COUNT)
        entries.forEach { (cell, digits) ->
            masks[cell.index] = digits.fold(0) { mask, digit -> mask or digitMask(digit) }
        }
        return SolverState.fromValuesAndCandidates(
            values = IntArray(Sudoku.CELL_COUNT),
            candidateMasks = masks,
        )
    }

    private fun SolveStep.removes(cell: CellRef, digit: Int): Boolean =
        eliminations.any { it.candidate == CandidateRef(cell, digit) }

    private fun CellRef.withCandidates(vararg digits: Int): Pair<CellRef, Set<Int>> =
        this to digits.toSet()

    private companion object {
        fun cell(row: Int, column: Int): CellRef = CellRef(row, column)

        const val SOLUTION =
            "534678912" +
                "672195348" +
                "198342567" +
                "859761423" +
                "426853791" +
                "713924856" +
                "961537284" +
                "287419635" +
                "345286179"
    }
}
