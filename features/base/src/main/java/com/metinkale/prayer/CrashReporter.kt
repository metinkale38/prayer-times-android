package com.metinkale.prayer

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashReporter {

    @JvmStatic
    fun initializeApp(ctx: Application) {
        FirebaseApp.initializeApp(ctx)
    }

    @JvmStatic
    fun setUserId(id: String) {
        FirebaseCrashlytics.getInstance().setUserId(id)
    }

    @JvmStatic
    fun recordException(e: Throwable) {
        e.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Boolean) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Double) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Float) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: Int) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    @JvmStatic
    fun setCustomKey(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

}