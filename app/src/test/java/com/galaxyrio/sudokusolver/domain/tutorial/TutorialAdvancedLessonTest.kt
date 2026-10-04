package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.game.UniqueSolutionChecker
import com.galaxyrio.sudokusolver.domain.game.UniquenessResult
import com.galaxyrio.sudokusolver.domain.solver.*
import com.galaxyrio.sudokusolver.ui.screens.tutorial.advancedLessonCopy
import com.galaxyrio.sudokusolver.ui.screens.tutorial.tutorialCategories
import org.junit.Assert.*
import org.junit.Test

class TutorialAdvancedLessonTest {
    @Test fun everyCatalogEntryHasContentAndEveryExampleBelongsToItsEntry() {
        val entries = tutorialCategories.flatMap { it.techniques }
        assertEquals(TechniqueId.entries.toSet(), entries.flatMap { it.solverTechniques }.toSet())
        assertEquals(entries.map { it.id }.toSet(), TutorialLessons.all.map { it.id }.toSet())
        entries.forEach { entry ->
            val lesson = requireNotNull(TutorialLessons.find(entry.id))
            if (lesson.examples.isEmpty()) {
                val text = advancedLessonCopy(lesson.technique)
                assertTrue(entry.id, text.rule != 0 && text.reason != 0 && text.tip != 0)
            }
            lesson.examples.forEach { example ->
                assertTrue(entry.id, example.technique in entry.solverTechniques)
                if (example.isAdvanced) {
                    val text = advancedLessonCopy(example.technique)
                    assertTrue(text.rule != 0 && text.reason != 0 && text.tip != 0)
                }
            }
        }
    }

    @Test fun allSnapshotsAreUniquelySolvableAndKeepTheSolutionInTheirCandidates() {
        val checker = UniqueSolutionChecker()
        advancedTutorialExamples.forEach { example ->
            val label = example.technique.name
            assertEquals(label, UniquenessResult.Unique, checker.check(example.givens.grid(), example.solution.grid()))
            assertTrue(label, example.initialState.isValid())
            assertTrue(label, example.initialState.apply(example.deduction).isValid())
            assertFalse(label, example.patternCells.isEmpty())
            if (example.technique == TechniqueId.UNIQUE_RECTANGLE_TYPE_3) {
                assertTrue("Type 3 must demonstrate a subset, not a naked single",
                    example.patternCells.all { example.initialState.candidatesAt(it).size >= 2 })
            }
            val legal = SolverState.fromSudoku(com.galaxyrio.sudokusolver.domain.model.Sudoku.fromGridString(example.givens))
            example.before.cells.forEachIndexed { index, cell ->
                if (cell.value == 0) {
                    assertTrue(label, cell.candidates.isNotEmpty())
                    assertTrue(label, legal.candidatesAt(CellRef.fromIndex(index)).containsAll(cell.candidates))
                    assertTrue(label, example.solution[index].digitToInt() in cell.candidates)
                } else {
                    assertTrue(cell.isFixed)
                    assertEquals(example.solution[index].digitToInt(), cell.value)
                }
            }
            example.after.cells.forEachIndexed { index, cell ->
                val answer = example.solution[index].digitToInt()
                if (cell.value == 0) assertTrue(label, answer in cell.candidates)
                else assertEquals(label, answer, cell.value)
                if (example.before.cells[index].isFixed) assertEquals(example.before.cells[index], cell)
            }
            val original = example.before.copy()
            assertEquals(original, example.boardAt(TutorialStage.PATTERN))
            assertEquals(example.after, example.boardAt(TutorialStage.RESULT))
            assertEquals(original, example.boardAt(TutorialStage.DEDUCTION))
        }
    }

