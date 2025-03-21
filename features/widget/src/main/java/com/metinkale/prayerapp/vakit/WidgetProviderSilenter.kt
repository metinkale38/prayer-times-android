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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.times.SilenterPrompt
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.widgets.R

class WidgetProviderSilenter : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        val thisWidget = ComponentName(context, WidgetProviderSilenter::class.java)
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
            val size = WidgetUtils.getSize(context, appWidgetManager, widgetId, 1f)
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
    }
}