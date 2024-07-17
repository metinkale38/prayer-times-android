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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.metinkale.prayer.times.R

object NotificationUtils {
    fun getAlarmChannel(c: Context): String {
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

    fun getOngoingChannel(c: Context): String {
        val notificationManager = c.getSystemService(
            NotificationManager::class.java
        )
        var channel = notificationManager.getNotificationChannel("ongoing")
        if (channel == null) {
            val name: CharSequence = c.getString(R.string.ongoingNotification)
            val importance = NotificationManager.IMPORTANCE_LOW
            channel = NotificationChannel("ongoing", name, importance)
            channel.setShowBadge(false)
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }
        return channel.id
    }

    fun getPlayingChannel(c: Context): String {
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

    fun getWidgetChannel(c: Context): String {
        val notificationManager = c.getSystemService(
            NotificationManager::class.java
        )
        var channel = notificationManager.getNotificationChannel("widget")
            ?: notificationManager.getNotificationChannel("foreground")
        if (channel == null) {
            val name: CharSequence = c.getString(R.string.appName) + " - Widget"
            channel = NotificationChannel("widget", name, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        return channel.id
    }

    /**
     * A dummy notification is a notification, which is only needed to start a foreground-service
     * It has a special channel, so it can be simple hidden by the user by clicking on it
     */
    fun createDummyNotification(c: Context, text: String? = null): Notification {
        c.getSystemService(NotificationManager::class.java)
        val channelId = getWidgetChannel(c)
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, c.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        val pendingIntent =
            PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(c, channelId)
        builder.setContentIntent(pendingIntent)
        builder.setSmallIcon(R.drawable.ic_abicon)
        builder.setContentText(text ?: c.getString(R.string.clickToDisableNotification))
        builder.priority = NotificationCompat.PRIORITY_MIN
        builder.setWhen(0) //show as last
        return builder.build()
    }

    fun getDummyNotificationId() = 571
}