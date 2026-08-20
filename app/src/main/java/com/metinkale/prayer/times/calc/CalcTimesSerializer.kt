package com.metinkale.prayer.times.calc

import dev.metinkale.calctimes.CalcTimes
import dev.metinkale.calctimes.HighLatsAdjustment
import dev.metinkale.calctimes.Method
import dev.metinkale.calctimes.MethodBuilder
import dev.metinkale.calctimes.Midnight
import kotlinx.datetime.TimeZone

fun CalcTimes.serialize(): String = listOf(
    "$latitude",
    "$longitude",
    elevation.takeIf { it != 0.0 }?.let { "$it" } ?: "",
    timezone.id.replace("/", "::"),
    serializeMethod(method))
    .joinToString(";")

fun deserializeCalcTimes(key: String): CalcTimes = key.split(";").let {
    CalcTimes(
        it[0].toDouble(),
        it[1].toDouble(),
        it[2].toDoubleOrNull() ?: 0.0,
        TimeZone.of(it[3].replace("::", "/")),
        deserializeMethod(it.drop(4).joinToString(";"))
    )
}

private fun serializeMethod(method: Method): String = method.shortName ?: listOf(
    method.highLats.name,
    method.midnight.name,
    method.imsakAngle?.toString(),
    method.imsakMinute.takeIf { it != 0 }?.toString(),
    method.fajrAngle?.toString(),
    method.fajrMinute.takeIf { it != 0 }?.toString(),
    method.sunriseMinute.takeIf { it != 0 }?.toString(),
    method.dhuhrMinute.takeIf { it != 0 }?.toString(),
    method.asrShafiMinute.takeIf { it != 0 }?.toString(),
    method.asrHanafiMinute.takeIf { it != 0 }?.toString(),
    method.sunsetMinutes.takeIf { it != 0 }?.toString(),
    method.maghribAngle?.toString(),
    method.maghribMinute.takeIf { it != 0 }?.toString(),
    method.ishaaAngle?.toString(),
    method.ishaaMinute.takeIf { it != 0 }?.toString()
).joinToString(";") { it.orEmpty() }

private fun deserializeMethod(value: String): MethodBuilder =
    Method.values().find { it.shortName == value } ?: value.split(";").let {
        Method(
            highLats = HighLatsAdjustment.valueOf(it[0]),
            midnight = Midnight.valueOf(it[1]),
            imsakAngle = it[2].toDoubleOrNull(),
            imsakMinute = it[3].toIntOrNull() ?: 0,
            fajrAngle = it[4].toDoubleOrNull(),
            fajrMinute = it[5].toIntOrNull() ?: 0,
            sunriseMinute = it[6].toIntOrNull() ?: 0,
            dhuhrMinute = it[7].toIntOrNull() ?: 0,
            asrShafiMinute = it[8].toIntOrNull() ?: 0,
            asrHanafiMinute = it[9].toIntOrNull() ?: 0,
            sunsetMinutes = it[10].toIntOrNull() ?: 0,
            maghribAngle = it[11].toDoubleOrNull(),
            maghribMinute = it[12].toIntOrNull() ?: 0,
            ishaaAngle = it[13].toDoubleOrNull(),
            ishaaMinute = it[14].toIntOrNull() ?: 0
        )
    }