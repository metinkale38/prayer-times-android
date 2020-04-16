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

package com.metinkale.prayer.times.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.metinkale.prayer.times.R;


public final class NotificationUtils {

    public static String getAlarmChannel(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);

            NotificationChannel channel = notificationManager.getNotificationChannel("alarm");
            if (channel == null) {
                CharSequence name = c.getString(R.string.prayerNotification);
                int importance = NotificationManager.IMPORTANCE_LOW;
                channel = new NotificationChannel("alarm", name, importance);

                notificationManager.createNotificationChannel(channel);
            }
            return channel.getId();
        }
        return "alarm";
    }

    public static String getOngoingChannel(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel("ongoing");
            if (channel == null) {
                CharSequence name = c.getString(R.string.ongoingNotification);
                int importance = NotificationManager.IMPORTANCE_LOW;
                channel = new NotificationChannel("ongoing", name, importance);
                channel.setShowBadge(false);
                notificationManager.createNotificationChannel(channel);
            }
            return channel.getId();
        }
        return "ongoing";
    }


    public static String getPlayingChannel(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel("sound");
            if (channel == null) {
                CharSequence name = c.getString(R.string.sound);
                channel = new NotificationChannel("sound", name, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            return channel.getId();
        }
        return "sound";
    }



}
