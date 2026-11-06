package com.metinkale.prayer.wear

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

private const val WEAR_SYNC_WORK = "wear-times-sync"

fun scheduleWearSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<WearTimesSyncWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WEAR_SYNC_WORK,
        ExistingWorkPolicy.REPLACE,
        request
    )
}
