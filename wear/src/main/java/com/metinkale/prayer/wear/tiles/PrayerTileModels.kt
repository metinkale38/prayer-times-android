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

internal data class PrayerTimeEntry(
    val labelRes: Int,
    val time: LocalTime
)

internal data class PrayerTileModel(
    val today: List<PrayerTimeEntry>,
    val currentIndex: Int,
    val current: PrayerTimeEntry,
    val nextIndex: Int,
    val next: PrayerTimeEntry,
    val countdownMinutes: Int,
    val dateText: String,
    val hijriText: String,
    val hijriDateText: String,
    val hijriDay: String,
    val hijriMonth: String,
    val hijriMonthYear: String,
    val hijriYear: String,
    val prayerCountdownMinutes: List<Int>,
    val prayerNextEpochMillis: List<Long>,
    val nextEpochMillis: Long,
    val countdownRoundUp: Boolean,
    val periodStartEpochMillis: Long,
    val periodEndEpochMillis: Long
)

internal data class PrayerTimes(
    val fajr: LocalTime,
    val sun: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val ishaa: LocalTime
) {
    fun asList(): List<LocalTime> = listOf(fajr, sun, dhuhr, asr, maghrib, ishaa)
}

