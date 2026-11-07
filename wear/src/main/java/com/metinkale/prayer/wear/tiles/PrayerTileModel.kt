package com.metinkale.prayer.wear.tiles

import android.content.Context
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.data.WearTimesRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first

internal data class PrayerTimeEntry(
    val labelRes: Int,
    val time: LocalTime
)

internal data class PrayerTileModel(
    val today: List<PrayerTimeEntry>,
    val currentIndex: Int,
    val nextIndex: Int,
    val next: PrayerTimeEntry,
    val nextIsTomorrow: Boolean,
    val countdownMinutes: Int,
    val justStarted: Boolean
)

private data class PrayerTimes(
    val fajr: LocalTime,
    val sun: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val ishaa: LocalTime
) {
    fun asList(): List<LocalTime> = listOf(fajr, sun, dhuhr, asr, maghrib, ishaa)
}

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
    val justStarted = currentIndex >= 0 && currentIndex != 1 &&
        Duration.between(
            LocalDateTime.of(todayDate, times[currentIndex]),
            now
        ).seconds in 0..59

    return PrayerTileModel(
        today = times.mapIndexed { index, time ->
            PrayerTimeEntry(labelResForIndex(index), time)
        },
        currentIndex = currentIndex,
        nextIndex = if (nextIsTomorrow) -1 else nextTodayIndex,
        next = PrayerTimeEntry(labelResForIndex(nextIndexInList), nextTime),
        nextIsTomorrow = nextIsTomorrow,
        countdownMinutes = countdownMinutes,
        justStarted = justStarted
    )
}
