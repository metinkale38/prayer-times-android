package com.metinkale.prayer.times.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import com.metinkale.prayer.Module;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.utils.Utils;

import androidx.annotation.NonNull;

public class MyAlarmManager {
    private final AlarmManager alarmManager;
    private final Context context;
    
    private MyAlarmManager(@NonNull Context c) {
        this.context = c;
        this.alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    }
    
    protected MyAlarmManager() {
        alarmManager = null;
        context = null;
    }
    
    public static MyAlarmManager with(Context c) {
        if (useHelper(c)) {
            return new MyAlarmManagerHelper(c);
        }
        return new MyAlarmManager(c);
    }
    
    public static boolean useHelper(Context c) {
        return Utils.isPackageInstalled(c, MyAlarmManagerHelper.HELPER_PKG) && !Preferences.USE_ALARM.get();
    }
    
    
    public void set(int type, long time, PendingIntent service) {
        alarmManager.set(type, time, service);
    }
    
    public void setExact(int type, long time, PendingIntent service) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && Preferences.USE_ALARM.get()) {
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(time,
                    PendingIntent.getActivity(context, 0, Module.TIMES.buildIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));
            alarmManager.setAlarmClock(info, service);
        } else if (type == AlarmManager.RTC_WAKEUP && Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(type, time, service);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(type, time, service);
        } else {
            alarmManager.set(type, time, service);
        }
    }
    
    public void cancel(PendingIntent service) {
        alarmManager.cancel(service);
    }
    
}
