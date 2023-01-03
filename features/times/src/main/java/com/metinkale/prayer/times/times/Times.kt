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
package com.metinkale.prayer.times.times

import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.LegacyPrayTimes
import com.metinkale.prayer.times.LegacyTimes
import com.metinkale.prayer.times.alarm.Alarm
import dev.metinkale.prayertimes.calc.Method
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.joda.time.*
import kotlin.math.roundToInt

@Serializable
data class Times(
    val ID: Int = 0,
    val name: String,
    val source: Source,
    val ongoing: Boolean = false,
    val timezone: Double = 0.0,
    val lng: Double = 0.0,
    val lat: Double = 0.0,
    val elv: Double = 0.0,
    val sortId: Int = Int.MAX_VALUE,
    val minuteAdj: List<Int> = List(6) { 0 },
    val isAutoLocation: Boolean = false,
    val alarms: List<Alarm> = createDefaultAlarms(ID),
    // WebTimes only
    //val times: Map<String?, String?> = ArrayMap(),
    val id: String? = null,
    //val lastSync: Long = 0,
    // calc times only (for migration)
    var prayTimes: LegacyPrayTimes? = null,
    val asrType: AsrType = AsrType.Shafi
) {


    init {
        // TODO migration of old praytimes. Remove after some updates
        prayTimes?.let { prayTimes ->
            val method = Method(
                highLats = prayTimes.highLats,
                midnight = prayTimes.midnight,
                imsakAngle = prayTimes.angles[LegacyTimes.Imsak.ordinal],
                imsakMinute = prayTimes.minutes[LegacyTimes.Imsak.ordinal],
                fajrAngle = prayTimes.angles[LegacyTimes.Fajr.ordinal],
                fajrMinute = prayTimes.minutes[LegacyTimes.Fajr.ordinal],
                sunriseMinute = prayTimes.minutes[LegacyTimes.Sunrise.ordinal],
                dhuhrMinute = prayTimes.minutes[LegacyTimes.Dhuhr.ordinal],
                asrShafiMinute = prayTimes.minutes[LegacyTimes.AsrShafi.ordinal],
                asrHanafiMinute = prayTimes.minutes[LegacyTimes.AsrHanafi.ordinal],
                sunsetMinutes = prayTimes.minutes[LegacyTimes.Sunset.ordinal],
                maghribAngle = prayTimes.angles[LegacyTimes.Maghrib.ordinal],
                maghribMinute = prayTimes.minutes[LegacyTimes.Maghrib.ordinal],
                ishaaAngle = prayTimes.angles[LegacyTimes.Ishaa.ordinal],
                ishaaMinute = prayTimes.minutes[LegacyTimes.Ishaa.ordinal],
                useElevation = true
            ).let { method ->
                Method.values().mapNotNull { it as? Method }.firstOrNull { it == method } ?: method
            }


            val pt = PrayTimes(
                prayTimes.lat,
                prayTimes.lng,
                prayTimes.elv,
                TimeZone.of(prayTimes.timeZone),
                method
            )

            copy(prayTimes = null, id = pt.serialize()).save()

        }
    }


    enum class AsrType {
        Shafi, Hanafi, Both
    }

    @Transient
    val dayTimes by lazy {
        when (source) {
            Source.Calc -> DayTimesCalcProvider(ID)
            else -> DayTimesWebProvider.from(ID)
        }
    }

    fun save() = save(this)
    fun delete() = delete(this)
    fun getDayTimes(date: LocalDate): DayTimes? = dayTimes.get(date)


    fun getTime(date: LocalDate, time: Int): LocalDateTime {
        @Suppress("NAME_SHADOWING") var date = date
        @Suppress("NAME_SHADOWING") var time = time
        while (time < 0) {
            date = date.minusDays(1)
            time += Vakit.LENGTH
        }

        while (time >= Vakit.LENGTH) {
            date = date.plusDays(1)
            time -= Vakit.LENGTH
        }
        var dt = getDayTimes(date)?.let {
            when (Vakit.getByIndex(time)) {
                Vakit.FAJR -> it.fajr
                Vakit.SUN -> it.sun
                Vakit.DHUHR -> it.dhuhr
                Vakit.ASR -> it.asr
                Vakit.MAGHRIB -> it.maghrib
                Vakit.ISHAA -> it.ishaa
            }
        }?.let { date.toLocalDateTime(it) }?.plusMinutes(minuteAdj[time])
            ?.plusMinutes((timezone * 60).roundToInt())


        if (dt != null) {
            val h = dt.hourOfDay
            if (time >= Vakit.DHUHR.ordinal && h < 5) {
                dt = dt.plusDays(1)
            }
        }

        return dt ?: date.toDateTimeAtStartOfDay().toLocalDateTime()
    }

    fun getNextTime(): Int {
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        @Suppress("KotlinConstantConditions")
        var vakit = Vakit.FAJR.ordinal
        while (getTime(today, vakit)?.isAfter(now) == false) {
            vakit++
        }
        return vakit
    }

    fun getCurrentTime(): Int {
        return getNextTime() - 1
    }

    fun isKerahat(): Boolean {
        val now = LocalDateTime.now()

        val sun = getTime(now.toLocalDate(), Vakit.SUN.ordinal)
        val untilSun = Period(sun, now, PeriodType.minutes()).minutes
        if (untilSun >= 0 && untilSun < Preferences.KERAHAT_SUNRISE.get()) {
            return true
        }

        val dhuhr = getTime(now.toLocalDate(), Vakit.DHUHR.ordinal)
        val untilDhuhr = Period(now, dhuhr, PeriodType.minutes()).minutes
        if ((untilDhuhr >= 0) && (untilDhuhr < (Preferences.KERAHAT_ISTIWA.get()))) {
            return true
        }

        val maghrib = getTime(now.toLocalDate(), Vakit.MAGHRIB.ordinal)
        val untilMaghrib = Period(now, maghrib, PeriodType.minutes()).minutes
        return (untilMaghrib >= 0) && (untilMaghrib < (Preferences.KERAHAT_SUNSET.get()))
    }


    override fun toString(): String {
        return "times_id_$ID"
    }

    companion object : TimesCompanion()
}


private fun createDefaultAlarms(cityId: Int) = Vakit.values().map {
    Alarm(
        cityId = cityId,
        times = listOf(it)
    )
}

