package com.metinkale.prayer.times.times

import com.metinkale.prayer.Preferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

sealed interface DayTimesProvider {
    fun get(key: LocalDate): DayTimes?
}


fun Times.getDayTimes(date: LocalDate): DayTimes? = dayTimes.get(date)


fun Times.getTime(date: LocalDate, time: Int): LocalDateTime {
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
    }?.let { date.atTime(it) }?.plusMinutes(minuteAdj[time].toLong())
        ?.plusMinutes((timezone * 60).roundToInt().toLong())


    if (dt != null) {
        val h = dt.hour
        if (time >= Vakit.DHUHR.ordinal && h < 5) {
            dt = dt.plusDays(1)
        }
    }

    return dt ?: date.atStartOfDay()
}

fun Times.getNextTime(): Int {
    val today = LocalDate.now()
    val now = LocalDateTime.now()

    @Suppress("KotlinConstantConditions")
    var vakit = Vakit.FAJR.ordinal
    while (!getTime(today, vakit).isAfter(now)) {
        vakit++
    }
    return vakit
}

fun Times.getCurrentTime(): Int {
    return getNextTime() - 1
}

fun Times.isKerahat(): Boolean {
    val now = LocalDateTime.now()

    val sun = getTime(now.toLocalDate(), Vakit.SUN.ordinal)
    val untilSun = ChronoUnit.MINUTES.between(sun, now)
    if (untilSun > 0 && untilSun < Preferences.KERAHAT_SUNRISE.get()) {
        return true
    }

    val dhuhr = getTime(now.toLocalDate(), Vakit.DHUHR.ordinal)
    val untilDhuhr = ChronoUnit.MINUTES.between(now, dhuhr)
    if ((untilDhuhr > 0) && (untilDhuhr < (Preferences.KERAHAT_ISTIWA.get()))) {
        return true
    }

    val maghrib = getTime(now.toLocalDate(), Vakit.MAGHRIB.ordinal)
    val untilMaghrib = ChronoUnit.MINUTES.between(now, maghrib)
    return (untilMaghrib > 0) && (untilMaghrib < (Preferences.KERAHAT_SUNSET.get()))
}
