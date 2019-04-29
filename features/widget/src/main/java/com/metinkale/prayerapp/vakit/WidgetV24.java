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

package com.metinkale.prayerapp.vakit;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.widget.RemoteViews;

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
@TargetApi(24)
class WidgetV24 {
    static void update2x2(Context context, AppWidgetManager appWidgetManager, int widgetId) {
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
        float scale = w / 10.5f;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_2x2);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2);
        LocalDate date = LocalDate.now();
        LocalDateTime[] daytimes = {times.getTime(date, Vakit.FAJR.ordinal()), times.getTime(date, Vakit.SUN.ordinal()), times.getTime(date, Vakit.DHUHR.ordinal()), times.getTime(date, Vakit.ASR.ordinal()), times.getTime(date, Vakit.MAGHRIB.ordinal()),
                times.getTime(date, Vakit.ISHAA.ordinal())};


        remoteViews.setOnClickPendingIntent(R.id.widget_layout, TimesFragment.getPendingIntent(times));


        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setTextColor(R.id.city, theme.textcolor);
        int current = times.getCurrentTime();
        int next = current + 1;
        int indicator = current;
        if ("next".equals(Preferences.VAKIT_INDICATOR_TYPE.get()))
            indicator = indicator + 1;
        int idsText[] = {R.id.fajrText, R.id.sunText, R.id.zuhrText, R.id.asrText, R.id.maghribText, R.id.ishaaText};
        int ids[] = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};
        for (Vakit v : Vakit.values()) {
            int i = v.ordinal();
            remoteViews.setTextViewTextSize(idsText[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f);
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f);
            remoteViews.setTextColor(idsText[i], theme.textcolor);
            remoteViews.setTextColor(ids[i], theme.textcolor);

            String name = Vakit.getByIndex(i).getString();
            String time = LocaleUtils.formatTime(daytimes[i].toLocalTime());
            if (Preferences.CLOCK_12H.get()) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>";
            }

            if (Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                if (v.ordinal() == indicator) {
                    name = "<b><i>" + name + "</i></b>";
                    time = "<b><i>" + time + "</i></b>";
                }
                remoteViews.setInt(idsText[i], "setBackgroundColor", 0);
                remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            } else {
                if (v.ordinal() == indicator) {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", theme.hovercolor);
                    remoteViews.setInt(ids[i], "setBackgroundColor", theme.hovercolor);
                } else {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", 0);
                    remoteViews.setInt(ids[i], "setBackgroundColor", 0);
                }
            }

            remoteViews.setTextViewText(idsText[i], Html.fromHtml(name));
            remoteViews.setTextViewText(ids[i], Html.fromHtml(time));

            remoteViews.setViewPadding(idsText[i], (int) ((Preferences.CLOCK_12H.get() ? 1.25 : 1.75) * scale), 0, (int) scale / 4, 0);
            remoteViews.setViewPadding(ids[i], 0, 0, (int) ((Preferences.CLOCK_12H.get() ? 1.25 : 1.75) * scale), 0);

        }

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);
        remoteViews.setViewPadding(R.id.city, (int) scale / 2, 0, (int) scale / 2, (int) scale / 4);

        if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS))
            remoteViews
                    .setChronometer(R.id.countdown, times.getTime(LocalDate.now(), next).toDateTime().getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next), false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    static void update4x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
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
        float scale = w / 25f;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2);
        LocalDate date = LocalDate.now();
        LocalDateTime[] daytimes = {times.getTime(date, Vakit.FAJR.ordinal()), times.getTime(date, Vakit.SUN.ordinal()), times.getTime(date, Vakit.DHUHR.ordinal()), times.getTime(date, Vakit.ASR.ordinal()), times.getTime(date, Vakit.MAGHRIB.ordinal()),
                times.getTime(date, Vakit.ISHAA.ordinal())};


        remoteViews.setOnClickPendingIntent(R.id.widget_layout, TimesFragment.getPendingIntent(times));
        remoteViews.setTextViewText(R.id.city, times.getName());
        remoteViews.setTextColor(R.id.city, theme.textcolor);
        int current = times.getCurrentTime();
        int next = current + 1;
        int ids[] = {R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa};

        int indicator = current;
        if ("next".equals(Preferences.VAKIT_INDICATOR_TYPE.get()))
            indicator = indicator + 1;
        for (Vakit v : Vakit.values()) {
            int i = v.ordinal();
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1.25f);
            remoteViews.setTextColor(ids[i], theme.textcolor);

            String name = Vakit.getByIndex(i).getString();
            String time = LocaleUtils.formatTime(daytimes[i].toLocalTime());
            if (Preferences.CLOCK_12H.get()) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>";
            }


            if (Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                if (v.ordinal() == indicator) {
                    name = "<b><i>" + name + "</i></b>";
                    time = "<b><i>" + time + "</i></b>";
                }
                remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            } else {
                if (v.ordinal() == indicator)
                    remoteViews.setInt(ids[i], "setBackgroundColor", theme.hovercolor);
                else
                    remoteViews.setInt(ids[i], "setBackgroundColor", 0);
            }

            remoteViews.setTextViewText(ids[i], Html.fromHtml(time + "<br/><small>" + name + "</small>"));

        }

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f);
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);

        remoteViews.setViewPadding(R.id.city, (int) scale / 2, (int) scale / 16, (int) scale / 4, (int) scale / 16);
        remoteViews.setViewPadding(R.id.countdown, (int) scale / 4, (int) scale / 16, (int) scale / 2, (int) scale / 16);

        if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS))
            remoteViews
                    .setChronometer(R.id.countdown, times.getTime(LocalDate.now(), next).toDateTime().getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next), false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }

        if (theme == Theme.Trans) {
            remoteViews.setViewPadding(R.id.divider, (int) scale / 2, 0, (int) scale / 2, 0);
        }
        appWidgetManager.updateAppWidget(widgetId, remoteViews);

    }

    static void update1x1(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1);
        int s = size.width;
        if (s <= 0)
            return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2);


        int next = times.getNextTime();

        String name = times.getName();
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, TimesFragment.getPendingIntent(times));
        if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS))
            remoteViews
                    .setChronometer(R.id.countdown, times.getTime(LocalDate.now(), next).toDateTime().getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next), false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, s / 4f);
        remoteViews.setTextViewText(R.id.city, name);
        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).getString());

        remoteViews.setTextColor(R.id.city, theme.textcolor);
        remoteViews.setTextColor(R.id.countdown, theme.textcolor);
        remoteViews.setTextColor(R.id.time, theme.textcolor);

        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, (float) Math.min(s / 5f, 1.5 * s / name.length()));
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, s / 5f);

        remoteViews.setViewPadding(R.id.countdown, 0, -s / 16, 0, -s / 16);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    static void updateSilenter(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Theme theme = WidgetUtils.getTheme(widgetId);
        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1);
        int s = size.width;
        if (s <= 0)
            return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_silenter);
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background);
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2);


        Intent i = new Intent(context, SilenterPrompt.class);
        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        remoteViews.setTextViewText(R.id.text, context.getString(R.string.silent));
        remoteViews.setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_PX, s / 4f);
        remoteViews.setTextColor(R.id.text, theme.textcolor);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);

    }

    static void update4x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f);
        int width = size.width;
        int height = size.height;
        if (width <= 0 || height <= 0)
            return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x2_clock);

        PendingIntent pendingIntent = TimesFragment.getPendingIntent(times);
        PendingIntent pendingIntentClock =
                PendingIntent.getActivity(context, UUID.asInt(), new Intent(AlarmClock.ACTION_SHOW_ALARMS), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://prayerapp.page.link/calendar"));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        PendingIntent pendingHijri = PendingIntent.getActivity(context, UUID.asInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


        long startMillis = System.currentTimeMillis();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent calendarIntent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        PendingIntent pendingIntentCalendar = PendingIntent.getActivity(context, UUID.asInt(), calendarIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        remoteViews.setOnClickPendingIntent(R.id.time, pendingIntentClock);
        remoteViews.setOnClickPendingIntent(R.id.greg, pendingIntentCalendar);
        remoteViews.setOnClickPendingIntent(R.id.hicri, pendingHijri);
        remoteViews.setOnClickPendingIntent(R.id.lastTime, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.lastText, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.nextTime, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.nextText, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.countdown, pendingIntent);

        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0);

        if (Preferences.DIGITS.get().equals("normal")) {
            if (Preferences.CLOCK_12H.get()) {
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
        } else {
            CharSequence txt = LocaleUtils.formatTimeForHTML(LocalTime.now());
            remoteViews.setCharSequence(R.id.time, "setFormat12Hour", txt);
            remoteViews.setCharSequence(R.id.time, "setFormat24Hour", txt);
        }

        int next = times.getNextTime();


        remoteViews.setTextViewText(R.id.lastText, Vakit.getByIndex(next - 1).getString());
        remoteViews.setTextViewText(R.id.nextText, Vakit.getByIndex(next).getString());
        remoteViews.setTextViewText(R.id.lastTime, LocaleUtils.formatTimeForHTML(times.getTime(LocalDate.now(), next - 1).toLocalTime()));
        remoteViews.setTextViewText(R.id.nextTime, LocaleUtils.formatTimeForHTML(times.getTime(LocalDate.now(), next).toLocalTime()));

        remoteViews.setTextViewText(R.id.greg, LocaleUtils.formatDate(LocalDate.now()));
        remoteViews.setTextViewText(R.id.hicri, LocaleUtils.formatDate(HijriDate.now()));

        if (times.isKerahat()) {
            remoteViews.setInt(R.id.progress, "setBackgroundColor", 0xffbf3f5b);
        } else {
            remoteViews.setInt(R.id.progress, "setBackgroundColor", Theme.Light.strokecolor);
        }


        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, height * 0.6f);
        remoteViews.setTextViewTextSize(R.id.greg, TypedValue.COMPLEX_UNIT_PX, height / 9f);
        remoteViews.setTextViewTextSize(R.id.hicri, TypedValue.COMPLEX_UNIT_PX, height / 9f);
        remoteViews.setTextViewTextSize(R.id.lastTime, TypedValue.COMPLEX_UNIT_PX, height / 6f);
        remoteViews.setTextViewTextSize(R.id.nextTime, TypedValue.COMPLEX_UNIT_PX, height / 6f);
        remoteViews.setTextViewTextSize(R.id.lastText, TypedValue.COMPLEX_UNIT_PX, height / 9f);
        remoteViews.setTextViewTextSize(R.id.nextText, TypedValue.COMPLEX_UNIT_PX, height / 9f);
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, height / 5f);
        if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS))
            remoteViews
                    .setChronometer(R.id.countdown, times.getTime(LocalDate.now(), next).toDateTime().getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next), false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }

        remoteViews.setViewPadding(R.id.progresscontainer, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.time, 0, -height / 6, 0, -height / 7);
        remoteViews.setViewPadding(R.id.greg, width / 10, 0, 0, 0);
        remoteViews.setViewPadding(R.id.hicri, 0, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.lastTime, width / 10, 0, width / 10, -width / 60);
        remoteViews.setViewPadding(R.id.lastText, width / 10, 0, width / 10, 0);
        remoteViews.setViewPadding(R.id.nextTime, width / 10, 0, width / 10, -width / 60);
        remoteViews.setViewPadding(R.id.nextText, width / 10, 0, width / 10, 0);
        int w = width * 10 / 8;
        remoteViews.setViewPadding(R.id.progress, (int) (w * getPassedPart(times)), width / 75, 0, 0);
        remoteViews.setViewPadding(R.id.progressBg, (int) (w * (1 - getPassedPart(times))), width / 75, 0, 0);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    static float getPassedPart(Times times) {
        int current = times.getCurrentTime();
        long now = LocalDateTime.now().toDateTime().getMillis();
        long prev = times.getTime(LocalDate.now(), current).toDateTime().getMillis();
        long next = times.getTime(LocalDate.now(), current + 1).toDateTime().getMillis();
        return (now - prev) / (float) (next - prev);
    }

    static void update2x2Clock(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Times times = WidgetUtils.getTimes(widgetId);
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId);
            return;
        }

        WidgetUtils.Size size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f);
        int width = size.width;
        int height = size.height;
        if (width <= 0 || height <= 0)
            return;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_2x2_clock);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, TimesFragment.getPendingIntent(times));


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.STROKE);

        Bitmap bmp1 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas1 = new Canvas(bmp1);

        paint.setColor(0xFFFFFFFF);
        paint.setStrokeWidth(width / 100f);
        canvas1.drawArc(new RectF(width / 100f, width / 100f, width - width / 100f, height - width / 100f), 0, 360, false, paint);

        if (times.isKerahat()) {
            remoteViews.setInt(R.id.progress, "setColorFilter", 0xffbf3f5b);
            remoteViews.setInt(R.id.minute, "setTextColor", 0xffbf3f5b);
        } else {
            remoteViews.setInt(R.id.progress, "setColorFilter", Theme.Light.strokecolor);
            remoteViews.setInt(R.id.minute, "setTextColor", Theme.Light.strokecolor);
        }


        Bitmap bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas2 = new Canvas(bmp2);
        canvas2.drawArc(new RectF(width / 100f, width / 100f, width - width / 100f, height - width / 100f), -90, getPassedPart(times) * 360, false,
                paint);


        remoteViews.setImageViewBitmap(R.id.progressBG, bmp1);
        remoteViews.setImageViewBitmap(R.id.progress, bmp2);


        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0);

        if (Preferences.CLOCK_12H.get()) {
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
        int next = times.getNextTime();

        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).getString());

        LocalDate date = LocalDate.now();
        remoteViews.setTextViewText(R.id.date, date.toString("d.MMM"));
        String wd = date.toString("EEEE");
        remoteViews.setTextViewText(R.id.weekDay, wd);

        if (Preferences.COUNTDOWN_TYPE.get().equals(Preferences.COUNTDOWN_TYPE_SHOW_SECONDS))
            remoteViews
                    .setChronometer(R.id.countdown, times.getTime(LocalDate.now(), next).toDateTime().getMillis() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()), null, true);
        else {
            String txt = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(LocalDate.now(), next), false);
            remoteViews.setString(R.id.countdown, "setFormat", txt);
            remoteViews.setChronometer(R.id.countdown, 0, txt, false);
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.15));

        height *= 1.2f;
        remoteViews.setTextViewTextSize(R.id.weekDay, TypedValue.COMPLEX_UNIT_PX, (float) Math.min(height * 0.15, height / (float) wd.length()));
        remoteViews.setTextViewTextSize(R.id.hour, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.4));
        remoteViews.setTextViewTextSize(R.id.minute, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.15));
        remoteViews.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.075));
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, (float) (height * 0.075));

        remoteViews.setViewPadding(R.id.minute, 0, (int) (-height * 0.05), 0, (int) (-height * 0.03));
        remoteViews.setViewPadding(R.id.hour, 0, (int) (-height * 0.13), 0, (int) (-height * 0.10));
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
