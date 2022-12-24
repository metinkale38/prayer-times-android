package com.metinkale.prayer

import android.app.Application

object CrashReporter {
    @JvmStatic
    fun initializeApp(ctx: Application) {
    }

    @JvmStatic
    fun setUserId(id: String) {
    }

    @JvmStatic
    fun recordException(e: Throwable) {
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Any) {
    }
}