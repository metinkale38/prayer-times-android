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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.receiver.InternalBroadcastReceiver.OnTimeTickListener
import com.metinkale.prayer.service.ForegroundService
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.widgets.R
import com.metinkale.prayerapp.vakit.WidgetConfigure
import com.metinkale.prayerapp.vakit.WidgetProvider
import com.metinkale.prayerapp.vakit.WidgetProviderClock
import com.metinkale.prayerapp.vakit.WidgetProviderClock2
import com.metinkale.prayerapp.vakit.WidgetProviderLong
import com.metinkale.prayerapp.vakit.WidgetProviderSilenter
import com.metinkale.prayerapp.vakit.WidgetProviderSmall

/**
 * Created by metin on 24.03.2017.
 */
class WidgetUtils : InternalBroadcastReceiver(), OnTimeTickListener {
    override fun onTimeTick() {
        updateWidgets(context)
    }

    class Size(w: Int, h: Int, aspectRatio: Float) {
        val width: Int
        val height: Int

        init {
            var w = w
            var h = h
            val r = App.get().resources
            w = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                w.toFloat(),
                r.displayMetrics
            ).toInt()
            h = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                h.toFloat(),
                r.displayMetrics
            ).toInt()
            val w1 = (h * aspectRatio).toInt()
            val h1 = (w / aspectRatio).toInt()
            width = Math.min(w, w1)
            height = Math.min(h, h1)
        }
    }

    companion object {
        const val WIDGETS_FOREGROUND_NEEDY = "widgets"
        fun getTheme(widgetId: Int): Theme {
            val widgets = App.get().getSharedPreferences("widgets", 0)
            val t = widgets.getInt(widgetId.toString() + "_theme", 0)
            val theme: Theme = when (t) {
                1 -> Theme.Dark
                2 -> Theme.LightTrans
                3 -> Theme.Trans
                else -> Theme.Light
            }
            return theme
        }

        fun getTimes(widgetId: Int): Times? {
            val widgets = App.get().getSharedPreferences("widgets", 0)

            // TODO remove after some versions
            (widgets.all[widgetId.toString()] as? Long)?.let {
                widgets.edit().putInt(widgetId.toString(), it.toInt()).commit()
            }

            val id = widgets.getInt(widgetId.toString() + "", 0)
            var t: Times? = null
            if (id != 0) {
                t = Times.getTimesById(id).value
            }
            if (t == null) widgets.edit().remove(widgetId.toString() + "").apply()
            return t
        }

        fun showNoCityWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_city_removed)
            val i = Intent(context, WidgetConfigure::class.java)
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            i.putExtra(WidgetConfigure.Companion.ONLYCITY, true)
            remoteViews.setOnClickPendingIntent(
                R.id.image,
                PendingIntent.getActivity(context, widgetId, i, PendingIntent.FLAG_CANCEL_CURRENT)
            )
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }

        fun getSize(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            aspectRatio: Float
        ): Size {
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            val isPort = context.resources.getBoolean(R.bool.isPort)
            val w =
                options.getInt(if (isPort) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val h =
                options.getInt(if (isPort) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            return Size(w, h, aspectRatio)
        }

        fun updateWidgets(c: Context) {
            try {
                var hasWidgets = false
                val manager = AppWidgetManager.getInstance(App.get())
                var thisWidget = ComponentName(c, WidgetProvider::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProvider.Companion.updateAppWidget(c, manager, i)
                    hasWidgets = true
                }
                thisWidget = ComponentName(c, WidgetProviderSmall::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProviderSmall.Companion.updateAppWidget(c, manager, i)
                    hasWidgets = true
                }
                thisWidget = ComponentName(c, WidgetProviderLong::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProviderLong.Companion.updateAppWidget(c, manager, i)
                    hasWidgets = true
                }
                thisWidget = ComponentName(c, WidgetProviderSilenter::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProviderSilenter.Companion.updateAppWidget(c, manager, i)
                }
                thisWidget = ComponentName(c, WidgetProviderClock::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProviderClock.Companion.updateAppWidget(c, manager, i)
                    hasWidgets = true
                }
                thisWidget = ComponentName(c, WidgetProviderClock2::class.java)
                for (i in manager.getAppWidgetIds(thisWidget)) {
                    WidgetProviderClock2.Companion.updateAppWidget(c, manager, i)
                    hasWidgets = true
                }
                if (hasWidgets) {
                    ForegroundService.addNeedy(c, WIDGETS_FOREGROUND_NEEDY)
                } else {
                    ForegroundService.removeNeedy(c, WIDGETS_FOREGROUND_NEEDY)
                }
            } catch (e: Exception) {
                recordException(e)
            }
        }
    }
}