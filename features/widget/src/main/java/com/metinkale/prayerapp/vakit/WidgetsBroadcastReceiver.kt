package com.metinkale.prayerapp.vakit

import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.receiver.InternalBroadcastReceiver.OnForegroundListener
import com.metinkale.prayer.receiver.InternalBroadcastReceiver.OnTimeTickListener

class WidgetsBroadcastReceiver : InternalBroadcastReceiver(), OnForegroundListener,
    OnTimeTickListener {
    override fun onForeground() {
        WidgetUtils.updateWidgets(context)
    }

    override fun onTimeTick() {
        WidgetUtils.updateWidgets(context)
    }
}