package com.galaxyrio.sudokusolver.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameEntity::class, PuzzleInventoryEntity::class],
    version = 5,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    }
}
