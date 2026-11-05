package com.metinkale.prayer

import android.app.Application

object CrashReporter {
    enum class Severity {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    fun initializeApp(ctx: Application) {
        // No crash reporting in the F-Droid flavor.
    }

    fun setUserId(id: String) {
        // No crash reporting in the F-Droid flavor.
    }

    @JvmStatic
    fun recordException(e: Throwable) {
        e.printStackTrace()
    }

    @JvmStatic
    fun recordException(
        e: Throwable,
        vararg fingerprint: String,
        severity: Severity = Severity.WARNING
    ) {
        e.printStackTrace()
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        // No crash reporting in the F-Droid flavor.
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        // No crash reporting in the F-Droid flavor.
    }
}
