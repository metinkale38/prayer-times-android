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

package com.metinkale.prayer.times;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import com.metinkale.prayer.App;
import com.metinkale.prayer.InternalBroadcastReceiver;

public class TimeTickReceiver extends BroadcastReceiver {
    
    private static int LAST_TIME_TICK; //avoid to often TIME_TICK
    
    private final Context mCtx;
    private IntentFilter mScreenOnOffFilter;
    private IntentFilter mTimeTickFilter;
    private IntentFilter mTimeChangedFilter;
    
    public TimeTickReceiver() {
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
        if (Build.VERSION.SDK_INT >= 20 && pm.isInteractive() || Build.VERSION.SDK_INT < 20 && pm.isScreenOn()) {
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
                } break;
            }
        }
    }
    
    public static void start(Context c) {
        c.sendBroadcast(new Intent(c, TimeTickReceiver.class));
    }
    
    
}
