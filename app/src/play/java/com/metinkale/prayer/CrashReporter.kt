package com.metinkale.prayer

import android.app.Application
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User
import kotlinx.coroutines.CancellationException

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
        if (!shouldReport(e)) return
        Sentry.captureException(e)
    }

    @JvmStatic
    fun recordException(
        e: Throwable,
        vararg fingerprint: String,
        severity: Severity = Severity.WARNING
    ) {
        e.printStackTrace()
        if (!shouldReport(e)) return
        Sentry.captureException(e) { scope ->
            scope.level = SentryLevel.valueOf(severity.name)
            scope.fingerprint = listOf(*fingerprint)
        }
    }

    private fun shouldReport(e: Throwable): Boolean {
        if (e is CancellationException || e.cause is CancellationException) return false
        val name = e.javaClass.name
        if (name == "kotlinx.coroutines.JobCancellationException") return false
        // Expected connectivity/transport failures. These are handled by retry/fallback logic
        // and would otherwise dominate the crash dashboard.
        return !(name.startsWith("java.net.") ||
                name.startsWith("java.nio.channels.") ||
                name.startsWith("javax.net.ssl.") ||
                name.startsWith("java.security.cert.") ||
                name.startsWith("io.ktor."))
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
