package com.metinkale.prayer.wear

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.metinkale.prayer.CrashReporter
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearDhikrSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            WearDhikrDataLayer.pushPhoneState(applicationContext)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CrashReporter.recordException(e, "wear-dhikr-sync")
            Result.retry()
        }
    }
}
