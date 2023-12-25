package com.metinkale.prayer.times.times

import android.content.SharedPreferences
import android.widget.Toast
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.times.R
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DayTimesWebProvider private constructor(val id: Int) :
    SharedPreferences.OnSharedPreferenceChangeListener,
    DayTimesProvider {

    private val prefs: SharedPreferences = App.get().getSharedPreferences("times_$id", 0)

    private val minAdj
        get() = Times.getTimesById(id).current?.minuteAdj?.map { it.toLong() } ?: List(6) { 0L }
    private val tzAdj
        get() = Times.getTimesById(id).current?.timezone?.let { it * 60 }?.toLong() ?: 0

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
            ?.let {
                it.copy(
                    fajr = it.fajr.plusMinutes(minAdj[0] + tzAdj),
                    sabah = it.sabah?.plusMinutes(minAdj[0] + tzAdj),
                    sun = it.sun.plusMinutes(minAdj[1] + tzAdj),
                    dhuhr = it.dhuhr.plusMinutes(minAdj[2] + tzAdj),
                    asr = it.asr.plusMinutes(minAdj[3] + tzAdj),
                    asrHanafi = it.asrHanafi?.plusMinutes(minAdj[3] + tzAdj),
                    maghrib = it.maghrib.plusMinutes(minAdj[4] + tzAdj),
                    ishaa = it.ishaa.plusMinutes(minAdj[5] + tzAdj),
                )
            }


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
            val daytimes = times.source.getDayTimes(times.key ?: "")
            prefs.edit().also {
                daytimes.forEach { dt ->
                    it.putString(
                        dt.date.toString(),
                        Json.encodeToString(DayTimes.serializer(), DayTimes.from(dt))
                    )
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
        scope.launch {
            try {
                sync()
            } catch (e: Exception) {
                CrashReporter.recordException(e)
            }
        }
    }


    val syncedDays: Int
        get() = lastSyncedDay?.let { ChronoUnit.DAYS.between(LocalDate.now(), it).toInt() } ?: 0
    val firstSyncedDay: LocalDate? get() = prefs.all.keys.minOrNull()?.let { LocalDate.parse(it) }
    val lastSyncedDay: LocalDate? get() = prefs.all.keys.maxOrNull()?.let { LocalDate.parse(it) }

    companion object {
        private val instances: MutableMap<Int, DayTimesWebProvider> = mutableMapOf()
        fun from(id: Int) = instances.getOrPut(id) { DayTimesWebProvider(id) }
    }
}