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

package com.metinkale.prayer;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.receiver.TimeTickReceiver;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;


import java.util.Locale;


public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String API_URL = "http://metinkale38.github.io/prayer-times-android";
    private static App sApp;


    private Thread.UncaughtExceptionHandler mDefaultUEH;
    @NonNull
    private final Thread.UncaughtExceptionHandler mCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
            //AppRatingDialog.setInstalltionTime(0);
            if (ex.getClass().getName().contains("RemoteServiceException")) {
                if (ex.getMessage().contains("Couldn't update icon")) {
                    Preferences.SHOW_ONGOING_NUMBER.set(false);
                    //Toast.makeText(App.get(), "Crash detected. Show ongoing number disabled...", Toast.LENGTH_LONG).show();
                    CrashReporter.setCustomKey("WORKAROUND#1", true);
                    CrashReporter.recordException(ex);
                    return;
                }
            }
            // This will make Crashlytics do its job
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };


    @NonNull
    public static App get() {
        return sApp;
    }

    public static boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }


    public static void setApp(App app) {
        sApp = app;
    }

    public App() {
        super();
        sApp = this;

    }


    @Override
    public void onCreate() {
        super.onCreate();
        CrashReporter.initializeApp(this);
        CrashReporter.setUserId(Preferences.UUID.get());
        if (BuildConfig.DEBUG) CrashReporter.setCustomKey("isDebug", true);


        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);

        LocaleUtils.init(getBaseContext());


        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


        /*if (AppRatingDialog.getInstallationTime() == 0) {
            AppRatingDialog.setInstalltionTime(System.currentTimeMillis());
        }*/

        InternalBroadcastReceiver.loadAll();
        InternalBroadcastReceiver.sender(this).sendOnStart();
        TimeTickReceiver.start(this);

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) return;

        InternalBroadcastReceiver.sender(this).sendOnPrefsChanged(key);
        if ("language".equals(key)) {
            LocaleUtils.init(getBaseContext());
        }


    }

    public static String getUserAgent() {
        return String.format(Locale.ENGLISH, "Android/%d prayer-times-android/%d (%s) metinkale38 at gmail dot com)", Build.VERSION.SDK_INT, Utils.getVersionCode(), Utils.getVersionName());
    }


}
