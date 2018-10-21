package com.metinkale.prayer.times.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.NotificationUtils;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;

import androidx.core.app.NotificationCompat;

public class AlarmUtils {


    public static Notification buildAlarmNotification(Context c, Alarm alarm, long time) {
        Times t = alarm.getCity();
        String text = t.getName() + " (" + t.getSource() + ")";

        String txt = alarm.getTitle();

        if (BuildConfig.DEBUG) {
            long difference = System.currentTimeMillis() - time;
            if (difference < 5000) {
                txt += " " + difference + "ms";
            } else if (difference < 3 * 60 * 1000) {
                txt += " " + difference / 1000 + "s";
            } else {
                txt += " " + difference / 1000 / 60 + "m";
            }
        }

        androidx.core.app.NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new androidx.core.app.NotificationCompat.Builder(c, NotificationUtils.getAlarmChannel(c).getId());
        } else {
            builder = new NotificationCompat.Builder(c);
        }

        builder = builder.setContentTitle(text)
                .setContentText(txt)
                .setContentIntent(TimesFragment.getPendingIntent(t))
                .setSmallIcon(R.drawable.ic_abicon)
                .setWhen(time);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationUtils.getAlarmChannel(c).getId());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        return builder.build();
    }

    public static Notification buildPlayingNotification(Context c, Alarm alarm, long time) {
        Times t = alarm.getCity();
        String text = t.getName() + " (" + t.getSource() + ")";

        String txt = alarm.getTitle();

        if (BuildConfig.DEBUG) {
            long difference = System.currentTimeMillis() - time;
            if (difference < 5000) {
                txt += " " + difference + "ms";
            } else if (difference < 3 * 60 * 1000) {
                txt += " " + difference / 1000 + "s";
            } else {
                txt += " " + difference / 1000 / 60 + "m";
            }
        }

        PendingIntent stopIndent = PendingIntent.getBroadcast(c, 0, new Intent(c, AlarmService.StopAlarmPlayerReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(c, NotificationUtils.getAlarmChannel(c).getId());
        } else {
            builder = new NotificationCompat.Builder(c);
        }

        builder = builder.setContentTitle(text)
                .setContentText(txt)
                .setContentIntent(TimesFragment.getPendingIntent(t))
                .setSmallIcon(R.drawable.ic_abicon)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_stop, c.getString(R.string.stop), stopIndent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                .setWhen(time);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationUtils.getAlarmChannel(c).getId());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MAX);
        }
        return builder.build();
    }
}
