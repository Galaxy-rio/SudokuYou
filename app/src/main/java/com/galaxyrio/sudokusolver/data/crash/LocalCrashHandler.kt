package com.galaxyrio.sudokusolver.data.crash

import android.content.Context
import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Records uncaught Java/Kotlin exceptions locally, then delegates to Android's prior handler.
 * No report is transmitted or handed to a network component.
 */
class LocalCrashHandler private constructor(
    private val applicationContext: Context,
    private val repository: CrashHistoryRepository,
    private val delegate: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val ownsCapture = captureInProgress.compareAndSet(false, true)
        try {
            if (ownsCapture) {
                runCatching {
                    repository.record(
                        CrashRecord.capture(
                            context = applicationContext,
                            thread = thread,
                            throwable = throwable,
                        ),
                    )
                }
            }
        } finally {
            try {
                val previousHandler = delegate
                if (previousHandler != null) {
                    previousHandler.uncaughtException(thread, throwable)
                } else {
                    Process.killProcess(Process.myPid())
                    exitProcess(10)
                }
            } finally {
                if (ownsCapture) captureInProgress.set(false)
            }
        }
    }

    companion object {
        private val installLock = Any()
        private val captureInProgress = AtomicBoolean(false)

        /**
         * Installs one process-wide handler. Repeated calls while it is active are no-ops.
         */
        @JvmStatic
        @JvmOverloads
        fun install(
            context: Context,
            repository: CrashHistoryRepository = FileCrashHistoryRepository(
                context.applicationContext,
            ),
        ): LocalCrashHandler = synchronized(installLock) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current is LocalCrashHandler) {
                return@synchronized current
            }

            val handler = LocalCrashHandler(
                applicationContext = context.applicationContext,
                repository = repository,
                delegate = current,
            )
            Thread.setDefaultUncaughtExceptionHandler(handler)
            handler
        }
    }
}
