package com.metinkale.prayer.times.times

import dev.metinkale.prayertimes.calc.PrayTimes
import kotlinx.datetime.toJavaLocalTime
import java.time.LocalDate

class DayTimesCalcProvider(id: Int) : DayTimesProvider {
    private val times = Times.getTimesById(id).current
    val prayTime = PrayTimes.deserialize(times?.key ?: "")

    override fun get(key: LocalDate): DayTimes {
        return prayTime.getTimes(
            kotlinx.datetime.LocalDate(
                key.year,
                key.monthValue,
                key.dayOfMonth
            )
        ).let {
            DayTimes(
                date = key,
                fajr = it.imsak.toJavaLocalTime(),
                sun = it.sunrise.toJavaLocalTime(),
                dhuhr = it.dhuhr.toJavaLocalTime(),
                asr = if (times?.asrType == Times.AsrType.Hanafi) it.asrHanafi.toJavaLocalTime() else it.asrShafi.toJavaLocalTime(),
                asrHanafi = if (times?.asrType == Times.AsrType.Both) it.asrHanafi.toJavaLocalTime() else null,
                maghrib = it.maghrib.toJavaLocalTime(),
                ishaa = it.ishaa.toJavaLocalTime(),
                sabah = null //it.fajr.toJavaLocalTime(), imsak and fajr are the same for all Methods
            )
        }
    }
}