    @Test fun recordedProofsReproduceWithTheNamedDetectorAndChangeOnlyProvedTargets() {
        advancedTutorialExamples.forEach { example ->
            val step = example.deduction
            assertEquals(example.technique.name, step, detector(example.technique).find(example.initialState))
            step.placements.forEach { placement ->
                assertTrue(example.initialState.hasCandidate(placement.cell, placement.digit))
                assertEquals(example.solution[placement.cell.index].digitToInt(), placement.digit)
            }
            step.eliminations.forEach { elimination ->
                val candidate = elimination.candidate
                assertTrue(example.initialState.hasCandidate(candidate.cell, candidate.digit))
                assertNotEquals(example.solution[candidate.cell.index].digitToInt(), candidate.digit)
                assertFalse(candidate.digit in example.after.cells[candidate.cell.index].candidates)
            }
            assertNotEquals(example.before, example.after)
            val allProofCandidates = step.evidence.causeCandidates + step.evidence.inferenceGraph.nodes.map { it.candidate } +
                step.evidence.groupedLinks.flatMap { it.from + it.to }
            allProofCandidates.forEach { candidate ->
                assertTrue("${example.technique}: $candidate", example.initialState.hasCandidate(candidate.cell, candidate.digit))
            }
        }
    }

    @Test fun forcingExamplesCoverAllAlternativesAndNetsActuallyJoinFacts() {
        advancedTutorialExamples.forEach { example ->
            val graph = example.deduction.evidence.inferenceGraph
            val premise = graph.premise ?: return@forEach
            val assumptions = graph.nodes.filter { it.isAssumption }
            when (premise.type) {
                InferencePremiseType.NISHIO -> {
                    assertEquals(1, assumptions.size)
                    assertNotNull(graph.contradiction)
                    assertEquals(InferenceTruth.TRUE, assumptions.single().truth)
                }
                InferencePremiseType.CELL -> {
                    val cell = premise.candidates.first().cell
                    assertEquals(example.initialState.candidatesAt(cell), premise.candidates.map { it.digit }.toSet())
                    assertEquals(premise.candidates.toSet(), assumptions.map { it.candidate }.toSet())
                    assertNull(graph.contradiction)
                }
                InferencePremiseType.REGION -> {
                    val digit = premise.candidates.first().digit
                    assertEquals(premise.house!!.cells().filter { example.initialState.hasCandidate(it, digit) }.toSet(),
                        premise.candidates.map { it.cell }.toSet())
                    assertEquals(premise.candidates.toSet(), assumptions.map { it.candidate }.toSet())
                    assertNull(graph.contradiction)
                }
                InferencePremiseType.DIGIT -> fail("No digit-forcing entry is in this catalog")
            }
            val byId = graph.nodes.associateBy { it.id }
            graph.edges.forEach { edge ->
                assertEquals(byId.getValue(edge.fromNodeId).branchId, byId.getValue(edge.toNodeId).branchId)
            }
            assertEquals(example.technique.name.endsWith("_NET"), graph.isNet)
        }
    }

    @Test fun coloringAlternatesAcrossEveryConjugateLinkAndGroupsStayGrouped() {
        advancedTutorialExamples.forEach { example ->
            if (example.technique in setOf(TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2)) {
                assertEquals(example.patternCandidates, example.coloring.keys)
                example.deduction.evidence.links.forEach { link ->
                    assertNotEquals(example.coloring.getValue(link.from), example.coloring.getValue(link.to))
                }
            }
            if (example.technique == TechniqueId.GROUPED_X_CHAIN) {
                val groups = example.deduction.evidence.groupedLinks
                assertTrue(groups.isNotEmpty())
                assertTrue(groups.any { it.from.size > 1 || it.to.size > 1 })
            }
        }
    }

