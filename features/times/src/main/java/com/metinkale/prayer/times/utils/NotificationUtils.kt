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
package com.metinkale.prayer.times.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.metinkale.prayer.times.R

object NotificationUtils {
    fun getAlarmChannel(c: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = c.getSystemService(
                NotificationManager::class.java
            )
            var channel = notificationManager.getNotificationChannel("alarm")
            if (channel == null) {
                val name: CharSequence = c.getString(R.string.prayerNotification)
                val importance = NotificationManager.IMPORTANCE_LOW
                channel = NotificationChannel("alarm", name, importance)
                notificationManager.createNotificationChannel(channel)
            }
            return channel.id
        }
        return "alarm"
    }

    fun getOngoingChannel(c: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = c.getSystemService(
                NotificationManager::class.java
            )
            var channel = notificationManager.getNotificationChannel("ongoing")
            if (channel == null) {
                val name: CharSequence = c.getString(R.string.ongoingNotification)
                val importance = NotificationManager.IMPORTANCE_LOW
                channel = NotificationChannel("ongoing", name, importance)
                channel.setShowBadge(false)
                notificationManager.createNotificationChannel(channel)
            }
            return channel.id
        }
        return "ongoing"
    }

    fun getPlayingChannel(c: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = c.getSystemService(
                NotificationManager::class.java
            )
            var channel = notificationManager.getNotificationChannel("sound")
            if (channel == null) {
                val name: CharSequence = c.getString(R.string.sound)
                channel = NotificationChannel("sound", name, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            return channel.id
        }
        return "sound"
    }
}