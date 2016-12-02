/*
 * Copyright (c) 2016 Metin Kale
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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.WidgetService;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.WebTimes;

import io.fabric.sdk.android.Fabric;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.TimeZone;


public class App extends MultiDexApplication implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String API_URL = "http://metinkale38.github.io/prayer-times-android/files";
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;
    private static Handler sHandler = new Handler();

    private static Thread.UncaughtExceptionHandler mDefaultUEH;
    private static Thread.UncaughtExceptionHandler mCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // Custom logic goes here
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ex.getClass().getName().contains("RemoteServiceException")) {
                if (ex.getMessage().contains("Couldn't update icon")) {
                    Prefs.setShowOngoingNumber(false);
                    Toast.makeText(App.getContext(), "Crash detected. Show ongoing number disabled...", Toast.LENGTH_LONG).show();
                    Crashlytics.setBool("WORKAROUND#1", true);
                }
            }
            // This will make Crashlytics do its job
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };


    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setExact(AlarmManager am, int type, long time, PendingIntent service) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
                && type == AlarmManager.RTC_WAKEUP && Prefs.useAlarm()) {
            AlarmManager.AlarmClockInfo info =
                    new AlarmManager.AlarmClockInfo(time,
                            PendingIntent.getActivity(App.getContext(), 0, new Intent(App.getContext(), Main.class), PendingIntent.FLAG_UPDATE_CURRENT));
            am.setAlarmClock(info, service);
        } else if (type == AlarmManager.RTC_WAKEUP && Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(type, time, service);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(type, time, service);
        } else {
            am.set(type, time, service);
        }

    }

    public static Handler getHandler() {
        return sHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;


        Fabric.with(this, new Crashlytics());
        Crashlytics.setString("uuid", Prefs.getUUID());
        if (BuildConfig.DEBUG)
            Crashlytics.setBool("isDebug", true);

        JobManager.create(this).addJobCreator(new MyJobCreator());

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);

        JodaTimeAndroid.init(this);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getDefault()));


        try {
            Times.getTimes();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        Utils.init(this);

        startService(new Intent(this, WidgetService.class));
        Times.setAlarms();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("calendarIntegration".equals(key)) {
            MainIntentService.startCalendarIntegration(App.getContext());
        } else if ("ongoingIcon".equals(key) || "ongoingNumber".equals(key)) {
            WidgetService.updateOngoing();
        } else if ("alternativeOngoing".equals(key)) {
            WidgetService.updateOngoing();
        } else if ("useAlarm".equals(key)) {
            Times.setAlarms();
        }
    }

    public static final class NotIds {
        public static final int ALARM = 1;
        public static final int ONGOING = 2;
    }

    private class MyJobCreator implements JobCreator {
        @Override
        public Job create(String tag) {
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
}
