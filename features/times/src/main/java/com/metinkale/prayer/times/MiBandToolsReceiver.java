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


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.utils.NotificationUtils;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;


/**
 * this class is a personal feature, i have implemented for me, but it can also be used by others...
 * <p>
 * it shows the prayer times on your Mi Band 4, when you do a swipe gesture
 * <p>
 * How To:
 * 1) Buy/Install In Mi Band Tools and setup
 * 2) Add a Gesture Action with "Send Broadcast (Intent)"
 * Intent Action: com.metinkale.prayer.MIBANDACTION
 * Intent Package: com.metinkale.prayer
 * 3) Setup a Notification for Prayer App
 * 4) Enable Notifications for Prayer App
 */
public class MiBandToolsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Times times = null;
        for (Times t : Times.getTimes()) {
            if (times == null) {
                times = t;
            } else if (t.isOngoingNotificationActive() && !times.isOngoingNotificationActive()) {
                times = t;
            }
            // short choose first city, or if existent, first with ongoing notifications
        }

        if (times == null) return;

        String title = times.getName() + " - " + context.getString(R.string.appName);
        LocalDate date = LocalDate.now();
        StringBuilder builder = new StringBuilder().append("$^");
        int marker = times.getCurrentTime();
        if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
            marker = marker + 1;
        for (Vakit v : Vakit.values()) {
            if (v.ordinal() == marker)
                builder.append(v.getString().charAt(0)).append(" :-").append(LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime())).append("-$^");
            else
                builder.append(v.getString().charAt(0)).append(" : ").append(LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime())).append("$^");
        }
        builder.append("$^");
        builder.append("- ").append(LocaleUtils.formatPeriod(new Period(LocalDateTime.now(), times.getTime(date, times.getNextTime())), false)).append(" -");

        Notification noti = new NotificationCompat.Builder(context, getMiBandChannel(context))
                .setContentTitle(title)
                .setContentText(builder.toString())
                .setSmallIcon(R.drawable.ic_placeholder)
                .build();

        NotificationManager notMan = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notMan.notify("MIBAND", 0, noti);

        new Handler().postDelayed(() -> notMan.cancel("MIBAND", 0), 3000);

    }


    public static String getMiBandChannel(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel("miband");
            if (channel == null) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                channel = new NotificationChannel("miband", "Mi Band", importance);
                channel.setShowBadge(false);
                notificationManager.createNotificationChannel(channel);
            }
            return channel.getId();
        }
        return "miband";
    }

}
