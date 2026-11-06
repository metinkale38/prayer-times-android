package com.metinkale.prayer.wear.sync

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.metinkale.prayer.sync.DhikrSyncConstants
import com.metinkale.prayer.sync.DhikrSyncJson
import com.metinkale.prayer.sync.WearDhikr
import com.metinkale.prayer.sync.WearDhikrPayload
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WearDhikrDataLayer {
    suspend fun fetchExisting(context: Context): WearDhikrPayload? = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val dataItem = Tasks.await(
                Wearable.getDataClient(appContext)
                    .getDataItem(Uri.parse(DhikrSyncConstants.PHONE_DATA_PATH)),
                10,
                TimeUnit.SECONDS
            ) ?: return@runCatching null
            val json = DataMapItem.fromDataItem(dataItem)
                .dataMap.getString(DhikrSyncConstants.KEY_JSON)
                ?: return@runCatching null
            DhikrSyncJson.decode(json)
        }.getOrNull()
    }

    suspend fun push(context: Context, dhikrs: List<WearDhikr>) = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val payload = WearDhikrPayload(
                generatedAtEpochMillis = System.currentTimeMillis(),
                dhikrs = dhikrs
            )
            val data = DataMap().apply {
                putString(DhikrSyncConstants.KEY_JSON, DhikrSyncJson.encode(payload))
            }.toByteArray()
            val request = PutDataRequest.create(DhikrSyncConstants.WEAR_DATA_PATH).setData(data)
            Tasks.await(
                Wearable.getDataClient(appContext).putDataItem(request),
                10,
                TimeUnit.SECONDS
            )
        }
    }
}
