/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayerapp.vakit

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.times.SilenterPrompt
import com.metinkale.prayer.times.fragments.TimesFragment.Companion.getPendingIntent
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.UUID
import com.metinkale.prayer.widgets.R
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

/**
 * Created by metin on 24.03.2017.
 */
internal object WidgetLegacy {
    fun update1x1(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val r = context.resources
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, r.displayMetrics)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val theme: Theme = WidgetUtils.getTheme(widgetId)
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
        val s = size.width
        if (s <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget)
        var next = times.getNextTime()
        val left = LocaleUtils.formatPeriod(now, times.getTime(today, next), false)
        if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") next = next + 1
        remoteViews.setOnClickPendingIntent(R.id.widget, getPendingIntent(times))
        val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        canvas.scale(0.99f, 0.99f, s / 2f, s / 2f)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL
        paint.color = theme.bgcolor
        canvas.drawRect(0f, 0f, s.toFloat(), s.toFloat(), paint)
        paint.color = theme.textcolor
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.color = theme.hovercolor
        val city = times.name
        paint.color = theme.textcolor
        var cs = s / 5f
        val ts = s * 35 / 100f
        val vs = s / 4
        paint.textSize = cs
        cs = cs * s * 0.9f / paint.measureText(city)
        cs = if (cs > vs) vs.toFloat() else cs
        paint.textSize = vs.toFloat()
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(Vakit.getByIndex(next).prevTime().string, s / 2f, s * 22 / 80f, paint)
        paint.textSize = ts
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(left, s / 2f, s / 2f + ts * 1 / 3, paint)
        paint.textSize = cs
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(city, s / 2f, s * 3 / 4f + cs * 2 / 3, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp
        paint.color = theme.strokecolor
        canvas.drawRect(0f, 0f, s.toFloat(), s.toFloat(), paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }

    fun update4x1(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val r = context.resources
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, r.displayMetrics)
        val theme: Theme = WidgetUtils.getTheme(widgetId)
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.Companion.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.Companion.getSize(context, appWidgetManager, widgetId, 300f / 60f)
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget)
        val dateTime = LocalDateTime.now()
        val date = dateTime.toLocalDate()
        var next = times.getNextTime()
        val left = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(date, next))
        if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") next = next + 1
        remoteViews.setOnClickPendingIntent(R.id.widget, getPendingIntent(times))
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        canvas.scale(0.99f, 0.99f, w / 2f, h / 2f)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL
        paint.color = theme.bgcolor
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.color = theme.hovercolor
        if (next != Vakit.FAJR.ordinal && !Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get() && next <= Vakit.ISHAA.ordinal) {
            canvas.drawRect(w * (next - 1) / 6f, h * 3 / 9f, w * next / 6f, h.toFloat(), paint)
        }
        val s = paint.strokeWidth
        val dip = 3f
        paint.strokeWidth = dip * dp
        canvas.drawLine(0f, h * 3 / 9f, w.toFloat(), h * 3 / 9f, paint)
        // canvas.drawRect(0, 0, w, h * 3 / 9, paint);
        paint.strokeWidth = s
        paint.color = theme.textcolor
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = h / 4f
        canvas.drawText(" " + times.name, 0f, h / 4f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$left ", w.toFloat(), h / 4f, paint)
        paint.textSize = h / 5f
        paint.textAlign = Paint.Align.CENTER
        var y = h * 6 / 7
        if (Preferences.CLOCK_12H.get()) {
            y += h / 14
        }
        var fits = true
        do {
            if (!fits) {
                paint.textSize = (paint.textSize * 0.95).toFloat()
            }
            fits = true
            for (v in Vakit.values()) {
                if (paint.measureText(v.string) > w / 6f && w > 5) {
                    fits = false
                }
            }
        } while (!fits)
        for (v in Vakit.values()) {
            val i = v.ordinal
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            }
            canvas.drawText(v.string, w * (1 + 2 * i) / 12f, y.toFloat(), paint)
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
        }
        paint.textSize = h * 2 / 9f
        if (Preferences.CLOCK_12H.get()) {
            for (v in Vakit.values()) {
                val i = v.ordinal
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                }
                var time = LocaleUtils.formatTime(times.getTime(date, v.ordinal).toLocalTime())
                val suffix = time.substring(time.indexOf(" ") + 1)
                time = time.substring(0, time.indexOf(" "))
                paint.textSize = h * 2 / 9f
                canvas.drawText(time, w * (1 + 2 * i) / 12f, h * 6 / 10f, paint)
                paint.textSize = h / 9f
                canvas.drawText(suffix, w * (1 + 2 * i) / 12f, h * 7 / 10f, paint)
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
            }
        } else {
            for (v in Vakit.values()) {
                val i = v.ordinal
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                }
                canvas.drawText(
                    LocaleUtils.formatTime(
                        times.getTime(date, v.ordinal).toLocalTime()
                    ), w * (1 + 2 * i) / 12f, h * 3 / 5f, paint
                )
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
            }
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp
        paint.color = theme.strokecolor
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }

    fun update2x2(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val r = context.resources
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, r.displayMetrics)
        val theme: Theme = WidgetUtils.getTheme(widgetId)
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 130f / 160f)
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget)
        val date = LocalDate.now()
        var next = times.getNextTime()
        val left = LocaleUtils.formatPeriod(LocalDateTime.now(), times.getTime(date, next))
        if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") next = next + 1
        remoteViews.setOnClickPendingIntent(R.id.widget, getPendingIntent(times))
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        canvas.scale(0.99f, 0.99f, w / 2f, h / 2f)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL
        paint.color = theme.bgcolor
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.color = theme.textcolor
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        val l = (h / 10f).toDouble()
        paint.textSize = l.toInt().toFloat()
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(times.name, w / 2f, (l * 1.8).toInt().toFloat(), paint)
        paint.textSize = (l * 8 / 10).toInt().toFloat()
        if (next != Vakit.FAJR.ordinal && !Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get() && next <= Vakit.ISHAA.ordinal) {
            paint.color = theme.hovercolor
            canvas.drawRect(
                0f,
                (l * (next + 1.42f)).toInt().toFloat(),
                w.toFloat(),
                (l * (next + 2.42)).toInt().toFloat(),
                paint
            )
        }
        paint.color = theme.textcolor
        paint.textAlign = Paint.Align.LEFT
        for (v in Vakit.values()) {
            val i = v.ordinal
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            }
            canvas.drawText(v.string, w / 6f, (l * (3.2 + v.ordinal)).toInt().toFloat(), paint)
            if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
        }
        paint.textAlign = Paint.Align.RIGHT
        if (Preferences.CLOCK_12H.get()) {
            for (v in Vakit.values()) {
                val i = v.ordinal
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                }
                var time = LocaleUtils.formatTime(times.getTime(date, v.ordinal).toLocalTime())
                val suffix = time.substring(time.indexOf(" ") + 1)
                time = time.substring(0, time.indexOf(" "))
                paint.textSize = (l * 8 / 10).toInt().toFloat()
                canvas.drawText(
                    time,
                    w * 5 / 6f - paint.measureText("A"),
                    (l * 3.2 + i * l).toInt().toFloat(),
                    paint
                )
                paint.textSize = (l * 4 / 10).toInt().toFloat()
                canvas.drawText(
                    suffix,
                    w * 5 / 6f + paint.measureText(time) / 4,
                    (l * 3 + i * l).toInt().toFloat(),
                    paint
                )
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
            }
        } else {
            for (v in Vakit.values()) {
                val i = v.ordinal
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                }
                canvas.drawText(
                    LocaleUtils.formatTime(
                        times.getTime(date, v.ordinal).toLocalTime()
                    ), w * 5 / 6f, (l * 3.2 + i * l).toInt().toFloat(), paint
                )
                if (i == next - 1 && Preferences.SHOW_ALT_WIDGET_HIGHLIGHT.get()) {
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
            }
        }
        paint.textSize = l.toInt().toFloat()
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(left, w / 2f, (l * 9.5).toInt().toFloat(), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp
        paint.color = theme.strokecolor
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }

    fun updateSilenter(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val r = context.resources
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, r.displayMetrics)
        val theme: Theme = WidgetUtils.Companion.getTheme(widgetId)
        val size: WidgetUtils.Size =
            WidgetUtils.Companion.getSize(context, appWidgetManager, widgetId, 1f)
        val s = size.width
        if (s <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget)
        val i = Intent(context, SilenterPrompt::class.java)
        remoteViews.setOnClickPendingIntent(
            R.id.widget,
            PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        )
        val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        canvas.scale(0.99f, 0.99f, s / 2f, s / 2f)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL
        paint.color = theme.bgcolor
        canvas.drawRect(0f, 0f, s.toFloat(), s.toFloat(), paint)
        paint.color = theme.textcolor
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.color = theme.hovercolor
        paint.color = theme.textcolor
        paint.textSize = s * 25 / 100f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Sessize", s / 2f, s * 125 / 300f, paint)
        canvas.drawText("al", s / 2f, s * 25 / 30f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp
        paint.color = theme.strokecolor
        canvas.drawRect(0f, 0f, s.toFloat(), s.toFloat(), paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }

    fun update4x2Clock(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.Companion.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.Companion.getSize(context, appWidgetManager, widgetId, 500f / 200f)
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget_clock)
        remoteViews.setOnClickPendingIntent(
            R.id.abovePart,
            PendingIntent.getActivity(
                context,
                UUID.asInt(),
                Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        remoteViews.setOnClickPendingIntent(R.id.belowPart, getPendingIntent(times))
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(java.lang.Long.toString(System.currentTimeMillis()))
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        remoteViews.setOnClickPendingIntent(
            R.id.center,
            PendingIntent.getActivity(
                context,
                UUID.asInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        val next = times.getNextTime()
        val last = next - 1
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.setShadowLayer(2f, 2f, 2f, -0xaaaaab)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        val ltime = LocalTime.now()
        paint.textSize = h * 0.55f
        if (Preferences.CLOCK_12H.get()) {
            var time = LocaleUtils.formatTime(ltime)
            val suffix = time.substring(time.indexOf(" ") + 1)
            time = time.substring(0, time.indexOf(" "))
            canvas.drawText(time, w / 2f - paint.measureText(suffix) / 4, h * 0.4f, paint)
            paint.textSize = h * 0.275f
            canvas.drawText(suffix, w / 2f + paint.measureText(time), h * 0.2f, paint)
        } else {
            canvas.drawText(
                LocaleUtils.formatNumber(LocaleUtils.formatTime(ltime)),
                w / 2f,
                h * 0.4f,
                paint
            )
        }
        val greg = LocaleUtils.formatDate(LocalDate.now())
        val hicri = LocaleUtils.formatDate(HijriDate.now())
        paint.textSize = h * 0.12f
        val m = paint.measureText("$greg  $hicri")
        if (m > w * 0.8f) {
            paint.textSize = h * 0.12f * w * 0.8f / m
        }
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(greg, w * .1f, h * 0.55f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(hicri, w * .9f, h * 0.55f, paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        canvas.drawRect(w * 0.1f, h * 0.6f, w * 0.9f, h * 0.63f, paint)
        if (times.isKerahat()) {
            paint.color = -0x40c0a5
        } else {
            paint.color = Theme.Light.strokecolor
        }
        canvas.drawRect(
            w * 0.1f,
            h * 0.6f,
            w * 0.1f + w * 0.8f * WidgetV24.getPassedPart(times),
            h * 0.63f,
            paint
        )
        paint.color = Color.WHITE
        paint.textSize = h * 0.2f
        paint.textAlign = Paint.Align.LEFT
        if (Preferences.CLOCK_12H.get()) {
            var l = LocaleUtils.formatTime(times.getTime(LocalDate.now(), last).toLocalTime())
            val s = l.substring(l.indexOf(" ") + 1)
            l = l.substring(0, l.indexOf(" "))
            canvas.drawText(l, w * 0.1f, h * 0.82f, paint)
            paint.textSize = h * 0.1f
            canvas.drawText(s, w * 0.1f + 2 * paint.measureText(l), h * 0.72f, paint)
        } else {
            canvas.drawText(
                LocaleUtils.formatTime(
                    times.getTime(LocalDate.now(), last).toLocalTime()
                ), w * 0.1f, h * 0.82f, paint
            )
        }
        paint.textSize = h * 0.12f
        canvas.drawText(Vakit.getByIndex(last).string, w * 0.1f, h * 0.95f, paint)
        paint.color = Color.WHITE
        paint.textSize = h * 0.2f
        paint.textAlign = Paint.Align.RIGHT
        if (Preferences.CLOCK_12H.get()) {
            var l = LocaleUtils.formatTime(times.getTime(LocalDate.now(), next).toLocalTime())
            val s = l.substring(l.indexOf(" ") + 1)
            l = l.substring(0, l.indexOf(" "))
            canvas.drawText(l, w * 0.9f - paint.measureText(s) / 2, h * 0.82f, paint)
            paint.textSize = h * 0.1f
            canvas.drawText(s, w * 0.9f, h * 0.72f, paint)
        } else {
            canvas.drawText(
                LocaleUtils.formatTime(
                    times.getTime(LocalDate.now(), next).toLocalTime()
                ), w * 0.9f, h * 0.82f, paint
            )
        }
        paint.textSize = h * 0.12f
        canvas.drawText(Vakit.getByIndex(next).string, w * 0.9f, h * 0.95f, paint)
        paint.color = Color.WHITE
        paint.textSize = h * 0.25f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText(
            LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDateTime.now().toLocalDate(), next)
            ), w * 0.5f, h * 0.9f, paint
        )
        paint.isFakeBoldText = false
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }

    fun update2x2Clock(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.vakit_widget_clock)
        remoteViews.setOnClickPendingIntent(
            R.id.abovePart, PendingIntent
                .getActivity(
                    context,
                    System.currentTimeMillis().toInt(),
                    Intent(AlarmClock.ACTION_SHOW_ALARMS),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
        )
        remoteViews.setOnClickPendingIntent(R.id.belowPart, getPendingIntent(times))
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(java.lang.Long.toString(System.currentTimeMillis()))
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        remoteViews.setOnClickPendingIntent(
            R.id.center,
            PendingIntent.getActivity(
                context,
                UUID.asInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bmp)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.setShadowLayer(2f, 2f, 2f, -0xaaaaab)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        paint.color = -0x1
        paint.strokeWidth = w / 100f
        canvas.drawArc(
            RectF(w / 100f, w / 100f, w - w / 100f, h - w / 100f),
            0f,
            360f,
            false,
            paint
        )
        val isKerahat = times.isKerahat()
        if (isKerahat) {
            paint.color = -0x40c0a5
        } else {
            paint.color = Theme.Light.strokecolor
        }
        val next = times.getNextTime()
        var indicator = next - 1
        if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") indicator = indicator + 1
        canvas.drawArc(
            RectF(w / 100f, w / 100f, w - w / 100f, h - w / 100f),
            -90f,
            WidgetV24.getPassedPart(times) * 360,
            false,
            paint
        )
        paint.strokeWidth = 1f
        val ltime = LocalDateTime.now()
        val time = LocaleUtils.formatNumber(ltime.toString("HH:mm")).replace(":", " ").split(" ")
            .toTypedArray()
        paint.textSize = h * 0.50f
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        if (time.size == 3) {
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(time[0], w * 0.59f, h * 0.65f, paint)
            paint.textAlign = Paint.Align.LEFT
            if (isKerahat) {
                paint.color = -0x40c0a5
            } else {
                paint.color = Theme.Light.strokecolor
            }
            paint.textSize = h * 0.18f
            canvas.drawText(time[1], w * 0.60f, h * 0.45f, paint)
            paint.textSize = h * 0.1f
            paint.textSize = h * 0.1f
            canvas.drawText(time[2], w * 0.80f, h * 0.45f, paint)
            paint.color = -0x1
            paint.textSize = h * 0.07f
            canvas.drawText(
                LocaleUtils.formatNumber(ltime.toString("d'.' MMM'.'")),
                w * 0.60f,
                h * 0.55f,
                paint
            )
            canvas.drawText(Vakit.getByIndex(indicator).string, w * 0.60f, h * 0.65f, paint)
        } else {
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(time[0], w * 0.62f, h * 0.65f, paint)
            paint.textAlign = Paint.Align.LEFT
            if (isKerahat) {
                paint.color = -0x40c0a5
            } else {
                paint.color = Theme.Light.strokecolor
            }
            paint.textSize = h * 0.22f
            canvas.drawText(time[1], w * 0.63f, h * 0.45f, paint)
            paint.color = -0x1
            paint.textSize = h * 0.07f
            canvas.drawText(
                LocaleUtils.formatNumber(ltime.toString("d'.' MMM'.'")),
                w * 0.63f,
                h * 0.55f,
                paint
            )
            canvas.drawText(Vakit.getByIndex(indicator).string, w * 0.63f, h * 0.65f, paint)
        }
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = h * 0.15f
        canvas.drawText(
            LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next)
            ), w / 2f, h * 0.85f, paint
        )
        paint.textSize = h * 0.12f
        canvas.drawText(ltime.toString("EEEE"), w / 2f, h * 0.22f, paint)
        remoteViews.setImageViewBitmap(R.id.widget, bmp)
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        } catch (e: RuntimeException) {
            if (!e.message!!.contains("exceeds maximum bitmap memory usage")) {
                recordException(e)
            }
        }
    }
}