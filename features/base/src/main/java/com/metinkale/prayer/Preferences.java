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

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.metinkale.prayer.utils.LocaleUtils;

import java.util.Locale;

public class Preferences {
    public static final String COUNTDOWN_TYPE_FLOOR = "default";
    public static final String COUNTDOWN_TYPE_TOP = "alt";
    public static final String COUNTDOWN_TYPE_SHOW_SECONDS = "secs";

    public static Preference<Boolean> USE_ARABIC = new BooleanPreference("arabicNames", false) {
        @Override
        public Boolean get() {
            return !new Locale("ar").getLanguage().equals(LocaleUtils.getLocale().getLanguage()) && getPrefs().getBoolean("arabicNames", false);
        }
    };
    public static Preference<String> SILENTER_MODE = new StringPreference("silenterType", "silent");
    public static Preference<String> LANGUAGE = new StringPreference("language", "system");
    public static Preference<Boolean> USE_ALARM = new BooleanPreference("useAlarm", true);
    public static Preference<String> DIGITS = new StringPreference("numbers", "normal");
    public static Preference<String> DATE_FORMAT = new StringPreference("dateformat", "DD MMM YYYY");
    public static Preference<String> HIJRI_DATE_FORMAT = new StringPreference("hdateformat", "DD MMM YYYY");
    public static Preference<Boolean> CLOCK_12H = new BooleanPreference("use12h", false);
    public static Preference<Boolean> STOP_ALARM_ON_FACEDOWN = new BooleanPreference("stopFacedown", false);
    public static Preference<Integer> CHANGELOG_VERSION = new IntPreference("changelog_version", -1);
    public static Preference<Float> COMPASS_LNG = new FloatPreference("compassLong", 0f);
    public static Preference<Float> COMPASS_LAT = new FloatPreference("compassLat", 0f);
    public static Preference<Integer> TESBIHAT_TEXTSIZE = new IntPreference("tesbihatTextSize", 0);
    public static Preference<Integer> KERAHAT_SUNRISE = new IntPreference("kerahat_sunrise", 45);
    public static Preference<Integer> KERAHAT_ISTIWA = new IntPreference("kerahat_istiva", 45);
    public static Preference<Integer> KERAHAT_SUNSET = new IntPreference("kerahat_sunset", 0);
    public static Preference<Integer> ONGOING_TEXT_COLOR = new IntPreference("ongoingTextColor", 0);
    public static Preference<Integer> ONGOING_BG_COLOR = new IntPreference("ongoingBGColor", 0);
    public static Preference<String> UUID = new StringPreference("uuid", null) {
        @Override
        public String get() {
            String uuid = super.get();
            if (uuid == null) {
                uuid = java.util.UUID.randomUUID().toString();
                set(uuid);
            }
            return uuid;
        }
    };
    public static Preference<Boolean> SHOW_LEGACY_WIDGET = new BooleanPreference("showLegacyWidgets", false) {
        @Override
        public Boolean get() {
            if (Build.VERSION.SDK_INT < 24)
                return true;
            return super.get();
        }
    };
    public static Preference<String> VAKIT_INDICATOR_TYPE = new StringPreference("vakit_indicator", "current");
    public static Preference<Boolean> SHOW_COMPASS_NOTE = new BooleanPreference("showCompassNote", true);
    public static Preference<Boolean> SHOW_ONGOING_ICON = new BooleanPreference("ongoingIcon", false);
    public static Preference<Boolean> SHOW_ONGOING_NUMBER = new BooleanPreference("ongoingNumber", true);
    public static Preference<Boolean> SHOW_NOTIFICATIONSCREEN = new BooleanPreference("notificationScreen", true);
    public static Preference<Boolean> SHOW_ALT_WIDGET_HIGHLIGHT = new BooleanPreference("showAltWidgetHightlight", false);
    public static Preference<Boolean> SHOW_INTRO = new BooleanPreference("showIntro", true) {
        @Override
        public void set(Boolean obj) {
            super.set(obj);
            if (obj)
                SHOW_COMPASS_NOTE.set(true);
        }
    };
    public static Preference<Integer> HIJRI_FIX = new Preference<Integer>("hijri_fix", 0) {
        private final Preference<String> intern = new StringPreference("hijri_fix", "0");

        @Override
        public Integer get() {
            return Integer.parseInt(intern.get().replace("+", ""));
        }

        @Override
        public void set(Integer obj) {
            intern.set(String.valueOf(obj));
        }
    };
    public static Preference<String> COUNTDOWN_TYPE = new StringPreference("widget_countdown", "default");


    public static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.get());
    }

    public static abstract class Preference<T> {
        private final String key;
        private final T def;

        public Preference(String key, T def) {
            this.key = key;
            this.def = def;
        }

        public String getKey() {
            return key;
        }

        public T getDef() {
            return def;
        }

        public abstract T get();

        public abstract void set(T obj);

    }

    public static class StringPreference extends Preference<String> {
        public StringPreference(String key, String def) {
            super(key, def);
        }

        @Override
        public String get() {
            return getPrefs().getString(getKey(), getDef());
        }

        @Override
        public void set(String obj) {
            getPrefs().edit().putString(getKey(), obj).apply();
        }
    }

    public static class IntPreference extends Preference<Integer> {
        public IntPreference(String key, Integer def) {
            super(key, def);
        }

        @Override
        public Integer get() {
            return getPrefs().getInt(getKey(), getDef());
        }

        @Override
        public void set(Integer obj) {
            getPrefs().edit().putInt(getKey(), obj).apply();
        }
    }

    public static class BooleanPreference extends Preference<Boolean> {
        public BooleanPreference(String key, Boolean def) {
            super(key, def);
        }

        @Override
        public Boolean get() {
            return getPrefs().getBoolean(getKey(), getDef());
        }

        @Override
        public void set(Boolean obj) {
            getPrefs().edit().putBoolean(getKey(), obj).apply();
        }
    }

    public static class FloatPreference extends Preference<Float> {
        public FloatPreference(String key, Float def) {
            super(key, def);
        }

        @Override
        public Float get() {
            return getPrefs().getFloat(getKey(), getDef());
        }

        @Override
        public void set(Float obj) {
            getPrefs().edit().putFloat(getKey(), obj).apply();
        }
    }
}
