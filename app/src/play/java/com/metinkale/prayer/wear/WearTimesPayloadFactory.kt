package com.metinkale.prayer.wear

import com.metinkale.prayer.Preferences
import com.metinkale.prayer.sync.WearCity
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.times.times.DayTimes
import com.metinkale.prayer.times.times.Times
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object WearTimesPayloadFactory {
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun day(date: LocalDate, times: DayTimes): WearDayTimes = WearDayTimes(
        date = date.toString(),
        fajr = times.fajr.format(timeFormat),
        sun = times.sun.format(timeFormat),
        dhuhr = times.dhuhr.format(timeFormat),
        asr = times.asr.format(timeFormat),
        maghrib = times.maghrib.format(timeFormat),
        ishaa = times.ishaa.format(timeFormat),
        asrHanafi = times.asrHanafi?.format(timeFormat),
        sabah = times.sabah?.format(timeFormat)
    )

    fun payload(times: Times, days: List<WearDayTimes>): WearTimesPayload = WearTimesPayload(
        generatedAtEpochMillis = System.currentTimeMillis(),
        city = WearCity(
            id = times.id,
            name = times.name,
            source = times.source.name,
            timezone = times.timezone,
            latitude = times.lat,
            longitude = times.lng
        ),
        days = days,
        countdownRoundUp = Preferences.COUNTDOWN_ROUND_UP,
        hijriCalculation = Preferences.HIJRI_CALCULATION,
        hijriDayOffset = Preferences.HIJRI_DAY_OFFSET
    )
}
