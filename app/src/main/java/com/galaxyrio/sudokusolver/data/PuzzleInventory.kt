package com.galaxyrio.sudokusolver.data

import com.galaxyrio.sudokusolver.data.local.PuzzleInventoryDao
import com.galaxyrio.sudokusolver.data.local.PuzzleInventoryEntity
import com.galaxyrio.sudokusolver.domain.game.SudokuGenerator
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PuzzleInventory {
    suspend fun take(difficulty: Difficulty): Sudoku?

    fun requestRefill(priority: Difficulty)
}

object EmptyPuzzleInventory : PuzzleInventory {
    override suspend fun take(difficulty: Difficulty): Sudoku? = null

    override fun requestRefill(priority: Difficulty) = Unit
}

class LocalPuzzleInventory(
    private val puzzleDao: PuzzleInventoryDao,
    applicationScope: CoroutineScope,
    private val generationDispatcher: CoroutineDispatcher =
        Dispatchers.Default.limitedParallelism(1),
    private val generatePuzzle: (Difficulty) -> Sudoku = SudokuGenerator()::generate,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : PuzzleInventory {
    private val refillRequests = Channel<Difficulty>(capacity = Channel.CONFLATED)

    init {
        applicationScope.launch {
            for (priority in refillRequests) {
                try {
                    refillNow(priority)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // Inventory is an optimization. A future request will retry a failed refill.
                }
            }
        }
    }

    override suspend fun take(difficulty: Difficulty): Sudoku? {
        puzzleDao.deleteStale(GENERATOR_VERSION)
        return puzzleDao.takeOldest(difficulty, GENERATOR_VERSION)?.sudoku
    }

    override fun requestRefill(priority: Difficulty) {
        refillRequests.trySend(priority)
    }

    internal suspend fun refillNow(priority: Difficulty) {
        puzzleDao.deleteStale(GENERATOR_VERSION)
        val refillOrder = buildList {
            add(priority)
            Difficulty.entries.filterTo(this) { it != priority }
        }
        val failedDifficulties = mutableSetOf<Difficulty>()

        // Give every difficulty one ready puzzle before filling the second slot.
        for (targetStock in 1..MAXIMUM_STOCK_PER_DIFFICULTY) {
            for (difficulty in refillOrder) {
                if (difficulty in failedDifficulties) continue
                if (puzzleDao.count(difficulty, GENERATOR_VERSION) >= targetStock) continue

                val generated = try {
                    withContext(generationDispatcher) {
                        generatePuzzle(difficulty)
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    failedDifficulties += difficulty
                    continue
                }

                puzzleDao.insertIfBelowLimit(
                    puzzle = PuzzleInventoryEntity(
                        difficulty = difficulty,
                        sudoku = generated,
                        fingerprint = generated.fingerprint(),
                        generatorVersion = GENERATOR_VERSION,
                        createdAt = currentTimeMillis(),
                    ),
                    maximumStock = MAXIMUM_STOCK_PER_DIFFICULTY,
                )
            }
        }
    }

    private fun Sudoku.fingerprint(): String = buildString(cells.size) {
        cells.forEach { cell -> append(cell.value) }
    }

    companion object {
        const val MAXIMUM_STOCK_PER_DIFFICULTY = 2
        const val GENERATOR_VERSION = 1
    }
}
