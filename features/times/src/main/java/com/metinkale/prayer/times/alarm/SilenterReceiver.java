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

package com.metinkale.prayer.times.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class SilenterReceiver extends BroadcastReceiver {
    public static void silent(Context c, int mins) {
        AudioManager aum = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int volume = aum.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (volume != 0) {
            MyAlarmManager am = MyAlarmManager.with(c);

            Intent i = new Intent(c, SilenterReceiver.class);
            i.putExtra("volume", volume);

            PendingIntent service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 * mins), service);

            aum.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, intent.getIntExtra("volume", 0), 0);
    }
}
