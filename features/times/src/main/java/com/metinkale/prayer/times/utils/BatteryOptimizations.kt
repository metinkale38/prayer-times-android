/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    val name = context.applicationContext.packageName
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return pwrm.isIgnoringBatteryOptimizations(name)
    }
    return true
}

fun checkBatteryOptimizations(context:Context) {
    if (!isIgnoringBatteryOptimizations(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }
}