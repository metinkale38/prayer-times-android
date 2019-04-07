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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.SilenterPrompt;
import com.metinkale.prayer.times.fragments.TimesFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.UUID;
import com.metinkale.prayer.widgets.R;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 * Created by metin on 24.03.2017.
 */

class WidgetLegacy {

    static void update1x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int s = size.width;
        if (s <= 0)
            return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        int next = times.getNextTime();
        String left = LocaleUtils.formatPeriod(now, times.getTime(today, next), false);
        if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
            next = next + 1;

        remoteViews.setOnClickPendingIntent(R.id.widget, TimesFragment.getPendingIntent(times));

        Bitmap bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, s / 2f, s / 2f);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, s, s, paint);

        paint.setColor(theme.textcolor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        paint.setColor(theme.hovercolor);

        String city = times.getName();

        paint.setColor(theme.textcolor);

        float cs = s / 5f;
        float ts = (s * 35) / 100f;
        int vs = s / 4;
        paint.setTextSize(cs);
        cs = (cs * s * 0.9f) / paint.measureText(city);
        cs = (cs > vs) ? vs : cs;

        paint.setTextSize(vs);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(Vakit.getByIndex(next).prevTime().getString(), s / 2f, (s * 22) / 80f, paint);

        paint.setTextSize(ts);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(left, s / 2f, (s / 2f) + ((ts * 1) / 3), paint);

        paint.setTextSize(cs);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(city, s / 2f, ((s * 3) / 4f) + ((cs * 2) / 3), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, s, s, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    static void update4x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 300f / 60f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0)
            return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        LocalDateTime dateTime = LocalDateTime.now();
        LocalDate date = dateTime.toLocalDate();

        int next = times.getNextTime();
        String left = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(date, next));
        if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
            next = next + 1;


        remoteViews.setOnClickPendingIntent(R.id.widget, TimesFragment.getPendingIntent(times));


        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, w / 2f, h / 2f);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        paint.setColor(theme.hovercolor);
        if (next != Vakit.FAJR.ordinal() && !Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
            canvas.drawRect((w * (next - 1)) / 6f, h * 3 / 9f, w * next / 6f, h, paint);
        }
        float s = paint.getStrokeWidth();
        float dip = 3f;
        paint.setStrokeWidth(dip * dp);
        canvas.drawLine(0, (h * 3) / 9f, w, h * 3 / 9f, paint);
        // canvas.drawRect(0, 0, w, h * 3 / 9, paint);
        paint.setStrokeWidth(s);

        paint.setColor(theme.textcolor);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(h / 4f);
        canvas.drawText(" " + times.getName(), 0, h / 4f, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(left + " ", w, h / 4f, paint);

        paint.setTextSize(h / 5f);
        paint.setTextAlign(Paint.Align.CENTER);
        int y = (h * 6) / 7;
        if (Preferences.CLOCK_12H.get()) {
            y += h / 14;
        }

        boolean fits = true;

        do {
            if (!fits) {
                paint.setTextSize((float) (paint.getTextSize() * 0.95));
            }
            fits = true;
            for (Vakit v : Vakit.values()) {
                if ((paint.measureText(v.getString()) > (w / 6)) && (w > 5)) {
                    fits = false;
                }
            }
        } while (!fits);

        for (Vakit v : Vakit.values()) {
            int i = v.ordinal();
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
            canvas.drawText(v.getString(), (w * (1 + (2 * i))) / 12f, y, paint);
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }


        paint.setTextSize((h * 2) / 9f);
        if (Preferences.CLOCK_12H.get()) {
            for (Vakit v : Vakit.values()) {
                int i = v.ordinal();
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                String time = LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime());
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((h * 2) / 9f);
                canvas.drawText(time, (w * (1 + (2 * i))) / 12f, h * 6 / 10f, paint);
                paint.setTextSize(h / 9f);
                canvas.drawText(suffix, (w * (1 + (2 * i))) / 12f, h * 7 / 10f, paint);
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        } else {
            for (Vakit v : Vakit.values()) {
                int i = v.ordinal();
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                canvas.drawText(LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime()), (w * (1 + (2 * i))) / 12f, h * 3 / 5f, paint);
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        }


        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, w, h, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    static void update2x2(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 130f / 160f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0)
            return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);


        LocalDate date = LocalDate.now();
        int next = times.getNextTime();
        String left = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(date, next));
        if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
            next = next + 1;


        remoteViews.setOnClickPendingIntent(R.id.widget, TimesFragment.getPendingIntent(times));

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, w / 2f, h / 2f);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(theme.textcolor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        double l = h / 10f;
        paint.setTextSize((int) l);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(times.getName(), w / 2f, (int) (l * 1.8), paint);

        paint.setTextSize((int) ((l * 8) / 10));

        if (next != Vakit.FAJR.ordinal() && !Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
            paint.setColor(theme.hovercolor);
            canvas.drawRect(0, (int) (l * (next + 1.42f)), w, (int) (l * (next + 2.42)), paint);
        }
        paint.setColor(theme.textcolor);

        paint.setTextAlign(Paint.Align.LEFT);
        for (Vakit v : Vakit.values()) {
            int i = v.ordinal();
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
            canvas.drawText(v.getString(), w / 6f, (int) (l * (3.2 + v.ordinal())), paint);
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }
        paint.setTextAlign(Paint.Align.RIGHT);
        if (Preferences.CLOCK_12H.get()) {
            for (Vakit v : Vakit.values()) {
                int i = v.ordinal();
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                String time = LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime());
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((int) ((l * 8) / 10));
                canvas.drawText(time, ((w * 5) / 6f) - paint.measureText("A"), (int) (l * 3.2 + i * l), paint);
                paint.setTextSize((int) ((l * 4) / 10));
                canvas.drawText(suffix, ((w * 5) / 6f) + (paint.measureText(time) / 4), (int) (l * 3 + i * l), paint);
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        } else {
            for (Vakit v : Vakit.values()) {
                int i = v.ordinal();
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                canvas.drawText(LocaleUtils.formatTime(times.getTime(date, v.ordinal()).toLocalTime()), (w * 5) / 6f, (int) (l * 3.2 + i * l), paint);
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        }
        paint.setTextSize((int) l);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(left, w / 2f, (int) (l * 9.5), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, w, h, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }

    }

    static void updateSilenter(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        Theme theme = WidgetUtils.getTheme(widgetId);
        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int s = size.width;
        if (s <= 0)
            return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        Intent i = new Intent(context, SilenterPrompt.class);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        Bitmap bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, s / 2f, s / 2f);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.bgcolor);
        canvas.drawRect(0, 0, s, s, paint);

        paint.setColor(theme.textcolor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);

        paint.setColor(theme.hovercolor);

        paint.setColor(theme.textcolor);

        paint.setTextSize((s * 25) / 100f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Sessize", s / 2f, (s * 125) / 300f, paint);
        canvas.drawText("al", s / 2f, (s * 25) / 30f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp);
        paint.setColor(theme.strokecolor);
        canvas.drawRect(0, 0, s, s, paint);

        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    static void update4x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0)
            return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);

        remoteViews.setOnClickPendingIntent(R.id.abovePart,
                PendingIntent.getActivity(context, UUID.asInt(), new Intent(AlarmClock.ACTION_SHOW_ALARMS), PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.belowPart, TimesFragment.getPendingIntent(times));


        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        remoteViews.setOnClickPendingIntent(R.id.center, PendingIntent.getActivity(context, UUID.asInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT));


        int next = times.getNextTime();
        int last = next - 1;

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);


        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setShadowLayer(2, 2, 2, 0xFF555555);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);

        LocalTime ltime = LocalTime.now();

        paint.setTextSize(h * 0.55f);
        if (Preferences.CLOCK_12H.get()) {
            String time = LocaleUtils.formatTime(ltime);
            String suffix = time.substring(time.indexOf(" ") + 1);
            time = time.substring(0, time.indexOf(" "));
            canvas.drawText(time, (w / 2f) - (paint.measureText(suffix) / 4), h * 0.4f, paint);
            paint.setTextSize(h * 0.275f);
            canvas.drawText(suffix, (w / 2f) + paint.measureText(time), h * 0.2f, paint);
        } else {
            canvas.drawText(LocaleUtils.formatNumber(LocaleUtils.formatTime(ltime)), w / 2f, h * 0.4f, paint);
        }

        String greg = LocaleUtils.formatDate(LocalDate.now());
        String hicri = LocaleUtils.formatDate(HijriDate.now());

        paint.setTextSize(h * 0.12f);
        float m = paint.measureText(greg + "  " + hicri);
        if (m > (w * 0.8f)) {
            paint.setTextSize((h * 0.12f * w * 0.8f) / m);
        }


        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(greg, w * .1f, h * 0.55f, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(hicri, w * .9f, h * 0.55f, paint);
        remoteViews.setImageViewBitmap(R.id.widget, bmp);

        canvas.drawRect(w * 0.1f, h * 0.6f, w * 0.9f, h * 0.63f, paint);

        if (times.isKerahat()) {
            paint.setColor(0xffbf3f5b);
        } else {
            paint.setColor(Theme.Light.strokecolor);
        }
        canvas.drawRect(w * 0.1f, h * 0.6f, (w * 0.1f) + (w * 0.8f * WidgetV24.getPassedPart(times)), h * 0.63f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Paint.Align.LEFT);
        if (Preferences.CLOCK_12H.get()) {
            String l = LocaleUtils.formatTime(times.getTime(LocalDate.now(), last).toLocalTime());
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(l, w * 0.1f, h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, (w * 0.1f) + (2 * paint.measureText(l)), h * 0.72f, paint);

        } else {
            canvas.drawText(LocaleUtils.formatTime(times.getTime(LocalDate.now(), last).toLocalTime()), w * 0.1f, h * 0.82f, paint);

        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.1f, h * 0.95f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (Preferences.CLOCK_12H.get()) {
            String l = LocaleUtils.formatTime(times.getTime(LocalDate.now(), next).toLocalTime());
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(l, (w * 0.9f) - (paint.measureText(s) / 2), h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, w * 0.9f, h * 0.72f, paint);

        } else {
            canvas.drawText(LocaleUtils.formatTime(times.getTime(LocalDate.now(), next).toLocalTime()), w * 0.9f, h * 0.82f, paint);
        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(next).getString(), w * 0.9f, h * 0.95f, paint);


        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.25f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDateTime.now().toLocalDate(), next)), w * 0.5f, h * 0.9f, paint);
        paint.setFakeBoldText(false);
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    static void update2x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0)
            return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);

        remoteViews.setOnClickPendingIntent(R.id.abovePart, PendingIntent
                .getActivity(context, (int) System.currentTimeMillis(), new Intent(AlarmClock.ACTION_SHOW_ALARMS),
                        PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.belowPart, TimesFragment.getPendingIntent(times));


        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
        remoteViews.setOnClickPendingIntent(R.id.center, PendingIntent.getActivity(context, UUID.asInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT));


        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);


        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setShadowLayer(2, 2, 2, 0xFF555555);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);


        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(w / 100f);

        canvas.drawArc(new RectF(w / 100f, w / 100f, w - w / 100f, h - w / 100f), 0, 360, false, paint);

        boolean isKerahat = times.isKerahat();
        if (isKerahat) {
            paint.setColor(0xffbf3f5b);
        } else {
            paint.setColor(Theme.Light.strokecolor);
        }

        int next = times.getNextTime();
        int indicator = next - 1;
        if (Preferences.VAKIT_INDICATOR_TYPE.get().equals("next"))
            indicator = indicator + 1;


        canvas.drawArc(new RectF(w / 100f, w / 100f, w - w / 100f, h - w / 100f), -90, WidgetV24.getPassedPart(times) * 360, false, paint);

        paint.setStrokeWidth(1);
        LocalDateTime ltime = LocalDateTime.now();

        String[] time = LocaleUtils.formatNumber(ltime.toString("HH:mm")).replace(":", " ").split(" ");
        paint.setTextSize(h * 0.50f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);


        if (time.length == 3) {
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(time[0], w * 0.59f, h * 0.65f, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            if (isKerahat) {
                paint.setColor(0xffbf3f5b);
            } else {
                paint.setColor(Theme.Light.strokecolor);
            }
            paint.setTextSize(h * 0.18f);
            canvas.drawText(time[1], w * 0.60f, h * 0.45f, paint);
            paint.setTextSize(h * 0.1f);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(time[2], w * 0.80f, h * 0.45f, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(h * 0.07f);

            canvas.drawText(LocaleUtils.formatNumber(ltime.toString("d'.' MMM'.'")), w * 0.60f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(indicator).getString(), w * 0.60f, h * 0.65f, paint);
        } else {

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(time[0], w * 0.62f, h * 0.65f, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            if (isKerahat) {
                paint.setColor(0xffbf3f5b);
            } else {
                paint.setColor(Theme.Light.strokecolor);
            }
            paint.setTextSize(h * 0.22f);
            canvas.drawText(time[1], w * 0.63f, h * 0.45f, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(h * 0.07f);

            canvas.drawText(LocaleUtils.formatNumber(ltime.toString("d'.' MMM'.'")), w * 0.63f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(indicator).getString(), w * 0.63f, h * 0.65f, paint);
        }


        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(h * 0.15f);

        canvas.drawText(LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next)), w / 2f, h * 0.85f, paint);

        paint.setTextSize(h * 0.12f);
        canvas.drawText(ltime.toString("EEEE"), w / 2f, h * 0.22f, paint);


        remoteViews.setImageViewBitmap(R.id.widget, bmp);


        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }

    }
}
