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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.OnPrefsChangedListener
import com.metinkale.prayer.utils.LocaleUtils
import java.util.Locale

object AliasManager : OnPrefsChangedListener {
    override fun onPrefsChanged(key: String) {
        if (key == "language") {
            val context: Context = App.get()
            val pm = context.packageManager
            val info: PackageInfo = try {
                pm.getPackageInfo(
                    context.applicationContext.packageName,
                    PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
                )
            } catch (e: PackageManager.NameNotFoundException) {
                recordException(e)
                throw RuntimeException(e)
            }

            val prefix = "com.metinkale.prayer.alias"
            val aliases = info.activities?.filter { it.name.startsWith(prefix) }
                ?.associate { Locale(it.name.substring(prefix.length)).language to it.name }
                ?: return

            var bestAlias = "Default"


            val locales = LocaleUtils.locales
            for (element in locales) {
                val lang = element.language
                if (aliases.containsKey(lang)) {
                    bestAlias = lang
                    break
                }
            }

            aliases.forEach { (key, value) ->
                if (bestAlias == key) {
                    //enable
                    pm.setComponentEnabledSetting(
                        ComponentName(context, value),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    //disable
                    pm.setComponentEnabledSetting(
                        ComponentName(context, value),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        }
    }
}