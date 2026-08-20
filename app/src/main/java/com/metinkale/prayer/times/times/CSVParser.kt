package com.metinkale.prayer.times.times

import androidx.core.net.toUri
import com.metinkale.prayer.App
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime

suspend fun getDayTimesFromCSV(key: String): List<DayTimes> {
    val year = java.time.LocalDate.now().year
    val content = if (key.startsWith("http://") || key.startsWith("https://")) {
        HttpClient().get(key).bodyAsText().lines()
    } else {
        withContext(Dispatchers.IO) {
            App.get().contentResolver.openInputStream(key.toUri())?.use {
                it.reader().readLines()
            } ?: emptyList()
        }
    }

    return content.map { it.replace("\"", "") }.mapNotNull {
        runCatching {
            val rows = it.split(';', ',', ';', '\t', ' ').filter { it.isNotBlank() }

            val date = rows[0].let {
                var parts = it.split(':', '-', '_').map { it.toInt() }

                if (parts.size == 3) {
                    if (parts[0] < parts[2]) parts = parts.reversed()
                    LocalDate(parts[0], parts[1], parts[2])
                } else if (parts.size == 2) {
                    LocalDate(year, parts[1], parts[0])
                } else {
                    return@mapNotNull null
                }
            }

            rows.drop(1).map {
                val parts = it.split(":", "-", "_").map { it.toInt() }
                LocalTime(parts[0], parts[1])
            }.let {
                DayTimes(
                    date = date.toJavaLocalDate(),
                    fajr = it[0].toJavaLocalTime(),
                    sun = it[1].toJavaLocalTime(),
                    dhuhr = it[2].toJavaLocalTime(),
                    asr = it[3].toJavaLocalTime(),
                    maghrib = it[4].toJavaLocalTime(),
                    ishaa = it[5].toJavaLocalTime(),
                    sabah = it.getOrNull(6)?.toJavaLocalTime(),
                    asrHanafi = it.getOrNull(7)?.toJavaLocalTime()
                )
            }
        }.getOrNull()
    }.toList()
}