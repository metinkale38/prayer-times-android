package com.metinkale.prayerapp.vakit;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Times.Alarm;

import java.io.File;

public class AlarmReceiver extends IntentService
{

    private static boolean sInterrupt;

    public AlarmReceiver()
    {
        super("AlarmReceiver");
    }

    public static void silenter(Context c, long dur)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        boolean silent = prefs.getString("silenterType", "silent").equals("silent");
        AudioManager aum = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int ringermode = aum.getRingerMode();
        if(ringermode != AudioManager.RINGER_MODE_SILENT && (ringermode != AudioManager.RINGER_MODE_VIBRATE || silent))
        {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

            Intent i;
            if(ringermode == AudioManager.RINGER_MODE_VIBRATE)
            {
                i = new Intent(c, setVibrate.class);
            } else
            {
                i = new Intent(c, setNormal.class);
            }

            PendingIntent service = PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * dur, service);

            aum.setRingerMode(silent ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_VIBRATE);

        }
    }

    public static MediaPlayer play(Context c, Alarm alarm)
    {
        Uri uri = null;
        try
        {

            String path = null;
            switch(alarm.sound)
            {
                case "ezan":
                    switch(alarm.vakit)
                    {

                        case IMSAK:
                        case SABAH:
                            path = "ezan/asehitoglu/sabah.mp3";
                            break;
                        case GUNES:
                            if(alarm.early <= 0) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            else path = "ezan/asehitoglu/sabah.mp3";
                            break;
                        case OGLE:
                            path = "ezan/asehitoglu/ogle.mp3";
                            break;
                        case IKINDI:
                            path = "ezan/asehitoglu/ikindi.mp3";
                            break;
                        case AKSAM:
                            path = "ezan/asehitoglu/aksam.mp3";
                            break;
                        case YATSI:
                            path = "ezan/asehitoglu/yatsi.mp3";
                            break;

                    }
                    break;
                case "sela":
                    path = "ezan/asehitoglu/sala.mp3";
                    break;
                case "dua":
                    path = "ezan/asehitoglu/ezanduasi.mp3";
                    break;
                default:
                    uri = Uri.parse(alarm.sound);

                    break;
            }
            if(path != null)
            {
                Crashlytics.setString("sound", path);
                Crashlytics.getInstance().answers.logCustom(new CustomEvent("oldsound"));
                File file = new File(c.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), path);
                if(!file.exists()) return null;
                uri = Uri.fromFile(file);
            }

            MediaPlayer mp = new MediaPlayer();
            mp.setLooping(false);
            mp.setDataSource(c, uri);
            mp.setAudioStreamType(getStreamType(c));

            mp.prepare();
            mp.start();

            return mp;
        } catch(Exception e)
        {
            Crashlytics.setString("data", uri.toString());
            Crashlytics.logException(e);
        }

        return null;
    }

    public static void setAlarm(Context c, Alarm alarm)
    {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(c, WakefulReceiver.class);
        i.putExtra("bdl", alarm.toBundle());
        PendingIntent service = PendingIntent.getBroadcast(c, (int) (alarm.vakit == null ? 0 : alarm.vakit.ordinal() + alarm.city * 10 + (alarm.time - alarm.time % (1000 * 60 * 60 * 24)) / 1000), i, PendingIntent.FLAG_UPDATE_CURRENT);

        App.setExact(am, AlarmManager.RTC_WAKEUP, alarm.time, service);

    }


    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            fireAlarm(intent);
        } catch(Exception e)
        {
            App.e(e);
        }

        Times.setAlarms();
    }


    public static int getStreamType(Context c)
    {
        String ezanvolume = PreferenceManager.getDefaultSharedPreferences(c).getString("ezanvolume", "noti");
        switch(ezanvolume)
        {
            case "alarm":
                return AudioManager.STREAM_ALARM;
            case "media":
                return AudioManager.STREAM_MUSIC;
            default:
                return AudioManager.STREAM_RING;

        }
    }

    public void fireAlarm(Intent intent)
    {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

        final Context c = App.getContext();

        if(intent == null || !intent.hasExtra("bdl"))
        {
            return;
        }
        final Alarm next = Alarm.fromBundle(intent.getExtras().getBundle("bdl"));
        intent.removeExtra("bdl");


        Times t = MainHelper.getTimes(next.city);
        if(!"TEST".equals(next.pref) && (t != null && next.pref != null && !t.is(next.pref)))
        {
            return;
        }

        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(next.city + "", NotIds.ALARM);
        String text = "Ezan";

        if(t != null)
        {
            text = t.getName() + " (" + t.getSource() + ")";
        }

        String txt;
        if(next.early != 0)
        {

            String[] left_part = App.getContext().getResources().getStringArray(R.array.lefttext_part);
            txt = App.getContext().getString(R.string.earlytext, left_part[next.vakit.index], next.early);
        } else if(next.vakit != null)
        {
            txt = next.vakit.getString();
        } else
        {
            txt = next.name;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c).setContentTitle(text).setContentText(txt).setContentIntent(Main.getPendingIntent(t)).setSmallIcon(R.drawable.ic_abicon);
        Notification not = builder.build();

        if(next.vibrate)
        {
            not.vibrate = new long[]{0, 300, 150, 300, 150, 500};
        }

        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);

        int volume = -2;

        class MPHolder
        {
            MediaPlayer mp = null;
        }
        final MPHolder mp = new MPHolder();
        if(next.sound != null && !next.sound.startsWith("silent") && !next.sound.startsWith("picker"))
        {

            if(next.sound.contains("$volume"))
            {
                volume = Integer.parseInt(next.sound.substring(next.sound.indexOf("$volume") + 7));
                next.sound = next.sound.substring(0, next.sound.indexOf("$volume"));
            }
            if(volume != -2)
            {
                int oldvalue = am.getStreamVolume(getStreamType(c));
                am.setStreamVolume(getStreamType(c), volume, 0);
                volume = oldvalue;
            }
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if(!pm.isScreenOn())
            {
                Intent i = new Intent(c, NotificationPopup.class);
                i.putExtra("city", next.city);
                i.putExtra("name", text);
                i.putExtra("vakit", txt);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                c.startActivity(i);
            }

            mp.mp = play(c, next);

            if(mp.mp != null)
            {

                mp.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer)
                    {
                        mp.mp.stop();
                        mp.mp.release();
                        mp.mp = null;
                    }
                });

                mp.mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
                {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer)
                    {
                        mp.mp.stop();
                        mp.mp.release();
                        mp.mp = null;
                        Log.e("", "scompleted");
                    }
                });

                not.deleteIntent = PendingIntent.getBroadcast(c, 0, new Intent(c, Audio.class), PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        nm.notify(next.city + "", NotIds.ALARM, not);

        sInterrupt = false;
        while(mp.mp != null && mp.mp.isPlaying())
        {
            if(sInterrupt)
            {
                mp.mp.stop();
                mp.mp.release();
                mp.mp = null;
            }
        }

        if(!sInterrupt && next.dua != null && !next.dua.startsWith("silent"))
        {

            next.sound = next.dua;
            next.dua = "silent";
            next.vibrate = false;
            Intent i = new Intent(AlarmReceiver.this, WakefulReceiver.class);
            i.putExtra("bdl", next.toBundle());
            sendBroadcast(i);

        }
        sInterrupt = false;

        if(NotificationPopup.instance != null) NotificationPopup.instance.finish();


        if(volume != -2)
        {
            am.setStreamVolume(getStreamType(c), volume, 0);

        }

        if(next.silenter != 0)
        {
            silenter(c, next.silenter);
        }


        wakeLock.release();
    }


    public static class Audio extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            sInterrupt = true;

        }
    }

    public static class setNormal extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        }
    }

    public static class setVibrate extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context c, Intent i)
        {
            AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    public static class WakefulReceiver extends WakefulBroadcastReceiver
    {
        public WakefulReceiver()
        {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle bdl = intent.getBundleExtra("bdl");
            if(bdl != null)
            {
                Intent service = new Intent(context, AlarmReceiver.class);
                service.putExtra("bdl", bdl);
                startWakefulService(context, service);
            } else
            {
                MainIntentService.setAlarms(context);
            }
        }
    }

}
