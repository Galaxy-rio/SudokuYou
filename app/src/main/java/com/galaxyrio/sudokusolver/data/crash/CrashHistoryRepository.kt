package com.galaxyrio.sudokusolver.data.crash

/** Synchronous API so crash-path writes never depend on a coroutine dispatcher. */
interface CrashHistoryRepository {
    /** Persists [record], returning false when the best-effort write cannot be completed. */
    fun record(record: CrashRecord): Boolean

    /** Returns all readable records, newest first. Corrupt and temporary files are ignored. */
    fun list(): List<CrashRecord>

    /** Returns one readable record by its stable id, or null if it is absent or corrupt. */
    fun get(id: String): CrashRecord?

    /** Deletes the record with [id], returning whether a stored record was removed. */
    fun delete(id: String): Boolean

    /** Removes all crash-history files, including corrupt records and abandoned temp files. */
    fun clear(): Boolean

    /** Applies the repository's age and count retention limits and returns the deletion count. */
    fun prune(): Int
}
