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
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.Module
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getCurrentTime
import com.metinkale.prayer.times.times.getNextTime
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.times.times.isKerahat
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.UUID
import com.metinkale.prayer.utils.Utils
import com.metinkale.prayer.widgets.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class WidgetProviderClock : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderClock::class.java)
        val manager = AppWidgetManager.getInstance(context)
        onUpdate(context, manager, manager.getAppWidgetIds(thisWidget))
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDisabled(context: Context) {}
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            LocaleUtils.init(context)
            val times: Times? = WidgetUtils.getTimes(widgetId)
            if (times == null) {
                WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
                return
            }
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 500f / 200f)
            val width = size.width
            val height = size.height
            if (width <= 0 || height <= 0) return
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_4x2_clock)
            val pendingIntent = TimesFragment.getPendingIntent(times)
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
            remoteViews.setInt(
                if (!rtl) R.id.progressBg else R.id.progress,
                "setBackgroundColor",
                -0x1
            )
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

        private fun getPassedPart(times: Times): Float {
            val current = times.getCurrentTime()
            val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val prev =
                times.getTime(LocalDate.now(), current).atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            val next =
                times.getTime(LocalDate.now(), current + 1).atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            return (now - prev) / (next - prev).toFloat()
        }

    }
}