package com.metinkale.prayer.times.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.utils.NotificationUtils;
import com.metinkale.prayer.utils.PermissionUtils;

public class SilenterReceiver extends BroadcastReceiver {
    public static void silent(Context c, int mins) {
        if (!PermissionUtils.get(c).pNotPolicy) {

            androidx.core.app.NotificationCompat.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new androidx.core.app.NotificationCompat.Builder(c, NotificationUtils.getPlayingChannel(c));
            } else {
                builder = new NotificationCompat.Builder(c);
            }

            builder = builder.setContentTitle(c.getString(R.string.silenterNotificationTitle))
                    .setContentText(c.getString(R.string.silenterNotificationInfo))
                    .setContentIntent(PendingIntent.getActivity(c, 0, new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0))
                    .setSmallIcon(R.drawable.ic_abicon);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(NotificationUtils.getAlarmChannel(c));
            } else {
                builder.setPriority(Notification.PRIORITY_DEFAULT);
            }

            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify("silenter", 557457, builder.build());
            return;
        }


        AudioManager aum = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int ringermode = aum.getRingerMode();
        boolean modeVibrate = "vibrate".equals(Preferences.SILENTER_MODE.get());
        boolean isSilent = ringermode == AudioManager.RINGER_MODE_SILENT;
        boolean isVibrate = ringermode == AudioManager.RINGER_MODE_VIBRATE;
        if ((modeVibrate && !isVibrate && !isSilent) || (!modeVibrate && !isSilent)) {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(c, SilenterReceiver.class);
            i.putExtra("mode", ringermode);

            PendingIntent service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 * mins), service);


            aum.setRingerMode(modeVibrate ? AudioManager.RINGER_MODE_VIBRATE : AudioManager.RINGER_MODE_SILENT);
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
