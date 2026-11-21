/*
 * Copyright (c) 2013-2026 Metin Kale
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
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.R
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.utils.LocaleUtils
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Ramadan widget: shows the imsak and maghrib times for the current/next fast
 * and counts down to the next event.
 */
class WidgetProviderRamadan : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderRamadan::class.java)
        val manager = AppWidgetManager.getInstance(context)
        onUpdate(context, manager, manager.getAppWidgetIds(thisWidget))
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgetsAsync(context, appWidgetManager, appWidgetIds) { ctx, manager, widgetId ->
            updateAppWidget(ctx, manager, widgetId)
        }
    }

    override fun onDisabled(context: Context) {}
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidgetsAsync(context, appWidgetManager, intArrayOf(appWidgetId)) { ctx, manager, id ->
            updateAppWidget(ctx, manager, id)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            LocaleUtils.init(context)
            val theme: Theme = WidgetUtils.getTheme(widgetId)
            val times: Times? = WidgetUtils.getTimes(widgetId)
            if (times == null) {
                WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
                return
            }
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 130f / 160f)
            val w = size.width
            val h = size.height
            if (w <= 0 || h <= 0) return

            val scale = w / 10.5f
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_2x2_ramadan)
            remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
            remoteViews.setViewPadding(R.id.padder, w / 2, h / 2, w / 2, h / 2)
            remoteViews.setOnClickPendingIntent(
                R.id.widget_layout,
                TimesFragment.getPendingIntent(times)
            )

            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val todayFajr = times.getTime(today, Vakit.FAJR.ordinal)
            val todayIftar = times.getTime(today, Vakit.MAGHRIB.ordinal)
            val previousIftar = times.getTime(today, Vakit.MAGHRIB.ordinal - Vakit.LENGTH)
            val nextFajr = times.getTime(today, Vakit.LENGTH + Vakit.FAJR.ordinal)
            val countdown = ramadanCountdownTarget(
                now = now,
                previousIftar = previousIftar,
                fajr = todayFajr,
                iftar = todayIftar,
                nextFajr = nextFajr
            )

            // After iftar the next fast already belongs to the following day.
            val displayDate = if (now.isBefore(todayIftar)) today else today.plusDays(1)
            val imsak = times.getTime(displayDate, Vakit.FAJR.ordinal)
            val maghrib = times.getTime(displayDate, Vakit.MAGHRIB.ordinal)

            remoteViews.setTextColor(R.id.city, theme.textcolor)
            remoteViews.setTextViewTextSize(
                R.id.city,
                TypedValue.COMPLEX_UNIT_PX,
                minOf(scale * 1.2f, 1.5f * w / times.name.length.coerceAtLeast(1))
            )
            remoteViews.setTextViewText(R.id.city, times.name)
            remoteViews.setViewPadding(
                R.id.city,
                0,
                0,
                0,
                (scale / 4).toInt()
            )
            remoteViews.setViewPadding(
                R.id.content,
                (scale / 1.5f).toInt(),
                0,
                (scale / 1.5f).toInt(),
                0
            )

            val rowTextIds = intArrayOf(R.id.fajrText, R.id.fajr, R.id.maghribText, R.id.maghrib)
            for (id in rowTextIds) {
                remoteViews.setTextColor(id, theme.textcolor)
                remoteViews.setTextViewTextSize(
                    id,
                    TypedValue.COMPLEX_UNIT_PX,
                    scale * 1.35f
                )
            }
            remoteViews.setTextViewText(R.id.fajrText, context.getString(R.string.imsak))
            remoteViews.setTextViewText(R.id.maghribText, context.getString(R.string.maghrib))
            remoteViews.setTextViewText(
                R.id.fajr,
                LocaleUtils.formatTimeForHTML(imsak.toLocalTime())
            )
            remoteViews.setTextViewText(
                R.id.maghrib,
                LocaleUtils.formatTimeForHTML(maghrib.toLocalTime())
            )
            remoteViews.setViewPadding(
                R.id.imsakRow,
                0,
                (scale / 8).toInt(),
                0,
                (scale / 8).toInt()
            )
            remoteViews.setViewPadding(
                R.id.maghribRow,
                0,
                (scale / 8).toInt(),
                0,
                (scale / 8).toInt()
            )

            remoteViews.setInt(
                R.id.divider,
                "setBackgroundColor",
                (theme.textcolor and 0x00FFFFFF) or 0x33000000
            )

            remoteViews.setTextViewText(
                R.id.countdownLabel,
                context.getString(if (countdown.isIftar) R.string.iftar else R.string.imsak)
            )
            remoteViews.setTextColor(R.id.countdownLabel, theme.textcolor)
            remoteViews.setTextViewTextSize(
                R.id.countdownLabel,
                TypedValue.COMPLEX_UNIT_PX,
                scale * 1.2f
            )
            remoteViews.setViewPadding(
                R.id.countdownLabel,
                0,
                (scale / 6).toInt(),
                0,
                0
            )

            val txt = LocaleUtils.formatPeriod(now, countdown.target, false)
            remoteViews.setString(R.id.countdown, "setFormat", txt)
            remoteViews.setChronometer(R.id.countdown, 0, txt, false)
            remoteViews.setTextColor(R.id.countdown, theme.textcolor)
            remoteViews.setTextViewTextSize(
                R.id.countdown,
                TypedValue.COMPLEX_UNIT_PX,
                minOf(scale * 2.6f, h * 0.24f)
            )
            remoteViews.setViewPadding(
                R.id.countdown,
                0,
                -(scale / 6).toInt(),
                0,
                -(scale / 6).toInt()
            )

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }
}
