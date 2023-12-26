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
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getNextTime
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.widgets.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class WidgetProviderSmall : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderSmall::class.java)
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
            val theme: Theme = WidgetUtils.getTheme(widgetId)
            val times: Times? = WidgetUtils.getTimes(widgetId)
            if (times == null) {
                WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
                return
            }
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
            val s = size.width
            if (s <= 0) return
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_1x1)
            remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", theme.background)
            remoteViews.setViewPadding(R.id.padder, s / 2, s / 2, s / 2, s / 2)
            val next = times.getNextTime()
            val name = times.name
            remoteViews.setOnClickPendingIntent(
                R.id.widget_layout,
                TimesFragment.getPendingIntent(times)
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

    }
}