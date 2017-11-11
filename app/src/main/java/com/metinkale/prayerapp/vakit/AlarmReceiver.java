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

package com.metinkale.prayerapp.vakit;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.utils.VibrationPreference;
import com.metinkale.prayerapp.vakit.fragments.NotificationPopup;
import com.metinkale.prayerapp.vakit.fragments.VakitFragment;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Times.Alarm;

import java.io.IOException;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;


public class AlarmReceiver extends IntentService implements SensorEventListener {

    private static boolean sInterrupt;
    private static Alarm sLastSchedule;

    public AlarmReceiver() {
        super("AlarmReceiver");
    }

    public static void silenter(@NonNull Context c, long dur) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean silent = "silent".equals(prefs.getString("silenterType", "silent"));
        AudioManager aum = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int ringermode = aum.getRingerMode();
        if ((ringermode != AudioManager.RINGER_MODE_SILENT) && ((ringermode != AudioManager.RINGER_MODE_VIBRATE) || silent)) {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

            Intent i;
            if (ringermode == AudioManager.RINGER_MODE_VIBRATE) {
                i = new Intent(c, setVibrate.class);
            } else {
                i = new Intent(c, setNormal.class);
            }

            PendingIntent service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60 * dur), service);

            if (PermissionUtils.get(c).pNotPolicy)
                aum.setRingerMode(silent ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_VIBRATE);


        }
    }

    @NonNull
    public static MediaPlayer play(@NonNull Context c, String sound) throws IOException {
        Uri uri = Uri.parse(sound);

        MediaPlayer mp = new MediaPlayer();
        mp.setLooping(false);
        mp.setDataSource(c, uri);
        mp.setAudioStreamType(getStreamType(c));

        mp.prepare();
        mp.start();
        return mp;
    }

    public static void setAlarm(@NonNull Context c, @Nullable Alarm alarm) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(c, WakefulReceiver.class);
        if (alarm != null) {
            if (alarm.equals(sLastSchedule)) return;

            if (!Build.MANUFACTURER.equals("samsung") || Build.VERSION.SDK_INT < 19) {
                sLastSchedule = alarm;
            } else {
                PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                if (pm.isInteractive()) {
                    sLastSchedule = alarm;
                }
            }

            i.putExtra("alarm", alarm.toJson());
            int id = alarm.hashCode();
            PendingIntent service = PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.cancel(service);
            long time = alarm.time;
            //if (Build.MANUFACTURER.equals("samsung") && Build.VERSION.SDK_INT >= 19) {
            //    time -= 5 * 60 * 1000;
            //}

            App.setExact(am, AlarmManager.RTC_WAKEUP, time, service);
        }

    }

    public static int getStreamType(Context c) {
        String ezanvolume = PreferenceManager.getDefaultSharedPreferences(c).getString("ezanvolume", "noti");
        switch (ezanvolume) {
            case "alarm":
                return AudioManager.STREAM_ALARM;
            case "media":
                return AudioManager.STREAM_MUSIC;
            default:
                return AudioManager.STREAM_RING;

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

        if ((intent == null) || !intent.hasExtra("alarm")) {

            return;
        }
        String json = intent.getStringExtra("alarm");
        Alarm next = Alarm.fromJson(json);

        long timeLeft = next.time - System.currentTimeMillis();

        if (timeLeft > 15 * 60 * 1000) {
            return; //will call Times.setAlarms in onHandleIntent
        } else if (timeLeft > 0) {
            do {
                try {
                    Thread.sleep(timeLeft);
                } catch (InterruptedException ignore) {
                }
            } while ((timeLeft = next.time - System.currentTimeMillis()) > 0);
        }


        intent.removeExtra("alarm");

        if (next.city == 0) {
            return;
        }

        Times t = Times.getTimes(next.city);
        if (t == null) return;
        boolean active;
        if (next.cuma) {
            active = t.isCumaActive();
        } else if (next.early) {
            active = t.isEarlyNotificationActive(next.vakit);
        } else {
            active = t.isNotificationActive(next.vakit);
        }
        if (!active) {
            return;
        }

        boolean vibrate;
        String sound;
        String dua;
        long silenter;
        if (next.cuma) {
            vibrate = t.hasCumaVibration();
            sound = t.getCumaSound();
            dua = "silent";
            silenter = t.getCumaSilenterDuration();
        } else if (next.early) {
            vibrate = t.hasEarlyVibration(next.vakit);
            sound = t.getEarlySound(next.vakit);
            dua = "silent";
            silenter = t.getEarlySilenterDuration(next.vakit);
        } else {
            vibrate = t.hasVibration(next.vakit);
            sound = t.getSound(next.vakit);
            dua = t.getDua(next.vakit);
            silenter = t.getSilenterDuration(next.vakit);
        }


        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(next.city + "", NotIds.ALARM);
        String text;

        text = t.getName() + " (" + t.getSource() + ")";

        String txt = "";
        if (next.early) {
            String[] left_part = App.get().getResources().getStringArray(R.array.lefttext_part);
            txt = App.get().getString(R.string.earlyText, left_part[next.vakit.index], "" + t.getEarlyTime(next.vakit));
        } else if (next.cuma) {
            String[] left_part = App.get().getResources().getStringArray(R.array.lefttext_part);
            txt = App.get().getString(R.string.earlyText, left_part[next.vakit.index], "" + t.getCumaTime());
        } else if (next.vakit != null) {
            txt = next.vakit.getString();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setContentTitle(text).setContentText(txt).setContentIntent(VakitFragment.getPendingIntent(t)).setSmallIcon(R.drawable.ic_abicon);
        Notification not = builder.build();

        if (vibrate) {
            not.vibrate = VibrationPreference.getPattern(c, "vibration");
        }

        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);


        class MPHolder {
            @Nullable
            MediaPlayer mp;
        }
        not.deleteIntent = PendingIntent.getBroadcast(c, 0, new Intent(c, Audio.class), PendingIntent.FLAG_UPDATE_CURRENT);

        nm.notify(next.city + "", NotIds.ALARM, not);

        final MPHolder mp = new MPHolder();

        //also play dua, if there is no adhan
        if (sound == null || sound.startsWith("silent")) {
            sound = dua;
            dua = "silent";
        }


        if (Prefs.showNotificationScreen() && (sound != null) && !sound.startsWith("silent")) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 20 && !pm.isInteractive()
                    || Build.VERSION.SDK_INT < 20 && !pm.isScreenOn()) {
                Intent i = new Intent(c, NotificationPopup.class);
                i.putExtra("city", next.city);
                i.putExtra("name", text);
                i.putExtra("vakit", txt);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                c.startActivity(i);

                Thread.sleep(1000);
            }
        }

        sInterrupt = false;
        boolean hasSound = false;
        while ((sound != null) && !sound.startsWith("silent") && !sInterrupt) {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            if (Prefs.stopByFacedown())
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
            int volume = -2;
            hasSound = true;

            if (!sound.startsWith("silent") && !sound.startsWith("picker")) {

                if (sound.contains("$volume")) {
                    volume = Integer.parseInt(sound.substring(sound.indexOf("$volume") + 7));
                    sound = sound.substring(0, sound.indexOf("$volume"));
                }
                if (volume != -2) {
                    int oldvalue = am.getStreamVolume(getStreamType(c));
                    try {
                        am.setStreamVolume(getStreamType(c), volume, 0);
                    } catch (SecurityException e) {
                        Crashlytics.logException(e);
                    }
                    volume = oldvalue;
                }


                try {
                    mp.mp = play(c, sound);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (next.cuma) {
                        t.setCumaSound("silent");
                    } else if (next.early) {
                        t.setEarlySound(next.vakit, "silent");
                    } else {
                        if ("sound".equals(t.getSound(next.vakit))) {
                            t.setSound(next.vakit, "silent");
                        } else {
                            t.setDua(next.vakit, "silent");
                        }
                    }
                    mp.mp = null;
                }

                if (mp.mp != null) {

                    mp.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if (mp.mp == null) {
                                return;
                            }
                            mp.mp.stop();
                            mp.mp.release();
                            mp.mp = null;
                        }
                    });

                    mp.mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mediaPlayer) {
                            if (mp.mp == null) {
                                return;
                            }
                            mp.mp.stop();
                            mp.mp.release();
                            mp.mp = null;
                        }
                    });

                }

                sInterrupt = false;

                while ((mp.mp != null) && mp.mp.isPlaying()) {
                    if (sInterrupt) {
                        mp.mp.stop();
                        mp.mp.release();
                        mp.mp = null;

                        dua = null;
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
                sInterrupt = false;


            }

            if (volume != -2) {
                try {
                    am.setStreamVolume(getStreamType(c), volume, 0);
                } catch (SecurityException e) {
                    Crashlytics.logException(e);
                }
            }
            sound = dua;
            dua = null;
            if (Prefs.stopByFacedown())
                sensorManager.unregisterListener(this);
        }

        if (hasSound && Prefs.autoRemoveNotification()) {
            nm.cancel(next.city + "", NotIds.ALARM);
        }
        if (silenter != 0) {
            silenter(c, silenter);
        }


    }


    private int mIsFaceDown = 1;

    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {

        if (event.values[2] < -3) {
            if (mIsFaceDown != 1) {//ignore if already was off
                mIsFaceDown += 2;
                if (mIsFaceDown >= 15) {//prevent accident
                    sInterrupt = true;
                }
            }
        } else {
            mIsFaceDown = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public static class Audio extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sInterrupt = true;

        }
    }

    public static class setNormal extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull Context context, Intent intent) {
            if (PermissionUtils.get(context).pNotPolicy) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
    }

    public static class setVibrate extends BroadcastReceiver {

        @Override
        public void onReceive(@NonNull Context c, Intent i) {
            if (PermissionUtils.get(c).pNotPolicy) {
                AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        }
    }

    public static class WakefulReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            String alarm = intent.getStringExtra("alarm");
            if (alarm != null) {
                Intent service = new Intent(context, AlarmReceiver.class);
                service.putExtra("alarm", alarm);
                startWakefulService(context, service);
            } else {
                Times.setAlarms();
            }
        }
    }

}
