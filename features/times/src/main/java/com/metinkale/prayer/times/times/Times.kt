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
package com.metinkale.prayer.times.times

import com.metinkale.prayer.times.LegacyPrayTimes
import com.metinkale.prayer.times.LegacyTimes
import com.metinkale.prayer.times.alarm.Alarm
import dev.metinkale.prayertimes.calc.Method
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers


@Serializable
data class Times(
    @SerialName("ID") val id: Int = 0,
    val name: String,
    val source: Source,
    val ongoing: Boolean = false,
    val timezone: Double = 0.0,
    val lng: Double = 0.0,
    val lat: Double = 0.0,
    val elv: Double = 0.0,
    val sortId: Int = Int.MAX_VALUE,
    val minuteAdj: List<Int> = List(6) { 0 },
    val autoLocation: Boolean = false,
    val alarms: List<Alarm> = createDefaultAlarms(id),
    // WebTimes only
    //val times: Map<String?, String?> = ArrayMap(),
    @SerialName("id") val key: String? = null,
    //val lastSync: Long = 0,
    // calc times only (for migration)
    var prayTimes: LegacyPrayTimes? = null,
    val asrType: AsrType = AsrType.Shafi
) {


    enum class AsrType {
        Shafi, Hanafi, Both
    }


    fun buildNotificationId(tag: String) = (tag + id).hashCode()


    fun migrate(): Times {
        // TODO migration of old praytimes. Remove after some updates
        return prayTimes?.let { prayTimes ->
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


            copy(prayTimes = null, key = pt.serialize())

        } ?: this
    }


    @Transient
    val dayTimes by lazy {
        when (source) {
            Source.Calc -> DayTimesCalcProvider(id)
            else -> DayTimesWebProvider.from(id)
        }
    }

    fun delete() = delete(this)

    override fun toString(): String {
        return "times_id_$id"
    }

    companion object : TimesCompanion()
}

