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
package com.metinkale.prayer.times.alarm

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.AlarmService.StopAlarmPlayerReceiver
import com.metinkale.prayer.times.fragments.TimesFragment.Companion.getPendingIntent
import com.metinkale.prayer.times.utils.NotificationUtils

object AlarmUtils {
    @JvmStatic
    fun buildAlarmNotification(c: Context?, alarm: Alarm, time: Long): Notification {
        val t = alarm.city
        val text = t.name + " (" + t.source + ")"
        var txt = alarm.buildNotificationTitle()
        if (BuildConfig.DEBUG) {
            val difference = System.currentTimeMillis() - time
            txt += if (difference < 5000) {
                " " + difference + "ms"
            } else if (difference < 3 * 60 * 1000) {
                " " + difference / 1000 + "s"
            } else {
                " " + difference / 1000 / 60 + "m"
            }
        }
        var builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(c!!, NotificationUtils.getAlarmChannel(c))
            } else {
                NotificationCompat.Builder(c!!)
            }
        builder = builder.setContentTitle(text)
            .setContentText(txt)
            .setContentIntent(getPendingIntent(t))
            .setSmallIcon(R.drawable.ic_abicon)
            .setWhen(time)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationUtils.getAlarmChannel(c))
        } else {
            builder.priority = Notification.PRIORITY_DEFAULT
        }
        return builder.build()
    }

    @JvmStatic
    fun buildPlayingNotification(c: Context, alarm: Alarm, time: Long): Notification {
        val t = alarm.city
        val text = t.name + " (" + t.source + ")"
        var txt = alarm.buildNotificationTitle()
        if (BuildConfig.DEBUG) {
            val difference = System.currentTimeMillis() - time
            txt += if (difference < 5000) {
                " " + difference + "ms"
            } else if (difference < 3 * 60 * 1000) {
                " " + difference / 1000 + "s"
            } else {
                " " + difference / 1000 / 60 + "m"
            }
        }
        val stopIndent = PendingIntent.getBroadcast(
            c,
            0,
            Intent(c, StopAlarmPlayerReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        var builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(c, NotificationUtils.getAlarmChannel(c))
            } else {
                NotificationCompat.Builder(c)
            }
        builder = builder.setContentTitle(text)
            .setContentText(txt)
            .setContentIntent(getPendingIntent(t))
            .setSmallIcon(R.drawable.ic_abicon)
            .setOngoing(true)
            .addAction(R.drawable.ic_action_stop, c.getString(R.string.stop), stopIndent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0)
            )
            .setWhen(time)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationUtils.getAlarmChannel(c))
        } else {
            builder.priority = Notification.PRIORITY_MAX
        }
        return builder.build()
    }
}