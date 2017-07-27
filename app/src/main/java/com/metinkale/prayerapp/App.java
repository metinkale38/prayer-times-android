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

package com.metinkale.prayerapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.AndroidTimeZoneProvider;
import com.metinkale.prayerapp.utils.AppRatingDialog;
import com.metinkale.prayerapp.utils.TimeZoneChangedReceiver;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.fragments.VakitFragment;
import com.metinkale.prayerapp.vakit.WidgetService;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;
import com.squareup.leakcanary.LeakCanary;

import org.joda.time.DateTimeZone;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;


public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String API_URL = "http://metinkale38.github.io/prayer-times-android/files";
    private static App sApp;
    @NonNull
    private Handler mHandler = new Handler();

    private Thread.UncaughtExceptionHandler mDefaultUEH;
    @NonNull
    private Thread.UncaughtExceptionHandler mCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, @NonNull Throwable ex) {
            AppRatingDialog.setInstalltionTime(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ex.getClass().getName().contains("RemoteServiceException")) {
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
        ConnectivityManager cm =
                (ConnectivityManager) get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static void setExact(@NonNull AlarmManager am, int type, long time, PendingIntent service) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
                && type == AlarmManager.RTC_WAKEUP && Prefs.useAlarm()) {
            AlarmManager.AlarmClockInfo info =
                    new AlarmManager.AlarmClockInfo(time,
                            PendingIntent.getActivity(App.get(), 0, new Intent(App.get(), VakitFragment.class), PendingIntent.FLAG_UPDATE_CURRENT));
            am.setAlarmClock(info, service);
        } else if (type == AlarmManager.RTC_WAKEUP && Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(type, time, service);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(type, time, service);
        } else {
            am.set(type, time, service);
        }

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
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mSystemLocale = Locale.getDefault();
        LeakCanary.install(this);

        Fabric.with(this, new Crashlytics());
        Crashlytics.setUserIdentifier(Prefs.getUUID());
        if (BuildConfig.DEBUG)
            Crashlytics.setBool("isDebug", true);


        JobManager.create(this).addJobCreator(new MyJobCreator());

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);

        DateTimeZone.setProvider(new AndroidTimeZoneProvider());
        registerReceiver(new TimeZoneChangedReceiver(), new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));

        try {
            Times.getTimes();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        Utils.init(this);

        WidgetService.start(this);
        Times.setAlarms();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


        if ("longcheer".equalsIgnoreCase(Build.BRAND)
                || "longcheer".equalsIgnoreCase(Build.MANUFACTURER)
                || "general mobile".equalsIgnoreCase(Build.BRAND)
                || "general mobile".equalsIgnoreCase(Build.MANUFACTURER)
                || "general_mobile".equalsIgnoreCase(Build.BRAND)
                || "general_mobile".equalsIgnoreCase(Build.MANUFACTURER)) {
            new ANRWatchDog().setANRListener(new ANRWatchDog.ANRListener() {
                @Override
                public void onAppNotResponding(ANRError error) {
                    Crashlytics.logException(error);
                }
            }).start();
        }

        if (AppRatingDialog.getInstallationTime() == 0) {
            AppRatingDialog.setInstalltionTime(System.currentTimeMillis());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "calendarIntegration":
                MainIntentService.startCalendarIntegration(App.get());
                break;
            case "useAlarm":
                Times.setAlarms();
                break;
            case "use12h":
            case "ongoingIcon":
            case "ongoingNumber":
            case "showAltWidgetHightlight":
            case "widget_countdown":
            case "alternativeOngoing":
            case "showLegacyWidgets":
                WidgetService.start(this);
                break;
        }


    }

    public static final class NotIds {
        public static final int ALARM = 1;
        public static final int ONGOING = 2;
    }

    private static class MyJobCreator implements JobCreator {
        @Nullable
        @Override
        public Job create(@NonNull String tag) {
            try {
                if (tag.startsWith(WebTimes.SyncJob.TAG)) {
                    Times t = Times.getTimes(Long.parseLong(tag.substring(WebTimes.SyncJob.TAG.length())));
                    if (t instanceof WebTimes)
                        return ((WebTimes) t).new SyncJob();
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            return null;
        }
    }

    public Locale getSystemLocale() {
        return mSystemLocale;
    }
}
