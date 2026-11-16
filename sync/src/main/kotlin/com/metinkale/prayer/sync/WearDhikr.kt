package com.metinkale.prayer.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WearDhikr(
    val syncId: String = "",
    val title: String,
    val target: Int = 33,
    val count: Int = 0,
    val position: Int = 0,
    val color: Int = DhikrDefaults.color,
    val updatedAt: Long = 0L,
    val deleted: Boolean = false
)

@Serializable
data class WearDhikrPayload(
    val schemaVersion: Int = DhikrSyncConstants.SCHEMA_VERSION,
    val generatedAtEpochMillis: Long,
    val dhikrs: List<WearDhikr>
)

object DhikrDefaults {
    val color: Int = 0xFF33B5E5.toInt()
}

object DhikrSyncConstants {
    const val SCHEMA_VERSION = 1
    const val PHONE_DATA_PATH = "/dhikr/state"
    const val WEAR_DATA_PATH = "/dhikr/state/wear"
    const val KEY_JSON = "payload"
}

object DhikrSyncJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(payload: WearDhikrPayload): String = json.encodeToString(payload)

    fun decode(text: String): WearDhikrPayload? = runCatching {
        json.decodeFromString<WearDhikrPayload>(text)
            .takeIf { it.schemaVersion == DhikrSyncConstants.SCHEMA_VERSION }
    }.getOrNull()
}

/**
 * Item-wise last-write-wins merge. Tombstones participate so deletions survive
 * a round trip. Items with an empty sync id are local legacy rows and are kept
 * only when they already exist locally.
 */
fun mergeWearDhikrs(
    local: List<WearDhikr>,
    remote: List<WearDhikr>
): List<WearDhikr> {
    val merged = LinkedHashMap<String, WearDhikr>()

    local.forEach { dhikr ->
        if (dhikr.syncId.isNotBlank()) merged[dhikr.syncId] = dhikr
    }

    remote.forEach { dhikr ->
        if (dhikr.syncId.isBlank()) return@forEach
        val current = merged[dhikr.syncId]
        if (current == null || dhikr.updatedAt > current.updatedAt) {
            merged[dhikr.syncId] = dhikr
        }
    }

    return merged.values.sortedWith(compareBy({ it.position }, { it.title }))
}
