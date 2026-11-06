package com.metinkale.prayer.wear.sync

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.metinkale.prayer.sync.SyncConstants
import com.metinkale.prayer.sync.SyncJson
import com.metinkale.prayer.sync.WearTimesPayload
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WearTimesDataLayer {
    suspend fun fetchExisting(context: Context): WearTimesPayload? = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val dataItem = Tasks.await(
                Wearable.getDataClient(appContext).getDataItem(Uri.parse(SyncConstants.DATA_PATH)),
                10,
                TimeUnit.SECONDS
            ) ?: return@runCatching null
            val json = DataMapItem.fromDataItem(dataItem)
                .dataMap.getString(SyncConstants.KEY_JSON)
                ?: return@runCatching null
            SyncJson.decode(json)
        }.getOrNull()
    }
}
