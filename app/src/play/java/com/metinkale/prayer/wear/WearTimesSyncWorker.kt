package com.metinkale.prayer.wear

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.sync.SyncConstants
import com.metinkale.prayer.sync.SyncJson
import com.metinkale.prayer.times.times.DayTimesCalcProvider
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sends the prayer times of the first city in the list to the Wear app.
 *
 * The payload contains the current month and, for calculated sources, the
 * following month as well. Web sources send whatever is cached after a sync.
 */
class WearTimesSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val times = Times.current.firstOrNull() ?: return@withContext Result.success()
            val provider = times.dayTimes

            if (provider is DayTimesWebProvider) {
                try {
                    provider.sync()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    CrashReporter.recordException(e, "wear-times-sync-provider")
                }
            }

            val today = LocalDate.now()
            val monthStart = today.withDayOfMonth(1)
            val start: LocalDate
            val end: LocalDate

            when (provider) {
                is DayTimesWebProvider -> {
                    val first = provider.firstSyncedDay
                    val last = provider.lastSyncedDay
                    if (first == null || last == null || last.isBefore(monthStart)) {
                        return@withContext Result.retry()
                    }
                    start = maxOf(monthStart, first)
                    end = last
                }

                is DayTimesCalcProvider -> {
                    start = monthStart
                    end = start.plusMonths(2).minusDays(1)
                }

                else -> return@withContext Result.retry()
            }

            val days = buildList {
                var date = start
                while (!date.isAfter(end)) {
                    provider.get(date)?.let { add(WearTimesPayloadFactory.day(date, it)) }
                    date = date.plusDays(1)
                }
            }

            if (days.isEmpty()) return@withContext Result.retry()

            val payload = WearTimesPayloadFactory.payload(times, days)
            val json = SyncJson.encode(payload)
            val data = DataMap().apply {
                putString(SyncConstants.KEY_JSON, json)
            }.toByteArray()

            val request = PutDataRequest.create(SyncConstants.DATA_PATH).setData(data)
            Tasks.await(
                Wearable.getDataClient(applicationContext).putDataItem(request),
                30,
                TimeUnit.SECONDS
            )
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CrashReporter.recordException(e, "wear-times-sync")
            Result.retry()
        }
    }
}
