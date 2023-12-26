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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.icu.util.Calendar
import android.os.Bundle
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getCurrentTime
import com.metinkale.prayer.times.times.getNextTime
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.times.times.isKerahat
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.widgets.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WidgetProviderClock2 : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderClock2::class.java)
        val manager = AppWidgetManager.getInstance(context)
        onUpdate(context, manager, manager.getAppWidgetIds(thisWidget))
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDisabled(context: Context) {}
    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
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
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
            val width = size.width
            var height = size.height
            if (width <= 0 || height <= 0) return
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_2x2_clock)
            remoteViews.setOnClickPendingIntent(
                R.id.widget_layout, TimesFragment.getPendingIntent(times)
            )
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
                    width / 100f, width / 100f, width - width / 100f, height - width / 100f
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
            if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) remoteViews.setChronometer(
                R.id.countdown,
                times.getTime(LocalDate.now(), next).atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            ) else {
                val txt = LocaleUtils.formatPeriod(
                    LocalDateTime.now(), times.getTime(LocalDate.now(), next), false
                )
                remoteViews.setString(R.id.countdown, "setFormat", txt)
                remoteViews.setChronometer(R.id.countdown, 0, txt, false)
            }
            remoteViews.setTextViewTextSize(
                R.id.countdown, TypedValue.COMPLEX_UNIT_PX, (height * 0.15).toFloat()
            )
            height *= 1.2f.toInt()
            remoteViews.setTextViewTextSize(
                R.id.weekDay,
                TypedValue.COMPLEX_UNIT_PX,
                Math.min(height * 0.15, (height / wd.length.toFloat()).toDouble()).toFloat()
            )
            remoteViews.setTextViewTextSize(
                R.id.hour, TypedValue.COMPLEX_UNIT_PX, (height * 0.4).toFloat()
            )
            remoteViews.setTextViewTextSize(
                R.id.minute, TypedValue.COMPLEX_UNIT_PX, (height * 0.15).toFloat()
            )
            remoteViews.setTextViewTextSize(
                R.id.date, TypedValue.COMPLEX_UNIT_PX, (height * 0.075).toFloat()
            )
            remoteViews.setTextViewTextSize(
                R.id.time, TypedValue.COMPLEX_UNIT_PX, (height * 0.075).toFloat()
            )
            remoteViews.setViewPadding(
                R.id.minute, 0, (-height * 0.05).toInt(), 0, (-height * 0.03).toInt()
            )
            remoteViews.setViewPadding(
                R.id.hour, 0, (-height * 0.13).toInt(), 0, (-height * 0.10).toInt()
            )
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }

        private fun getPassedPart(times: Times): Float {
            val current = times.getCurrentTime()
            val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val prev =
                times.getTime(LocalDate.now(), current).atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            val next = times.getTime(LocalDate.now(), current + 1).atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            return (now - prev) / (next - prev).toFloat()
        }

    }
}