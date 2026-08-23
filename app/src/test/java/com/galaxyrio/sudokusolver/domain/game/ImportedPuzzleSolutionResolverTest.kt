package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImportedPuzzleSolutionResolverTest {
    private val resolver = ImportedPuzzleSolutionResolver()

    @Test
    fun resolvesAUniqueStandardPuzzle() {
        val solution = resolver.resolve(Sudoku.fromGridString(PUZZLE))

        assertEquals(SOLUTION, solution?.digits?.joinToString(separator = ""))
    }

    @Test
    fun explicitCandidatesAreTreatedAsConstraints() {
        val puzzle = Sudoku.fromGridString(PUZZLE)
        val cells = puzzle.cells.toMutableList()
        cells[2] = Cell(
            candidates = setOf(1, 2),
            isCandidateSetExplicit = true,
        )

        assertNull(resolver.resolve(Sudoku(cells)))
    }

    @Test
    fun rejectsAPuzzleWithMultipleSolutions() {
        assertNull(resolver.resolve(Sudoku()))
    }

    private companion object {
        const val PUZZLE =
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79"

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
