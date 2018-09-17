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

package com.metinkale.prayerapp.settings;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.Utils;

import java.util.Locale;
import java.util.UUID;

public class Prefs {


    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.get());
    }


    public static boolean useAlarm() {
        return getPrefs().getBoolean("useAlarm", false);
    }


    @NonNull
    public static String getLanguage() {
        return getPrefs().getString("language", "system");
    }


    public static void setLanguage(String language) {
        getPrefs().edit().putString("language", language).apply();
    }


    @Nullable
    public static String getDigits() {
        return getPrefs().getString("numbers", "normal");
    }

    @Nullable
    public static String getDF() {
        return getPrefs().getString("dateformat", "DD MMM YYYY");
    }

    @Nullable
    public static String getHDF() {
        return getPrefs().getString("hdateformat", "DD MMM YYYY");
    }


    @Nullable
    public static String getUUID() {
        String uuid = getPrefs().getString("uuid", null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            getPrefs().edit().putString("uuid", uuid).apply();
        }
        return uuid;
    }


    public static boolean useArabic() {
        return !new Locale("ar").getLanguage().equals(Utils.getLocale().getLanguage())
                && getPrefs().getBoolean("arabicNames", false);
    }

    public static boolean use12H() {
        return getPrefs().getBoolean("use12h", false);
    }

    public static int getLastCalSync() {
        return getPrefs().getInt("lastCalSync", 0);
    }

    public static void setLastCalSync(int date) {
        getPrefs().edit().putInt("lastCalSync", date).apply();
    }

    @Nullable
    public static String getCalendar() {
        return getPrefs().getString("calendarIntegration", "-1");
    }

    public static void setCalendar(String cal) {
        getPrefs().edit().putString("calendarIntegration", cal).apply();
    }


    public static boolean isDefaultWidgetMinuteType() {
        return !"alt".equals(getPrefs().getString("widget_countdown", "default"));
    }


    public static boolean showWidgetSeconds() {
        return "secs".equals(getPrefs().getString("widget_countdown", "default"));
    }

    public static boolean showLegacyWidget() {
        if (Build.VERSION.SDK_INT < 24) return true;
        return getPrefs().getBoolean("showLegacyWidgets", false);
    }


    public static boolean stopByFacedown() {
        return getPrefs().getBoolean("stopFacedown", false);
    }

    public static int getHijriFix() {
        return Integer.parseInt(getPrefs().getString("hijri_fix", "0").replace("+", ""));
    }

    public static int getChangelogVersion() {
        return getPrefs().getInt("changelog_version", -1);
    }

    public static void setChangelogVersion(int clv) {
        getPrefs().edit().putInt("changelog_version", clv).apply();
    }

    public static float getCompassLng() {
        return getPrefs().getFloat("compassLong", 0);
    }

    public static float getCompassLat() {
        return getPrefs().getFloat("compassLat", 0);
    }

    public static int getTesbihatTextSize() {
        return getPrefs().getInt("tesbihatTextSize", 0);
    }

    public static void setTesbihatTextSize(int size) {
        getPrefs().edit().putInt("tesbihatTextSize", size).apply();
    }

    public static float getKerahatSunrise() {
        return getPrefs().getInt("kerahat_sunrise", 45);
    }

    public static float getKerahatIstiwa() {
        return getPrefs().getInt("kerahat_istiva", 45);
    }

    public static int getKerahatSunet() {
        return getPrefs().getInt("kerahat_sunset", 45);
    }

    @Nullable
    public static String getVakitIndicator() {
        return getPrefs().getString("vakit_indicator", "current");
    }


    public static boolean showOngoingIcon() {
        return getPrefs().getBoolean("ongoingIcon", showOngoingNumber());
    }

    @Deprecated
    public static boolean autoRemoveNotification() {
        return getPrefs().getBoolean("autoRemoveNotification", false);
    }


    public static boolean showOngoingNumber() {
        return getPrefs().getBoolean("ongoingNumber", false);
    }

    public static void setShowOngoingNumber(boolean show) {
        getPrefs().edit().putBoolean("ongoingNumber", show).apply();
    }

    public static boolean showNotificationScreen() {
        return getPrefs().getBoolean("notificationScreen", true);
    }

    public static void setCompassPos(float lat, float lon) {
        getPrefs().edit().putFloat("compassLat", lat).putFloat("compassLong", lon).apply();
    }

    public static boolean showAltWidgetHightlight() {
        return getPrefs().getBoolean("showAltWidgetHightlight", false);
    }

    public static boolean showExtraTimes() {
        return getPrefs().getBoolean("showExtraTimes", false);
    }


    public static boolean showIntro() {
        return getPrefs().getBoolean("showIntro", true);
    }


    public static void setShowIntro(boolean show) {
        getPrefs().edit().putBoolean("showIntro", show).apply();
        if (show)
            setShowCompassNote(true);
    }


    public static boolean showCompassNote() {
        return getPrefs().getBoolean("showCompassNote", true);
    }


    public static void setShowCompassNote(boolean show) {
        getPrefs().edit().putBoolean("showCompassNote", show).apply();
    }

}
