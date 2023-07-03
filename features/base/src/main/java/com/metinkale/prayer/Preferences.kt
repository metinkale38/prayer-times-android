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

import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import com.metinkale.prayer.utils.LocaleUtils.locale
import java.util.*
import kotlin.reflect.KProperty

object Preferences {
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.get())

    const val COUNTDOWN_TYPE_FLOOR = "default"
    const val COUNTDOWN_TYPE_TOP = "alt"
    const val COUNTDOWN_TYPE_SHOW_SECONDS = "secs"


    var USE_ARABIC by object : BooleanPreference("arabicNames", false) {
        override fun getValue() =
            Locale("ar").language != locale.language && prefs.getBoolean("arabicNames", false)
    }

    var SILENTER_MODE by StringPreference("silenterType", "silent")
    var LANGUAGE by StringPreference("language", "system")
    var USE_ALARM by BooleanPreference("useAlarm", true)
    var DIGITS by StringPreference("numbers", "normal")
    var DATE_FORMAT by StringPreference("dateformat", "DD MMM YYYY")
    var HIJRI_DATE_FORMAT by StringPreference("hdateformat", "DD MMM YYYY")
    var CLOCK_12H by BooleanPreference("use12h", false)
    var STOP_ALARM_ON_FACEDOWN by BooleanPreference("stopFacedown", false)
    var CHANGELOG_VERSION by IntPreference("changelog_version", -1)
    var COMPASS_LNG by FloatPreference("compassLong", 0f)
    var COMPASS_LAT by FloatPreference("compassLat", 0f)
    var TESBIHAT_TEXTSIZE by IntPreference("tesbihatTextSize", 0)
    var KERAHAT_SUNRISE by IntPreference("kerahat_sunrise", 45)
    var KERAHAT_ISTIWA by IntPreference("kerahat_istiva", 45)
    var KERAHAT_SUNSET by IntPreference("kerahat_sunset", 0)
    var ONGOING_TEXT_COLOR by IntPreference("ongoingTextColor", 0)
    var ONGOING_BG_COLOR by IntPreference("ongoingBGColor", 0)

    var UUID by object : StringPreference("uuid", "") {
        override fun getValue() = super.getValue().ifBlank {
            java.util.UUID.randomUUID().toString().also { setValue(it) }
        }
    }
    var SHOW_LEGACY_WIDGET by object : BooleanPreference("showLegacyWidgets", false) {
        override fun getValue() = if (Build.VERSION.SDK_INT < 24) true else super.getValue()
    }

    var VAKIT_INDICATOR_TYPE by StringPreference("vakit_indicator", "current")
    var SHOW_COMPASS_NOTE by BooleanPreference("showCompassNote", true)

    var SHOW_ONGOING_ICON by BooleanPreference("ongoingIcon", true)
    var SHOW_ONGOING_NUMBER by BooleanPreference("ongoingNumber", false)
    var SHOW_NOTIFICATIONSCREEN by BooleanPreference("notificationScreen", true)
    var SHOW_ALT_WIDGET_HIGHLIGHT by BooleanPreference("showAltWidgetHightlight", false)

    var SHOW_INTRO by object : BooleanPreference("showIntro", true) {
        override fun setValue(obj: Boolean) {
            super.setValue(obj)
            if (obj) SHOW_COMPASS_NOTE = true
        }
    }
    var COUNTDOWN_TYPE by StringPreference("widget_countdown", "default")
}

interface Preference<T> {
    fun getValue(): T
    fun setValue(obj: T)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
}

open class StringPreference(val key: String, val def: String = "") : Preference<String> {
    override fun getValue(): String = Preferences.prefs.getString(key, def) ?: def
    override fun setValue(obj: String) = Preferences.prefs.edit().putString(key, obj).apply()
}

open class IntPreference(val key: String, val def: Int = 0) : Preference<Int> {
    override fun getValue(): Int = Preferences.prefs.getInt(key, def)
    override fun setValue(obj: Int) = Preferences.prefs.edit().putInt(key, obj).apply()
}

open class BooleanPreference(val key: String, val def: Boolean = false) : Preference<Boolean> {
    override fun getValue(): Boolean = Preferences.prefs.getBoolean(key, def)
    override fun setValue(obj: Boolean) = Preferences.prefs.edit().putBoolean(key, obj).apply()

}

open class FloatPreference(val key: String, val def: Float = 0f) : Preference<Float> {
    override fun getValue(): Float = Preferences.prefs.getFloat(key, def)
    override fun setValue(obj: Float) = Preferences.prefs.edit().putFloat(key, obj).apply()
}