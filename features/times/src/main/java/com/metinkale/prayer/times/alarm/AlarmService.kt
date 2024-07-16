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

import android.app.AlarmManager
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.util.Pair
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.MyAlarmManager
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.times.alarm.Alarm.Companion.fromId
import com.metinkale.prayer.times.alarm.AlarmUtils.buildAlarmNotification
import com.metinkale.prayer.times.alarm.AlarmUtils.buildPlayingNotification
import com.metinkale.prayer.times.alarm.sounds.MyPlayer
import com.metinkale.prayer.times.fragments.NotificationPopup
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

class AlarmService : IntentService("AlarmService") {
    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            val alarmId = intent.getIntExtra(EXTRA_ALARMID, -1).takeIf { it >= 0 }
            val time = intent.getLongExtra(EXTRA_TIME, -1).takeIf { it >= 0 }
            alarmId?.let {
                time?.let {
                    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                    val wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "prayer-times:AlarmService"
                    )
                    wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

                    try {
                        runBlocking { fireAlarm(alarmId, time) }
                    } catch (e: Exception) {
                        recordException(e)
                    }
                    wakeLock.release()

                }
            }
        }
        stopForeground(STOP_FOREGROUND_DETACH)
        Times.setAlarms()
    }

    private suspend fun fireAlarm(alarmId: Int, time: Long) {
        val c: Context = App.get()
        val alarm = fromId(alarmId)
        if (alarm == null || !alarm.enabled) return
        val notId = alarm.getCity().buildNotificationId("alarm")
        val nm = c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notId)

        ServiceCompat.startForeground(
            this, notId, buildPlayingNotification(c, alarm, time),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )


        alarm.vibrateNow(c)
        val player = MyPlayer.from(alarm)
        if (player != null) {
            if (Preferences.SHOW_NOTIFICATIONSCREEN) {
                NotificationPopup.start(c, alarm)
                delay(1000)
            }
            try {
                player.play()
                if (Preferences.STOP_ALARM_ON_FACEDOWN) {
                    StopByFacedownMgr.start(this, player)
                }
                sInterrupt.set(false)
                while (!sInterrupt.get() && player.isPlaying) {
                    delay(500)
                }
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {
                recordException(e)
            }
            if (NotificationPopup.instance != null && Preferences.SHOW_NOTIFICATIONSCREEN) {
                NotificationPopup.instance!!.finish()
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)

        if (!alarm.removeNotification || player == null) {
            val not = buildAlarmNotification(c, alarm, time)
            nm.notify(notId, not)
        }
        if (alarm.silenter != 0) {
            SilenterReceiver.silent(c, alarm.silenter)
        }
    }

    class StopAlarmPlayerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            sInterrupt.set(true)
        }
    }


    companion object {
        private const val EXTRA_ALARMID = "alarmId"
        private const val EXTRA_TIME = "time"
        private val sInterrupt = AtomicBoolean(false)
        private var sLastSchedule: Pair<Alarm, LocalDateTime>? = null

        fun setAlarm(c: Context, alarm: Pair<Alarm, LocalDateTime>?) {
            val am = MyAlarmManager.with(c)
            val i = Intent(c, AlarmService::class.java)
            if (alarm != null) {

                if (alarm == sLastSchedule && !BuildConfig.DEBUG) return
                if (Build.MANUFACTURER != "samsung") {
                    sLastSchedule = alarm
                } else {
                    val pm = c.getSystemService(POWER_SERVICE) as PowerManager
                    if (pm.isInteractive) {
                        sLastSchedule = alarm
                    }
                }
                val time = alarm.second.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (BuildConfig.DEBUG) Log.e("ALARM", "Next Alarm: " + alarm.second)
                i.putExtra(EXTRA_ALARMID, alarm.first.id)
                i.putExtra(EXTRA_TIME, time)

                val service =
                    PendingIntent.getForegroundService(
                        c, 0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                am.cancel(service)
                am.setExact(AlarmManager.RTC_WAKEUP, time, service)
            }
        }
    }
}
