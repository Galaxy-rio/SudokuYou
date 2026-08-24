package com.galaxyrio.sudokusolver.data.crash

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Stores crash reports only in the application's no-backup directory. */
class FileCrashHistoryRepository(
    context: Context,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : CrashHistoryRepository {
    private val directory = File(context.noBackupFilesDir, DIRECTORY_NAME)

    init {
        // Construction is the startup retention pass. Failure is intentionally non-fatal.
        runCatching { prune() }
    }

    override fun record(record: CrashRecord): Boolean = synchronized(FILE_LOCK) {
        if (!ensureDirectory()) return@synchronized false

        val destination = fileFor(record.id)
        val temporary = runCatching {
            File.createTempFile("crash-${record.id}.", TEMP_SUFFIX, directory)
        }.getOrNull() ?: return@synchronized false

        val encoded = runCatching { JSON.encodeToString(record) }
            .getOrElse {
                temporary.delete()
                return@synchronized false
            }

        val written = runCatching {
            FileOutputStream(temporary, false).use { output ->
                output.write(encoded.toByteArray(StandardCharsets.UTF_8))
                output.flush()
                output.fd.sync()
            }
            try {
                Files.move(
                    temporary.toPath(),
                    destination.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temporary.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
            true
        }.getOrElse { false }

        if (!written) {
            temporary.delete()
            return@synchronized false
        }

        true
    }

    override fun list(): List<CrashRecord> = synchronized(FILE_LOCK) {
        listLocked()
    }

    override fun get(id: String): CrashRecord? = synchronized(FILE_LOCK) {
        if (!CrashRecord.isValidId(id)) return@synchronized null
        readRecord(fileFor(id))?.takeIf { it.id == id }
    }

    override fun delete(id: String): Boolean = synchronized(FILE_LOCK) {
        if (!CrashRecord.isValidId(id)) return@synchronized false
        val file = fileFor(id)
        file.isFile && file.delete()
    }

    override fun clear(): Boolean = synchronized(FILE_LOCK) {
        val files = directory.listFiles() ?: return@synchronized !directory.exists()
        var success = true
        files.filter(File::isFile).forEach { file ->
            if (!file.delete()) success = false
        }
        success
    }

    override fun prune(): Int = synchronized(FILE_LOCK) {
        pruneLocked()
    }

    private fun pruneLocked(): Int {
        var deletedCount = directory.listFiles()
            ?.asSequence()
            ?.filter(File::isFile)
            ?.filter { it.name.endsWith(TEMP_SUFFIX) }
            ?.count { it.delete() }
            ?: 0
        val cutoff = nowMillis() - MAX_AGE_MILLIS
        val records = buildList {
            historyFiles().forEach { file ->
                val record = readRecord(file)
                if (record == null || file.name != "${record.id}$RECORD_SUFFIX") {
                    if (file.delete()) deletedCount++
                } else {
                    add(StoredRecord(file, record))
                }
            }
        }
            .sortedWith(
                compareByDescending<StoredRecord> { it.record.timestampEpochMillis }
                    .thenByDescending { it.record.id },
            )

        val retained = records.filter { it.record.timestampEpochMillis >= cutoff }
        val toDelete = buildList {
            addAll(records.filter { it.record.timestampEpochMillis < cutoff })
            addAll(retained.drop(MAX_RECORDS))
        }

        deletedCount += toDelete.count { it.file.delete() }
        return deletedCount
    }

    private fun listLocked(): List<CrashRecord> = readableFilesLocked()
        .map(StoredRecord::record)
        .sortedWith(
            compareByDescending<CrashRecord> { it.timestampEpochMillis }
                .thenByDescending { it.id },
        )

    private fun readableFilesLocked(): List<StoredRecord> = historyFiles()
        .mapNotNull { file ->
            val record = readRecord(file) ?: return@mapNotNull null
            if (file.name != "${record.id}$RECORD_SUFFIX") return@mapNotNull null
            StoredRecord(file, record)
        }

    private fun historyFiles(): List<File> = directory.listFiles()
        ?.asSequence()
        ?.filter(File::isFile)
        ?.filter { it.name.endsWith(RECORD_SUFFIX) && !it.name.endsWith(TEMP_SUFFIX) }
        ?.toList()
        .orEmpty()

    private fun readRecord(file: File): CrashRecord? {
        if (!file.isFile || !file.name.endsWith(RECORD_SUFFIX)) return null
        return runCatching {
            JSON.decodeFromString<CrashRecord>(file.readText(StandardCharsets.UTF_8))
        }.getOrNull()
    }

    private fun ensureDirectory(): Boolean = when {
        directory.isDirectory -> true
        directory.exists() -> false
        else -> directory.mkdirs() || directory.isDirectory
    }

    private fun fileFor(id: String): File = File(directory, "$id$RECORD_SUFFIX")

    private data class StoredRecord(
        val file: File,
        val record: CrashRecord,
    )

    companion object {
        const val MAX_RECORDS: Int = 20
        const val MAX_AGE_DAYS: Long = 30

        private const val DIRECTORY_NAME = "crash_history"
        private const val RECORD_SUFFIX = ".json"
        private const val TEMP_SUFFIX = ".tmp"
        private val FILE_LOCK = Any()
        private val MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(MAX_AGE_DAYS)
        private val JSON = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
