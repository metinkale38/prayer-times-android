package com.metinkale.prayerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.metinkale.prayerapp.vakit.WidgetService;

/**
 * Created by metin on 20.03.2016.
 */
public class UserPresentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WidgetService.class));
        MainIntentService.rescheduleAlarms(context);
    }
}
