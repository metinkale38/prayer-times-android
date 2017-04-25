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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.icu.util.Calendar;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.SilenterPrompt;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import org.joda.time.LocalDate;

/**
 * Created by metin on 24.03.2017.
 */
@TargetApi(24)
public class WidgetV24 {
    public static void update2x2(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 130f / 160f);
        int w = size.width;
        int h = size.height;
        float scale = w / 10.5f;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_2x2);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2);
        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};


        remoteViews.setOnClickPendingIntent(R.id.widget_layout, Main.getPendingIntent(times));
        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setTextColor(R.id.city, theme.textcolor);
        int next = times.getNext();
        int idsText[] = {R.id.fajrText, R.id.sunText, R.id.zuhrText, R.id.asrText, R.id.maghribText, R.id.ishaaText};
        int ids[] = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};
        for (int i = 0; i < 6; i++) {
            remoteViews.setTextViewTextSize(idsText[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f);
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f);
            remoteViews.setTextColor(idsText[i], theme.textcolor);
            remoteViews.setTextColor(ids[i], theme.textcolor);

            String name = Vakit.getByIndex(i).getString();
            String time = Utils.fixTime(daytimes[i]);
            if (Prefs.use12H()) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>";
            }

            if (Prefs.showAltWidgetHightlight()) {
                if (i + 1 == next) {
                    name = "<b><i>" + name + "</i></b>";
                    time = "<b><i>" + time + "</i></b>";
                }
                remoteViews.setInt(idsText[i], "setBackgroundColor", 0);
                remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            } else {
                if (i + 1 == next) {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", theme.hovercolor);
                    remoteViews.setInt(ids[i], "setBackgroundColor", theme.hovercolor);
                } else {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", 0);
                    remoteViews.setInt(ids[i], "setBackgroundColor", 0);
                }
            }

            remoteViews.setTextViewText(idsText[i], Html.fromHtml(name));
            remoteViews.setTextViewText(ids[i], Html.fromHtml(time));

            remoteViews.setViewPadding(idsText[i], (int) ((Prefs.use12H() ? 1.25 : 1.75) * scale), 0, (int) scale / 4, 0);
            remoteViews.setViewPadding(ids[i], 0, 0, (int) ((Prefs.use12H() ? 1.25 : 1.75) * scale), 0);

        }

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);
        remoteViews.setViewPadding(R.id.city, (int) scale / 2, 0, (int) scale / 2, (int) scale / 4);

        if (Prefs.showWidgetSeconds())
            remoteViews.setChronometer(R.id.countdown, times.getMills(next)
                    - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = times.getLeft(next, false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    public static void update4x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 300f / 60f);
        int w = size.width;
        int h = size.height;
        float scale = w / 25f;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2);
        LocalDate date = LocalDate.now();
        String[] daytimes = {times.getTime(date, 0), times.getTime(date, 1), times.getTime(date, 2), times.getTime(date, 3), times.getTime(date, 4), times.getTime(date, 5)};


        remoteViews.setOnClickPendingIntent(R.id.widget_layout, Main.getPendingIntent(times));
        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setTextColor(R.id.city, theme.textcolor);
        int next = times.getNext();
        int ids[] = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};
        for (int i = 0; i < 6; i++) {
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f);
            remoteViews.setTextColor(ids[i], theme.textcolor);

            String name = Vakit.getByIndex(i).getString();
            String time = Utils.fixTime(daytimes[i]);
            if (Prefs.use12H()) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>";
            }


            if (Prefs.showAltWidgetHightlight()) {
                if (i + 1 == next) {
                    name = "<b><i>" + name + "</i></b>";
                    time = "<b><i>" + time + "</i></b>";
                }
                remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            } else {
                if (i + 1 == next)
                    remoteViews.setInt(ids[i], "setBackgroundColor", theme.hovercolor);
                else
                    remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            }

            remoteViews.setTextViewText(ids[i], Html.fromHtml(time + "<br/>" + name));

        }

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.2f);
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.2f);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);

        remoteViews.setViewPadding(R.id.city, (int) scale / 4, (int) scale / 16, (int) scale / 4, (int) scale / 16);
        remoteViews.setViewPadding(R.id.countdown, (int) scale / 4, (int) scale / 16, (int) scale / 4, (int) scale / 16);

        if (Prefs.showWidgetSeconds())
            remoteViews.setChronometer(R.id.countdown, times.getMills(next)
                    - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = times.getLeft(next, false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.2f);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);

    }

    public static void update1x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1);
        int s = size.width;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2);

        int next = times.getNext();

        String name = times.getName();
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, Main.getPendingIntent(times));
        if (Prefs.showWidgetSeconds())
            remoteViews.setChronometer(R.id.countdown, times.getMills(next)
                    - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = times.getLeft(next, false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, s / 4);
        remoteViews.setTextViewText(R.id.city, name);
        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).getString());

        remoteViews.setTextColor(R.id.city, theme.textcolor);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);
        remoteViews.setTextColor(R.id.time, theme.textcolor);

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, (float) Math.min(s / 5, 1.5 * s / name.length()));
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, s / 5);

        remoteViews.setViewPadding(R.id.countdown, 0, -s / 16, 0, -s / 16);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    public static void updateSilenter(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1);
        int s = size.width;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_silenter);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2);


        Intent i = new Intent(context, SilenterPrompt.class);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        remoteViews.setTextViewText(R.id.text, context.getString(R.string.silent));
        remoteViews.setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_PX, s / 4);
        remoteViews.setTextColor(R.id.text, theme.textcolor);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);

    }

    public static void update4x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f);
        int width = size.width;
        int height = size.height;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x2_clock);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, Main.getPendingIntent(times));


        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0);

        if (Prefs.use12H()) {
            Calendar cal = Calendar.getInstance();
            String ampm = "AM";
            if (cal.get(Calendar.AM_PM) == Calendar.PM) {
                ampm = "PM";
            }
            Spannable span = new SpannableString("hh:mm'" + ampm + "'");
            span.setSpan(new SuperscriptSpan(), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(0.3f), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            remoteViews.setCharSequence(R.id.time, "setFormat12Hour", span);
            remoteViews.setCharSequence(R.id.time, "setFormat24Hour", span);
        } else {
            remoteViews.setCharSequence(R.id.time, "setFormat12Hour", "HH:mm");
            remoteViews.setCharSequence(R.id.time, "setFormat24Hour", "HH:mm");
        }
        int next = times.getNext();


        remoteViews.setTextViewText(R.id.lastText, Vakit.getByIndex(next - 1).getString());
        remoteViews.setTextViewText(R.id.nextText, Vakit.getByIndex(next).getString());
        remoteViews.setTextViewText(R.id.lastTime, Utils.fixTimeForHTML(times.getTime(next - 1)));
        remoteViews.setTextViewText(R.id.nextTime, Utils.fixTimeForHTML(times.getTime(next)));

        LocalDate date = LocalDate.now();
        remoteViews.setTextViewText(R.id.greg, Utils.format(date));
        remoteViews.setTextViewText(R.id.hicri, Utils.format(new HicriDate(date)));

        if (times.isKerahat()) {
            remoteViews.setInt(R.id.progress, "setBackgroundColor", 0xffbf3f5b);
        } else {
            remoteViews.setInt(R.id.progress, "setBackgroundColor", Theme.Light.strokecolor);
        }


        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, height * 0.6f);
        remoteViews.setTextViewTextSize(R.id.greg, TypedValue.COMPLEX_UNIT_PX, height / 9);
        remoteViews.setTextViewTextSize(R.id.hicri, TypedValue.COMPLEX_UNIT_PX, height / 9);
        remoteViews.setTextViewTextSize(R.id.lastTime, TypedValue.COMPLEX_UNIT_PX, height / 6);
        remoteViews.setTextViewTextSize(R.id.nextTime, TypedValue.COMPLEX_UNIT_PX, height / 6);
        remoteViews.setTextViewTextSize(R.id.lastText, TypedValue.COMPLEX_UNIT_PX, height / 9);
        remoteViews.setTextViewTextSize(R.id.nextText, TypedValue.COMPLEX_UNIT_PX, height / 9);
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, height / 5);
        if (Prefs.showWidgetSeconds())
            remoteViews.setChronometer(R.id.countdown, times.getMills(next)
                    - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = times.getLeft(next, false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }

        remoteViews.setViewPadding(R.id.progresscontainer, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.time, 0, -height / 6, 0, -height / 7);
        remoteViews.setViewPadding(R.id.greg, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.hicri, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.lastTime, width / 10, 0, width / 10, -width / 60);
        remoteViews.setViewPadding(R.id.lastText, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.nextTime, width / 10, 0, width / 10, -width / 60);
        remoteViews.setViewPadding(R.id.nextText, width / 10, 0, width / 10, 0);
        int w = width * 10 / 8;
        remoteViews.setViewPadding(R.id.progress, (int) (w * times.getPassedPart()), width / 75, 0, 0);
        remoteViews.setViewPadding(R.id.progressBg, (int) (w * (1 - times.getPassedPart())), width / 75, 0, 0);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    public static void update2x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int width = size.width;
        int height = size.height;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_2x2_clock);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, Main.getPendingIntent(times));


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap bmp1 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas1 = new Canvas(bmp1);

        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(width / 100);
        canvas1.drawArc(new RectF(width / 100, width / 100, width - width / 100, height - width / 100), 0, 360, false, paint);

        if (times.isKerahat()) {
            remoteViews.setInt(R.id.progress, "setColorFilter", 0xffbf3f5b);
            remoteViews.setInt(R.id.minute, "setTextColor", 0xffbf3f5b);
        } else {
            remoteViews.setInt(R.id.progress, "setColorFilter", Theme.Light.strokecolor);
            remoteViews.setInt(R.id.minute, "setTextColor", Theme.Light.strokecolor);
        }


        Bitmap bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas2 = new Canvas(bmp2);
        canvas2.drawArc(new RectF(width / 100, width / 100, width - width / 100, height - width / 100), -90, times.getPassedPart() * 360, false, paint);


        remoteViews.setImageViewBitmap(R.id.progressBG, bmp1);
        remoteViews.setImageViewBitmap(R.id.progress, bmp2);


        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0);

        if (Prefs.use12H()) {
            Calendar cal = Calendar.getInstance();
            String ampm = "AM";
            if (cal.get(Calendar.AM_PM) == Calendar.PM) {
                ampm = "PM";
            }
            Spannable span = new SpannableString("mm'" + ampm + "'");
            span.setSpan(new SuperscriptSpan(), 2, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(0.3f), 2, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            remoteViews.setCharSequence(R.id.hour, "setFormat12Hour", "hh");
            remoteViews.setCharSequence(R.id.hour, "setFormat24Hour", "hh");
            remoteViews.setCharSequence(R.id.minute, "setFormat12Hour", span);
            remoteViews.setCharSequence(R.id.minute, "setFormat24Hour", span);
        } else {
            remoteViews.setCharSequence(R.id.hour, "setFormat12Hour", "HH");
            remoteViews.setCharSequence(R.id.hour, "setFormat24Hour", "HH");
            remoteViews.setCharSequence(R.id.minute, "setFormat12Hour", "mm");
            remoteViews.setCharSequence(R.id.minute, "setFormat24Hour", "mm");
        }
        int next = times.getNext();

        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).getString());

        LocalDate date = LocalDate.now();
        remoteViews.setTextViewText(R.id.date, date.toString("d.MMM"));
        String wd = date.toString("EEEE");
        remoteViews.setTextViewText(R.id.weekDay, wd);

        if (Prefs.showWidgetSeconds())
            remoteViews.setChronometer(R.id.countdown, times.getMills(next)
                    - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = times.getLeft(next, false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.15));

        height *= 1.2f;
        remoteViews.setTextViewTextSize(R.id.weekDay, TypedValue.COMPLEX_UNIT_PX, (float) Math.min(height * 0.15, height / wd.length()));
        remoteViews.setTextViewTextSize(R.id.hour, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.4));
        remoteViews.setTextViewTextSize(R.id.minute, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.15));
        remoteViews.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.075));
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.075));

        remoteViews.setViewPadding(R.id.minute, 0, (int) (-height * 0.05), 0, (int) (-height * 0.03));
        remoteViews.setViewPadding(R.id.hour, 0, (int) (-height * 0.13), 0, (int) (-height * 0.10));
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
