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

package com.metinkale.prayerapp.vakit.widget;

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
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.SilenterPrompt;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 * Created by metin on 24.03.2017.
 */

public class WidgetLegacy {

    public static void update1x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int s = size.width;
        if (s <= 0) return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        int next = times.getNext();
        String left = times.getLeft(next, false);
        if (Prefs.getVakitIndicator().equals("next")) next++;

        remoteViews.setOnClickPendingIntent(R.id.widget, Main.getPendingIntent(times));

        Bitmap bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, s / 2, s / 2);
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

        float cs = s / 5;
        float ts = (s * 35) / 100;
        int vs = s / 4;
        paint.setTextSize(cs);
        cs = (cs * s * 0.9f) / paint.measureText(city);
        cs = (cs > vs) ? vs : cs;

        paint.setTextSize(vs);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(Vakit.getByIndex(next - 1).getString(), s / 2, (s * 22) / 80, paint);

        paint.setTextSize(ts);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(left, s / 2, (s / 2) + ((ts * 1) / 3), paint);

        paint.setTextSize(cs);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(city, s / 2, ((s * 3) / 4) + ((cs * 2) / 3), paint);

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

    public static void update4x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
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
        if (w <= 0 || h <= 0) return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};

        int next = times.getNext();
        String left = times.getLeft(next, false);
        if (Prefs.getVakitIndicator().equals("next")) next++;


        remoteViews.setOnClickPendingIntent(R.id.widget, Main.getPendingIntent(times));


        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, w / 2, h / 2);
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
        if (next != 0 && !Prefs.showAltWidgetHightlight()) {
            canvas.drawRect((w * (next - 1)) / 6, h * 3 / 9, w * next / 6, h, paint);
        }
        float s = paint.getStrokeWidth();
        float dip = 3f;
        paint.setStrokeWidth(dip * dp);
        canvas.drawLine(0, (h * 3) / 9, w, h * 3 / 9, paint);
        // canvas.drawRect(0, 0, w, h * 3 / 9, paint);
        paint.setStrokeWidth(s);

        paint.setColor(theme.textcolor);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(h / 4);
        canvas.drawText(" " + times.getName(), 0, h / 4, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(left + " ", w, h / 4, paint);

        paint.setTextSize(h / 5);
        paint.setTextAlign(Paint.Align.CENTER);
        int y = (h * 6) / 7;
        if (Prefs.use12H()) {
            y += h / 14;
        }

        boolean fits = true;
        String[] vakits = {Vakit.getByIndex(0).getString(), Vakit.GUNES.getString(), Vakit.OGLE.getString(), Vakit.IKINDI.getString(), Vakit.AKSAM.getString(), Vakit.YATSI.getString()};
        do {
            if (!fits) {
                paint.setTextSize((float) (paint.getTextSize() * 0.95));
            }
            fits = true;
            for (String v : vakits) {
                if ((paint.measureText(v) > (w / 6)) && (w > 5)) {
                    fits = false;
                }
            }
        } while (!fits);

        for (int i = 0; i < vakits.length; i++) {
            if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
            canvas.drawText(vakits[i], (w * (1 + (2 * i))) / 12, y, paint);
            if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }


        paint.setTextSize((h * 2) / 9);
        if (Prefs.use12H()) {
            for (int i = 0; i < daytimes.length; i++) {
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                String time = Utils.fixTime(daytimes[i]);
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((h * 2) / 9);
                canvas.drawText(time, (w * (1 + (2 * i))) / 12, h * 6 / 10, paint);
                paint.setTextSize(h / 9);
                canvas.drawText(suffix, (w * (1 + (2 * i))) / 12, h * 7 / 10, paint);
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        } else {
            for (int i = 0; i < daytimes.length; i++) {
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                canvas.drawText(Utils.toArabicNrs(daytimes[i]), (w * (1 + (2 * i))) / 12, h * 3 / 5, paint);
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
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

    public static void update2x2(Context context, AppWidgetManager appWidgetManager, int widgetId) {
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
        if (w <= 0 || h <= 0) return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);


        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};

        int next = times.getNext();
        String left = times.getLeft(next, false);
        if (Prefs.getVakitIndicator().equals("next")) next++;


        remoteViews.setOnClickPendingIntent(R.id.widget, Main.getPendingIntent(times));

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, w / 2, h / 2);

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

        double l = h / 10;
        paint.setTextSize((int) l);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(times.getName(), w / 2, (int) (l * 1.8), paint);

        paint.setTextSize((int) ((l * 8) / 10));

        if (next != 0 && !Prefs.showAltWidgetHightlight()) {
            paint.setColor(theme.hovercolor);
            canvas.drawRect(0, (int) (l * (next + 1.42)), w, (int) (l * (next + 2.42)), paint);
        }
        paint.setColor(theme.textcolor);

        paint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < 6; i++) {
            if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
            canvas.drawText(Vakit.getByIndex(i).getString(), w / 6, (int) (l * (3.2 + i)), paint);
            if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
        }
        paint.setTextAlign(Paint.Align.RIGHT);
        if (Prefs.use12H()) {
            for (int i = 0; i < daytimes.length; i++) {
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                String time = Utils.fixTime(daytimes[i]);
                String suffix = time.substring(time.indexOf(" ") + 1);
                time = time.substring(0, time.indexOf(" "));
                paint.setTextSize((int) ((l * 8) / 10));
                canvas.drawText(time, ((w * 5) / 6) - paint.measureText("A"), (int) (l * 3.2 + i * l), paint);
                paint.setTextSize((int) ((l * 4) / 10));
                canvas.drawText(suffix, ((w * 5) / 6) + (paint.measureText(time) / 4), (int) (l * 3 + i * l), paint);
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        } else {
            for (int i = 0; i < daytimes.length; i++) {
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                }
                canvas.drawText(Utils.toArabicNrs(daytimes[i]), (w * 5) / 6, (int) (l * 3.2 + i * l), paint);
                if (i == next - 1 && Prefs.showAltWidgetHightlight()) {
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                }
            }
        }
        paint.setTextSize((int) l);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(left, w / 2, (int) (l * 9.5), paint);

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

    public static void updateSilenter(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Resources r = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());

        Theme theme = WidgetUtils.getTheme(widgetId);
        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int s = size.width;
        if (s <= 0) return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget);

        Intent i = new Intent(context, SilenterPrompt.class);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        Bitmap bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.99f, 0.99f, s / 2, s / 2);
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

        paint.setTextSize((s * 25) / 100);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Sessize", s / 2, (s * 125) / 300, paint);
        canvas.drawText("al", s / 2, (s * 25) / 30, paint);

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

    public static void update4x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0) return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);

        remoteViews.setOnClickPendingIntent(R.id.abovePart, PendingIntent.getActivity(context, (int) System.currentTimeMillis(), new Intent(AlarmClock.ACTION_SHOW_ALARMS), PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.belowPart, Main.getPendingIntent(times));


        Uri.Builder builder =
                CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent =
                new Intent(Intent.ACTION_VIEW, builder.build());
        remoteViews.setOnClickPendingIntent(R.id.center, PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT));


        int next = times.getNext();
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
        String time = ltime.toString("HH:mm");
        if (Prefs.use12H()) {
            time = Utils.fixTime(time);
            String suffix = time.substring(time.indexOf(" ") + 1);
            time = time.substring(0, time.indexOf(" "));
            canvas.drawText(time, (w / 2) - (paint.measureText(suffix) / 4), h * 0.4f, paint);
            paint.setTextSize(h * 0.275f);
            canvas.drawText(suffix, (w / 2) + paint.measureText(time), h * 0.2f, paint);
        } else {
            canvas.drawText(Utils.toArabicNrs(time), w / 2, h * 0.4f, paint);
        }


        LocalDate date = LocalDate.now();
        String greg = Utils.format(date);
        String hicri = Utils.format(new HicriDate(date));

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
        canvas.drawRect(w * 0.1f, h * 0.6f, (w * 0.1f) + (w * 0.8f * times.getPassedPart()), h * 0.63f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Paint.Align.LEFT);
        if (Prefs.use12H()) {
            String l = Utils.fixTime(times.getTime(last));
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(l, w * 0.1f, h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, (w * 0.1f) + (2 * paint.measureText(l)), h * 0.72f, paint);

        } else {
            canvas.drawText(Utils.fixTime(times.getTime(last)), w * 0.1f, h * 0.82f, paint);

        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.1f, h * 0.95f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.2f);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (Prefs.use12H()) {
            String l = Utils.fixTime(times.getTime(next));
            String s = l.substring(l.indexOf(" ") + 1);
            l = l.substring(0, l.indexOf(" "));
            canvas.drawText(l, (w * 0.9f) - (paint.measureText(s) / 2), h * 0.82f, paint);
            paint.setTextSize(h * 0.1f);
            canvas.drawText(s, w * 0.9f, h * 0.72f, paint);

        } else {
            canvas.drawText(Utils.fixTime(times.getTime(next)), w * 0.9f, h * 0.82f, paint);
        }
        paint.setTextSize(h * 0.12f);
        canvas.drawText(Vakit.getByIndex(next).getString(), w * 0.9f, h * 0.95f, paint);


        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.25f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText(times.getLeft(next, false), w * 0.5f, h * 0.9f, paint);
        paint.setFakeBoldText(false);
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("exceeds maximum bitmap memory usage")) {
                Crashlytics.logException(e);
            }
        }
    }

    public static void update2x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int w = size.width;
        int h = size.height;
        if (w <= 0 || h <= 0) return;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.vakit_widget_clock);

        remoteViews.setOnClickPendingIntent(R.id.abovePart, PendingIntent.getActivity(context, (int) System.currentTimeMillis(), new Intent(AlarmClock.ACTION_SHOW_ALARMS), PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.belowPart, Main.getPendingIntent(times));


        Uri.Builder builder =
                CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        builder.appendPath(Long.toString(System.currentTimeMillis()));
        Intent intent =
                new Intent(Intent.ACTION_VIEW, builder.build());
        remoteViews.setOnClickPendingIntent(R.id.center, PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT));


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
        paint.setStrokeWidth(w / 100);

        canvas.drawArc(new RectF(w / 100, w / 100, w - w / 100, h - w / 100), 0, 360, false, paint);

        boolean isKerahat = times.isKerahat();
        if (isKerahat) {
            paint.setColor(0xffbf3f5b);
        } else {
            paint.setColor(Theme.Light.strokecolor);
        }

        int next = times.getNext();
        int last = next - 1;
        if (Prefs.getVakitIndicator().equals("next")) last++;


        canvas.drawArc(new RectF(w / 100, w / 100, w - w / 100, h - w / 100), -90, times.getPassedPart() * 360, false, paint);

        paint.setStrokeWidth(1);
        LocalDateTime ltime = LocalDateTime.now();

        String[] time = Utils.fixTime(ltime.toString("HH:mm")).replace(":", " ").split(" ");
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

            canvas.drawText(Utils.toArabicNrs(ltime.toString("d'.' MMM'.'")), w * 0.60f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.60f, h * 0.65f, paint);
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

            canvas.drawText(Utils.toArabicNrs(ltime.toString("d'.' MMM'.'")), w * 0.63f, h * 0.55f, paint);
            canvas.drawText(Vakit.getByIndex(last).getString(), w * 0.63f, h * 0.65f, paint);
        }


        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(h * 0.15f);

        canvas.drawText(times.getLeft(next, false), w / 2, h * 0.85f, paint);

        paint.setTextSize(h * 0.12f);
        canvas.drawText(ltime.toString("EEEE"), w / 2, h * 0.22f, paint);


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
