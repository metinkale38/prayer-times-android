package dev.metinkale.prayer.wear

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class PrayerData(
    val city: String,
    val days: List<DayData>
) {
    val today = LocalDate.now().let { today -> days.find { it.gregDate == today } }
}

@Serializable
data class DayData(
    val gregDate: LocalDate,
    val hijriDate: HijriDate,
    val fajr: LocalTime,
    val sun: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val ishaa: LocalTime
)

@Serializable
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int
)
