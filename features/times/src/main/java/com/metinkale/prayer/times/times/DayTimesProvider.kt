package com.metinkale.prayer.times.times

import org.joda.time.LocalDate

interface DayTimesProvider {
    fun get(key: LocalDate): DayTimes?
}