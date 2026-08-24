package com.galaxyrio.sudokusolver.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GameEntity::class,
        PuzzleInventoryEntity::class,
        GameStatisticsEntity::class,
        StatisticsMetadataEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun puzzleInventoryDao(): PuzzleInventoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku_database",
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                    )
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS games_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        difficulty TEXT NOT NULL,
                        sudoku TEXT NOT NULL,
                        timeSpent INTEGER NOT NULL,
                        lastPlayed INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO games_new (difficulty, sudoku, timeSpent, lastPlayed)
                    SELECT difficulty, sudoku, 0, lastPlayed FROM games
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE games")
                db.execSQL("ALTER TABLE games_new RENAME TO games")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS puzzle_inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        difficulty TEXT NOT NULL,
                        sudoku TEXT NOT NULL,
                        fingerprint TEXT NOT NULL,
                        generatorVersion INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_puzzle_inventory_difficulty_generatorVersion
                    ON puzzle_inventory (difficulty, generatorVersion)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_puzzle_inventory_fingerprint
                    ON puzzle_inventory (fingerprint)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN solution TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN advancedNotes TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE games ADD COLUMN statisticsGeneration " +
                        "INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_statistics (
                        difficulty TEXT NOT NULL PRIMARY KEY,
                        totalPlayTimeSeconds INTEGER NOT NULL,
                        gamesStarted INTEGER NOT NULL,
                        gamesCompleted INTEGER NOT NULL,
                        totalCompletionTimeSeconds INTEGER NOT NULL,
                        bestCompletionTimeSeconds INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS statistics_metadata (
                        id INTEGER NOT NULL PRIMARY KEY,
                        generation INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO statistics_metadata (id, generation) VALUES (0, 0)"
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO game_statistics (
                        difficulty,
                        totalPlayTimeSeconds,
                        gamesStarted,
                        gamesCompleted,
                        totalCompletionTimeSeconds,
                        bestCompletionTimeSeconds
                    )
                    SELECT
                        difficulty,
                        COALESCE(SUM(timeSpent), 0),
                        COUNT(*),
                        SUM(CASE WHEN sudoku NOT LIKE '%0|%' THEN 1 ELSE 0 END),
                        COALESCE(SUM(
                            CASE WHEN sudoku NOT LIKE '%0|%' THEN timeSpent ELSE 0 END
                        ), 0),
                        MIN(CASE WHEN sudoku NOT LIKE '%0|%' THEN timeSpent ELSE NULL END)
                    FROM games
                    GROUP BY difficulty
                    """.trimIndent()
                )
            }
        }
    }
}
