// Copyright © 2024 Brent Tunnicliff <brent@tunnicliff.dev>

package dev.tunnicliff.dadjokes.data

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.tunnicliff.logging.logger.LogUploadHandler
import dev.tunnicliff.logging.model.LogLevel

class DefaultLogUploadHandler(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : LogUploadHandler {
    private companion object {
        const val MESSAGE_PARAM = "message"
        const val THROWABLE_PARAM = "throwable"
    }

    override suspend fun uploadLog(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?
    ): Boolean {
        crashlytics.log("[$tag][$level] $message")
        throwable?.let {
            crashlytics.recordException(it)
        }

        analytics.logEvent("log-$tag-$level") {
            param(MESSAGE_PARAM, message)
            throwable?.let {
                param(THROWABLE_PARAM, it.toString())
            }
        }

        return true
    }
}