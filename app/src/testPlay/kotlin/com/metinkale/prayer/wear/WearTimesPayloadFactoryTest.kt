package com.metinkale.prayer.wear

import com.metinkale.prayer.times.times.DayTimes
import java.time.LocalDate
import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WearTimesPayloadFactoryTest {
    @Test
    fun `maps day times to exchange format`() {
        val date = LocalDate.of(2026, 11, 5)
        val times = DayTimes(
            date = date,
            fajr = LocalTime.of(5, 24),
            sun = LocalTime.of(7, 13),
            dhuhr = LocalTime.of(12, 7),
            asr = LocalTime.of(14, 20),
            maghrib = LocalTime.of(16, 50),
            ishaa = LocalTime.of(18, 26)
        )

        val day = WearTimesPayloadFactory.day(date, times)

        assertEquals("2026-11-05", day.date)
        assertEquals("05:24", day.fajr)
        assertEquals("07:13", day.sun)
        assertEquals("12:07", day.dhuhr)
        assertEquals("14:20", day.asr)
        assertEquals("16:50", day.maghrib)
        assertEquals("18:26", day.ishaa)
    }
}
