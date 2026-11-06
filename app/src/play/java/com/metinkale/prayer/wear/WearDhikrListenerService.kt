package com.metinkale.prayer.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.metinkale.prayer.sync.DhikrSyncConstants
import com.metinkale.prayer.sync.DhikrSyncJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearDhikrListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                if (event.dataItem.uri.path != DhikrSyncConstants.WEAR_DATA_PATH) continue
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString(DhikrSyncConstants.KEY_JSON) ?: continue
                val payload = DhikrSyncJson.decode(json) ?: continue
                scope.launch {
                    runCatching {
                        WearDhikrDataLayer.mergeWearState(applicationContext, payload)
                    }
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
