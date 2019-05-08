package com.metinkale.prayer.times.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.metinkale.prayer.utils.PermissionUtils;

public class SilenterReceiver extends BroadcastReceiver {
    public static void silent(Context c, int mins) {
        AudioManager aum = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int ringermode = aum.getRingerMode();
        if (ringermode != AudioManager.RINGER_MODE_SILENT) {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(c, SilenterReceiver.class);
            i.putExtra("mode", ringermode);

            PendingIntent service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 * mins), service);

            if (PermissionUtils.get(c).pNotPolicy)
                aum.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PermissionUtils.get(context).pNotPolicy && intent.hasExtra("mode")) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(intent.getIntExtra("mode", 0));
        }
    }
}
