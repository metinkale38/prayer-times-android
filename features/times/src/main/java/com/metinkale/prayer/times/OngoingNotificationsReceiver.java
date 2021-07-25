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
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.service.ForegroundService;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.utils.NotificationUtils;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OngoingNotificationsReceiver extends InternalBroadcastReceiver implements InternalBroadcastReceiver.OnTimeTickListener, InternalBroadcastReceiver.OnPrefsChangedListener {
    private static final String FOREGROUND_NEEDY_ONGOING = "ongoing";
    private Integer mDefaultTextColor = null;

    @Override
    public void onTimeTick() {

        int textColor = Preferences.ONGOING_TEXT_COLOR.get();
        int bgColor = Preferences.ONGOING_BG_COLOR.get();

        NotificationManager notMan = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        LocalDate cal = LocalDate.now();

        List<Pair<Integer, Notification>> notifications = new ArrayList<>();
        for (Times t : Times.getTimes()) {
            if (!t.isOngoingNotificationActive()) {
                notMan.cancel(t.getIntID());
                continue;
            }


            boolean icon = Preferences.SHOW_ONGOING_ICON.get();
            boolean number = Preferences.SHOW_ONGOING_NUMBER.get();
            FirebaseCrashlytics.getInstance().setCustomKey("showIcon", icon);
            FirebaseCrashlytics.getInstance().setCustomKey("showNumber", number);

            RemoteViews views = new RemoteViews(getContext().getPackageName(), R.layout.notification_layout);
            if (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("xiaomi")) {
                views.setViewPadding(R.id.notification, 0, 0, 0, 0);
            }

            if (bgColor != 0) {
                views.setInt(R.id.notification, "setBackgroundColor", bgColor);
            }
            views.setTextViewText(android.R.id.title, t.getName());

            if (textColor != 0)
                views.setTextColor(android.R.id.title, textColor);

            int[] timeIds = {R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5};
            int[] vakitIds = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};

            int marker = t.getCurrentTime();
            if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next")) {
                marker = marker + 1;
            }
            for (Vakit vakit : Vakit.values()) {
                LocalTime time = t.getTime(cal, vakit.ordinal()).toLocalTime();
                if (marker == vakit.ordinal()) {
                    views.setTextViewText(vakitIds[vakit.ordinal()], Html.fromHtml("<strong>" + vakit.getString() + "</strong>"));
                    if (Preferences.CLOCK_12H.get()) {
                        Spannable span = (Spannable) LocaleUtils.formatTimeForHTML(time);
                        span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        views.setTextViewText(timeIds[vakit.ordinal()], span);
                    } else {
                        views.setTextViewText(timeIds[vakit.ordinal()],
                                Html.fromHtml("<strong>" + LocaleUtils.formatTimeForHTML(time) + "</strong>"));
                    }
                } else {
                    views.setTextViewText(vakitIds[vakit.ordinal()], vakit.getString());
                    views.setTextViewText(timeIds[vakit.ordinal()], LocaleUtils.formatTimeForHTML(time));
                }
                if (textColor != 0) {
                    views.setTextColor(timeIds[vakit.ordinal()], textColor);
                    views.setTextColor(vakitIds[vakit.ordinal()], textColor);
                }
            }

            DateTime nextTime = t.getTime(cal, t.getNextTime()).toDateTime();
            if (Build.VERSION.SDK_INT >= 24 && Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS)) {
                views.setChronometer(R.id.countdown, nextTime.getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
            } else {
                String txt = LocaleUtils.formatPeriod(DateTime.now(), nextTime, false);
                views.setString(R.id.countdown, "setFormat", txt);
                views.setChronometer(R.id.countdown, 0, txt, false);
            }
            if (textColor != 0) {
                views.setTextColor(R.id.countdown, textColor);
            }

            Notification.Builder builder =
                    new Notification.Builder(getContext());
            builder.setContentIntent(TimesFragment.getPendingIntent(t));

            if (!icon) {
                builder.setSmallIcon(R.drawable.ic_placeholder);
            } else if (number && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder.setSmallIcon(Icon.createWithBitmap(getIconFromMinutes(t)));
            } else {
                builder.setSmallIcon(R.drawable.ic_abicon);
            }
            builder.setOngoing(true);
            builder.setWhen(icon ? System.currentTimeMillis() : 0);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomContentView(views);
            } else {
                builder.setContent(views);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(NotificationUtils.getOngoingChannel(getContext()));
            }


            Notification noti = builder.build();
            noti.priority = Notification.PRIORITY_LOW;
            notifications.add(new Pair<>(t.getIntID(), noti));
        }

        if (!notifications.isEmpty()) {
            for (int i = 0; i < notifications.size(); i++) {
                Pair<Integer, Notification> pair = notifications.get(i);
                if (i == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.addNeedy(getContext(), FOREGROUND_NEEDY_ONGOING, pair.second, pair.first);
                } else {
                    notMan.notify(pair.first, pair.second);
                }
            }
        } else {
            ForegroundService.removeNeedy(getContext(), FOREGROUND_NEEDY_ONGOING);
        }

    }


    private Bitmap getIconFromMinutes(Times t) {
        int left = new Period(LocalDateTime.now(), t.getTime(LocalDate.now(), t.getNextTime()), PeriodType.minutes()).getMinutes();
        Resources r = getContext().getResources();

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size);
        paint.setTextSize(size * size / paint.measureText((left < 10 ? left * 10 : left) + ""));
        float yPos = c.getHeight() / 2f - (paint.descent() + paint.ascent()) / 2;
        c.drawText(left + "", size / 2f, yPos, paint);
        return b;
    }

    @Override
    public void onPrefsChanged(@NonNull String key) {
        if (key.equals(Preferences.SHOW_ONGOING_ICON.getKey())
                || key.equals(Preferences.SHOW_ONGOING_NUMBER.getKey())
                || key.equals(Preferences.ONGOING_TEXT_COLOR.getKey())
                || key.equals(Preferences.ONGOING_BG_COLOR.getKey())) {
            onTimeTick();
        }
    }


}
