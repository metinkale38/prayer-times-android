package com.metinkale.prayer.wear.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.metinkale.prayer.sync.SyncJson
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.complications.PrayerComplicationsUpdater
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private val Context.wearTimesDataStore: DataStore<WearTimesPayload?> by dataStore(
    fileName = "wear_times.json",
    serializer = WearTimesSerializer
)

object WearTimesSerializer : Serializer<WearTimesPayload?> {
    override val defaultValue: WearTimesPayload? = null

    override suspend fun readFrom(input: InputStream): WearTimesPayload? {
        val text = input.readBytes().decodeToString()
        return if (text.isEmpty()) null else SyncJson.decode(text)
    }

    override suspend fun writeTo(t: WearTimesPayload?, output: OutputStream) {
        if (t != null) {
            output.write(SyncJson.encode(t).encodeToByteArray())
        }
    }
}

class WearTimesRepository private constructor(
    private val context: Context,
    private val dataStore: DataStore<WearTimesPayload?>
) {
    val payload: Flow<WearTimesPayload?> = dataStore.data.catch { emit(null) }

    suspend fun save(payload: WearTimesPayload) {
        dataStore.updateData { payload }
        PrayerComplicationsUpdater.requestUpdates(context)
    }

    companion object {
        @Volatile
        private var instance: WearTimesRepository? = null

        fun get(context: Context): WearTimesRepository =
            instance ?: synchronized(this) {
                instance ?: WearTimesRepository(
                    context.applicationContext,
                    context.applicationContext.wearTimesDataStore
                ).also { instance = it }
            }
    }
}
