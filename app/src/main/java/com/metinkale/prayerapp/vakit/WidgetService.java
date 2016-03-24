package com.metinkale.prayerapp.vakit;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class WidgetService extends Service {

    private static List<Long> mOngoing = new ArrayList<>();
    private static Bitmap mAbIcon;

    public static void updateOngoing() {
        NotificationManager nm = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = mOngoing.size() - 1; i >= 0; i--) {
            long id = mOngoing.get(i);
            Times t = MainHelper.getTimes(id);

            if ((t == null) || !t.isOngoingNotificationActive()) {
                nm.cancel(id + "", NotIds.ONGOING);
                mOngoing.remove(i);
            }
        }
        List<Long> ids = MainHelper.getIds();
        for (long id : ids) {

            Times t = MainHelper.getTimes(id);

            if ((t != null) && t.isOngoingNotificationActive() && !mOngoing.contains(id)) {
                mOngoing.add(id);
            }
        }

        if (mAbIcon == null)
            mAbIcon = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.ic_abicon);

        Calendar cal = new GregorianCalendar();

        String[] left_part = App.getContext().getResources().getStringArray(R.array.lefttext_part);
        for (long id : mOngoing) {
            Times t = MainHelper.getTimes(id);

            String[] dt = {t.getTime(cal, 0), t.getTime(cal, 1), t.getTime(cal, 2), t.getTime(cal, 3), t.getTime(cal, 4), t.getTime(cal, 5)};

            boolean icon = PreferenceManager.getDefaultSharedPreferences(App.getContext()).getBoolean("ongoingIcon", true);
            int n = t.getNext();
            String sum = App.getContext().getString(R.string.lefttext, Vakit.getByIndex(n - 1).getString(), left_part[n], t.getLeft().substring(0, 5));
            Notification noti = new NotificationCompat.InboxStyle(new NotificationCompat.Builder(App.getContext()).setContentTitle(t.getName() + " (" + t.getSource() + ")").setContentText("").setLargeIcon(mAbIcon).setSmallIcon(icon ? R.drawable.ic_abicon : R.drawable.ic_placeholder).setContentInfo(sum).setContentIntent(Main.getPendingIntent(t)).setOngoing(true)).addLine(Vakit.getByIndex(0).getString() + ": " + Utils.fixTime(dt[0])).addLine(Vakit.GUNES.getString() + ": " + Utils.fixTime(dt[1])).addLine(Vakit.OGLE.getString() + ": " + Utils.fixTime(dt[2])).addLine(Vakit.IKINDI.getString() + ": " + Utils.fixTime(dt[3])).addLine(Vakit.AKSAM.getString() + ": " + Utils.fixTime(dt[4])).addLine(Vakit.YATSI.getString() + ": " + Utils.fixTime(dt[5])).setSummaryText("").build();
            if (Build.VERSION.SDK_INT >= 16) noti.priority = Notification.PRIORITY_LOW;

            noti.when = icon ? System.currentTimeMillis() : 0;
            try {
                nm.notify(id + "", NotIds.ONGOING, noti);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.init();
        WidgetProvider.updateWidgets(this);
        updateOngoing();

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent service = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        App.setExact(am, AlarmManager.RTC, cal.getTimeInMillis(), service);

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
