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

package com.metinkale.prayer.helper.alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyAlarmManagerReceiver extends BroadcastReceiver {
    private static final String ACTION_SET = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.SET";
    private static final String ACTION_SETEXACT = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.SETEXACT";
    private static final String ACTION_CANCEL = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.CANCEL";
    private static final String ACTION_CALLBACK = "com.metinkale.prayer.helper.alarm.MyAlarmManagerReceiver.CALLBACK";
    private static final String TAG = "prayertimes-alarmhelper";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        if (intent == null || intent.getAction() == null) return;
        Log.e(TAG, intent.getAction());
        switch (intent.getAction()) {
            case ACTION_SET:
                if (!intent.hasExtra("type") || !intent.hasExtra("time") || !intent.hasExtra("operation")) {
                    Log.e(TAG, "Intent does not have sufficent extras for ACTION_SET");
                }
                new MyAlarmManager(context).set(intent.getIntExtra("type", 0),
                        intent.getLongExtra("time", 0),
                        (PendingIntent) intent.getParcelableExtra("operation"));
                break;
            case ACTION_SETEXACT:
                if (!intent.hasExtra("type") || !intent.hasExtra("time") || !intent.hasExtra("operation")) {
                    Log.e(TAG, "Intent does not have sufficent extras for ACTION_SETEXACT");
                }
                new MyAlarmManager(context).setExact(intent.getIntExtra("type", 0),
                        intent.getLongExtra("time", 0),
                        (PendingIntent) intent.getParcelableExtra("operation"));

                break;
            case ACTION_CANCEL:
                if (!intent.hasExtra("operation")) {
                    Log.e(TAG, "Intent does not have sufficent extras for ACTION_CANCEL");
                }
                new MyAlarmManager(context).cancel((PendingIntent) intent.getParcelableExtra("operation"));
                break;
            default:
                Log.e(TAG, "unknown action");
        }
    }

}
