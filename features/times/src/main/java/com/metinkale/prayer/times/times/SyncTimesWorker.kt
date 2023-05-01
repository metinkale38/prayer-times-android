package com.metinkale.prayer.times.times

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.*
import com.metinkale.prayer.CrashReporter
import java.util.concurrent.TimeUnit

class SyncTimesWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {
        return try {
            val successAll = Times.current.mapNotNull { it.dayTimes as? DayTimesWebProvider }
                .filter { it.syncedDays < 15 }.map { it.sync() }.any { !it }

            ContextCompat.getMainExecutor(applicationContext).execute { Times.setAlarms() }
            if (successAll) Result.success() else Result.retry()
        } catch (e: Throwable) {
            CrashReporter.recordException(e)
            Result.retry()
        }
    }

    companion object {
        val ONE_SHOT_WORK_REQUEST = OneTimeWorkRequestBuilder<SyncTimesWorker>()
            .build()

        private val NETWORK_CONSTRAINT =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val DAILY_WORK_REQUEST = PeriodicWorkRequestBuilder<SyncTimesWorker>(1, TimeUnit.DAYS)
            .setConstraints(NETWORK_CONSTRAINT).build()

        fun syncNow(ctx: Context) {
            WorkManager.getInstance(ctx).enqueue(ONE_SHOT_WORK_REQUEST)
        }

        fun scheduleWorker(ctx: Context) {
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                SyncTimesWorker::class.simpleName!!,
                ExistingPeriodicWorkPolicy.KEEP,
                DAILY_WORK_REQUEST
            )
        }

    }

}
