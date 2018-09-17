package com.metinkale.prayer.helper.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

public class MyAlarmManager {
    private final AlarmManager alarmManager;

    MyAlarmManager(@NonNull Context c) {
        this.alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    }


    public void set(int type, long time, PendingIntent service) {
        alarmManager.set(type, time, service);
    }

    public void setExact(int type, long time, PendingIntent service) {
        if (type == AlarmManager.RTC_WAKEUP && Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(type, time, service);
        } else /*if (Build.VERSION.SDK_INT >= 19)*/ {
            alarmManager.setExact(type, time, service);
        }/* else {
            alarmManager.set(type, time, service);
        }*/
    }

    public void cancel(PendingIntent service) {
        alarmManager.cancel(service);
    }

}
