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
 *
 */

package com.metinkale.prayerapp.vakit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App.NotIds;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;
import com.metinkale.prayerapp.vakit.widget.WidgetUtils;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class WidgetService extends Service {

    private static final String COLOR_SEARCH_1ST = "COLOR_SEARCH_1ST";
    private static final String COLOR_SEARCH_2ND = "COLOR_SEARCH_2ND";
    @NonNull
    private List<Long> mOngoing = new ArrayList<>();
    private Bitmap mAbIcon;
    private Integer mColor1st = null;
    private Integer mColor2nd = null;
    private IntentFilter mScreenOnOffFilter;
    private IntentFilter mTimeTickFilter;
    private IntentFilter mTimeChangedFilter;
    private NotificationManager mNotMan;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScreenOnOffFilter = new IntentFilter();
        mScreenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBroadcastReceiver, mScreenOnOffFilter);

        mTimeChangedFilter = new IntentFilter();
        mTimeChangedFilter.addAction(Intent.ACTION_TIME_CHANGED);

        mTimeTickFilter = new IntentFilter();
        mTimeTickFilter.addAction(Intent.ACTION_TIME_TICK);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 20 && pm.isInteractive()
                || Build.VERSION.SDK_INT < 20 && pm.isScreenOn()) {
            registerReceiver(mBroadcastReceiver, mTimeTickFilter);
            registerReceiver(mBroadcastReceiver, mTimeChangedFilter);
        }

        mNotMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        extractColors();
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (action == null) action = Intent.ACTION_TIME_TICK;
        switch (action) {
            case Intent.ACTION_SCREEN_OFF: {
                unregisterReceiver(mBroadcastReceiver);
                registerReceiver(mBroadcastReceiver, mScreenOnOffFilter);
                break;
            }
            case Intent.ACTION_SCREEN_ON: {
                unregisterReceiver(mBroadcastReceiver);
                registerReceiver(mBroadcastReceiver, mScreenOnOffFilter);
                registerReceiver(mBroadcastReceiver, mTimeTickFilter);
                registerReceiver(mBroadcastReceiver, mTimeChangedFilter);
            }
            case Intent.ACTION_USER_PRESENT:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_TIME_TICK: {
                WidgetUtils.updateWidgets(this);
                updateOngoing();
                break;
            }
        }
        return START_STICKY;
    }

    public static void start(Context c) {
        Intent i = new Intent(c, WidgetService.class);
        c.startService(i);
    }

    public void updateOngoing() {

        for (int i = mOngoing.size() - 1; i >= 0; i--) {
            long id = mOngoing.get(i);
            Times t = Times.getTimes(id);

            if ((t == null) || !t.isOngoingNotificationActive()) {
                mNotMan.cancel(id + "", NotIds.ONGOING);
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


        LocalDate cal = LocalDate.now();
        String[] left_part = getResources().getStringArray(R.array.lefttext_part);
        for (long id : mOngoing) {


            Times t = Times.getTimes(id);

            String[] dt = {t.getTime(cal, 0), t.getTime(cal, 1), t.getTime(cal, 2), t.getTime(cal, 3), t.getTime(cal, 4), t.getTime(cal, 5)};

            boolean icon = Prefs.showOngoingIcon();
            boolean number = Prefs.showOngoingNumber();
            Crashlytics.setBool("showIcon", icon);
            Crashlytics.setBool("showNumber", number);

            Notification noti;
            if (Prefs.getAlternativeOngoing()) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_layout);

                int[] timeIds = {R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5};
                int[] vakitIds = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};

                int next = t.getNext();
                if (Prefs.getVakitIndicator().equals("next")) next++;
                for (int i = 0; i < dt.length; i++) {
                    if ((next - 1) == i) {
                        if (Prefs.use12H()) {
                            Spannable span = (Spannable) Utils.fixTimeForHTML(dt[i]);
                            span.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            views.setTextViewText(timeIds[i], span);
                        } else
                            views.setTextViewText(timeIds[i], Html.fromHtml("<strong><em>" + Utils.fixTimeForHTML(dt[i]) + "</em></strong>"));
                    } else {
                        views.setTextViewText(timeIds[i], Utils.fixTimeForHTML(dt[i]));
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


                views.setTextColor(R.id.fajr, mColor1st);
                views.setTextColor(R.id.sun, mColor1st);
                views.setTextColor(R.id.zuhr, mColor1st);
                views.setTextColor(R.id.asr, mColor1st);
                views.setTextColor(R.id.maghrib, mColor1st);
                views.setTextColor(R.id.ishaa, mColor1st);

                views.setTextColor(R.id.time0, mColor1st);
                views.setTextColor(R.id.time1, mColor1st);
                views.setTextColor(R.id.time2, mColor1st);
                views.setTextColor(R.id.time3, mColor1st);
                views.setTextColor(R.id.time4, mColor1st);
                views.setTextColor(R.id.time5, mColor1st);


                views.setTextColor(R.id.time, mColor1st);
                views.setTextColor(R.id.city, mColor1st);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    long left = t.getLeftMinutes(t.getNext());
                    noti = new Notification.Builder(this)
                            .setContent(views)
                            .setContentIntent(Main.getPendingIntent(t))
                            .setSmallIcon(icon ? (number ?
                                    Icon.createWithBitmap(getIconFromMinutes(left)) :
                                    Icon.createWithResource(this, R.drawable.ic_abicon)) :
                                    Icon.createWithResource(this, R.drawable.ic_placeholder))
                            .setOngoing(true)
                            .build();
                } else {
                    noti = new NotificationCompat.Builder(this)
                            .setContent(views)
                            .setContentIntent(Main.getPendingIntent(t))
                            .setSmallIcon(icon ? R.drawable.ic_abicon : R.drawable.ic_placeholder)
                            .setOngoing(true)
                            .build();
                }
            } else {
                int n = t.getNext();
                String sum = getString(R.string.leftText, Vakit.getByIndex(n - 1).getString(), left_part[n], t.getLeft().substring(0, 5));

                if (mAbIcon == null) {
                    mAbIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_abicon)).getBitmap();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    long left = t.getLeftMinutes(t.getNext());
                    noti = new Notification.InboxStyle(new Notification.Builder(this)
                            .setContentTitle(t.getName() + " (" + t.getSource() + ")")
                            .setContentText("")
                            .setLargeIcon(mAbIcon)
                            .setSmallIcon(icon ? (number ?
                                    Icon.createWithBitmap(getIconFromMinutes(left)) :
                                    Icon.createWithResource(this, R.drawable.ic_abicon)) :
                                    Icon.createWithResource(this, R.drawable.ic_placeholder))
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
                } else {
                    noti = new NotificationCompat.InboxStyle(new NotificationCompat.Builder(this)
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

            }

            if (Build.VERSION.SDK_INT >= 16) {
                noti.priority = Notification.PRIORITY_LOW;
            }
            noti.when = icon ? System.currentTimeMillis() : 0;
            try {
                mNotMan.notify(id + "", NotIds.ONGOING, noti);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        }

    }

    private Bitmap getIconFromMinutes(long left) {
        String text = left + "";
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());
        Bitmap b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);
        Paint paint = new Paint();
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text.length() == 1 ? "0" + text : text, 0, text.length() == 1 ? 2 : text.length(), bounds);
        float desiredTextSize = testTextSize * (px * 0.9f) / bounds.width();
        paint.setTextSize(desiredTextSize);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        int yPos = (int) ((c.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        c.drawText(text, px / 2, yPos, paint);
        c.drawText(text, px / 2, yPos, paint);
        return b;
    }


    private boolean recurseGroup(@NonNull ViewGroup gp) {
        int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = gp.getChildAt(i);
            if (v instanceof TextView) {
                TextView text = (TextView) v;
                String szText = text.getText().toString();
                if (COLOR_SEARCH_1ST.equals(szText)) {
                    mColor1st = text.getCurrentTextColor();
                }
                if (COLOR_SEARCH_2ND.equals(szText)) {
                    mColor2nd = text.getCurrentTextColor();
                }

                if ((mColor1st != null) && (mColor2nd != null)) {
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

    private void extractColors() {
        if (mColor1st != null && mColor2nd != null) {
            return;
        }


        try {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(COLOR_SEARCH_1ST)
                    .setContentText(COLOR_SEARCH_2ND);
            Notification ntf = mBuilder.build();
            LinearLayout group = new LinearLayout(this);
            ViewGroup event = (ViewGroup) ntf.contentView.apply(this, group);
            recurseGroup(event);
            group.removeAllViews();
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        if (mColor1st == null) {
            mColor1st = Color.BLACK;
        }
        if (mColor2nd == null) {
            mColor2nd = Color.DKGRAY;
        }
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onStartCommand(intent, 0, 0);
        }
    };
}
