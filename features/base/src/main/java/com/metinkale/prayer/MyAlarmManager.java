/*
 * Copyright (c) 2013-2019 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

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
