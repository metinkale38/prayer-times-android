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

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.widget.RemoteViews
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnStartListener
import com.metinkale.prayer.receiver.OnTimeTickListener
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.utils.NotificationUtils
import com.metinkale.prayer.widgets.R

/**
 * Created by metin on 24.03.2017.
 */
class WidgetService : Service(), OnTimeTickListener {

    override fun onCreate() {
        super.onCreate()
        AppEventManager.register(this)
    }

    override fun onDestroy() {
        AppEventManager.unregister(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notMan =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notMan.cancel(NotificationUtils.getDummyNotificationId())
        super.onDestroy()
    }

    override fun onTimeTick() {
        onStartCommand(null, 0, 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            startForeground(
                NotificationUtils.getDummyNotificationId(),
                NotificationUtils.createDummyNotification(this)
            )
        }
        var hasWidgets = false
        try {
            val ctx = this
            val manager = AppWidgetManager.getInstance(App.get())
            var thisWidget = ComponentName(ctx, WidgetProvider::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProvider.updateAppWidget(ctx, manager, i)
                hasWidgets = true
            }
            thisWidget = ComponentName(ctx, WidgetProviderSmall::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderSmall.updateAppWidget(ctx, manager, i)
                hasWidgets = true
            }
            thisWidget = ComponentName(ctx, WidgetProviderLong::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderLong.updateAppWidget(ctx, manager, i)
                hasWidgets = true
            }
            thisWidget = ComponentName(ctx, WidgetProviderSilenter::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderSilenter.updateAppWidget(ctx, manager, i)
            }
            thisWidget = ComponentName(ctx, WidgetProviderClock::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderClock.updateAppWidget(ctx, manager, i)
                hasWidgets = true
            }
            thisWidget = ComponentName(ctx, WidgetProviderClock2::class.java)
            for (i in manager.getAppWidgetIds(thisWidget)) {
                WidgetProviderClock2.updateAppWidget(ctx, manager, i)
                hasWidgets = true
            }

            if (!hasWidgets) {
                stopSelf()
            }
        } catch (e: Exception) {
            recordException(e)
        }
        return START_STICKY
    }


    companion object : OnStartListener {

        fun updateWidgets(c: Context) {
            c.startForegroundService(Intent(c, WidgetService::class.java))
        }

        override fun onStart() {
            updateWidgets(App.get())
        }

    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}
