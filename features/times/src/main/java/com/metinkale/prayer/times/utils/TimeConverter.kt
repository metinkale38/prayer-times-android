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


fun org.joda.time.LocalTime.toJava(): LocalTime =
    LocalTime(hourOfDay, minuteOfHour, secondOfMinute)


fun org.joda.time.LocalDateTime.toJava(): LocalDateTime =
    LocalDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute)

fun org.joda.time.LocalDate.toJava(): LocalDate =
    LocalDate(year, monthOfYear, dayOfMonth)

