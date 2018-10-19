/*
 * Copyright (c) 2013-2017 Metin Kale
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

import com.crashlytics.android.Crashlytics;
import com.google.android.play.core.splitcompat.SplitCompat;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.utils.AndroidTimeZoneProvider;
import com.metinkale.prayer.utils.TimeZoneChangedReceiver;
import com.metinkale.prayer.utils.Utils;
import com.squareup.leakcanary.LeakCanary;

import org.joda.time.DateTimeZone;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import io.fabric.sdk.android.Fabric;


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
                    Prefs.setShowOngoingNumber(false);
                    //Toast.makeText(App.get(), "Crash detected. Show ongoing number disabled...", Toast.LENGTH_LONG).show();
                    Crashlytics.setBool("WORKAROUND#1", true);
                    Crashlytics.logException(ex);
                    return;
                }
            }
            // This will make Crashlytics do its job
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };
    private Locale mSystemLocale;
    
    
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
        InternalBroadcast.with(this).registerClass("com.metinkale.prayer.times.InternalBroadcastReceiver")
                .registerClass("com.metinkale.prayerapp.vakit.InternalBroadcastReceiver")
                .registerClass("com.metinkale.prayer.InternalBroadcastReceiver");
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        
        Fabric.with(this, new Crashlytics());
        Crashlytics.setUserIdentifier(Prefs.getUUID());
        if (BuildConfig.DEBUG)
            Crashlytics.setBool("isDebug", true);
        
        
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);
        
        
        mSystemLocale = Locale.getDefault();
        Utils.init(getBaseContext());
        
        
        DateTimeZone.setProvider(new AndroidTimeZoneProvider());
        registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
        
        
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


        /*if (AppRatingDialog.getInstallationTime() == 0) {
            AppRatingDialog.setInstalltionTime(System.currentTimeMillis());
        }*/
        
        InternalBroadcast.with(this).sendOnStart();
        
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        MultiDex.install(this);
        SplitCompat.install(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        InternalBroadcast.with(this).sendOnPrefsChanged(key);
        switch (key) {
            case "calendarIntegration":
                MainIntentService.startCalendarIntegration(App.get());
                break;
            case "language":
                Utils.init(getBaseContext());
            
        }
        
        
    }
    
    public Locale getSystemLocale() {
        return mSystemLocale;
    }
    
    public static String getUserAgent() {
        return "Android/Prayer-Times com.metinkale.prayer (contact: metinkale38@gmail.com)";
    }
    
    
}
