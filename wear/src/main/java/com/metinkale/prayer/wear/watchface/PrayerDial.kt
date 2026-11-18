package com.metinkale.prayer.wear.watchface

import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

internal const val PrayerDialGapDegrees = 5f

internal data class PrayerDialLabel(
    val labelRes: Int,
    val timeText: String
)

internal data class PrayerDialSegment(
    val label: PrayerDialLabel?,
    val labelAngle: Float,
    val startAngle: Float,
    val sweepAngle: Float,
    val fillSweepAngle: Float
)

internal data class PrayerDial(
    val segments: List<PrayerDialSegment>,
    val nowAngle: Float
)

private val DialTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private val DialLabels = listOf(
    R.string.fajr,
    R.string.sun,
    R.string.zuhr,
    R.string.asr,
    R.string.maghrib,
    R.string.ishaa
)

private fun WearDayTimes.dialTimes(): List<LocalTime>? = runCatching {
    listOf(fajr, sun, dhuhr, asr, maghrib, ishaa).map(LocalTime::parse)
}.getOrNull()

private fun degrees(value: Double): Double = ((value % 360.0) + 360.0) % 360.0

internal fun buildPrayerDial(payload: WearTimesPayload, now: ZonedDateTime): PrayerDial? {
    val zone = now.zone
    val days = payload.days.associate { it.date to it.dialTimes() }

    fun timesOf(date: LocalDate): List<LocalTime>? = days[date.toString()]

    fun at(date: LocalDate, time: LocalTime): ZonedDateTime =
        LocalDateTime.of(date, time).atZone(zone)

    fun anchor(date: LocalDate): ZonedDateTime? {
        val maghrib = timesOf(date.minusDays(1))?.getOrNull(4) ?: return null
        val fajr = timesOf(date)?.getOrNull(0) ?: return null
        val from = at(date.minusDays(1), maghrib)
        val to = at(date, fajr)
        if (!to.isAfter(from)) return null
        return from.plus(Duration.between(from, to).dividedBy(2))
    }

    val today = now.toLocalDate()
    val midnight = anchor(today) ?: return null
    val day = if (now.isBefore(midnight)) today.minusDays(1) else today

    val start = if (day == today) midnight else anchor(day) ?: return null
    val noon = timesOf(day)?.getOrNull(2)?.let { at(day, it) } ?: return null
    val end = anchor(day.plusDays(1)) ?: return null
    if (!noon.isAfter(start) || !end.isAfter(noon)) return null
    if (now.isBefore(start) || !now.isBefore(end)) return null

    val dayTimes = timesOf(day) ?: return null
    val nextDate = day.plusDays(1)
    val nextFajr = timesOf(nextDate)?.getOrNull(0) ?: return null
    val events = dayTimes.map { at(day, it) } + at(nextDate, nextFajr)
    events.zipWithNext().forEach { (from, to) ->
        if (!to.isAfter(from)) return null
    }

    val startMillis = start.toInstant().toEpochMilli()
    val noonMillis = noon.toInstant().toEpochMilli()
    val endMillis = end.toInstant().toEpochMilli()
    val nowMillis = now.toInstant().toEpochMilli()

    fun angleOf(millis: Long): Double =
        if (millis < noonMillis) {
            180.0 * (millis - startMillis) / (noonMillis - startMillis)
        } else {
            180.0 + 180.0 * (millis - noonMillis) / (endMillis - noonMillis)
        }

    fun fillFraction(from: Long, to: Long): Double =
        ((nowMillis - from).toDouble() / (to - from)).coerceIn(0.0, 1.0)

    val gap = PrayerDialGapDegrees.toDouble()
    val fajrMillis = events[0].toInstant().toEpochMilli()
    val nightSweep = angleOf(fajrMillis)
    val night = PrayerDialSegment(
        label = null,
        labelAngle = 0f,
        startAngle = 0f,
        sweepAngle = nightSweep.toFloat(),
        fillSweepAngle = (nightSweep * fillFraction(startMillis, fajrMillis)).toFloat()
    )
    val prayerArcs = (0 until 6).map { index ->
        val from = events[index].toInstant().toEpochMilli()
        val to = events[index + 1].toInstant().toEpochMilli()
        val labelAngle = angleOf(from)
        val start = labelAngle + gap
        val endAngle = min(angleOf(to), 360.0)
        val arcEnd = if (index == 5) endMillis else to
        val sweep = max(1.0, endAngle - start)
        PrayerDialSegment(
            label = PrayerDialLabel(
                DialLabels[index],
                events[index].toLocalTime().format(DialTimeFormat)
            ),
            labelAngle = degrees(labelAngle).toFloat(),
            startAngle = degrees(start).toFloat(),
            sweepAngle = sweep.toFloat(),
            fillSweepAngle = (sweep * fillFraction(from, arcEnd)).toFloat()
        )
    }
    val segments = listOf(night) + prayerArcs

    return PrayerDial(
        segments = segments,
        nowAngle = degrees(angleOf(nowMillis)).toFloat()
    )
}
