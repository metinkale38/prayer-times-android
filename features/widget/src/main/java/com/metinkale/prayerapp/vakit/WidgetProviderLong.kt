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
import android.text.Html
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getCurrentTime
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.Utils
import com.metinkale.prayer.widgets.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class WidgetProviderLong : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderLong::class.java)
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
            val theme: Theme = WidgetUtils.getTheme(widgetId)
            val times: Times? = WidgetUtils.getTimes(widgetId)
            if (times == null) {
                WidgetUtils.showNoCityWidget(context, appWidgetManager, widgetId)
                return
            }
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 300f / 60f)
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
            remoteViews.setOnClickPendingIntent(
                R.id.widget_layout, TimesFragment.getPendingIntent(times)
            )
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
                        ids[i], "setBackgroundColor", theme.hovercolor
                    ) else remoteViews.setInt(
                        ids[i], "setBackgroundColor", 0
                    )
                }
                remoteViews.setTextViewText(ids[i], Html.fromHtml("$time<br/><small>$name</small>"))
            }
            remoteViews.setTextViewTextSize(R.id.city, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f)
            remoteViews.setTextViewTextSize(
                R.id.countdown, TypedValue.COMPLEX_UNIT_PX, scale * 1.25f
            )
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
            if (theme == Theme.Trans) {
                remoteViews.setViewPadding(R.id.divider, scale.toInt() / 2, 0, scale.toInt() / 2, 0)
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }

    }
}