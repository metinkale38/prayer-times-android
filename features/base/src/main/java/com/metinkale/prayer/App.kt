/*
 * Copyright (c) 2013-2023 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.metinkale.prayer

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.preference.PreferenceManager
import com.metinkale.prayer.CrashReporter.initializeApp
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.CrashReporter.setCustomKey
import com.metinkale.prayer.CrashReporter.setUserId
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.utils.LocaleUtils.init

open class App : Application(), OnSharedPreferenceChangeListener {
    private var mDefaultUEH: Thread.UncaughtExceptionHandler? = null
    private val mCaughtExceptionHandler =
        Thread.UncaughtExceptionHandler { thread, ex -> //AppRatingDialog.setInstalltionTime(0);
            if (ex.javaClass.name.contains("RemoteServiceException")) {
                if (ex.message!!.contains("Couldn't update icon")) {
                    Preferences.SHOW_ONGOING_NUMBER = false
                    //Toast.makeText(App.get(), "Crash detected. Show ongoing number disabled...", Toast.LENGTH_LONG).show();
                    setCustomKey("WORKAROUND#1", true)
                    recordException(ex)
                    return@UncaughtExceptionHandler
                }
            }
            // This will make Crashlytics do its job
            mDefaultUEH!!.uncaughtException(thread, ex)
        }

    init {
        INSTANCE = this
    }

    override fun onCreate() {
        super.onCreate()
        initializeApp(this)
        setUserId(Preferences.UUID)
        if (BuildConfig.DEBUG) setCustomKey("isDebug", true)
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler)
        init(baseContext)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)


        /*if (AppRatingDialog.getInstallationTime() == 0) {
            AppRatingDialog.setInstalltionTime(System.currentTimeMillis());
        }*/
    }



    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        key?.let { AppEventManager.sendOnPrefsChanged(key) }
        if ("language" == key) {
            init(baseContext)
        }
    }

    companion object {
        const val API_URL = "http://metinkale38.github.io/prayer-times-android"

        private lateinit var INSTANCE: App


        @JvmStatic
        fun get(): App {
            return INSTANCE
        }

        @JvmStatic
        fun isOnline(): Boolean {
            val connectivityManager =
                get().getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw)
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
        }

    }
}