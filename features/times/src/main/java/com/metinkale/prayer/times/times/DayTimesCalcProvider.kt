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
                asr = if (times?.asrType == Times.AsrType.Hanafi) it.asrHanafi.toJoda() else it.asrShafi.toJoda(),
                asrHanafi = if (times?.asrType == Times.AsrType.Both) it.asrHanafi.toJoda() else null,
                maghrib = it.maghrib.toJoda(),
                ishaa = it.ishaa.toJoda(),
                sabah = null //it.fajr.toJoda(), imsak and fajr are the same for all Methods
            )
        }
    }
}