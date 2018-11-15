package com.metinkale.prayer.times.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.metinkale.prayer.times.R;

import androidx.annotation.RequiresApi;


public final class NotificationUtils {
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getAlarmChannel(Context c) {
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        NotificationChannel channel = notificationManager.getNotificationChannel("alarm");
        if (channel == null) {
            CharSequence name = c.getString(R.string.prayerNotification);
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel("alarm", name, importance);
            
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getOngoingChannel(Context c) {
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        NotificationChannel channel = notificationManager.getNotificationChannel("ongoing");
        if (channel == null) {
            CharSequence name = c.getString(R.string.ongoingNotification);
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel("ongoing", name, importance);
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }
    
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getPlayingChannel(Context c) {
        NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
        NotificationChannel channel = notificationManager.getNotificationChannel("sound");
        if (channel == null) {
            CharSequence name = c.getString(R.string.sound);
            channel = new NotificationChannel("sound", name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }
    
}
