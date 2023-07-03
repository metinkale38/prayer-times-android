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

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.util.Pair
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.MyAlarmManager
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.service.ForegroundService
import com.metinkale.prayer.times.alarm.Alarm.Companion.fromId
import com.metinkale.prayer.times.alarm.AlarmUtils.buildAlarmNotification
import com.metinkale.prayer.times.alarm.AlarmUtils.buildPlayingNotification
import com.metinkale.prayer.times.alarm.sounds.MyPlayer
import com.metinkale.prayer.times.fragments.NotificationPopup
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.setAlarms
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

class AlarmService : IntentService("AlarmService") {
    override fun onHandleIntent(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(javaClass.hashCode(), ForegroundService.createNotification(this))
        }
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock =
            powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "prayer-times:AlarmService")
        wakeLock.acquire()
        try {
            fireAlarm(intent)
        } catch (e: Exception) {
            recordException(e)
        }
        Times.setAlarms()
        wakeLock.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    @Throws(InterruptedException::class)
    fun fireAlarm(intent: Intent?) {
        val c: Context = App.get()
        if (intent == null || !intent.hasExtra(EXTRA_ALARMID)) {
            return
        }
        val alarmId = intent.getIntExtra(EXTRA_ALARMID, 0)
        val time = intent.getLongExtra(EXTRA_TIME, 0)
        val alarm = fromId(alarmId)
        if (alarm == null || !alarm.enabled) return
        intent.removeExtra(EXTRA_ALARMID)
        val notId = alarm.city.id
        val nm = c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_TAG, notId)
        alarm.vibrateNow(c)
        val player = MyPlayer.from(alarm)
        if (player != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(javaClass.hashCode(), buildPlayingNotification(c, alarm, time))
            } else {
                nm.notify(NOTIFICATION_TAG, notId, buildPlayingNotification(c, alarm, time))
            }
            if (Preferences.SHOW_NOTIFICATIONSCREEN) {
                NotificationPopup.start(c, alarm)
                Thread.sleep(1000)
            }
            try {
                player.play()
                if (Preferences.STOP_ALARM_ON_FACEDOWN) {
                    StopByFacedownMgr.start(this, player)
                }
                sInterrupt.set(false)
                while (!sInterrupt.get() && player.isPlaying) {
                    Thread.sleep(500)
                }
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {
                recordException(e)
            }
            nm.cancel(NOTIFICATION_TAG, notId)
            if (NotificationPopup.instance != null && Preferences.SHOW_NOTIFICATIONSCREEN) {
                NotificationPopup.instance!!.finish()
            }
        }
        if (!alarm.removeNotification || player == null) {
            val not = buildAlarmNotification(c, alarm, time)
            nm.notify(NOTIFICATION_TAG, notId, not)
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

    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val alarmId = intent.getIntExtra(EXTRA_ALARMID, -1)
            val time = intent.getLongExtra(EXTRA_TIME, -1)
            if (alarmId > 0 && time > 0) {
                val service = Intent(context, AlarmService::class.java)
                service.putExtra(EXTRA_ALARMID, alarmId)
                service.putExtra(EXTRA_TIME, time)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        context.startForegroundService(service)
                    } catch (e: Exception) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || e !is ForegroundServiceStartNotAllowedException) {
                            throw e
                        }
                    }

                } else {
                    context.startService(service)
                }
            } else {
                Times.setAlarms()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_TAG = "alarm"
        private const val EXTRA_ALARMID = "alarmId"
        private const val EXTRA_TIME = "time"
        private val sInterrupt = AtomicBoolean(false)
        private var sLastSchedule: Pair<Alarm, LocalDateTime>? = null
        private val ALARM_SERVICE_NEEDY = "alarmService"
        fun setAlarm(c: Context, alarm: Pair<Alarm, LocalDateTime>?) {
            val am = MyAlarmManager.with(c)
            val i = Intent(c, AlarmReceiver::class.java)
            if (alarm != null) {
                ForegroundService.addNeedy(c, ALARM_SERVICE_NEEDY)

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
                val service = PendingIntent.getBroadcast(
                    c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                am.cancel(service)
                am.setExact(AlarmManager.RTC_WAKEUP, time, service)
            } else {
                ForegroundService.removeNeedy(c, ALARM_SERVICE_NEEDY)
            }
        }
    }
}