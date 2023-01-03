package com.metinkale.prayer.times.times

import com.metinkale.prayer.times.utils.toJoda
import dev.metinkale.prayertimes.calc.PrayTimes
import org.joda.time.LocalDate

class DayTimesCalcProvider(id: Int) : DayTimesProvider {
    private val times = Times.getTimesById(id).value
    val prayTime = PrayTimes.deserialize(times?.id ?: "")

    override fun get(key: LocalDate): DayTimes {
        return prayTime.getTimes(
            kotlinx.datetime.LocalDate(
                key.year,
                key.monthOfYear,
                key.dayOfMonth
            )
        ).let {
            DayTimes(
                date = key,
                fajr = it.imsak.toJoda(),
                sun = it.sunrise.toJoda(),
                dhuhr = it.dhuhr.toJoda(),
                asr = it.asrShafi.toJoda(),
                asrHanafi = it.asrHanafi.toJoda(),
                maghrib = it.maghrib.toJoda(),
                ishaa = it.ishaa.toJoda(),
                sabah = it.fajr.toJoda(),
            )
        }
    }
}