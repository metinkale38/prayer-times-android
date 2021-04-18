/*
 * Copyright (c) 2013-2019 Metin Kale
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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.receiver.TimeTickReceiver;
import com.metinkale.prayer.receiver.TimeZoneChangedReceiver;
import com.metinkale.prayer.service.CalendarIntegrationService;
import com.metinkale.prayer.utils.AndroidTimeZoneProvider;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;

import org.joda.time.DateTimeZone;

import java.util.Locale;


public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String API_URL = "http://metinkale38.github.io/prayer-times-android";
    private static App sApp;
    @NonNull
    private Handler mHandler = new Handler();

    private Thread.UncaughtExceptionHandler mDefaultUEH;
    @NonNull
    private Thread.UncaughtExceptionHandler mCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, @NonNull Throwable ex) {
            //AppRatingDialog.setInstalltionTime(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ex.getClass().getName().contains("RemoteServiceException")) {
                if (ex.getMessage().contains("Couldn't update icon")) {
                    Preferences.SHOW_ONGOING_NUMBER.set(false);
                    //Toast.makeText(App.get(), "Crash detected. Show ongoing number disabled...", Toast.LENGTH_LONG).show();
                    FirebaseCrashlytics.getInstance().setCustomKey("WORKAROUND#1", true);
                    FirebaseCrashlytics.getInstance().recordException(ex);
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
        //only checks for connection, not for actual internet connection
        //everything else need (or should be in) a seperate thread
        ConnectivityManager cm = (ConnectivityManager) get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    public static void setApp(App app) {
        sApp = app;
    }

    @NonNull
    public Handler getHandler() {
        return mHandler;
    }

    public App() {
        super();
        sApp = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setUserId(Preferences.UUID.get());
        if (BuildConfig.DEBUG)
            FirebaseCrashlytics.getInstance().setCustomKey("isDebug", true);


        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);


        DateTimeZone.setProvider(new AndroidTimeZoneProvider());
        LocaleUtils.init(getBaseContext());


        registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));


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
        if (key == null)
            return;

        InternalBroadcastReceiver.sender(this).sendOnPrefsChanged(key);
        switch (key) {
            case "calendarIntegration":
                CalendarIntegrationService.startCalendarIntegration(App.get());
                break;
            case "language":
                LocaleUtils.init(getBaseContext());

        }


    }

    public static String getUserAgent() {
        return String.format(Locale.ENGLISH, "Android/%d prayer-times-android/%d (%s) metinkale38 at gmail dot com)", Build.VERSION.SDK_INT,
                Utils.getVersionCode(), Utils.getVersionName());
    }


}
