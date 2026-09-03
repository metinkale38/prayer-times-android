/*
 * Copyright (c) 2013-2026 Metin Kale
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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle

enum class Module(val iconRes: Int, val titleRes: Int) {
    TIMES(R.drawable.ic_menu_times, R.string.appName), COMPASS(
        R.drawable.ic_menu_compass,
        R.string.compass
    ),
    NAMES(R.drawable.ic_menu_names, R.string.names), CALENDAR(
        R.drawable.ic_menu_calendar,
        R.string.calendar
    ),
    TESBIHAT(R.drawable.ic_menu_tesbihat, R.string.tesbihat), HADITH(
        R.drawable.ic_menu_hadith,
        R.string.hadith
    ),
    MISSEDPRAYERS(
        R.drawable.ic_menu_missed,
        R.string.missedPrayers
    ),
    DHIKR(R.drawable.ic_menu_dhikr, R.string.dhikr),
    SETTINGS(R.drawable.ic_menu_settings, R.string.settings), ABOUT(
        R.drawable.ic_menu_about,
        R.string.about
    ),
    INTRO(0, 0), WIDGET(0, 0);


    val key: String = name.lowercase()


    fun buildIntent(c: Context): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setComponent(
            ComponentName(
                c.getPackageName(),
                "com.metinkale.prayer.$key.MainActivity"
            )
        )
        intent.setPackage(c.packageName)
        return intent
    }

    @JvmOverloads
    fun launch(c: Context, extras: Bundle? = null) {
        val intent = buildIntent(c)
        if (extras != null) intent.putExtras(extras)
        c.startActivity(intent)
    }
}
