package com.galaxyrio.sudokusolver.data.crash

import android.content.Context
import android.os.Build
import com.galaxyrio.sudokusolver.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.serialization.Serializable

/** A self-contained, local-only snapshot of an uncaught exception. */
@Serializable
data class CrashRecord(
    val id: String,
    val timestampEpochMillis: Long,
    val threadName: String,
    val exceptionType: String,
    val exceptionMessage: String?,
    val stackTrace: String,
    val appVersionName: String,
    val appVersionCode: Long,
    val androidApiLevel: Int,
    val androidRelease: String,
    val manufacturer: String,
    val model: String,
) {
    init {
        require(ID_PATTERN.matches(id)) { "Crash record id is not file-name safe" }
    }

    companion object {
        /** The maximum UTF-8 payload retained for [stackTrace]. */
        const val MAX_STACK_TRACE_BYTES: Int = 64 * 1024

        private val ID_PATTERN = Regex("[A-Za-z0-9_-]{1,128}")
        private const val MAX_EXCEPTION_MESSAGE_CHARS = 4 * 1024
        private const val TRUNCATION_MARKER = "\n\u2026 [stack trace truncated]"

        /**
         * Captures device and application metadata at the point of failure.
         *
         * The generated UUID remains stable for the lifetime of the persisted record and is
         * suitable for list keys, detail routes, and deletion.
         */
        fun capture(
            context: Context,
            thread: Thread,
            throwable: Throwable,
            timestampEpochMillis: Long = System.currentTimeMillis(),
            id: String = UUID.randomUUID().toString(),
        ): CrashRecord {
            val packageInfo = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()

            val renderedStackTrace = StringWriter().use { writer ->
                PrintWriter(writer).use { printWriter ->
                    throwable.printStackTrace(printWriter)
                }
                writer.toString()
            }

            return CrashRecord(
                id = id,
                timestampEpochMillis = timestampEpochMillis,
                threadName = thread.name,
                exceptionType = throwable.javaClass.name,
                exceptionMessage = throwable.message?.take(MAX_EXCEPTION_MESSAGE_CHARS),
                stackTrace = renderedStackTrace.truncateUtf8(MAX_STACK_TRACE_BYTES),
                appVersionName = packageInfo?.versionName ?: BuildConfig.VERSION_NAME,
                appVersionCode = packageInfo?.longVersionCode
                    ?: BuildConfig.VERSION_CODE.toLong(),
                androidApiLevel = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE.orEmpty(),
                manufacturer = Build.MANUFACTURER.orEmpty(),
                model = Build.MODEL.orEmpty(),
            )
        }

        internal fun isValidId(id: String): Boolean = ID_PATTERN.matches(id)

        private fun String.truncateUtf8(maxBytes: Int): String {
            val bytes = toByteArray(StandardCharsets.UTF_8)
            if (bytes.size <= maxBytes) return this

            val markerBytes = TRUNCATION_MARKER.toByteArray(StandardCharsets.UTF_8)
            var contentLength = (maxBytes - markerBytes.size).coerceAtLeast(0)

            // Do not split a multi-byte UTF-8 code point at the boundary.
            while (
                contentLength > 0 &&
                contentLength < bytes.size &&
                (bytes[contentLength].toInt() and 0xC0) == 0x80
            ) {
                contentLength--
            }

            return String(bytes, 0, contentLength, StandardCharsets.UTF_8) + TRUNCATION_MARKER
        }
    }
}
