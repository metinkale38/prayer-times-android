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
 *
 */

package com.metinkale.prayerapp.utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.about.AboutAct;
import com.metinkale.prayerapp.vakit.WidgetProvider;
import com.metinkale.prayerapp.vakit.WidgetProviderClock;
import com.metinkale.prayerapp.vakit.WidgetProviderClock2;
import com.metinkale.prayerapp.vakit.WidgetProviderLong;
import com.metinkale.prayerapp.vakit.WidgetProviderSilenter;
import com.metinkale.prayerapp.vakit.WidgetProviderSmall;
import com.metinkale.prayerapp.vakit.times.Times;

import java.util.List;
import java.util.Set;

/**
 * Created by metin on 21.02.2017.
 */

public class AppRatingDialog {
    private static boolean APP_STARTED = false;

    private static SharedPreferences getPrefs() {
        return App.get().getSharedPreferences("AppRatings", 0);
    }


    public static long getInstallationTime() {
        return getPrefs().getLong("installation", 0);
    }

    public static void setInstalltionTime(long mills) {
        if (mills == 0) mills = System.currentTimeMillis();
        getPrefs().edit().putLong("installation", mills).apply();
    }

    private static long getAppStarts() {
        return getPrefs().getInt("appStarts", 0);
    }

    public static void increaseAppStarts() {
        if (APP_STARTED) return;
        APP_STARTED = true;
        getPrefs().edit().putLong("installation", getAppStarts() + 1).apply();
    }

    private static Set<String> getOpenedMenus() {
        return getPrefs().getStringSet("openedMenus", new ArraySet<String>());
    }


    public static void addToOpenedMenus(String menu) {
        ArraySet<String> set = new ArraySet<String>();
        set.addAll(getPrefs().getStringSet("openedMenus", new ArraySet<String>()));
        if (set.contains(menu)) return;
        set.add(menu);

        getPrefs().edit().putStringSet("openedMenus", set).apply();
    }


    public static boolean showDialog(final Activity act, long duration) {
        if (!hasPlayStore()) return false;
        if (duration < 5000) return false;
        if (!App.isOnline()) return false;
        long installTime = System.currentTimeMillis() - getInstallationTime();
        if (installTime < 1000 * 60 * 60 * 24 * 7) return false;
        if (getAppStarts() < 7) return false;
        int days = 30;

        days -= getOpenedMenus().size();

        List<Times> times = Times.getTimes();
        if (times == null || times.isEmpty()) return false;
        for (Times t : times) {
            if (t.getTime(3).equals("00:00")) return false;
        }
        days -= Math.min(2 * times.size(), 5);


        days -= Math.min(5, 2 * getAppWidgetCount());
        if (hasWifi()) days -= 3;
        if (duration > 60000) days -= 3;

        if (installTime < 1000 * 60 * 60 * days) return false;
        showDialog(act);
        return true;
    }

    private static void showDialog(final Activity act) {
        new AlertDialog.Builder(act)
                .setTitle(R.string.ratingFirstDlgTitle)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        act.finish();
                        Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                .putCustomAttribute("Like this App?", "cancel"));
                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new AlertDialog.Builder(act)
                        .setTitle(R.string.ratingPositiveTitle)
                        .setMessage(R.string.ratingPositiveText)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                setInstalltionTime(0);
                                act.finish();
                                Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                        .putCustomAttribute("Like this App?", "yes")
                                        .putCustomAttribute("Rate this App?", "cancel"));
                            }
                        }).setPositiveButton(R.string.totheplaystore, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("market://details?id=" + act.getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            act.startActivity(goToMarket);
                            setInstalltionTime(Long.MAX_VALUE);
                            Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                    .putCustomAttribute("Like this App?", "yes")
                                    .putCustomAttribute("Rate this App?", "yes"));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(act, "Couldn't launch the market", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.notnow, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setInstalltionTime(0);
                        act.finish();
                        Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                .putCustomAttribute("Like this App?", "yes")
                                .putCustomAttribute("Rate this App?", "no"));
                    }
                }).show();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new AlertDialog.Builder(act)
                        .setTitle(R.string.ratingNegativeTitle)
                        .setMessage(R.string.ratingNegativeText)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                setInstalltionTime(0);
                                act.finish();
                                Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                        .putCustomAttribute("Like this App?", "no")
                                        .putCustomAttribute("Write Feedback?", "cancel"));
                            }
                        }).setPositiveButton(R.string.sendMail, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AboutAct.sendMail(act);
                        setInstalltionTime(Long.MAX_VALUE);
                        act.finish();
                        Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                .putCustomAttribute("Like this App?", "no")
                                .putCustomAttribute("Write Feedback?", "yes"));
                    }
                }).setNegativeButton(R.string.notnow, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setInstalltionTime(0);
                        act.finish();
                        Answers.getInstance().logCustom(new CustomEvent("RatingDialog")
                                .putCustomAttribute("Like this App?", "no")
                                .putCustomAttribute("Write Feedback?", "no"));
                    }
                }).show();
            }
        }).show();
    }

    private static boolean hasWifi() {
        ConnectivityManager connManager = (ConnectivityManager) App.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    private static int getAppWidgetCount() {
        Context c = App.get();
        AppWidgetManager manager = AppWidgetManager.getInstance(c);
        int i = 0;

        ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        thisWidget = new ComponentName(c, WidgetProviderSmall.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        thisWidget = new ComponentName(c, WidgetProviderLong.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        thisWidget = new ComponentName(c, WidgetProviderSilenter.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        thisWidget = new ComponentName(c, WidgetProviderClock.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        thisWidget = new ComponentName(c, WidgetProviderClock2.class);
        i += manager.getAppWidgetIds(thisWidget).length;

        return i;
    }


    private static boolean hasPlayStore() {
        PackageManager packageManager = App.get().getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.google.market") ||
                    packageInfo.packageName.equals("com.android.vending")) {
                return true;
            }
        }
        return false;
    }
}
