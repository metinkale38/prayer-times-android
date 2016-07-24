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

package com.metinkale.prayerapp.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.vakit.WidgetService;

/**
 * Created by Metin on 14.06.2015.
 */
public class Prefs implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final Object LOCK = new Object();
    private static Prefs ourInstance;
    private final String mLanguage;
    private final String mDF;
    private final String mHDF;
    private final boolean mUseArabic;
    private final boolean mUse12H;
    private final int mlastCalSync;
    private final String mCalendarIntegration;
    private final boolean mWidgetMinute;
    private final int mHijriFix;
    private final int mChangelogVersion;
    private final int mTesbihatTextSize;
    private final float mCompassLat;
    private final float mCompassLng;
    private final int mKerahatSunrise;
    private final int mKerahatIstiwa;
    private final int mKerahatSunset;
    private final boolean mMigratedFromSqlite;
    private final boolean mAlternativeOngoing;
    private SharedPreferences.Editor mEditor;

    private Prefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        mEditor = prefs.edit();

        mLanguage = prefs.getString("language", null);

        mDF = prefs.getString("dateformat", "DD MMM YYYY");
        mHDF = prefs.getString("hdateformat", "DD MMM YYYY");
        mUseArabic = prefs.getBoolean("arabicNames", false);
        mUse12H = prefs.getBoolean("use12h", false);
        mlastCalSync = prefs.getInt("lastCalSync", 0);
        mWidgetMinute = "default".equals(prefs.getString("widget_countdown", "default"));
        mHijriFix = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("hijri_fix", "0").replace("+", ""));
        mChangelogVersion = prefs.getInt("changelog_version", -1);
        mTesbihatTextSize = prefs.getInt("tesbihatTextSize", 0);
        mCompassLat = prefs.getFloat("compassLat", 0);
        mCompassLng = prefs.getFloat("compassLong", 0);
        mAlternativeOngoing = prefs.getBoolean("alternativeOngoing", false);
        mKerahatSunrise = prefs.getInt("kerahat_sunrise", 45);
        mKerahatIstiwa = prefs.getInt("kerahat_istiva", 45);
        mKerahatSunset = prefs.getInt("kerahat_sunset", 45);

        mMigratedFromSqlite = prefs.getBoolean("migratedFromSqlite", false);


        String calIntegration = "-1";
        try {
            calIntegration = prefs.getString("calendarIntegration", "-1");
            "-1".equals(calIntegration);
        } catch (Exception e) {
            Crashlytics.logException(e);

            //catch Exception if calendarIntegration is an INT
            mEditor.remove("calendarIntegration").apply();//revert
            mEditor = prefs.edit();

            calIntegration = "-1";
        }
        mCalendarIntegration = calIntegration;
    }

    private static Prefs get() {
        synchronized (LOCK) {
            if (ourInstance == null) {
                ourInstance = new Prefs();
            }
        }
        return ourInstance;
    }

    public static String getLanguage() {
        return get().mLanguage;
    }

    public static void setLanguage(String language) {
        get().mEditor.putString("language", language);
        commit();
    }

    public static String getDF() {
        return get().mDF;
    }

    public static String getHDF() {
        return get().mHDF;
    }

    public static boolean useArabic() {
        return get().mUseArabic;
    }

    public static boolean use12H() {
        return get().mUse12H;
    }

    public static int getLastCalSync() {
        return get().mlastCalSync;
    }

    public static String getCalendar() {
        return get().mCalendarIntegration;
    }

    public static boolean getAlternativeOngoing() {
        return get().mAlternativeOngoing;
    }

    public static void setCalendar(String cal) {
        get().mEditor.putString("calendarIntegration", cal);
        commit();
    }

    public static boolean isDefaultWidgetMinuteType() {
        return get().mWidgetMinute;
    }

    public static int getHijriFix() {
        return get().mHijriFix;
    }

    public static int getChangelogVersion() {
        return get().mChangelogVersion;
    }

    public static void setChangelogVersion(int clv) {
        get().mEditor.putInt("changelog_version", clv);
        commit();
    }

    public static float getCompassLng() {
        return get().mCompassLng;
    }

    public static float getCompassLat() {
        return get().mCompassLat;
    }

    public static int getTesbihatTextSize() {
        return get().mTesbihatTextSize;
    }

    public static void setTesbihatTextSize(int size) {
        get().mEditor.putInt("tesbihatTextSize", size);
        commit();
    }

    public static float getKerahatSunrise() {
        return get().mKerahatSunrise;
    }

    public static float getKerahatIstiwa() {
        return get().mKerahatIstiwa;
    }

    public static int getKerahatSunet() {
        return get().mKerahatSunset;
    }

    public static boolean getMigratedFromSqlite() {
        return get().mMigratedFromSqlite;
    }

    public static void setMigratedFromSqlite(boolean b) {
        get().mEditor.putBoolean("migratedFromSqlite", b);
        commit();
    }

    public static void setLastCalSync(int date) {
        get().mEditor.putInt("lastCalSync", date);
        commit();
    }

    private static void commit() {
        get().mEditor.commit();
        ourInstance = null;
    }

    public static void reset() {
        ourInstance = null;
    }

    public static void setCompassPos(float lat, float lon) {
        get().mEditor.putFloat("compassLat", lat).putFloat("compassLong", lon);
        commit();
    }


    public static void setAlternativeOngoing(boolean Alternative) {
        get().mEditor.putBoolean("alternativeOngoing", Alternative);
        commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ourInstance = null;

        if ("calendarIntegration".equals(key)) {
            MainIntentService.startCalendarIntegration(App.getContext());
        } else if ("ongoingIcon".equals(key)) {
            WidgetService.updateOngoing();
        } else if ("alternativeOngoing".equals(key)) {
            WidgetService.updateOngoing();
        }
    }
}