    @Test fun wingLessonsNameValidPivotAndWingGroupsForTheActualEliminations() {
        val wings = setOf(TechniqueId.XY_WING, TechniqueId.XYZ_WING, TechniqueId.WXYZ_WING,
            TechniqueId.FIVE_Y_WING, TechniqueId.SIX_Y_WING, TechniqueId.SEVEN_Y_WING)
        advancedTutorialExamples.filter { it.technique in wings }.forEach { example ->
            val groups = requireNotNull(example.wingGroups) { example.technique.name }
            assertEquals(example.patternCells, groups.pivot + groups.firstWing + groups.secondWing)
            val z = example.deduction.eliminations.first().candidate.digit
            fun Set<CellRef>.digits() = flatMap(example.initialState::candidatesAt).toSet()
            assertEquals(setOf(z), groups.firstWing.digits().intersect(groups.secondWing.digits()))
            assertEquals(example.patternCells.size, example.patternCells.digits().size)
        }
    }

    private fun String.grid() = Array(9) { row -> IntArray(9) { col -> this[row * 9 + col].digitToInt() } }

    private fun detector(technique: TechniqueId): TechniqueDetector = when (technique) {
        TechniqueId.POINTING_PAIR, TechniqueId.POINTING_TRIPLE -> PointingCandidatesDetector()
        TechniqueId.CLAIMING_PAIR, TechniqueId.CLAIMING_TRIPLE -> ClaimingCandidatesDetector()
        TechniqueId.X_WING -> BasicFishDetector(2)
        TechniqueId.SWORDFISH -> BasicFishDetector(3)
        TechniqueId.JELLYFISH -> BasicFishDetector(4)
        TechniqueId.FINNED_X_WING -> FinnedFishDetector(2)
        TechniqueId.FINNED_SWORDFISH -> FinnedFishDetector(3)
        TechniqueId.FINNED_JELLYFISH -> FinnedFishDetector(4)
        TechniqueId.SKYSCRAPER -> SkyscraperDetector()
        TechniqueId.TWO_STRING_KITE -> TwoStringKiteDetector()
        TechniqueId.TURBOT_CRANE -> TurbotCraneDetector()
        TechniqueId.EMPTY_RECTANGLE -> EmptyRectangleDetector()
        TechniqueId.XY_WING -> XYWingDetector()
        TechniqueId.XYZ_WING -> XYZWingDetector()
        TechniqueId.WXYZ_WING -> WXYZWingDetector()
        TechniqueId.FIVE_Y_WING -> NYWingDetector(5)
        TechniqueId.SIX_Y_WING -> NYWingDetector(6)
        TechniqueId.SEVEN_Y_WING -> NYWingDetector(7)
        TechniqueId.W_WING -> WWingDetector()
        TechniqueId.SIMPLE_COLORING_TYPE_1, TechniqueId.SIMPLE_COLORING_TYPE_2 -> SimpleColoringDetector()
        TechniqueId.X_CHAIN, TechniqueId.X_CHAIN_LOOP, TechniqueId.X_CHAIN_ONE_ENDPOINT -> XChainDetector()
        TechniqueId.GROUPED_X_CHAIN -> GroupedXChainDetector()
        TechniqueId.THREE_D_MEDUSA -> ThreeDMedusaDetector()
        TechniqueId.XY_CHAIN, TechniqueId.XY_CHAIN_LOOP -> XYChainDetector()
        TechniqueId.AIC -> AicDetector()
        TechniqueId.NISHIO_FORCING_CHAIN -> ForcingChainDetector(setOf(InferencePremiseType.NISHIO))
        TechniqueId.CELL_FORCING_CHAIN, TechniqueId.CELL_FORCING_NET -> ForcingChainDetector(setOf(InferencePremiseType.CELL))
        TechniqueId.REGION_FORCING_CHAIN, TechniqueId.REGION_FORCING_NET -> ForcingChainDetector(setOf(InferencePremiseType.REGION))
        TechniqueId.UNIQUE_RECTANGLE_TYPE_1, TechniqueId.UNIQUE_RECTANGLE_TYPE_2, TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
        TechniqueId.UNIQUE_RECTANGLE_TYPE_4, TechniqueId.UNIQUE_RECTANGLE_TYPE_5 -> UniqueRectangleDetector()
        TechniqueId.BUG_PLUS_ONE -> BugPlusOneDetector()
        else -> error("Not an advanced catalog technique: $technique")
    }
}
