package com.metinkale.prayer.times.times

import android.content.SharedPreferences
import android.widget.Toast
import com.metinkale.prayer.App
import com.metinkale.prayer.times.OpenPrayerTimesDayTimesEndpoint
import com.metinkale.prayer.times.R
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.Days
import org.joda.time.LocalDate

class DayTimesWebProvider private constructor(val id: Int) :
    SharedPreferences.OnSharedPreferenceChangeListener,
    DayTimesProvider {

    private val prefs: SharedPreferences = App.get().getSharedPreferences("times_$id", 0)

    private val keys = MutableStateFlow(prefs.all.keys)
    private var lastSync: Long = 0
    private var deleted: Boolean = false

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)

        val monthStart = LocalDate.now().withDayOfMonth(1).toString()
        prefs.edit().also {
            prefs.all.keys.filter { it < monthStart }.forEach { date ->
                it.remove(date)
            }
        }.apply()

        MainScope().launch {
            Times.map { it.none { it.id == id } }.collect {
                if (it && !deleted) {
                    instances.remove(id)
                    prefs.edit().clear().apply()
                    deleted = true
                }
            }
        }
    }


    override fun get(key: LocalDate): DayTimes? =
        prefs.getString(key.toString(), null)
            ?.let { Json.decodeFromString(DayTimes.serializer(), it) }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        keys.update { prefs.all.keys }
    }


    suspend fun sync(): Boolean {
        if (!App.isOnline()) return false
        if (System.currentTimeMillis() - lastSync < 1000 * 60) return false
        if (deleted) return false
        val times = Times.getTimesById(id).current
        return if (times != null && times.source != Source.Calc) {
            lastSync = System.currentTimeMillis()
            val daytimes =
                OpenPrayerTimesDayTimesEndpoint(times.source).getDayTimes(times.key ?: "")
            prefs.edit().also {
                daytimes.forEach { dt ->
                    it.putString(dt.date.toString(), Json.encodeToString(dt))
                }
            }.apply()
            daytimes.size > 25
        } else false
    }


    fun syncAsync() {
        if (deleted) return
        if (!App.isOnline()) {
            Toast.makeText(App.get(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            return
        }

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch { sync() }
    }


    val syncedDays: Int
        get() = lastSyncedDay?.let {
            Days.daysBetween(LocalDate.now(), it).days
        } ?: 0
    val firstSyncedDay: LocalDate? get() = prefs.all.keys.minOrNull()?.let { LocalDate.parse(it) }
    val lastSyncedDay: LocalDate? get() = prefs.all.keys.maxOrNull()?.let { LocalDate.parse(it) }

    companion object {
        private val instances: MutableMap<Int, DayTimesWebProvider> = mutableMapOf()
        fun from(id: Int) = instances.getOrPut(id) { DayTimesWebProvider(id) }
    }
}