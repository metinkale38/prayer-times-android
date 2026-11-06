package com.metinkale.prayer.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Exchange format between the phone app and the Wear app.
 *
 * Times are stored as "HH:mm" strings so the payload is independent of
 * platform date/time serializers and can be validated by the receiver.
 */
@Serializable
data class WearTimesPayload(
    val schemaVersion: Int = SyncConstants.SCHEMA_VERSION,
    val generatedAtEpochMillis: Long,
    val city: WearCity,
    val days: List<WearDayTimes>
)

@Serializable
data class WearCity(
    val id: Int,
    val name: String,
    val source: String,
    val timezone: Double,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class WearDayTimes(
    val date: String,
    val fajr: String,
    val sun: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val ishaa: String,
    val asrHanafi: String? = null,
    val sabah: String? = null
)

object SyncConstants {
    const val SCHEMA_VERSION = 1
    const val DATA_PATH = "/prayer-times/times"
    const val KEY_JSON = "payload"
}

object SyncJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(payload: WearTimesPayload): String = json.encodeToString(payload)

    fun decode(text: String): WearTimesPayload? = runCatching {
        json.decodeFromString<WearTimesPayload>(text)
            .takeIf { it.schemaVersion == SyncConstants.SCHEMA_VERSION }
    }.getOrNull()
}
