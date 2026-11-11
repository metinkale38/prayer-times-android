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

import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.IBinder
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnStartListener
import com.metinkale.prayer.receiver.OnTimeTickListener
import com.metinkale.prayer.times.utils.NotificationUtils

/**
 * Created by metin on 24.03.2017.
 */
class WidgetService : Service(), OnTimeTickListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        AppEventManager.register(this)
    }

    override fun onDestroy() {
        AppEventManager.unregister(this)
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notMan =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notMan.cancel(NotificationUtils.getDummyNotificationId())
        super.onDestroy()
    }

    override fun onTimeTick() {
        onStartCommand(null, 0, 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ServiceCompat.startForeground(
                this,
                NotificationUtils.getDummyNotificationId(),
                NotificationUtils.createDummyNotification(this),
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } catch (e: Exception) {
            if (!e.javaClass.simpleName.equals("ForegroundServiceStartNotAllowedException")) throw e;
        }
        if (updateJob?.isActive != true) {
            updateJob = scope.launch {
                val hasWidgets = try {
                    updateWidgets()
                } catch (e: Exception) {
                    recordException(e)
                    true
                }
                if (!hasWidgets) {
                    stopSelfResult(startId)
                }
            }
        }
        return START_STICKY
    }

    private fun updateWidgets(): Boolean {
        val ctx = this
        val manager = AppWidgetManager.getInstance(App.get())
        var hasWidgets = false

        fun update(provider: Class<*>, updater: (Context, AppWidgetManager, Int) -> Unit) {
            val component = ComponentName(ctx, provider)
            for (id in manager.getAppWidgetIds(component)) {
                updater(ctx, manager, id)
                hasWidgets = true
            }
        }

        update(WidgetProvider::class.java) { c, m, i -> WidgetProvider.updateAppWidget(c, m, i) }
        update(WidgetProviderSmall::class.java) { c, m, i -> WidgetProviderSmall.updateAppWidget(c, m, i) }
        update(WidgetProviderLong::class.java) { c, m, i -> WidgetProviderLong.updateAppWidget(c, m, i) }
        update(WidgetProviderSilenter::class.java) { c, m, i -> WidgetProviderSilenter.updateAppWidget(c, m, i) }
        update(WidgetProviderClock::class.java) { c, m, i -> WidgetProviderClock.updateAppWidget(c, m, i) }
        update(WidgetProviderClock2::class.java) { c, m, i -> WidgetProviderClock2.updateAppWidget(c, m, i) }
        update(WidgetProviderProgress::class.java) { c, m, i -> WidgetProviderProgress.updateAppWidget(c, m, i) }

        return hasWidgets
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
        return null
    }
}
