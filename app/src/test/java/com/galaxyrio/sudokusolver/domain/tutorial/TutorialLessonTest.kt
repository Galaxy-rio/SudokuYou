package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.game.UniqueSolutionChecker
import com.galaxyrio.sudokusolver.domain.game.UniquenessResult
import com.galaxyrio.sudokusolver.domain.solver.HouseType
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialLessonTest {
    @Test
    fun allEightLessonsCoverBoxesRowsAndColumns() {
        assertEquals(setOf(
            TechniqueId.HIDDEN_SINGLE, TechniqueId.HIDDEN_PAIR, TechniqueId.HIDDEN_TRIPLE, TechniqueId.HIDDEN_QUAD,
            TechniqueId.NAKED_SINGLE, TechniqueId.NAKED_PAIR, TechniqueId.NAKED_TRIPLE, TechniqueId.NAKED_QUAD,
        ), TutorialLessons.all.filterNot { it.examples.first().isAdvanced }.map { it.technique }.toSet())
        TutorialLessons.all.filterNot { it.examples.first().isAdvanced }.forEach { lesson ->
            assertEquals(3, lesson.examples.size)
            assertEquals(HouseType.entries.toSet(), lesson.examples.map { it.house.type }.toSet())
            assertEquals(HouseType.BOX, lesson.examples.first().house.type)
            assertSame(lesson, TutorialLessons.find(lesson.technique.name))
        }
        assertTrue(TutorialLessons.find(TechniqueId.X_WING.name)!!.examples.isNotEmpty())
        assertNull(TutorialLessons.find("unknown"))
    }

    @Test
    fun all24ExamplesHaveExactlyOneSolutionAndCompleteLegalCandidates() {
        val checker = UniqueSolutionChecker()
        assertEquals(24, tutorialExamples.size)
        tutorialExamples.forEach { example ->
            assertEquals(example.label(), UniquenessResult.Unique, checker.check(example.givens.toGrid(), example.solution.toGrid()))
            example.before.cells.forEachIndexed { index, cell ->
                if (cell.value != 0) {
                    assertTrue(cell.isFixed)
                    assertEquals(example.solution[index].digitToInt(), cell.value)
                    assertTrue(cell.candidates.isEmpty())
                } else {
                    val excluded = example.before.cells.mapIndexedNotNull { other, peer ->
                        peer.value.takeIf { it != 0 && peers(index, other) }
                    }.toSet()
                    assertEquals(example.label(), (1..9).toSet() - excluded, cell.candidates)
                    assertTrue(example.solution[index].digitToInt() in cell.candidates)
                }
            }
        }
    }

    @Test
    fun everyHighlightedGroupSatisfiesItsTechniqueDefinition() {
        tutorialExamples.forEach { example ->
            val state = example.initialState
            assertEquals(example.digits.size, example.patternCells.size)
            assertTrue(example.house.cells().containsAll(example.patternCells))
            assertTrue(example.patternCells.all { state.valueAt(it) == 0 })
            if (example.isHidden) {
                val positions = example.house.cells().filter { cell ->
                    state.candidatesAt(cell).any { it in example.digits }
                }.toSet()
                assertEquals(example.label(), example.patternCells, positions)
                example.digits.forEach { digit ->
                    assertTrue(example.patternCells.any { state.hasCandidate(it, digit) })
                }
                // The lesson must demonstrate a hidden pattern, not an already naked group.
                assertTrue(example.patternCells.any { (state.candidatesAt(it) - example.digits).isNotEmpty() })
            } else {
                assertEquals(example.label(), example.digits, example.patternCells.flatMap { state.candidatesAt(it) }.toSet())
            }
            assertEquals(example.digits, example.patternCells.map { example.solution[it.index].digitToInt() }.toSet())
        }
    }

    @Test
    fun largerGroupsDoNotHideAnAlreadySufficientSmallerGroup() {
        tutorialExamples.filterNot { it.isSingle }.forEach { example ->
            val size = example.digits.size
            val cells = example.patternCells.toList()
            val digits = example.digits.toList()
            for (mask in 1 until (1 shl size) - 1) {
                val selected = (0 until size).filter { mask and (1 shl it) != 0 }
                if (example.isHidden) {
                    val subset = selected.map { digits[it] }.toSet()
                    val positions = cells.filter { cell -> example.initialState.candidatesAt(cell).any { it in subset } }
                    assertTrue(example.label(), positions.size > subset.size)
                } else {
                    val candidates = selected.flatMap { example.initialState.candidatesAt(cells[it]) }.toSet()
                    assertTrue(example.label(), candidates.size > selected.size)
                }
            }
        }
    }

    @Test
    fun deductionsPreserveTheSolutionAndChangeOnlyTheirTargets() {
        tutorialExamples.forEach { example ->
            val before = example.before
            val after = example.after
            assertNotEquals(example.label(), before, after)
            val removals = example.deduction.eliminations.map { it.candidate }.toSet()
            if (!example.isSingle) assertTrue(removals.isNotEmpty())
            removals.forEach { candidate ->
                assertTrue(candidate.digit in before.cells[candidate.cell.index].candidates)
                assertFalse(candidate.digit in after.cells[candidate.cell.index].candidates)
                assertNotEquals(example.solution[candidate.cell.index].digitToInt(), candidate.digit)
                if (example.isHidden) {
                    assertTrue(candidate.cell in example.patternCells && candidate.digit !in example.digits)
                } else {
                    assertTrue(candidate.cell !in example.patternCells && candidate.digit in example.digits)
                    assertTrue(example.commonHouses.any { candidate.cell in it.cells() })
                }
            }
            after.cells.forEachIndexed { index, cell ->
                val expected = example.solution[index].digitToInt()
                if (cell.value != 0) assertEquals(expected, cell.value)
                else assertTrue(example.label(), expected in cell.candidates)
                if (before.cells[index].isFixed) assertEquals(before.cells[index], cell)
                if (!example.isSingle) {
                    val expectedCandidates = before.cells[index].candidates - removals.filter { it.cell.index == index }.map { it.digit }.toSet()
                    assertEquals(expectedCandidates, cell.candidates)
                }
            }
            if (example.isSingle) {
                val target = example.patternCells.single().index
                val digit = example.digits.single()
                assertEquals(digit, after.cells[target].value)
                assertTrue(after.cells[target].candidates.isEmpty())
                before.cells.indices.filter { it != target && peers(target, it) }.forEach {
                    assertFalse(digit in after.cells[it].candidates)
                }
            }
        }
    }

    @Test
    fun revisitingAStepOrExampleRestoresTheOriginalBoard() {
        tutorialExamples.forEach { example ->
            val original = example.before.copy(cells = example.before.cells.toList())
            assertEquals(original, example.boardAt(TutorialStage.RULE))
            assertEquals(original, example.boardAt(TutorialStage.PATTERN))
            assertEquals(original, example.boardAt(TutorialStage.DEDUCTION))
            assertEquals(example.after, example.boardAt(TutorialStage.RESULT))
            assertEquals(example.after, example.boardAt(TutorialStage.TAKEAWAY))
            assertEquals(original, example.boardAt(TutorialStage.DEDUCTION))
        }
    }

    private fun TutorialExample.label() = "$technique ${house.type} ${house.index + 1}"
    private fun String.toGrid() = Array(9) { row -> IntArray(9) { col -> this[row * 9 + col].digitToInt() } }
    private fun peers(first: Int, second: Int): Boolean = first / 9 == second / 9 || first % 9 == second % 9 ||
        (first / 27 == second / 27 && first % 9 / 3 == second % 9 / 3)
}
