package com.metinkale.prayerapp.vakit;

import android.content.Context;
import android.content.Intent;

import com.metinkale.prayer.InternalBroadcast;

public class InternalBroadcastReceiver extends InternalBroadcast.Receiver {

    public InternalBroadcastReceiver() {
        super(InternalBroadcast.ACTION_TIMETICK);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        if (intent == null || (action = intent.getAction()) == null) return;
        switch (action) {
            case InternalBroadcast.ACTION_TIMETICK:
                WidgetUtils.updateWidgets(context);
        }
    }

}
