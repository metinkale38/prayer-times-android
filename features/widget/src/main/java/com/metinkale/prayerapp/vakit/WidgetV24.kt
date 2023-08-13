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

import android.annotation.TargetApi
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.*
import android.icu.util.Calendar
import android.net.Uri
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.Module
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.times.SilenterPrompt
import com.metinkale.prayer.times.fragments.TimesFragment.Companion.getPendingIntent
import com.metinkale.prayer.times.times.*
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.UUID
import com.metinkale.prayer.utils.Utils
import com.metinkale.prayer.widgets.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Created by metin on 24.03.2017.
 */
@TargetApi(24)
internal object WidgetV24 {
    fun update2x2(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
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
        val scale = w / 10.5f
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_2x2)
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2)
        val date = LocalDate.now()
        val daytimes = arrayOf(
            times.getTime(date, Vakit.FAJR.ordinal),
            times.getTime(date, Vakit.SUN.ordinal),
            times.getTime(date, Vakit.DHUHR.ordinal),
            times.getTime(date, Vakit.ASR.ordinal),
            times.getTime(date, Vakit.MAGHRIB.ordinal),
            times.getTime(date, Vakit.ISHAA.ordinal)
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingIntent(times))
        remoteViews.setTextViewText(R.id.city, times.name)
        remoteViews.setTextColor(R.id.city, theme.textcolor)
        val current = times.getCurrentTime()
        val next = current + 1
        var indicator = current
        if ("next" == Preferences.VAKIT_INDICATOR_TYPE) indicator = indicator + 1
        val idsText = intArrayOf(
            R.id.fajrText,
            R.id.sunText,
            R.id.zuhrText,
            R.id.asrText,
            R.id.maghribText,
            R.id.ishaaText
        )
        val ids = intArrayOf(R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa)
        val rtl = Utils.isRTL(context)
        for (v in Vakit.values()) {
            val i = v.ordinal
            remoteViews.setTextViewTextSize(idsText[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f)
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1f)
            remoteViews.setTextColor(idsText[i], theme.textcolor)
            remoteViews.setTextColor(ids[i], theme.textcolor)
            var name = Vakit.getByIndex(i).string
            var time = LocaleUtils.formatTime(daytimes[i].toLocalTime())
            if (Preferences.CLOCK_12H) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>"
            }
            if (Preferences.SHOW_ALT_WIDGET_HIGHLIGHT) {
                if (v.ordinal == indicator) {
                    name = "<b><i>$name</i></b>"
                    time = "<b><i>$time</i></b>"
                }
                remoteViews.setInt(idsText[i], "setBackgroundColor", 0)
                remoteViews.setInt(ids[i], "setBackgroundColor", 0)
            } else {
                if (v.ordinal == indicator) {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", theme.hovercolor)
                    remoteViews.setInt(ids[i], "setBackgroundColor", theme.hovercolor)
                } else {
                    remoteViews.setInt(idsText[i], "setBackgroundColor", 0)
                    remoteViews.setInt(ids[i], "setBackgroundColor", 0)
                }
            }
            remoteViews.setTextViewText(idsText[i], Html.fromHtml(if (!rtl) name else time))
            remoteViews.setTextViewText(ids[i], Html.fromHtml(if (!rtl) time else name))
            remoteViews.setViewPadding(
                idsText[i],
                ((if (Preferences.CLOCK_12H) 1.25 else 1.75) * scale).toInt(),
                0,
                scale.toInt() / 4,
                0
            )
            remoteViews.setViewPadding(
                ids[i],
                0,
                0,
                ((if (Preferences.CLOCK_12H) 1.25 else 1.75) * scale).toInt(),
                0
            )
        }
        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f)
        remoteViews.setTextColor(R.id.countdown, theme.textcolor)
        remoteViews.setViewPadding(
            R.id.city,
            scale.toInt() / 2,
            0,
            scale.toInt() / 2,
            scale.toInt() / 4
        )
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews
            .setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
            val txt = LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next),
                false
            )
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.3f)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    fun update4x1(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val theme: Theme = WidgetUtils.getTheme(widgetId)
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 300f / 60f)
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return
        val scale = w / 25f
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_4x1)
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
        remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2)
        val date = LocalDate.now()
        val daytimes = arrayOf(
            times.getTime(date, Vakit.FAJR.ordinal),
            times.getTime(date, Vakit.SUN.ordinal),
            times.getTime(date, Vakit.DHUHR.ordinal),
            times.getTime(date, Vakit.ASR.ordinal),
            times.getTime(date, Vakit.MAGHRIB.ordinal),
            times.getTime(date, Vakit.ISHAA.ordinal)
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingIntent(times))
        remoteViews.setTextViewText(R.id.city, times.name)
        remoteViews.setTextColor(R.id.city, theme.textcolor)
        val current = times.getCurrentTime()
        val next = current + 1
        val ids = intArrayOf(R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa)
        val rtl = Utils.isRTL(context)
        if (rtl) {
            for (i in 0 until ids.size / 2) {
                val temp = ids[i]
                ids[i] = ids[ids.size - i - 1]
                ids[ids.size - i - 1] = temp
            }
        }
        var indicator = current
        if ("next" == Preferences.VAKIT_INDICATOR_TYPE) indicator += 1
        for (v in Vakit.values()) {
            val i = v.ordinal
            remoteViews.setTextViewTextSize(ids[i], TypedValue.COMPLEX_UNIT_PX, scale * 1.25f)
            remoteViews.setTextColor(ids[i], theme.textcolor)
            var name = Vakit.getByIndex(i).string
            var time = LocaleUtils.formatTime(daytimes[i].toLocalTime())
            if (Preferences.CLOCK_12H) {
                time = time.replace(" ", "<sup><small>") + "</small></sup>"
            }
            if (Preferences.SHOW_ALT_WIDGET_HIGHLIGHT) {
                if (v.ordinal == indicator) {
                    name = "<b><i>$name</i></b>"
                    time = "<b><i>$time</i></b>"
                }
                remoteViews.setInt(ids[i], "setBackgroundColor", 0)
            } else {
                if (v.ordinal == indicator) remoteViews.setInt(
                    ids[i],
                    "setBackgroundColor",
                    theme.hovercolor
                ) else remoteViews.setInt(
                    ids[i], "setBackgroundColor", 0
                )
            }
            remoteViews.setTextViewText(ids[i], Html.fromHtml("$time<br/><small>$name</small>"))
        }
        remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f)
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f)
        remoteViews.setTextColor(R.id.countdown, theme.textcolor)
        remoteViews.setViewPadding(
            R.id.city,
            scale.toInt() / 2,
            scale.toInt() / 16,
            scale.toInt() / 4,
            scale.toInt() / 16
        )
        remoteViews.setViewPadding(
            R.id.countdown,
            scale.toInt() / 4,
            scale.toInt() / 16,
            scale.toInt() / 2,
            scale.toInt() / 16
        )
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews
            .setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
            val txt = LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next),
                false
            )
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
        }
        if (theme == Theme.Trans) {
            remoteViews.setViewPadding(R.id.divider, scale.toInt() / 2, 0, scale.toInt() / 2, 0)
        }
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    fun update1x1(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_1x1)
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2)
        val next = times.getNextTime()
        val name = times.name
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingIntent(times))
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews
            .setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
            val txt = LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next),
                false
            )
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
        }
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, s / 4f)
        remoteViews.setTextViewText(R.id.city, name)
        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).string)
        remoteViews.setTextColor(R.id.city, theme.textcolor)
        remoteViews.setTextColor(R.id.countdown, theme.textcolor)
        remoteViews.setTextColor(R.id.time, theme.textcolor)
        remoteViews.setTextViewTextSize(
            R.id.city,
            TypedValue.COMPLEX_UNIT_PX,
            Math.min((s / 5f).toDouble(), 1.5 * s / name.length).toFloat()
        )
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, s / 5f)
        remoteViews.setViewPadding(R.id.countdown, 0, -s / 16, 0, -s / 16)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)

    }

    fun updateSilenter(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val theme: Theme = WidgetUtils.getTheme(widgetId)
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
        val s = size.width
        if (s <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_1x1_silenter)
        remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
        remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2)
        val i = Intent(context, SilenterPrompt::class.java)
        remoteViews.setOnClickPendingIntent(
            R.id.widget,
            PendingIntent.getActivity(
                context,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        remoteViews.setTextViewText(R.id.text, context.getString(R.string.silent))
        remoteViews.setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_PX, s / 4f)
        remoteViews.setTextColor(R.id.text, theme.textcolor)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    fun update4x2Clock(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f)
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_4x2_clock)
        val pendingIntent = getPendingIntent(times)
        val pendingIntentClock = PendingIntent.getActivity(
            context,
            UUID.asInt(),
            Intent(AlarmClock.ACTION_SHOW_ALARMS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intent = Module.CALENDAR.buildIntent(context)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val pendingHijri = PendingIntent.getActivity(
            context,
            UUID.asInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val startMillis = System.currentTimeMillis()
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        ContentUris.appendId(builder, startMillis)
        val calendarIntent = Intent(Intent.ACTION_VIEW).setData(builder.build())
        val pendingIntentCalendar = PendingIntent.getActivity(
            context,
            UUID.asInt(),
            calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.time, pendingIntentClock)
        remoteViews.setOnClickPendingIntent(R.id.greg, pendingIntentCalendar)
        remoteViews.setOnClickPendingIntent(R.id.hicri, pendingHijri)
        remoteViews.setOnClickPendingIntent(R.id.lastTime, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.lastText, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.nextTime, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.nextText, pendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.countdown, pendingIntent)
        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0)
        if (Preferences.DIGITS == "normal") {
            if (Preferences.CLOCK_12H) {
                val cal = Calendar.getInstance()
                var ampm = "AM"
                if (cal[Calendar.AM_PM] == Calendar.PM) {
                    ampm = "PM"
                }
                val span: Spannable = SpannableString("hh:mm'$ampm'")
                span.setSpan(SuperscriptSpan(), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                span.setSpan(RelativeSizeSpan(0.3f), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                remoteViews.setCharSequence(R.id.time, "setFormat12Hour", span)
                remoteViews.setCharSequence(R.id.time, "setFormat24Hour", span)
            } else {
                remoteViews.setCharSequence(R.id.time, "setFormat12Hour", "HH:mm")
                remoteViews.setCharSequence(R.id.time, "setFormat24Hour", "HH:mm")
            }
        } else {
            val txt = LocaleUtils.formatTimeForHTML(LocalTime.now())
            remoteViews.setCharSequence(R.id.time, "setFormat12Hour", txt)
            remoteViews.setCharSequence(R.id.time, "setFormat24Hour", txt)
        }
        val next = times.getNextTime()
        val rtl = Utils.isRTL(context)
        remoteViews.setTextViewText(
            if (!rtl) R.id.lastText else R.id.nextText,
            Vakit.getByIndex(next - 1).string
        )
        remoteViews.setTextViewText(
            if (!rtl) R.id.nextText else R.id.lastText,
            Vakit.getByIndex(next).string
        )
        remoteViews.setTextViewText(
            if (!rtl) R.id.lastTime else R.id.nextTime, LocaleUtils.formatTimeForHTML(
                times.getTime(
                    LocalDate.now(), next - 1
                ).toLocalTime()
            )
        )
        remoteViews.setTextViewText(
            if (!rtl) R.id.nextTime else R.id.lastTime, LocaleUtils.formatTimeForHTML(
                times.getTime(
                    LocalDate.now(), next
                ).toLocalTime()
            )
        )
        remoteViews.setTextViewText(
            if (!rtl) R.id.greg else R.id.hicri, LocaleUtils.formatDate(
                LocalDate.now()
            )
        )
        remoteViews.setTextViewText(
            if (!rtl) R.id.hicri else R.id.greg,
            LocaleUtils.formatDate(HijriDate.now())
        )
        remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, height * 0.6f)
        remoteViews.setTextViewTextSize(R.id.greg, TypedValue.COMPLEX_UNIT_PX, height / 9f)
        remoteViews.setTextViewTextSize(R.id.hicri, TypedValue.COMPLEX_UNIT_PX, height / 9f)
        remoteViews.setTextViewTextSize(R.id.lastTime, TypedValue.COMPLEX_UNIT_PX, height / 6f)
        remoteViews.setTextViewTextSize(R.id.nextTime, TypedValue.COMPLEX_UNIT_PX, height / 6f)
        remoteViews.setTextViewTextSize(R.id.lastText, TypedValue.COMPLEX_UNIT_PX, height / 9f)
        remoteViews.setTextViewTextSize(R.id.nextText, TypedValue.COMPLEX_UNIT_PX, height / 9f)
        remoteViews.setTextViewTextSize(R.id.countdown, TypedValue.COMPLEX_UNIT_PX, height / 5f)
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews
            .setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
            val txt = LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next),
                false
            )
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
        }
        remoteViews.setViewPadding(R.id.progresscontainer, width / 10, 0, width / 10, 0)
        remoteViews.setViewPadding(R.id.time, 0, -height / 6, 0, -height / 7)
        remoteViews.setViewPadding(R.id.greg, width / 10, 0, 0, 0)
        remoteViews.setViewPadding(R.id.hicri, 0, 0, width / 10, 0)
        remoteViews.setViewPadding(R.id.lastTime, width / 10, 0, width / 10, -width / 60)
        remoteViews.setViewPadding(R.id.lastText, width / 10, 0, width / 10, 0)
        remoteViews.setViewPadding(R.id.nextTime, width / 10, 0, width / 10, -width / 60)
        remoteViews.setViewPadding(R.id.nextText, width / 10, 0, width / 10, 0)
        val w = width * 10 / 8
        remoteViews.setInt(if (!rtl) R.id.progressBg else R.id.progress, "setBackgroundColor", -0x1)
        remoteViews.setInt(
            if (!rtl) R.id.progress else R.id.progressBg,
            "setBackgroundColor",
            if (times.isKerahat()) -0x40c0a5 else Theme.Light.strokecolor
        )
        var passedPart = getPassedPart(times)
        if (rtl) passedPart = 1 - passedPart
        remoteViews.setViewPadding(R.id.progress, (w * passedPart).toInt(), width / 75, 0, 0)
        remoteViews.setViewPadding(
            R.id.progressBg,
            (w * (1 - passedPart)).toInt(),
            width / 75,
            0,
            0
        )
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    fun getPassedPart(times: Times): Float {
        val current = times.getCurrentTime()
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val prev =
            times.getTime(LocalDate.now(), current).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        val next =
            times.getTime(LocalDate.now(), current + 1).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        return (now - prev) / (next - prev).toFloat()
    }

    fun update2x2Clock(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val times: Times? = WidgetUtils.getTimes(widgetId)
        if (times == null) {
            WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
            return
        }
        val size: WidgetUtils.Size =
            WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
        val width = size.width
        var height = size.height
        if (width <= 0 || height <= 0) return
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_2x2_clock)
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingIntent(times))
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.style = Paint.Style.STROKE
        val bmp1 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas1 = Canvas(bmp1)
        paint.color = -0x1
        paint.strokeWidth = width / 100f
        canvas1.drawArc(
            RectF(
                width / 100f,
                width / 100f,
                width - width / 100f,
                height - width / 100f
            ), 0f, 360f, false, paint
        )
        if (times.isKerahat()) {
            remoteViews.setInt(R.id.progress, "setColorFilter", -0x40c0a5)
            remoteViews.setInt(R.id.minute, "setTextColor", -0x40c0a5)
        } else {
            remoteViews.setInt(R.id.progress, "setColorFilter", Theme.Light.strokecolor)
            remoteViews.setInt(R.id.minute, "setTextColor", Theme.Light.strokecolor)
        }
        val bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas2 = Canvas(bmp2)
        canvas2.drawArc(
            RectF(width / 100f, width / 100f, width - width / 100f, height - width / 100f),
            -90f,
            getPassedPart(times) * 360,
            false,
            paint
        )
        remoteViews.setImageViewBitmap(R.id.progressBg, bmp1)
        remoteViews.setImageViewBitmap(R.id.progress, bmp2)
        remoteViews.setViewPadding(R.id.padder, width, height, 0, 0)
        if (Preferences.CLOCK_12H) {
            val cal = Calendar.getInstance()
            var ampm = "AM"
            if (cal[Calendar.AM_PM] == Calendar.PM) {
                ampm = "PM"
            }
            val span: Spannable = SpannableString("mm'$ampm'")
            span.setSpan(SuperscriptSpan(), 2, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(RelativeSizeSpan(0.3f), 2, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            remoteViews.setCharSequence(R.id.hour, "setFormat12Hour", "hh")
            remoteViews.setCharSequence(R.id.hour, "setFormat24Hour", "hh")
            remoteViews.setCharSequence(R.id.minute, "setFormat12Hour", span)
            remoteViews.setCharSequence(R.id.minute, "setFormat24Hour", span)
        } else {
            remoteViews.setCharSequence(R.id.hour, "setFormat12Hour", "HH")
            remoteViews.setCharSequence(R.id.hour, "setFormat24Hour", "HH")
            remoteViews.setCharSequence(R.id.minute, "setFormat12Hour", "mm")
            remoteViews.setCharSequence(R.id.minute, "setFormat24Hour", "mm")
        }
        val next = times.getNextTime()
        remoteViews.setTextViewText(R.id.time, Vakit.getByIndex(next - 1).string)
        val date = LocalDate.now()
        remoteViews.setTextViewText(
            R.id.date, LocaleUtils.formatNumber(
                date.format(
                    DateTimeFormatter.ofPattern("d.MMM")
                )
            )
        )
        val wd = date.format(DateTimeFormatter.ofPattern("EEEE"))
        remoteViews.setTextViewText(R.id.weekDay, wd)
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews
            .setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next)
                    .atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
            val txt = LocaleUtils.formatPeriod(
                LocalDateTime.now(),
                times.getTime(LocalDate.now(), next),
                false
            )
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
        }
        remoteViews.setTextViewTextSize(
            R.id.countdown,
            TypedValue.COMPLEX_UNIT_PX,
            (height * 0.15).toFloat()
        )
        height *= 1.2f.toInt()
        remoteViews.setTextViewTextSize(
            R.id.weekDay,
            TypedValue.COMPLEX_UNIT_PX,
            Math.min(height * 0.15, (height / wd.length.toFloat()).toDouble()).toFloat()
        )
        remoteViews.setTextViewTextSize(
            R.id.hour,
            TypedValue.COMPLEX_UNIT_PX,
            (height * 0.4).toFloat()
        )
        remoteViews.setTextViewTextSize(
            R.id.minute,
            TypedValue.COMPLEX_UNIT_PX,
            (height * 0.15).toFloat()
        )
        remoteViews.setTextViewTextSize(
            R.id.date,
            TypedValue.COMPLEX_UNIT_PX,
            (height * 0.075).toFloat()
        )
        remoteViews.setTextViewTextSize(
            R.id.time,
            TypedValue.COMPLEX_UNIT_PX,
            (height * 0.075).toFloat()
        )
        remoteViews.setViewPadding(
            R.id.minute,
            0,
            (-height * 0.05).toInt(),
            0,
            (-height * 0.03).toInt()
        )
        remoteViews.setViewPadding(
            R.id.hour,
            0,
            (-height * 0.13).toInt(),
            0,
            (-height * 0.10).toInt()
        )
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}