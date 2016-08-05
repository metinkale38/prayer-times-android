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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class WidgetService extends Service {

    private static List<Long> mOngoing = new ArrayList<>();
    private static Bitmap mAbIcon;


    public static void updateOngoing() {
        extractColors();
        NotificationManager nm = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = mOngoing.size() - 1; i >= 0; i--) {
            long id = mOngoing.get(i);
            Times t = Times.getTimes(id);

            if ((t == null) || !t.isOngoingNotificationActive()) {
                nm.cancel(id + "", NotIds.ONGOING);
                mOngoing.remove(i);
            }
        }
        List<Long> ids = Times.getIds();
        for (long id : ids) {

            Times t = Times.getTimes(id);

            if ((t != null) && t.isOngoingNotificationActive() && !mOngoing.contains(id)) {
                mOngoing.add(id);
            }
        }

        if (mAbIcon == null) {
            mAbIcon = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.ic_abicon);
        }

        LocalDate cal = LocalDate.now();
        String[] left_part = App.getContext().getResources().getStringArray(R.array.lefttext_part);
        for (long id : mOngoing) {


            Times t = Times.getTimes(id);

            String[] dt = {t.getTime(cal, 0), t.getTime(cal, 1), t.getTime(cal, 2), t.getTime(cal, 3), t.getTime(cal, 4), t.getTime(cal, 5)};
            Notification noti = null;
            boolean icon = Prefs.showOngoingIcon();
            if (Prefs.getAlternativeOngoing()) {
                RemoteViews views = new RemoteViews(App.getContext().getPackageName(), R.layout.notification_layout);

                int[] timeIds = {R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5};
                int[] vakitIds = {R.id.imsak, R.id.gunes, R.id.ogle, R.id.ikindi, R.id.aksam, R.id.yatsi};

                int next = t.getNext();

                for (int i = 0; i < dt.length; i++) {
                    if ((next - 1) == i) {
                        views.setTextViewText(timeIds[i], Html.fromHtml("<strong><em>" + Utils.fixTime(dt[i]) + "</em></strong>"));
                    } else {
                        views.setTextViewText(timeIds[i], Utils.fixTime(dt[i]));
                    }
                }

                for (int i = 0; i < dt.length; i++) {
                    if ((next - 1) == i) {
                        views.setTextViewText(vakitIds[i], Html.fromHtml("<strong><em>" + Vakit.getByIndex(i).getString() + "</em></strong>"));
                    } else {
                        views.setTextViewText(vakitIds[i], Vakit.getByIndex(i).getString());
                    }
                }

                views.setTextViewText(R.id.time, t.getLeft(t.getNext(), false));
                views.setTextViewText(R.id.city, t.getName());


                views.setTextColor(R.id.imsak, COLOR_1ST);
                views.setTextColor(R.id.gunes, COLOR_1ST);
                views.setTextColor(R.id.ogle, COLOR_1ST);
                views.setTextColor(R.id.ikindi, COLOR_1ST);
                views.setTextColor(R.id.aksam, COLOR_1ST);
                views.setTextColor(R.id.yatsi, COLOR_1ST);

                views.setTextColor(R.id.time0, COLOR_1ST);
                views.setTextColor(R.id.time1, COLOR_1ST);
                views.setTextColor(R.id.time2, COLOR_1ST);
                views.setTextColor(R.id.time3, COLOR_1ST);
                views.setTextColor(R.id.time4, COLOR_1ST);
                views.setTextColor(R.id.time5, COLOR_1ST);


                views.setTextColor(R.id.time, COLOR_1ST);
                views.setTextColor(R.id.city, COLOR_1ST);

                noti = new NotificationCompat.Builder(App.getContext())
                        .setContent(views)
                        .setSmallIcon(icon ? R.drawable.ic_abicon : R.drawable.ic_placeholder)
                        .setOngoing(true)
                        .build();
            } else {
                int n = t.getNext();
                String sum = App.getContext().getString(R.string.leftText, Vakit.getByIndex(n - 1).getString(), left_part[n], t.getLeft().substring(0, 5));
                noti = new NotificationCompat.InboxStyle(new NotificationCompat.Builder(App.getContext())
                        .setContentTitle(t.getName() + " (" + t.getSource() + ")")
                        .setContentText("")
                        .setLargeIcon(mAbIcon)
                        .setSmallIcon(icon ? R.drawable.ic_abicon : R.drawable.ic_placeholder)
                        .setContentInfo(sum)
                        .setContentIntent(Main.getPendingIntent(t))
                        .setOngoing(true))
                        .addLine(Vakit.getByIndex(0).getString() + ": " + Utils.fixTime(dt[0]))
                        .addLine(Vakit.GUNES.getString() + ": " + Utils.fixTime(dt[1]))
                        .addLine(Vakit.OGLE.getString() + ": " + Utils.fixTime(dt[2]))
                        .addLine(Vakit.IKINDI.getString() + ": " + Utils.fixTime(dt[3]))
                        .addLine(Vakit.AKSAM.getString() + ": " + Utils.fixTime(dt[4]))
                        .addLine(Vakit.YATSI.getString() + ": " + Utils.fixTime(dt[5]))
                        .setSummaryText("")
                        .build();
            }

            if (Build.VERSION.SDK_INT >= 16) {
                noti.priority = Notification.PRIORITY_LOW;
            }
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
        WidgetProvider.updateWidgets(this);
        updateOngoing();

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent service = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        App.setExact(am, AlarmManager.RTC, DateTime.now().withMillisOfSecond(0).withSecondOfMinute(0).plusMinutes(1).getMillis(), service);

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private static Integer COLOR_1ST;
    private static Integer COLOR_2ND;
    private static final String COLOR_SEARCH_1ST = "COLOR_SEARCH_1ST";
    private static final String COLOR_SEARCH_2ND = "COLOR_SEARCH_2ND";

    private static boolean recurseGroup(ViewGroup gp) {
        int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = gp.getChildAt(i);
            if (v instanceof TextView) {
                TextView text = (TextView) v;
                String szText = text.getText().toString();
                if (COLOR_SEARCH_1ST.equals(szText)) {
                    COLOR_1ST = text.getCurrentTextColor();
                }
                if (COLOR_SEARCH_2ND.equals(szText)) {
                    COLOR_2ND = text.getCurrentTextColor();
                }

                if ((COLOR_1ST != null) && (COLOR_2ND != null)) {
                    return true;
                }
            } else if (gp.getChildAt(i) instanceof ViewGroup) {
                if (recurseGroup((ViewGroup) v)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void extractColors() {
        if (COLOR_1ST != null) {
            return;
        }


        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(App.getContext())
                            .setContentTitle(COLOR_SEARCH_1ST)
                            .setContentText(COLOR_SEARCH_2ND);
            Notification ntf = mBuilder.build();
            LinearLayout group = new LinearLayout(App.getContext());
            ViewGroup event = (ViewGroup) ntf.contentView.apply(App.getContext(), group);
            recurseGroup(event);
            group.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (COLOR_1ST == null) {
            COLOR_1ST = Color.BLACK;
        }
        if (COLOR_2ND == null) {
            COLOR_2ND = Color.DKGRAY;
        }
    }
}
