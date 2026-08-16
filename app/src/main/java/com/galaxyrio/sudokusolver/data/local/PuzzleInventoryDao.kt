package com.galaxyrio.sudokusolver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.galaxyrio.sudokusolver.domain.model.Difficulty

@Dao
interface PuzzleInventoryDao {
    @Query(
        """
        SELECT COUNT(*) FROM puzzle_inventory
        WHERE difficulty = :difficulty AND generatorVersion = :generatorVersion
        """
    )
    suspend fun count(
        difficulty: Difficulty,
        generatorVersion: Int,
    ): Int

    @Query(
        """
        SELECT * FROM puzzle_inventory
        WHERE difficulty = :difficulty AND generatorVersion = :generatorVersion
        ORDER BY createdAt ASC, id ASC
        LIMIT 1
        """
    )
    suspend fun findOldest(
        difficulty: Difficulty,
        generatorVersion: Int,
    ): PuzzleInventoryEntity?

    @Query("DELETE FROM puzzle_inventory WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(puzzle: PuzzleInventoryEntity): Long

    @Query("DELETE FROM puzzle_inventory WHERE generatorVersion != :generatorVersion")
    suspend fun deleteStale(generatorVersion: Int)

    @Transaction
    suspend fun takeOldest(
        difficulty: Difficulty,
        generatorVersion: Int,
    ): PuzzleInventoryEntity? {
        val puzzle = findOldest(difficulty, generatorVersion) ?: return null
        return puzzle.takeIf { deleteById(puzzle.id) == 1 }
    }

    @Transaction
    suspend fun insertIfBelowLimit(
        puzzle: PuzzleInventoryEntity,
        maximumStock: Int,
    ): Boolean {
        require(maximumStock > 0)
        if (count(puzzle.difficulty, puzzle.generatorVersion) >= maximumStock) return false
        return insert(puzzle) != INSERT_CONFLICT
    }

    private companion object {
        const val INSERT_CONFLICT = -1L
    }
}
