package com.galaxyrio.sudokusolver.data

import com.galaxyrio.sudokusolver.data.local.PuzzleInventoryDao
import com.galaxyrio.sudokusolver.data.local.PuzzleInventoryEntity
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalPuzzleInventoryTest {
    @Test
    fun refillPrioritizesRequestedDifficultyAndStopsAtTwoPerDifficulty() = runTest {
        val dao = FakePuzzleInventoryDao()
        val generatedDifficulties = mutableListOf<Difficulty>()
        var puzzleNumber = 0
        val inventory = LocalPuzzleInventory(
            puzzleDao = dao,
            applicationScope = backgroundScope,
            generationDispatcher = UnconfinedTestDispatcher(testScheduler),
            generatePuzzle = { difficulty ->
                generatedDifficulties += difficulty
                puzzle(++puzzleNumber)
            },
            currentTimeMillis = { puzzleNumber.toLong() },
        )

        inventory.refillNow(priority = Difficulty.HARD)

        assertEquals(Difficulty.HARD, generatedDifficulties.first())
        Difficulty.entries.forEach { difficulty ->
            assertEquals(
                LocalPuzzleInventory.MAXIMUM_STOCK_PER_DIFFICULTY,
                dao.count(difficulty, LocalPuzzleInventory.GENERATOR_VERSION),
            )
        }

        inventory.refillNow(priority = Difficulty.EASY)
        assertEquals(8, generatedDifficulties.size)
    }

    @Test
    fun takingPuzzleRemovesItAndNextRefillRestoresTheLimit() = runTest {
        val dao = FakePuzzleInventoryDao()
        var puzzleNumber = 0
        val inventory = LocalPuzzleInventory(
            puzzleDao = dao,
            applicationScope = backgroundScope,
            generationDispatcher = UnconfinedTestDispatcher(testScheduler),
            generatePuzzle = { puzzle(++puzzleNumber) },
        )
        inventory.refillNow(priority = Difficulty.MEDIUM)

        assertNotNull(inventory.take(Difficulty.MEDIUM))
        assertEquals(
            1,
            dao.count(Difficulty.MEDIUM, LocalPuzzleInventory.GENERATOR_VERSION),
        )

        inventory.refillNow(priority = Difficulty.MEDIUM)
        assertEquals(
            2,
            dao.count(Difficulty.MEDIUM, LocalPuzzleInventory.GENERATOR_VERSION),
        )
    }

    private fun puzzle(number: Int): Sudoku {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(value = number / Sudoku.GRID_SIZE + 1, isFixed = true)
        cells[1] = Cell(value = number % Sudoku.GRID_SIZE + 1, isFixed = true)
        return Sudoku(cells)
    }

    private class FakePuzzleInventoryDao : PuzzleInventoryDao {
        private val puzzles = mutableListOf<PuzzleInventoryEntity>()
        private var nextId = 1L

        override suspend fun count(
            difficulty: Difficulty,
            generatorVersion: Int,
        ): Int = puzzles.count {
            it.difficulty == difficulty && it.generatorVersion == generatorVersion
        }

        override suspend fun findOldest(
            difficulty: Difficulty,
            generatorVersion: Int,
        ): PuzzleInventoryEntity? = puzzles
            .filter { it.difficulty == difficulty && it.generatorVersion == generatorVersion }
            .minWithOrNull(compareBy(PuzzleInventoryEntity::createdAt, PuzzleInventoryEntity::id))

        override suspend fun deleteById(id: Long): Int =
            if (puzzles.removeAll { it.id == id }) 1 else 0

        override suspend fun insert(puzzle: PuzzleInventoryEntity): Long {
            if (puzzles.any { it.fingerprint == puzzle.fingerprint }) return -1L
            val id = nextId++
            puzzles += puzzle.copy(id = id)
            return id
        }

        override suspend fun deleteStale(generatorVersion: Int) {
            puzzles.removeAll { it.generatorVersion != generatorVersion }
        }
    }
}
