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
import android.view.View
import android.widget.RemoteViews
import com.metinkale.prayer.R
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.utils.LocaleUtils
import dev.metinkale.openprayertimes.hijri.HijriDate
import dev.metinkale.openprayertimes.hijri.HijriMonth
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Ramadan widget: counts down to iftar during the day and to imsak after iftar.
 * Outside Ramadan it shows the number of days until the next Ramadan.
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

            remoteViews.setTextViewText(R.id.city, times.name)
            remoteViews.setTextColor(R.id.city, theme.textcolor)
            remoteViews.setTextViewTextSize(
                R.id.city,
                TypedValue.COMPLEX_UNIT_PX,
                minOf(scale * 1.2f, 1.5f * w / times.name.length.coerceAtLeast(1))
            )
            remoteViews.setViewPadding(
                R.id.city,
                (scale / 2).toInt(),
                0,
                (scale / 2).toInt(),
                (scale / 4).toInt()
            )

            val now = LocalDateTime.now()
            val today = LocalDate.now()
            val previousIftar = times.getTime(today, Vakit.MAGHRIB.ordinal - Vakit.LENGTH)
            val fajr = times.getTime(today, Vakit.FAJR.ordinal)
            val iftar = times.getTime(today, Vakit.MAGHRIB.ordinal)
            val nextFajr = times.getTime(today, Vakit.LENGTH + Vakit.FAJR.ordinal)
            val countdown = ramadanCountdownTarget(
                now = now,
                previousIftar = previousIftar,
                fajr = fajr,
                iftar = iftar,
                nextFajr = nextFajr
            )
            val hijriTarget =
                HijriDate.fromLocalDate(countdown.target.toLocalDate().toKotlinLocalDate())

            remoteViews.setTextColor(R.id.event, theme.textcolor)
            remoteViews.setTextViewTextSize(R.id.event, TypedValue.COMPLEX_UNIT_PX, scale * 1.4f)
            remoteViews.setTextColor(R.id.time, theme.textcolor)
            remoteViews.setTextColor(R.id.ramadanDay, theme.textcolor)
            remoteViews.setTextViewTextSize(
                R.id.ramadanDay,
                TypedValue.COMPLEX_UNIT_PX,
                scale * 1.1f
            )
            remoteViews.setViewPadding(
                R.id.ramadanDay,
                (scale / 2).toInt(),
                (scale / 4).toInt(),
                (scale / 2).toInt(),
                0
            )

            if (hijriTarget.month == HijriMonth.RAMADAN) {
                remoteViews.setTextViewText(
                    R.id.event,
                    context.getString(if (countdown.isIftar) R.string.iftar else R.string.imsak)
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
                    -(scale / 4).toInt(),
                    0,
                    -(scale / 3).toInt()
                )
                remoteViews.setViewVisibility(R.id.countdown, View.VISIBLE)

                remoteViews.setTextViewText(
                    R.id.time,
                    LocaleUtils.formatTimeForHTML(countdown.target.toLocalTime())
                )
                remoteViews.setTextViewTextSize(
                    R.id.time,
                    TypedValue.COMPLEX_UNIT_PX,
                    scale * 1.6f
                )
                remoteViews.setViewPadding(
                    R.id.time,
                    0,
                    (scale / 8).toInt(),
                    0,
                    0
                )

                remoteViews.setTextViewText(
                    R.id.ramadanDay,
                    context.getString(
                        R.string.ramadanDay,
                        LocaleUtils.formatNumber(hijriTarget.day.toString())
                    )
                )
                remoteViews.setViewVisibility(R.id.ramadanDay, View.VISIBLE)
            } else {
                val days = daysUntilNextRamadan(today)
                remoteViews.setTextViewText(R.id.event, context.getString(R.string.hmonth9))
                remoteViews.setTextViewText(
                    R.id.time,
                    context.resources.getQuantityString(
                        R.plurals.ramadanStartsInDays,
                        days,
                        days
                    )
                )
                remoteViews.setTextViewTextSize(
                    R.id.time,
                    TypedValue.COMPLEX_UNIT_PX,
                    scale * 1.4f
                )
                remoteViews.setViewPadding(
                    R.id.time,
                    0,
                    (scale / 2).toInt(),
                    0,
                    0
                )
                remoteViews.setViewVisibility(R.id.countdown, View.GONE)
                remoteViews.setViewVisibility(R.id.ramadanDay, View.GONE)
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }

        private fun daysUntilNextRamadan(today: LocalDate): Int {
            val hijri = HijriDate.now()
            val year = if (hijri.month.value < HijriMonth.RAMADAN.value) {
                hijri.year
            } else {
                hijri.year + 1
            }
            val start = HijriDate.of(year, HijriMonth.RAMADAN, 1).toLocalDate().toJavaLocalDate()
            return ChronoUnit.DAYS.between(today, start).coerceAtLeast(0).toInt()
        }
    }
}
