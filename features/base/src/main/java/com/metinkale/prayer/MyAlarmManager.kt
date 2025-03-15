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

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.os.Build

class MyAlarmManager private constructor(val c: Context) {
    private val alarmManager: AlarmManager =
        c.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    operator fun set(type: Int, time: Long, service: PendingIntent) {
        alarmManager[type, time] = service
    }

    fun setExact(type: Int, time: Long, service: PendingIntent) {
        val canUseExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (Preferences.USE_ALARM) {
            val pendingIntent = PendingIntent.getActivity(
                c, 0, Module.TIMES.buildIntent(c),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val info = AlarmClockInfo(time, pendingIntent)
            alarmManager.setAlarmClock(info, service)
        } else if (!canUseExact) {
            set(type, time, service)
        } else if (type == AlarmManager.RTC_WAKEUP) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(type, time, service)
            else
                alarmManager.setAndAllowWhileIdle(type, time, service)
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
                alarmManager.setExact(type, time, service)
            else
                alarmManager.set(type, time, service)
        }
    }

    fun cancel(service: PendingIntent) {
        alarmManager.cancel(service)
    }

    companion object {
        @JvmStatic
        fun with(c: Context): MyAlarmManager {
            return MyAlarmManager(c)
        }
    }
}