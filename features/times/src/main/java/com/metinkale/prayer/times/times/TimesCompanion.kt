package com.metinkale.prayer.times.times

import android.content.SharedPreferences
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.times.utils.Store
import com.metinkale.prayer.times.utils.asStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


open class TimesCompanion : Flow<List<Times>> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences = App.get().getSharedPreferences("cities", 0)

    private val store: Store<List<Times>> by lazy {
        MutableStateFlow(prefs.all.keys.filter { it.startsWith("id") }
            .mapNotNull { fromSharedPrefs(it.substring(2).toLong().hashCode()) }).asStore()
            .map({ it -> it.sortedBy { it.sortId } }, { _, new -> new })
    }

    init {
        MainScope().launch {
            debounce(1000).distinctUntilChanged().collect {
                synchronized(this) {
                    val edit = prefs.edit()
                    edit.clear()
                    it.filter { it.id > 0 }.forEach {
                        val json = json.encodeToString(it)
                        edit.putString("id${it.id}", json)
                    }
                    edit.apply()
                }
            }
        }
    }


    fun size() = store.data.map { it.size }.distinctUntilChanged()

    fun getTimesById(id: Int): Store<Times?> =
        store.map(get = { it.firstOrNull { it.id == id } },
            set = { parent, save ->
                parent.map { if (it.id == save?.id) save else it }
            })

    fun getTimesByIndex(idx: Int): Store<Times?> =
        store.map(get = { it.getOrNull(idx) },
            set = { parent, save ->
                parent.mapIndexed { index, it -> if (save != null && index == idx) save else it }
            })

    fun add(times: Times) {
        store.update {
            if (it.isEmpty() && times.id > 0)
                listOf(times.copy(ongoing = true))
            else
                it + times
        }
    }

    fun queueAction(function: () -> Unit) {
        store.update {
            function()
            it
        }
    }

    fun delete(times: Times) {
        store.update { it.filter { it.id != times.id } }
    }

    fun deleteAll() {
        store.update { emptyList() }
    }

    fun clearTemporaryTimes() = store.update {
        it.filter { it.id > 0 }
    }


    private fun fromSharedPrefs(id: Int): Times? =
        App.get().getSharedPreferences("cities", 0).getString("id$id", null)
            ?.let {
                try {
                    json.decodeFromString(Times.serializer(), it).copy(id = id).migrate()
                } catch (e: Exception) {
                    CrashReporter.recordException(e)
                    e.printStackTrace()
                    null
                }
            }

    override suspend fun collect(collector: FlowCollector<List<Times>>) =
        store.data.collect(collector)

    val current get() = store.current

}

