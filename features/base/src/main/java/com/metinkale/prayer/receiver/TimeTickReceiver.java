/*
 * Copyright (c) 2013-2023 Metin Kale
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

package com.metinkale.prayer.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import com.metinkale.prayer.App;
import com.metinkale.prayer.MyAlarmManager;

import org.joda.time.DateTime;

public class TimeTickReceiver extends BroadcastReceiver {
    private final BroadcastReceiver mReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new TimeTickReceiverV26() : new TimeTickReceiverLegacy();
    private static int LAST_TIME_TICK; //avoid to often TIME_TICK

    @Override
    public void onReceive(Context context, Intent intent) {
        mReceiver.onReceive(context, intent);
    }

    private static class TimeTickReceiverV26 extends BroadcastReceiver {

        private final Context mCtx;
        private final IntentFilter mScreenOnOffFilter;
        private final IntentFilter mTimeTickFilter;
        private final IntentFilter mTimeChangedFilter;

        public TimeTickReceiverV26() {
            mCtx = App.get();
            mScreenOnOffFilter = new IntentFilter();
            mScreenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mScreenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
            mCtx.registerReceiver(this, mScreenOnOffFilter);

            mTimeChangedFilter = new IntentFilter();
            mTimeChangedFilter.addAction(Intent.ACTION_TIME_CHANGED);

            mTimeTickFilter = new IntentFilter();
            mTimeTickFilter.addAction(Intent.ACTION_TIME_TICK);
            PowerManager pm = (PowerManager) mCtx.getSystemService(Context.POWER_SERVICE);
            if (pm.isInteractive()) {
                mCtx.registerReceiver(this, mTimeTickFilter);
                mCtx.registerReceiver(this, mTimeChangedFilter);
            }


        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent == null ? null : intent.getAction();
            if (action == null)
                action = Intent.ACTION_TIME_TICK;

            switch (action) {
                case Intent.ACTION_SCREEN_OFF: {
                    mCtx.unregisterReceiver(this);
                    mCtx.registerReceiver(this, mScreenOnOffFilter);
                    break;
                }
                case Intent.ACTION_SCREEN_ON: {
                    mCtx.unregisterReceiver(this);
                    mCtx.registerReceiver(this, mScreenOnOffFilter);
                    mCtx.registerReceiver(this, mTimeTickFilter);
                    mCtx.registerReceiver(this, mTimeChangedFilter);
                }
                case Intent.ACTION_USER_PRESENT:
                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIME_TICK:
                default: {
                    int timeTick = (int) (System.currentTimeMillis() / 1000 / 60);
                    if (LAST_TIME_TICK != timeTick) {
                        InternalBroadcastReceiver.sender(mCtx).sendTimeTick();
                        LAST_TIME_TICK = timeTick;
                    }
                    break;
                }
            }
        }
    }

    private static class TimeTickReceiverLegacy extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int timeTick = (int) (System.currentTimeMillis() / 1000 / 60);
            if (LAST_TIME_TICK != timeTick) {
                InternalBroadcastReceiver.sender(context).sendTimeTick();
                LAST_TIME_TICK = timeTick;
            }

            MyAlarmManager am = MyAlarmManager.with(context);
            DateTime dt = DateTime.now();
            am.setExact(AlarmManager.RTC, dt.plusMinutes(1).withSecondOfMinute(0).withMillisOfSecond(0).getMillis(),
                    PendingIntent.getBroadcast(App.get(), 0, new Intent(App.get(), TimeTickReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    public static void start(Context c) {
        c.sendBroadcast(new Intent(c, TimeTickReceiver.class));
    }


}
