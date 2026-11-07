package com.metinkale.prayer.wear.sync

import android.content.Context
import android.util.Log
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
    suspend fun requestSync(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val nodes = Tasks.await(
                Wearable.getNodeClient(appContext).connectedNodes,
                5,
                TimeUnit.SECONDS
            )
            nodes.forEach { node ->
                Tasks.await(
                    Wearable.getMessageClient(appContext)
                        .sendMessage(node.id, SyncConstants.REQUEST_PATH, ByteArray(0)),
                    5,
                    TimeUnit.SECONDS
                )
            }
        }
    }

    suspend fun fetchExisting(context: Context): WearTimesPayload? = withContext(Dispatchers.IO) {
        runCatching {
            val appContext = context.applicationContext
            val dataItems = Tasks.await(
                Wearable.getDataClient(appContext).dataItems,
                10,
                TimeUnit.SECONDS
            )
            try {
                val dataItem = dataItems.firstOrNull {
                    it.uri.path == SyncConstants.DATA_PATH
                } ?: run {
                    Log.d("WearTimesSync", "fetch: no data item at ${SyncConstants.DATA_PATH}")
                    return@runCatching null
                }
                val json = DataMapItem.fromDataItem(dataItem)
                    .dataMap.getString(SyncConstants.KEY_JSON)
                    ?: return@runCatching null
                val payload = SyncJson.decode(json)
                Log.d("WearTimesSync", "fetch: days=${payload?.days?.size ?: -1}")
                payload
            } finally {
                dataItems.release()
            }
        }.getOrNull()
    }
}
