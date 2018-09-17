package com.metinkale.prayerapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.metinkale.prayer.R;

public final class NotificationUtils {
    public static final int ALARM = 1;
    public static final int ONGOING = 2;
    public static final int PLAYING = 3;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getAlarmChannel(Context c) {
        CharSequence name = c.getString(R.string.prayerNotification);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel("alarm", name, importance);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getOngoingChannel(Context c) {
        CharSequence name = c.getString(R.string.ongoingNotification);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel("ongoing", name, importance);
        channel.setShowBadge(false);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        return channel;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getPlayingChannel(Context c) {
        CharSequence name = c.getString(R.string.sound);
        int importance = NotificationManager.IMPORTANCE_MAX;
        NotificationChannel channel = new NotificationChannel("sound", name, importance);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        return channel;
    }

}
