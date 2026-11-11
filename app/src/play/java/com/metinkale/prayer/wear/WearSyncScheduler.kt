package com.metinkale.prayer.wear

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val WEAR_SYNC_WORK = "wear-times-sync"
private const val WEAR_DHIKR_SYNC_WORK = "wear-dhikr-sync"

fun scheduleWearSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<WearTimesSyncWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WEAR_SYNC_WORK,
        ExistingWorkPolicy.REPLACE,
        request
    )
}

fun scheduleDhikrSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<WearDhikrSyncWorker>()
        .setInitialDelay(500, TimeUnit.MILLISECONDS)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WEAR_DHIKR_SYNC_WORK,
        ExistingWorkPolicy.REPLACE,
        request
    )
}
