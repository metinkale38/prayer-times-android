package com.metinkale.prayer.wear.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.metinkale.prayer.sync.DhikrDefaults
import com.metinkale.prayer.sync.WearDhikr
import com.metinkale.prayer.sync.mergeWearDhikrs
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dhikrDataStore: DataStore<List<WearDhikr>> by dataStore(
    fileName = "wear_dhikr.json",
    serializer = DhikrListSerializer
)

private object DhikrListSerializer : Serializer<List<WearDhikr>> {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val defaultValue: List<WearDhikr> = emptyList()

    override suspend fun readFrom(input: InputStream): List<WearDhikr> {
        val text = input.readBytes().decodeToString()
        if (text.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(WearDhikr.serializer()), text)
        }.getOrDefault(emptyList())
    }

    override suspend fun writeTo(t: List<WearDhikr>, output: OutputStream) {
        output.write(
            json.encodeToString(ListSerializer(WearDhikr.serializer()), t)
                .encodeToByteArray()
        )
    }
}

class DhikrRepository private constructor(
    private val dataStore: DataStore<List<WearDhikr>>
) {
    val dhikrs: Flow<List<WearDhikr>> = dataStore.data
        .catch { emit(emptyList()) }
        .map { list ->
            list.filterNot { it.deleted }.sortedBy { it.position }
        }

    val allDhikrs: Flow<List<WearDhikr>> = dataStore.data
        .catch { emit(emptyList()) }
        .map { list -> list.sortedBy { it.position } }

    suspend fun ensureSyncIds() {
        val current = dataStore.data.first()
        if (current.none { it.syncId.isBlank() }) return
        val now = System.currentTimeMillis()
        dataStore.updateData { list ->
            list.map { dhikr ->
                if (dhikr.syncId.isBlank()) {
                    dhikr.copy(
                        syncId = UUID.randomUUID().toString(),
                        updatedAt = if (dhikr.updatedAt == 0L) now else dhikr.updatedAt
                    )
                } else {
                    dhikr
                }
            }
        }
    }

    suspend fun add(title: String, target: Int) {
        val cleanTitle = title.trim().take(20)
        if (cleanTitle.isEmpty()) return
        val now = System.currentTimeMillis()
        dataStore.updateData { current ->
            val nextPosition = (current.maxOfOrNull { it.position } ?: -1) + 1
            current + WearDhikr(
                syncId = UUID.randomUUID().toString(),
                title = cleanTitle,
                target = target.coerceIn(1, 100_000),
                position = nextPosition,
                color = DhikrDefaults.color,
                updatedAt = now
            )
        }
    }

    suspend fun increment(syncId: String) {
        val now = System.currentTimeMillis()
        dataStore.updateData { current ->
            current.map { dhikr ->
                if (dhikr.syncId == syncId && !dhikr.deleted) {
                    dhikr.copy(count = dhikr.count + 1, updatedAt = now)
                } else {
                    dhikr
                }
            }
        }
    }

    suspend fun reset(syncId: String) {
        val now = System.currentTimeMillis()
        dataStore.updateData { current ->
            current.map { dhikr ->
                if (dhikr.syncId == syncId && !dhikr.deleted) {
                    dhikr.copy(count = 0, updatedAt = now)
                } else {
                    dhikr
                }
            }
        }
    }

    suspend fun delete(syncId: String) {
        val now = System.currentTimeMillis()
        dataStore.updateData { current ->
            current.map { dhikr ->
                if (dhikr.syncId == syncId) {
                    dhikr.copy(deleted = true, updatedAt = now)
                } else {
                    dhikr
                }
            }
        }
    }

    suspend fun mergeRemote(remote: List<WearDhikr>) {
        val current = dataStore.data.first()
        val merged = mergeWearDhikrs(current, remote)
        if (merged != current) {
            dataStore.updateData { merged }
        }
    }

    companion object {
        @Volatile
        private var instance: DhikrRepository? = null

        fun get(context: Context): DhikrRepository =
            instance ?: synchronized(this) {
                instance ?: DhikrRepository(context.applicationContext.dhikrDataStore)
                    .also { instance = it }
            }
    }
}
