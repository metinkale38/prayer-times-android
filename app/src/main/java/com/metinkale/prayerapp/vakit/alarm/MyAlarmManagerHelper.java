package com.metinkale.prayerapp.vakit.alarm;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MyAlarmManagerHelper extends MyAlarmManager {
    public static final String HELPER_PKG = "com.metinkale.prayer.helper.alarm";
    private static final String HELPER_RECEIVER = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver";
    private static final String ACTION_SET = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.SET";
    private static final String ACTION_SETEXACT = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.SETEXACT";
    private static final String ACTION_CANCEL = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.CANCEL";
    private final Context context;

    protected MyAlarmManagerHelper(Context c) {
        super();
        context = c;
    }


    public void set(final int type, final long time, final PendingIntent operation) {
        Intent i = new Intent(ACTION_SET);
        i.setPackage(HELPER_PKG);
        i.setComponent(new ComponentName(HELPER_PKG, HELPER_RECEIVER));
        i.putExtra("type", type);
        i.putExtra("time", time);
        i.putExtra("operation", operation);
        context.sendBroadcast(i);
    }


    public void setExact(final int type, final long time, final PendingIntent operation) {
        Intent i = new Intent(ACTION_SETEXACT);
        i.setPackage(HELPER_PKG);
        i.setComponent(new ComponentName(HELPER_PKG, HELPER_RECEIVER));
        i.putExtra("type", type);
        i.putExtra("time", time);
        i.putExtra("operation", operation);
        context.sendBroadcast(i);
    }

    public void cancel(final PendingIntent operation) {
        Intent i = new Intent(ACTION_CANCEL);
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        i.setPackage(HELPER_PKG);
        i.setComponent(new ComponentName(HELPER_PKG, HELPER_RECEIVER));
        i.putExtra("operation", operation);
        context.sendBroadcast(i);
    }

}
