package com.metinkale.prayer.wear.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.metinkale.prayer.sync.SyncConstants
import com.metinkale.prayer.sync.SyncJson
import com.metinkale.prayer.wear.data.WearTimesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearTimesListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                if (event.dataItem.uri.path != SyncConstants.DATA_PATH) continue
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString(SyncConstants.KEY_JSON) ?: continue
                val payload = SyncJson.decode(json) ?: continue
                scope.launch {
                    WearTimesRepository.get(applicationContext).save(payload)
                }
            }
        } finally {
            dataEvents.release()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
