package com.metinkale.prayer.wear.tiles

import android.content.Context
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.data.WearTimesRepository
import dev.metinkale.openprayertimes.hijri.HijriDate
import dev.metinkale.openprayertimes.hijri.HijriMonth
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate as KLocalDate

private fun WearDayTimes.toPrayerTimes(): PrayerTimes? = runCatching {
    PrayerTimes(
        fajr = LocalTime.parse(fajr),
        sun = LocalTime.parse(sun),
        dhuhr = LocalTime.parse(dhuhr),
        asr = LocalTime.parse(asr),
        maghrib = LocalTime.parse(maghrib),
        ishaa = LocalTime.parse(ishaa)
    )
}.getOrNull()

private fun labelResForIndex(index: Int): Int = when (index) {
    0 -> R.string.fajr
    1 -> R.string.sun
    2 -> R.string.zuhr
    3 -> R.string.asr
    4 -> R.string.maghrib
    else -> R.string.ishaa
}

private fun hijriMonthRes(month: HijriMonth): Int = when (month) {
    HijriMonth.MUHARRAM -> R.string.hmonth1
    HijriMonth.SAFAR -> R.string.hmonth2
    HijriMonth.RABIAL_AWWAL -> R.string.hmonth3
    HijriMonth.RABIAL_AKHIR -> R.string.hmonth4
    HijriMonth.JUMADAAL_AWWAL -> R.string.hmonth5
    HijriMonth.JUMADAAL_AKHIR -> R.string.hmonth6
    HijriMonth.RAJAB -> R.string.hmonth7
    HijriMonth.SHABAN -> R.string.hmonth8
    HijriMonth.RAMADAN -> R.string.hmonth9
    HijriMonth.SHAWWAL -> R.string.hmonth10
    HijriMonth.DHUL_QADA -> R.string.hmonth11
    HijriMonth.DHUL_HIJJA -> R.string.hmonth12
}

private fun currentPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
    if (now < times.fajr) return -1
    if (!now.isBefore(times.ishaa)) return 5
    val nextIndex = times.asList().indexOfFirst { it > now }
    return if (nextIndex <= 0) 5 else nextIndex - 1
}

internal suspend fun loadPrayerTileModel(
    context: Context,
    now: LocalDateTime = LocalDateTime.now()
): PrayerTileModel? {
    val payload = runCatching {
        WearTimesRepository.get(context).payload.first()
    }.getOrNull() ?: return null

    val days = payload.days.associateBy { it.date }
    val todayDate = now.toLocalDate()
    val today = days[todayDate.toString()]?.toPrayerTimes() ?: return null
    val nowTime = now.toLocalTime()
    val times = today.asList()
    val currentIndex = currentPrayerIndex(today, nowTime)
    val nextTodayIndex = times.indexOfFirst { it > nowTime }
    val nextIsTomorrow = nextTodayIndex < 0
    val nextDate = if (nextIsTomorrow) todayDate.plusDays(1) else todayDate
    val nextDay = if (nextIsTomorrow) {
        days[nextDate.toString()]?.toPrayerTimes()
    } else {
        today
    }
    val nextTime = if (nextIsTomorrow) {
        nextDay?.fajr ?: today.fajr
    } else {
        times[nextTodayIndex]
    }
    val nextIndexInList = if (nextIsTomorrow) 0 else nextTodayIndex
    val target = LocalDateTime.of(nextDate, nextTime)
    val seconds = Duration.between(now, target).seconds
    val countdownMinutes = if (seconds <= 0) 0 else ((seconds + 59) / 60).toInt()

    val dateText = String.format(
        Locale.US,
        "%02d. %s %d",
        todayDate.dayOfMonth,
        todayDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
        todayDate.year
    )
    val hijri = runCatching {
        HijriDate.fromLocalDate(
            KLocalDate(todayDate.year, todayDate.monthValue, todayDate.dayOfMonth)
        )
    }.getOrNull()
    val hijriMonth = hijri?.let { context.getString(hijriMonthRes(it.month)) }
    val hijriDay = hijri?.day?.toString() ?: ""
    val hijriMonthYear = if (hijri != null && hijriMonth != null) {
        "$hijriMonth ${hijri.year}"
    } else {
        ""
    }
    val hijriText = if (hijri != null && hijriMonth != null) {
        "${hijri.day}. $hijriMonth ${hijri.year}"
    } else {
        ""
    }
    val hijriDateText = if (hijri != null && hijriMonth != null) {
        "${hijri.day} $hijriMonth ${hijri.year}"
    } else {
        ""
    }
    val hijriYear = hijri?.year?.toString() ?: ""
    val tomorrow = days[todayDate.plusDays(1).toString()]?.toPrayerTimes()
    val zone = ZoneId.systemDefault()
    val prayerTargets = times.mapIndexed { index, time ->
        if (time > nowTime) {
            LocalDateTime.of(todayDate, time)
        } else {
            val nextTime = tomorrow?.asList()?.getOrNull(index) ?: time
            LocalDateTime.of(todayDate.plusDays(1), nextTime)
        }
    }
    val prayerCountdownMinutes = prayerTargets.map {
        val seconds = Duration.between(now, it).seconds
        if (seconds <= 0) 0 else ((seconds + 59) / 60).toInt()
    }
    val prayerNextEpochMillis = prayerTargets.map {
        it.atZone(zone).toInstant().toEpochMilli()
    }
    val nextEpochMillis = target.atZone(zone).toInstant().toEpochMilli()
    val previousIshaa = days[todayDate.minusDays(1).toString()]?.toPrayerTimes()?.ishaa
    val current = if (currentIndex >= 0) {
        PrayerTimeEntry(labelResForIndex(currentIndex), times[currentIndex])
    } else {
        PrayerTimeEntry(R.string.ishaa, previousIshaa ?: today.ishaa)
    }
    val periodStart = if (currentIndex >= 0) {
        LocalDateTime.of(todayDate, times[currentIndex])
    } else if (previousIshaa != null) {
        LocalDateTime.of(todayDate.minusDays(1), previousIshaa)
    } else {
        LocalDateTime.of(todayDate, LocalTime.MIDNIGHT)
    }
    val periodStartEpochMillis = periodStart.atZone(zone).toInstant().toEpochMilli()

    return PrayerTileModel(
        today = times.mapIndexed { index, time ->
            PrayerTimeEntry(labelResForIndex(index), time)
        },
        currentIndex = currentIndex,
        current = current,
        nextIndex = if (nextIsTomorrow) -1 else nextTodayIndex,
        next = PrayerTimeEntry(labelResForIndex(nextIndexInList), nextTime),
        countdownMinutes = countdownMinutes,
        dateText = dateText,
        hijriText = hijriText,
        hijriDateText = hijriDateText,
        hijriDay = hijriDay,
        hijriMonth = hijriMonth ?: "",
        hijriMonthYear = hijriMonthYear,
        hijriYear = hijriYear,
        prayerCountdownMinutes = prayerCountdownMinutes,
        prayerNextEpochMillis = prayerNextEpochMillis,
        nextEpochMillis = nextEpochMillis,
        periodStartEpochMillis = periodStartEpochMillis,
        periodEndEpochMillis = nextEpochMillis
    )
}
