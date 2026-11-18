package com.metinkale.prayer.wear.watchface

import com.metinkale.prayer.sync.WearCity
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.R
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PrayerDialTest {
    private val zone = ZoneId.of("Europe/Berlin")

    private fun day(
        date: String,
        fajr: String = "04:30",
        sun: String = "06:10",
        dhuhr: String = "13:15",
        asr: String = "17:00",
        maghrib: String = "19:45",
        ishaa: String = "21:15"
    ) = WearDayTimes(
        date = date,
        fajr = fajr,
        sun = sun,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        ishaa = ishaa
    )

    private fun payload(days: List<WearDayTimes>) = WearTimesPayload(
        generatedAtEpochMillis = 0L,
        city = WearCity(1, "Berlin", "Diyanet", 2.0, 52.52, 13.405),
        days = days
    )

    private fun days(vararg overrides: WearDayTimes) = listOf(
        "2026-04-14",
        "2026-04-15",
        "2026-04-16",
        "2026-04-17"
    ).map { date -> overrides.firstOrNull { it.date == date } ?: day(date) }

    private fun now(hour: Int, minute: Int, dayOfMonth: Int = 16) =
        LocalDateTime.of(2026, 4, dayOfMonth, hour, minute).atZone(zone)

    private fun assertAngle(expected: Double, actual: Float) =
        assertEquals(expected, actual.toDouble(), 0.01)

    @Test
    fun `midday has six segments and dhuhr at the bottom`() {
        val dial = buildPrayerDial(payload(days()), now(13, 30))!!
        assertEquals(7, dial.segments.size)
        assertNull(dial.segments.first().label)
        assertEquals(
            listOf(R.string.fajr, R.string.sun, R.string.zuhr, R.string.asr, R.string.maghrib, R.string.ishaa),
            dial.segments.mapNotNull { it.label?.labelRes }
        )
        assertAngle(60.0, dial.segments[1].labelAngle!!)
        assertAngle(180.0, dial.segments[3].labelAngle!!)
        assertAngle(65.0, dial.segments[1].startAngle)
        assertAngle(185.0, dial.segments[3].startAngle)
        assertEquals("04:30", dial.segments[1].label!!.timeText)
        assertEquals("21:15", dial.segments[6].label!!.timeText)
    }

    @Test
    fun `angles increase clockwise and ishaa segment wraps`() {
        val dial = buildPrayerDial(payload(days()), now(13, 30))!!
        dial.segments.forEach {
            assertTrue(it.labelAngle in 0f..360f)
            assertTrue(it.startAngle in 0f..360f)
            assertTrue(it.sweepAngle >= 1f)
            assertTrue(it.fillSweepAngle in 0f..it.sweepAngle)
        }
        val night = dial.segments.first()
        assertAngle(0.0, night.startAngle)
        assertAngle(60.0, night.sweepAngle)
        val ishaa = dial.segments.last()
        assertAngle(317.41, ishaa.startAngle)
        assertAngle(42.59, ishaa.sweepAngle)
        assertAngle(312.41, ishaa.labelAngle!!)
        assertAngle(360.0, ishaa.startAngle + ishaa.sweepAngle)
    }

    @Test
    fun `after midnight the night segment is split across the anchor`() {
        val dial = buildPrayerDial(payload(days()), now(3, 0))!!
        val night = dial.segments.first()
        assertNull(night.label)
        assertTrue(night.fillSweepAngle > 0f)
        assertTrue(night.fillSweepAngle < night.sweepAngle)
        assertEquals(0f, dial.segments[1].fillSweepAngle)
        assertEquals(0f, dial.segments[6].fillSweepAngle)
    }

    @Test
    fun `before the anchor the previous day is shown`() {
        val dial = buildPrayerDial(payload(days()), now(0, 0))!!
        val night = dial.segments.first()
        assertEquals(night.sweepAngle, night.fillSweepAngle)
        val ishaa = dial.segments.last()
        assertEquals(R.string.ishaa, ishaa.label!!.labelRes)
        assertTrue(ishaa.fillSweepAngle > 0f)
        assertTrue(ishaa.fillSweepAngle < ishaa.sweepAngle)
    }

    @Test
    fun `fill follows the current interval`() {
        val beforeFajr = buildPrayerDial(payload(days()), now(4, 29))!!
        assertTrue(beforeFajr.segments[0].fillSweepAngle < beforeFajr.segments[0].sweepAngle)
        assertEquals(0f, beforeFajr.segments[1].fillSweepAngle)

        val atFajr = buildPrayerDial(payload(days()), now(4, 30))!!
        assertEquals(atFajr.segments[0].sweepAngle, atFajr.segments[0].fillSweepAngle)
        assertEquals(0f, atFajr.segments[1].fillSweepAngle)

        val atSun = buildPrayerDial(payload(days()), now(6, 10))!!
        assertEquals(atSun.segments[1].sweepAngle, atSun.segments[1].fillSweepAngle)
        assertEquals(0f, atSun.segments[2].fillSweepAngle)

        val afterIshaa = buildPrayerDial(payload(days()), now(21, 30))!!
        assertTrue(afterIshaa.segments.drop(1).take(5).all { it.fillSweepAngle == it.sweepAngle })
        assertEquals(afterIshaa.segments[0].sweepAngle, afterIshaa.segments[0].fillSweepAngle)
        assertTrue(afterIshaa.segments[6].fillSweepAngle > 0f)
        assertTrue(afterIshaa.segments[6].fillSweepAngle < afterIshaa.segments[6].sweepAngle)
    }

    @Test
    fun `now angle matches the current position`() {
        assertAngle(180.0, buildPrayerDial(payload(days()), now(13, 15))!!.nowAngle)
        assertAngle(312.41, buildPrayerDial(payload(days()), now(21, 15))!!.nowAngle)
    }

    @Test
    fun `missing previous day returns null`() {
        val incomplete = days().filter { it.date != "2026-04-15" }
        assertNull(buildPrayerDial(payload(incomplete), now(13, 30)))
    }

    @Test
    fun `missing next day returns null`() {
        val incomplete = days().filter { it.date != "2026-04-17" }
        assertNull(buildPrayerDial(payload(incomplete), now(13, 30)))
    }

    @Test
    fun `non monotonic times return null`() {
        val broken = days(day("2026-04-16", fajr = "07:00", sun = "06:10"))
        assertNull(buildPrayerDial(payload(broken), now(13, 30)))
    }

    @Test
    fun `gap is cut from every segment`() {
        val dial = buildPrayerDial(payload(days()), now(13, 30))!!
        val prayers = dial.segments.filter { it.label != null }
        prayers.forEach {
            assertTrue(it.startAngle != it.labelAngle)
        }
        val fajr = dial.segments[1]
        assertAngle(17.86, fajr.sweepAngle)
        assertAngle(60.0, fajr.labelAngle!!)
    }
}
