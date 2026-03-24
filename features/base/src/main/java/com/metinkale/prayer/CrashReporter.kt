package com.metinkale.prayer

import android.app.Application
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User

object CrashReporter {
    enum class Severity {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    fun initializeApp(ctx: Application) {
        // SentryAndroid.init(ctx)
        // Sentry uses auto-init
    }

    fun setUserId(id: String) {
        val user = User()
        user.id = id
        Sentry.setUser(user)
    }

    @JvmStatic
    fun recordException(
        e: Throwable
    ) {
        e.printStackTrace()
        Sentry.captureException(e)
    }

    @JvmStatic
    fun recordException(
        e: Throwable,
        vararg fingerprint: String,
        severity: Severity = Severity.WARNING
    ) {
        e.printStackTrace()
        Sentry.captureException(e) { scope ->
            scope.level = SentryLevel.valueOf(severity.name)
            scope.fingerprint = listOf(*fingerprint)
        }
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        Sentry.setTag(key, value)
    }


    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        Sentry.setTag(key, if (value) "true" else "false")
    }
}