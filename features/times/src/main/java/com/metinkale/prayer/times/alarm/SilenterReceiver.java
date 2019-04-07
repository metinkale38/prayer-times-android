package com.metinkale.prayer.times.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.metinkale.prayer.Preferences;

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
