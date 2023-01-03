package com.metinkale.prayer.times.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime


fun LocalTime.toJoda(): org.joda.time.LocalTime =
    org.joda.time.LocalTime(hour, minute, second)


fun LocalDateTime.toJoda(): org.joda.time.LocalDateTime =
    org.joda.time.LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second)

fun LocalDate.toJoda(): org.joda.time.LocalDate =
    org.joda.time.LocalDate(year, monthNumber, dayOfMonth)