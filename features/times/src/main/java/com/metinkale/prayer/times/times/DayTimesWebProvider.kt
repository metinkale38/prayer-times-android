package com.metinkale.prayer.times.times

import android.content.SharedPreferences
import android.widget.Toast
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.times.R
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

class DayTimesWebProvider private constructor(val id: Int) :
    SharedPreferences.OnSharedPreferenceChangeListener,
    DayTimesProvider {

    private val prefs: SharedPreferences = App.get().getSharedPreferences("times_$id", 0)

    private val keys = MutableStateFlow(emptySet<String>())
    private var jobId: Int = -1
    private var lastSync: Long = 0
    private var deleted: Boolean = false

    init {
        keys.update { prefs.all.keys }

        prefs.registerOnSharedPreferenceChangeListener(this)

        val monthStart = LocalDate.now().withDayOfMonth(1).toString()
        prefs.edit().also {
            prefs.all.keys.filter { it < monthStart }.forEach { date ->
                it.remove(date)
            }
        }.apply()

        MainScope().launch {
            Times.map { it.none { it.ID == id } }.collect {
                if (it && !deleted) {
                    if (jobId != -1) JobManager.instance().cancel(jobId)
                    instances.remove(id)
                    prefs.edit().clear().apply()
                    deleted = true
                }
            }
        }

        scheduleJob()

    }


    override fun get(key: LocalDate): DayTimes? =
        prefs.getString(key.toString(), null)
            ?.let { Json.decodeFromString(DayTimes.serializer(), it) }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        keys.update { prefs.all.keys }
    }


    private suspend fun sync(): Boolean {
        if (deleted) return false
        val times = Times.getTimesById(id).value
        return if (times != null && times.source != Source.Calc) {
            val daytimes = times.source.getDayTimes(times.id ?: "").map { DayTimes.from(it) }

            prefs.edit().also {
                daytimes.forEach { dt ->
                    it.putString(dt.date.toString(), Json.encodeToString(dt))
                }
            }.apply()
            daytimes.size > 25
        } else false
    }

    private fun scheduleJob() {
        if (deleted) return
        val syncedDays = syncedDays
        JobManager.create(App.get())
        if (syncedDays == 0 && System.currentTimeMillis() - lastSync < 1000 * 60 * 60) {
            lastSync = System.currentTimeMillis()
            if (App.isOnline()) syncAsync() else jobId =
                JobRequest.Builder(SYNC_JOB_TAG + id)
                    .setExecutionWindow(1, TimeUnit.MINUTES.toMillis(3))
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setBackoffCriteria(
                        TimeUnit.MINUTES.toMillis(3),
                        JobRequest.BackoffPolicy.EXPONENTIAL
                    ).setUpdateCurrent(true).build()
                    .schedule()
        } else if (syncedDays < 3) jobId = JobRequest.Builder(SYNC_JOB_TAG + id)
            .setExecutionWindow(1, TimeUnit.HOURS.toMillis(3))
            .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED).setUpdateCurrent(true)
            .setBackoffCriteria(TimeUnit.HOURS.toMillis(1), JobRequest.BackoffPolicy.EXPONENTIAL)
            .build().schedule() else if (syncedDays < 10) jobId =
            JobRequest.Builder(SYNC_JOB_TAG + id)
                .setExecutionWindow(1, TimeUnit.DAYS.toMillis(3))
                .setBackoffCriteria(TimeUnit.DAYS.toMillis(1), JobRequest.BackoffPolicy.LINEAR)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED).setRequiresCharging(true)
                .setUpdateCurrent(true).build().schedule() else if (syncedDays < 20) jobId =
            JobRequest.Builder(SYNC_JOB_TAG + id)
                .setExecutionWindow(1, TimeUnit.DAYS.toMillis(10))
                .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .setBackoffCriteria(TimeUnit.DAYS.toMillis(3), JobRequest.BackoffPolicy.LINEAR)
                .setUpdateCurrent(true).build().schedule()
    }

    fun syncAsync() {
        if (deleted) return
        if (!App.isOnline()) {
            Toast.makeText(App.get(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            scheduleJob()
            return
        }
        // TODO use coroutines
        object : Thread("SyncWebTimes${id}") {
            override fun run() {
                try {
                    if (runBlocking { sync() }) {
                        App.get().handler.post { Times.setAlarms() }
                    }
                } catch (e: Exception) {
                    CrashReporter.recordException(e)
                }
            }
        }.start()
    }

    fun syncJob() = object : Job() {
        override fun onRunJob(params: Params): Result {
            if (!App.isOnline()) return Result.RESCHEDULE
            syncAsync()
            return Result.SUCCESS
        }

        override fun onReschedule(newJobId: Int) {
            jobId = newJobId
        }
    }


    private val syncedDays: Int
        get() = lastSyncedDay?.let {
            Days.daysBetween(LocalDate.now(), it).days
        } ?: 0
    val firstSyncedDay: LocalDate? get() = prefs.all.keys.minOrNull()?.let { LocalDate.parse(it) }
    val lastSyncedDay: LocalDate? get() = prefs.all.keys.maxOrNull()?.let { LocalDate.parse(it) }

    companion object {
        const val SYNC_JOB_TAG = "SyncJob"
        private val instances: MutableMap<Int, DayTimesWebProvider> = mutableMapOf()
        fun from(id: Int) = instances.getOrPut(id) { DayTimesWebProvider(id) }
    }
}