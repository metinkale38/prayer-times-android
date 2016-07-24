/*
 * Copyright (c) 2016 Metin Kale
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

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.custom.VibrationPreference;
import com.metinkale.prayerapp.vakit.fragments.NotificationPopup;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Times.Alarm;

import java.io.IOException;


public class AlarmReceiver extends IntentService {

    private static boolean sInterrupt;

    public AlarmReceiver() {
        super("AlarmReceiver");
    }

    public static void silenter(Context c, long dur) {
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

            aum.setRingerMode(silent ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_VIBRATE);

        }
    }

    public static MediaPlayer play(Context c, String sound) throws IOException {
        Uri uri = null;
        uri = Uri.parse(sound);

        MediaPlayer mp = new MediaPlayer();
        mp.setLooping(false);
        mp.setDataSource(c, uri);
        mp.setAudioStreamType(getStreamType(c));

        mp.prepare();
        mp.start();
        return mp;
    }

    public static void setAlarm(Context c, Alarm alarm) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(c, WakefulReceiver.class);
        if (alarm != null) {
            i.putExtra("json", alarm.toString());
        }
        int id = (int) (((alarm.city / 1000 / 1000) * 1000) + (alarm.dayOffset * 100) + (alarm.vakit.ordinal() * 10) + (alarm.early ? (alarm.cuma ? 3 : 2) : 1));
        PendingIntent service = PendingIntent.getBroadcast(c, id, i, PendingIntent.
                FLAG_UPDATE_CURRENT);

        am.cancel(service);

        if (alarm != null) {
            App.setExact(am, AlarmManager.RTC_WAKEUP, alarm.time, service);
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
        wakeLock.release();
        if (NotificationPopup.instance != null) {
            NotificationPopup.instance.finish();
        }

        Times.setAlarms();
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

    public void fireAlarm(Intent intent) throws InterruptedException {


        Context c = App.getContext();

        if ((intent == null) || !intent.hasExtra("json")) {

            return;
        }
        Alarm next = Alarm.fromString(intent.getStringExtra("json"));
        intent.removeExtra("json");

        if (next.city == 0) {
            return;
        }

        Times t = Times.getTimes(next.city);
        boolean active = false;
        if (t != null) {
            if (next.cuma) {
                active = t.isCumaActive();
            } else if (next.early) {
                active = t.isEarlyNotificationActive(next.vakit);
            } else {
                active = t.isNotificationActive(next.vakit);
            }
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
        String text = "Ezan";

        if (t != null) {
            text = t.getName() + " (" + t.getSource() + ")";
        }

        String txt = "";
        if (next.early) {
            String[] left_part = App.getContext().getResources().getStringArray(R.array.lefttext_part);
            txt = App.getContext().getString(R.string.earlyText, left_part[next.vakit.index], t.getEarlyTime(next.vakit));
        } else if (next.cuma) {
            String[] left_part = App.getContext().getResources().getStringArray(R.array.lefttext_part);
            txt = App.getContext().getString(R.string.earlyText, left_part[next.vakit.index], t.getCumaTime());
        } else if (next.vakit != null) {
            txt = next.vakit.getString();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setContentTitle(text).setContentText(txt).setContentIntent(Main.getPendingIntent(t)).setSmallIcon(R.drawable.ic_abicon);
        Notification not = builder.build();

        if (vibrate) {
            not.vibrate = VibrationPreference.getPattern(c, "vibration");
        }

        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);


        class MPHolder {
            MediaPlayer mp;
        }
        not.deleteIntent = PendingIntent.getBroadcast(c, 0, new Intent(c, Audio.class), PendingIntent.FLAG_UPDATE_CURRENT);

        nm.notify(next.city + "", NotIds.ALARM, not);

        final MPHolder mp = new MPHolder();


        if ((sound != null) && !sound.startsWith("silent")) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()) {
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
        while ((sound != null) && !sound.startsWith("silent") && !sInterrupt) {
            int volume = -2;

            if ((sound != null) && !sound.startsWith("silent") && !sound.startsWith("picker")) {

                if (sound.contains("$volume")) {
                    volume = Integer.parseInt(sound.substring(sound.indexOf("$volume") + 7));
                    sound = sound.substring(0, sound.indexOf("$volume"));
                }
                if (volume != -2) {
                    int oldvalue = am.getStreamVolume(getStreamType(c));
                    am.setStreamVolume(getStreamType(c), volume, 0);
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
                            if ((mp == null) || (mp.mp == null)) {
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
                            if ((mp == null) || (mp.mp == null)) {
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

                        sound = null;
                        dua = null;
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                sInterrupt = false;


            }

            if (volume != -2) {
                am.setStreamVolume(getStreamType(c), volume, 0);
            }
            sound = dua;
            dua = null;
        }


        if (silenter != 0) {
            silenter(c, silenter);
        }


    }


    public static class Audio extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sInterrupt = true;

        }
    }

    public static class setNormal extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        }
    }

    public static class setVibrate extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent i) {
            AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    public static class WakefulReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("json");
            if (json != null) {
                Intent service = new Intent(context, AlarmReceiver.class);
                service.putExtra("json", json);
                startWakefulService(context, service);
            } else {
                MainIntentService.setAlarms(context);
            }
        }
    }

}
