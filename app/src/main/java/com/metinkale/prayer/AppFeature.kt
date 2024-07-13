package com.metinkale.prayer

import com.metinkale.prayer.receiver.AppEventListener

object AppFeature : Feature {
    override val name: String = "app"
    override val appEventListeners: List<AppEventListener> =
        listOf(AppBroadcastReceiver, AliasManager)
}