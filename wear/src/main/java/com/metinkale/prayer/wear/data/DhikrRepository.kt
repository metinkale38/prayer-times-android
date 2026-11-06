package com.metinkale.prayer.wear.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.metinkale.prayer.sync.DhikrDefaults
import com.metinkale.prayer.sync.WearDhikr
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
        .map { list -> list.sortedBy { it.position } }

    suspend fun add(title: String, target: Int) {
        val cleanTitle = title.trim().take(20)
        if (cleanTitle.isEmpty()) return
        dataStore.updateData { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
            val nextPosition = (current.maxOfOrNull { it.position } ?: -1) + 1
            current + WearDhikr(
                id = nextId,
                title = cleanTitle,
                target = target.coerceIn(1, 100_000),
                position = nextPosition,
                color = DhikrDefaults.color
            )
        }
    }

    suspend fun increment(id: Long) {
        dataStore.updateData { current ->
            current.map { dhikr ->
                if (dhikr.id == id) dhikr.copy(count = dhikr.count + 1) else dhikr
            }
        }
    }

    suspend fun reset(id: Long) {
        dataStore.updateData { current ->
            current.map { dhikr ->
                if (dhikr.id == id) dhikr.copy(count = 0) else dhikr
            }
        }
    }

    suspend fun delete(id: Long) {
        dataStore.updateData { current -> current.filterNot { it.id == id } }
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
