package com.metinkale.prayer.times.times

import android.content.SharedPreferences
import android.util.Log
import com.metinkale.prayer.App
import com.metinkale.prayer.times.LocationReceiver
import com.metinkale.prayer.times.utils.UpdatableStateFlow
import com.metinkale.prayer.times.utils.asStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

open class TimesCompanion(private val state: MutableStateFlow<List<Times>> = MutableStateFlow(listOf())) :
    UpdatableStateFlow<List<Times>> by state.asStore(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefs: SharedPreferences = App.get().getSharedPreferences("cities", 0)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        MainScope().launch {
            state.update {
                prefs.all.keys.filter { it.startsWith("id") }
                    .mapNotNull { fromSharedPrefs(it.substring(2).toInt()) }.sortedBy { it.sortId }
            }

            prefs.registerOnSharedPreferenceChangeListener(this@TimesCompanion)

            map { it.any { it.autoLocation } }.distinctUntilChanged().filter { it }.collect {
                LocationReceiver.start(App.get())
            }
        }
    }

    fun getTimesById(id: Int): UpdatableStateFlow<Times?> =
        map(get = { it.firstOrNull { it.ID == id } },
            set = { parent, it -> parent.apply { it?.save() } })

    fun getTimesByIndex(idx: Int): UpdatableStateFlow<Times?> =
        map(get = { it.getOrNull(idx) }, set = { parent, it -> parent.apply { it?.save() } })


    private fun fromSharedPrefs(id: Int): Times? {
        val json = App.get().getSharedPreferences("cities", 0).getString("id$id", null)
        return json?.let {
            try {
                this.json.decodeFromString(Times.serializer(), json).copy(ID = id)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && key.startsWith("id")) {
            state.update { value ->
                val id = key.substring(2).toInt()
                (value.filter { it.ID != id } + fromSharedPrefs(id)).filterNotNull()
                    .sortedBy { it.sortId }
            }
        }
    }


    fun save(entry: Times) {
        Log.e("Test", entry.toString())
        if (entry.ID > 0) {
            val json = json.encodeToString(entry)
            prefs.edit().putString("id${entry.ID}", json).apply()
        } else {
            state.update { ((it.filter { it.ID != entry.ID }) + entry).sortedBy { it.sortId } }
        }
    }

    fun delete(times: Times) {
        prefs.edit().remove("id" + times.ID).apply()
    }

    fun clearTemporaryTimes() = state.update {
        it.filter { it.ID > 0 }
    }

}

object BooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BooleanLegacy", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return runCatching {
            decoder.decodeString() == "true"
        }.getOrNull() ?: runCatching {
            decoder.decodeInt() == 1
        }.getOrNull() ?: runCatching {
            decoder.decodeBoolean()
        }.getOrNull() ?: false
    }
}