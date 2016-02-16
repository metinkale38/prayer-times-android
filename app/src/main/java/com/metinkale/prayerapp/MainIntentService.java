package com.metinkale.prayerapp;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.text.format.Time;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.sounds.Sounds;
import com.metinkale.prayerapp.vakit.times.Times;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class MainIntentService extends IntentService {

    private static final String ACTION_DOWNLOAD_HADIS = "com.metinkale.prayer.action.DOWNLOAD_HADIS";
    private static final String ACTION_DOWNLOAD_SOUND = "com.metinkale.prayer.action.DOWNLOAD_SOUND";
    private static final String ACTION_SET_ALARMS = "com.metinkale.prayer.action.SET_ALARMS";
    private static final String ACTION_CALENDAR_INTEGRATION = "com.metinkale.prayer.action.CALENDAR_INTEGRATION";
    private static final String EXTRA_SOUND = "com.metinkale.prayer.extra.SOUND";

    private static Runnable mCallback;

    public MainIntentService() {
        super("MainIntentService");
    }


    public static void startCalendarIntegration(Context context) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_CALENDAR_INTEGRATION);
        context.startService(intent);
    }


    public static void downloadHadis(Context context, Runnable callback) {
        mCallback = callback;
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_HADIS);
        context.startService(intent);
    }

    public static void downloadSound(Context context, Sounds.Sound sound, Runnable callback) {
        mCallback = callback;
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_SOUND);
        intent.putExtra(EXTRA_SOUND, sound);
        context.startService(intent);
    }

    public static void setAlarms(Context context) {
        Intent intent = new Intent(context, MainIntentService.class);
        intent.setAction(ACTION_SET_ALARMS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                final String action = intent.getAction();
                Runnable callback = mCallback;
                switch (action) {
                    case ACTION_DOWNLOAD_HADIS:
                        mCallback = null;
                        handleDownloadHadis(callback);
                        break;
                    case ACTION_DOWNLOAD_SOUND:
                        Sounds.Sound sound = (Sounds.Sound) intent.getSerializableExtra(EXTRA_SOUND);
                        mCallback = null;
                        handleDownloadSound(sound, callback);
                        break;
                    case ACTION_SET_ALARMS:
                        Times.setAlarms();
                        break;
                    case ACTION_CALENDAR_INTEGRATION:
                        handleCalendarIntegration();
                        break;
                }

            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        }
    }


    private void downloadFile(String Url, File to, final String notificationText) {
        final Activity act = BaseActivity.CurrectAct;


        NotificationManager nm = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final File f = to;

        if (f.exists()) {
            f.delete();
        }
        nm.notify(1111, new NotificationCompat.Builder(App.getContext()).setSmallIcon(R.drawable.ic_abicon).setContentTitle("Downloading").setContentText(notificationText).build());

        class Holder {
            ProgressDialog dlg;
        }
        final Holder holder = new Holder();
        if (act != null) act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.dlg = new ProgressDialog(act);
                holder.dlg.setTitle("Downloading");
                holder.dlg.setMessage(notificationText);
                holder.dlg.setIndeterminate(true);
                holder.dlg.setCancelable(false);
                holder.dlg.setCanceledOnTouchOutside(false);
                holder.dlg.show();
            }
        });
        try {
            URL url = new URL(Url);
            f.getParentFile().mkdirs();
            URLConnection ucon = url.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = bis.read(data)) != -1) {
                total += count;
                fos.write(data, 0, count);
            }


            fos.close();
        } catch (Exception e) {
            f.delete();
            Crashlytics.logException(e);
        }

        if (act != null) act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.dlg.dismiss();
            }
        });
        nm.cancel(1111);

    }


    private void handleDownloadHadis(Runnable callback) {
        if (Prefs.getLanguage() == null) return;


        String file = Prefs.getLanguage() + "/hadis.db";
        File f = new File(App.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);

        String url = App.API_URL + "/hadis." + Prefs.getLanguage() + ".db";
        downloadFile(url, f, getString(R.string.hadis));
        if (callback != null) callback.run();
    }


    private void handleDownloadSound(Sounds.Sound sound, Runnable callback) {
        downloadFile(sound.url, sound.getFile(), sound.name);
        if (callback != null) callback.run();
    }


    private void handleCalendarIntegration() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Prefs.setCalendar("-1");
            return;
        }
        Context context = App.getContext();
        try {
            ContentResolver cr = context.getContentResolver();


            cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events.DESCRIPTION + "=\"com.metinkale.prayer\"", null);


            String id = Prefs.getCalendar();

            if (id.equals("-1") || Prefs.getLanguage() == null) return;
            int year = Calendar.getInstance().get(Calendar.YEAR);
            HashMap<Date, String> days = new LinkedHashMap<>();
            days.putAll(Date.getHolydays(year, false));
            days.putAll(Date.getHolydays(year + 1, false));

            int i = 0;
            ContentValues[] events = new ContentValues[days.size()];
            for (Date date : days.keySet()) {
                ContentValues event = new ContentValues();

                event.put(CalendarContract.Events.CALENDAR_ID, id);
                event.put(CalendarContract.Events.TITLE, days.get(date));
                event.put(CalendarContract.Events.DESCRIPTION, "com.metinkale.prayer");

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, date.getGregDay());
                cal.set(Calendar.MONTH, date.getGregMonth() - 1);
                cal.set(Calendar.YEAR, date.getGregYear());
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));

                long dtstart = cal.getTimeInMillis();
                long dtend = dtstart + DateUtils.DAY_IN_MILLIS;

                event.put(CalendarContract.Events.DTSTART, dtstart);
                event.put(CalendarContract.Events.DTEND, dtend);
                event.put(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);
                event.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
                event.put(CalendarContract.Events.ALL_DAY, 1);

                events[i] = event;
                i++;
            }
            cr.bulkInsert(CalendarContract.Events.CONTENT_URI, events);
            Prefs.setLastCalIntegration(year);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Prefs.setCalendar("-1");
        }
    }
}
