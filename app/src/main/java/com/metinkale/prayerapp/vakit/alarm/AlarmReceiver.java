/*
 * Copyright (c) 2013-2017 Metin Kale
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

package com.metinkale.prayerapp.vakit.alarm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.VibrationPreference;
import com.metinkale.prayerapp.vakit.fragments.NotificationPopup;
import com.metinkale.prayerapp.vakit.fragments.VakitFragment;
import com.metinkale.prayerapp.vakit.sounds.MyPlayer;
import com.metinkale.prayerapp.vakit.times.Times;

import org.joda.time.LocalDateTime;

import java.util.concurrent.atomic.AtomicBoolean;


public class AlarmReceiver extends IntentService {
    private static final String EXTRA_ALARMID = "alarmId";
    private static final String EXTRA_TIME = "time";
    private static AtomicBoolean sInterrupt = new AtomicBoolean(false);

    private static Pair<Alarm, LocalDateTime> sLastSchedule;

    public AlarmReceiver() {
        super("AlarmReceiver");
    }


    public static void setAlarm(@NonNull Context c, @Nullable Pair<Alarm, LocalDateTime> alarm) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(c, WakefulReceiver.class);
        if (alarm != null) {
            if (alarm.equals(sLastSchedule)) return;

            if (!Build.MANUFACTURER.equals("samsung") || Build.VERSION.SDK_INT < 20) {
                sLastSchedule = alarm;
            } else {
                PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                if (pm.isInteractive()) {
                    sLastSchedule = alarm;
                }
            }

            long time = alarm.second.toDateTime().getMillis();

            i.putExtra(EXTRA_ALARMID, alarm.first.getId());
            i.putExtra(EXTRA_TIME, time);
            int id = alarm.hashCode();
            PendingIntent service = PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.cancel(service);

            App.setExact(am, AlarmManager.RTC_WAKEUP, time, service);
        }

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AlarmReceiver");
        wakeLock.acquire();
        try {
            fireAlarm(intent);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        Times.setAlarms();

        wakeLock.release();
        if (NotificationPopup.instance != null && Prefs.showNotificationScreen()) {
            NotificationPopup.instance.finish();
        }

    }

    public void fireAlarm(@Nullable Intent intent) throws InterruptedException {


        Context c = App.get();

        if ((intent == null) || !intent.hasExtra(EXTRA_ALARMID)) {
            return;
        }


        int alarmId = intent.getIntExtra(EXTRA_ALARMID, 0);


        long time = intent.getLongExtra(EXTRA_TIME, 0);


        Alarm alarm = Alarm.fromId(alarmId);

        if (alarm == null) return;

        intent.removeExtra(EXTRA_ALARMID);

        Times t = alarm.getCity();
        if (t == null) return;

        String notId = t.getID() + "";

        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notId, App.NotIds.ALARM);


        String text = t.getName() + " (" + t.getSource() + ")";

        String txt = alarm.getTitle();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setContentTitle(text)
                .setContentText(txt)
                .setContentIntent(VakitFragment.getPendingIntent(t))
                .setSmallIcon(R.drawable.ic_abicon);
        if (alarm.isVibrate()) {
            builder.setVibrate(VibrationPreference.getPattern(c, "vibration"));
        }

        PendingIntent stopIndent = PendingIntent.getBroadcast(c, 0, new Intent(c, StopAlarmPlayerReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setOngoing(true).setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .addAction(R.drawable.ic_action_stop, getString(R.string.stop), stopIndent);
            Notification not = builder.build();
            startForeground(App.NotIds.PLAYING, not);
        } else {
            builder.setDeleteIntent(stopIndent);
            Notification not = builder.build();
            nm.notify(notId, App.NotIds.ALARM, not);
        }


        final MyPlayer player = MyPlayer.from(alarm);
        if (player == null) return;//no audio, nothing to do here


        if (Prefs.showNotificationScreen()) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 20 && !pm.isInteractive()
                    || Build.VERSION.SDK_INT < 20 && !pm.isScreenOn()) {
                Intent i = new Intent(c, NotificationPopup.class);
                i.putExtra("city", t.getID());
                i.putExtra("name", text);
                i.putExtra("vakit", txt);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                c.startActivity(i);

                Thread.sleep(1000);
            }
        }

        player.play();

        if (Prefs.stopByFacedown()) {
            StopByFacedownMgr.start(this, player);
        }

        sInterrupt.set(false);
        while (!sInterrupt.get() && player.isPlaying()) {
            Thread.sleep(500);
        }

        if (player.isPlaying()) {
            player.stop();
        }

        if (alarm.isRemoveNotification()) {
            nm.cancel(notId, App.NotIds.ALARM);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
            builder.setPriority(NotificationManager.IMPORTANCE_DEFAULT).setOngoing(false).setVibrate(null).mActions.clear();
            Notification not = builder.build();
            nm.notify(notId, App.NotIds.ALARM, not);
        }

        if (alarm.getSilenter() != 0) {
            SilenterReceiver.silent(c, alarm.getSilenter());
        }

    }


    public static class StopAlarmPlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sInterrupt.set(true);
        }
    }

    public static class WakefulReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            String alarm = intent.getStringExtra("alarm");
            if (alarm != null) {
                Intent service = new Intent(context, AlarmReceiver.class);
                service.putExtra("alarm", alarm);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(service);
                } else {
                    context.startService(service);
                }
            } else {
                Times.setAlarms();
            }
        }
    }

}
