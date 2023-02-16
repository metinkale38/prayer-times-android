@file:UseSerializers(BooleanSerializer::class)
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

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.preference.PreferenceManager
import com.metinkale.prayer.App
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.sounds.Sound
import com.metinkale.prayer.times.times.*
import com.metinkale.prayer.utils.UUID
import com.metinkale.prayer.utils.Utils
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.Period
import java.text.DateFormatSymbols
import java.util.*
import kotlin.math.abs


@Serializable
data class Alarm(
    val id: Int = UUID.asInt(),
    val enabled: Boolean = false,
    val weekdays: Set<Int> = ALL_WEEKDAYS,
    val sounds: List<Sound> = listOf(),
    val times: Set<Vakit> = ALL_TIMES,
    val mins: Int = 0,
    val vibrate: Boolean = false,
    val removeNotification: Boolean = false,
    val silenter: Int = 0,
    val volume: Int = getLegacyVolumeMode(App.get()),
    val cityId: Int = 0
) : Comparable<Alarm> {

    val nextAlarm: LocalDateTime?
        get() {
            val city = city
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            for (i in 0..7) {
                val date = today.plusDays(i)
                if (!weekdays.contains(jodaWDToJavaWD(date.dayOfWeek))) continue
                for (vakit in Vakit.values()) {
                    if (!times.contains(vakit)) continue
                    val time: LocalDateTime =
                        city.getTime(date, vakit.ordinal).plusMinutes(mins) ?: continue
                    if (time.isAfter(now)) return time
                }
            }
            return null
        }

    val city: Times
        get() = Times.current.first { it.id == cityId }

    // avoid messages like 1 minute before/after Maghtib for minimal deviations
    fun buildNotificationTitle(): String {
        val city = city
        var time: Int = city.getCurrentTime()
        while (times.isNotEmpty() && !times.contains(Vakit.getByIndex(time))) {
            time++
        }
        var minutes = Period(
            city.getTime(LocalDate.now(), time),
            LocalDateTime.now()
        ).toStandardMinutes().minutes
        if (minutes > 0 && times.contains(Vakit.getByIndex(time + 1))) {
            val minutesToNext = Period(
                city.getTime(LocalDate.now(), time + 1),
                LocalDateTime.now()
            ).toStandardMinutes().minutes
            if (minutes > abs(minutesToNext)) {
                time++
                minutes = minutesToNext
            }
        }

        // avoid messages like 1 minute before/after Maghtib for minimal deviations
        val minutesThreshold = 2
        if (abs(minutes) < minutesThreshold) {
            minutes = 0
        }
        val strRes2: Int = if (minutes < 0) {
            R.string.noti_beforeTime
        } else if (minutes > 0) {
            R.string.noti_afterTime
        } else {
            R.string.noti_exactTime
        }
        val ctx: Context = App.get()
        return ctx.getString(strRes2, abs(minutes), Vakit.getByIndex(time).string)
    }

    val title: String by lazy {
        val eachDay = weekdays.size == 7
        val strRes1 = if (eachDay) R.string.noti_eachDay else R.string.noti_weekday
        var days: String? = null
        if (!eachDay) {
            val daysBuilder = StringBuilder()
            val namesOfDays =
                if (weekdays.size == 1) DateFormatSymbols.getInstance().weekdays else DateFormatSymbols.getInstance().shortWeekdays
            for (i in weekdays.sorted()) {
                daysBuilder.append("/").append(namesOfDays[i])
            }
            days = daysBuilder.toString()
            if (days.isNotEmpty()) days = days.substring(1)
        }
        val eachPrayerTime = ALL_TIMES == times
        val strRes2: Int = if (mins < 0) {
            if (eachPrayerTime) R.string.noti_beforeAll else R.string.noti_beforeTime
        } else if (mins > 0) {
            if (eachPrayerTime) R.string.noti_afterAll else R.string.noti_afterTime
        } else {
            if (eachPrayerTime) R.string.noti_exactAll else R.string.noti_exactTime
        }
        var times: String? = null
        if (!eachPrayerTime) {
            val timesBuilder = StringBuilder()
            for (vakit in this.times.sorted()) {
                timesBuilder.append("/").append(vakit.string)
            }
            if (timesBuilder.isNotEmpty()) times = timesBuilder.toString().substring(1)
        }
        val ctx: Context = App.get()
        ctx.getString(strRes1, days, ctx.getString(strRes2, abs(mins), times))
    }

    override fun compareTo(other: Alarm): Int {
        var comp: Int = Collections.min(times).ordinal - Collections.min(other.times).ordinal
        if (comp != 0) return comp
        comp = mins - other.mins
        if (comp != 0) return comp
        comp = Collections.min(weekdays) - Collections.min(other.weekdays)
        return comp
    }

    fun vibrateNow(c: Context) {
        if (!vibrate) return
        val v = c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(Utils.getVibrationPattern(c, "vibration"), -1))
        } else {
            v.vibrate(Utils.getVibrationPattern(c, "vibration"), -1)
        }
    }


    companion object {
        val ALL_TIMES: Set<Vakit> = setOf(
            Vakit.FAJR,
            Vakit.DHUHR,
            Vakit.ASR,
            Vakit.MAGHRIB,
            Vakit.ISHAA
        )
        val ALL_WEEKDAYS: Set<Int> = setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )
        const val VOLUME_MODE_RINGTONE = -1
        const val VOLUME_MODE_NOTIFICATION = -2
        const val VOLUME_MODE_ALARM = -3
        const val VOLUME_MODE_MEDIA = -4

        fun fromId(id: Int): Alarm? {
            for (t in Times.current) {
                for (a in t.alarms) {
                    if (a.id == id) {
                        return a
                    }
                }
            }
            return null
        }

        private fun jodaWDToJavaWD(wd: Int): Int {
            when (wd) {
                DateTimeConstants.MONDAY -> return Calendar.MONDAY
                DateTimeConstants.TUESDAY -> return Calendar.TUESDAY
                DateTimeConstants.WEDNESDAY -> return Calendar.WEDNESDAY
                DateTimeConstants.THURSDAY -> return Calendar.THURSDAY
                DateTimeConstants.FRIDAY -> return Calendar.FRIDAY
                DateTimeConstants.SATURDAY -> return Calendar.SATURDAY
                DateTimeConstants.SUNDAY -> return Calendar.SUNDAY
            }
            return 0
        }

        fun getLegacyVolumeMode(c: Context): Int {
            return when (PreferenceManager.getDefaultSharedPreferences(c)
                .getString("ezanvolume", "noti")) {
                "alarm" -> VOLUME_MODE_ALARM
                "media" -> VOLUME_MODE_MEDIA
                "noti" -> {
                    val am = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
                }
                else -> {
                    val am = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
                }
            }
        }
    }
}