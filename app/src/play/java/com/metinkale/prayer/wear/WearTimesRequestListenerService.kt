package com.metinkale.prayer.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.metinkale.prayer.sync.SyncConstants

class WearTimesRequestListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == SyncConstants.REQUEST_PATH) {
            scheduleWearSync(applicationContext)
        }
    }
}
