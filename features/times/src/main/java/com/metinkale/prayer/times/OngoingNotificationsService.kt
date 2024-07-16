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
package com.metinkale.prayer.times

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.SystemClock
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.text.toSpannable
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.setCustomKey
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.receiver.OnPrefsChangedListener
import com.metinkale.prayer.receiver.OnStartListener
import com.metinkale.prayer.receiver.OnTimeTickListener
import com.metinkale.prayer.times.fragments.TimesFragment.Companion.getPendingIntent
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getCurrentTime
import com.metinkale.prayer.times.times.getNextTime
import com.metinkale.prayer.times.times.getTime
import com.metinkale.prayer.times.utils.NotificationUtils
import com.metinkale.prayer.utils.LocaleUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class OngoingNotificationsService : LifecycleService(), OnTimeTickListener, OnPrefsChangedListener {
    private val textColor get() = Preferences.ONGOING_TEXT_COLOR.takeIf { it != 0 }
    private val bgColor get() = Preferences.ONGOING_BG_COLOR.takeIf { it != 0 }
    private val icon get() = Preferences.SHOW_ONGOING_ICON
    private val number get() = Preferences.SHOW_ONGOING_NUMBER

    private val states =
        Times.map { it.map { it.ongoing } }.distinctUntilChanged().drop(1).asLiveData()

    private val channelId = NotificationUtils.getOngoingChannel(App.get())


    private val observer: Observer<List<Boolean>> = Observer<List<Boolean>> {
        onStartCommand(null, 0, 0)
    }

    override fun onCreate() {
        super.onCreate()
        AppEventManager.register(this)
        states.observeForever(observer)
    }

    override fun onDestroy() {
        AppEventManager.unregister(this)
        states.removeObserver(observer)
        super.onDestroy()
    }

    override fun onTimeTick() {
        onStartCommand(null, 0, 0)
    }

    override fun onPrefsChanged(key: String) {
        if (key == "ongoingIcon" ||
            key == "ongoingNumber" ||
            key == "ongoingTextColor"
            || key == "ongoingBGColor"
        ) {
            onStartCommand(null, 0, 0)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val notMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val notifications: List<Pair<Notification, Int>> = Times.current.mapNotNull { t ->
            val notId = t.buildNotificationId("ongoing")
            if (!t.ongoing) {
                if (notMan.activeNotifications.any { it.id == notId })
                    notMan.cancel(notId)
                null
            } else {
                setCustomKey("showIcon", icon)
                setCustomKey("showNumber", number)


                val builder = NotificationCompat.Builder(this, channelId)
                builder.setContentIntent(getPendingIntent(t))
                if (!icon) {
                    builder.setSmallIcon(R.drawable.ic_placeholder)
                } else if (number) {
                    builder.setSmallIcon(IconCompat.createWithBitmap(getIconFromMinutes(t)))
                } else {
                    builder.setSmallIcon(R.drawable.ic_abicon)
                }
                builder.setOngoing(true)
                builder.setWhen(if (icon) System.currentTimeMillis() else 0)
                // builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                builder.setCustomContentView(buildLargeRemoteView(t))
                builder.setCustomBigContentView(buildLargeRemoteView(t))
                builder.setShowWhen(false)
                val noti = builder.build()
                noti to notId
            }
        }

        // remove orphaned notifications
        notMan.activeNotifications.filter { it.notification.channelId == channelId }
            .filter { notifications.none { not -> not.second == it.id } }.forEach {
                notMan.cancel(it.id)
            }

        if (notifications.isNotEmpty()) {
            hasOngoingNotifications = true
            notifications.forEachIndexed { index, (noti, id) ->
                if (index == 0) {
                    runCatching {
                        ServiceCompat.startForeground(
                            this,
                            id, noti,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    }.onFailure { notMan.notify(id, noti) }
                }
                notMan.notify(id, noti)
            }
        } else {
            hasOngoingNotifications = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }


    fun RemoteViews.initCommon(t: Times) = let { views ->

        val today = LocalDate.now()

        bgColor?.let { views.setInt(R.id.notification, "setBackgroundColor", it) }

        // Title
        views.setTextViewText(android.R.id.title, t.name)
        textColor?.let { views.setTextColor(android.R.id.title, it) }

        // Countdown
        val nextTime = t.getTime(today, t.getNextTime()).atZone(ZoneId.systemDefault()).toInstant()
        if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) {
            views.setChronometer(
                R.id.countdown,
                nextTime.toEpochMilli() - (System.currentTimeMillis() - SystemClock.elapsedRealtime()),
                null,
                true
            )
        } else {
            val txt = LocaleUtils.formatPeriod(Instant.now(), nextTime, false)
            views.setString(R.id.countdown, "setFormat", txt)
            views.setChronometer(R.id.countdown, 0, txt, false)
        }
        textColor?.let { views.setTextColor(R.id.countdown, it) }
    }

    private fun buildLargeRemoteView(t: Times): RemoteViews {
        val today = LocalDate.now()


        val nameIds = listOf(R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa)
        val timeIds = listOf(R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5)


        val views = RemoteViews(packageName, R.layout.notification_layout_large)
        views.initCommon(t)

        // Times
        val marker = t.getCurrentTime()
            .let { if (Preferences.VAKIT_INDICATOR_TYPE == "next") it + 1 else it }
        for (vakit in Vakit.values()) {
            val time = t.getTime(today, vakit.ordinal).toLocalTime()

            fun Spannable.strongIfCurrent() = apply {
                if (marker == vakit.ordinal) {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            val timeString = LocaleUtils.formatTimeForHTML(time).toSpannable().strongIfCurrent()
            views.setTextViewText(timeIds[vakit.ordinal], timeString)

            val nameString = vakit.string.toSpannable().strongIfCurrent()
            views.setTextViewText(nameIds[vakit.ordinal], nameString)

            textColor?.let {
                views.setTextColor(timeIds[vakit.ordinal], it)
                views.setTextColor(nameIds[vakit.ordinal], it)
            }
        }




        return views
    }


    private fun getIconFromMinutes(t: Times): Bitmap {
        val left =
            Duration.between(LocalDateTime.now(), t.getTime(LocalDate.now(), t.getNextTime()))
                .toMinutes()
        val r = resources
        val size =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, r.displayMetrics).toInt()
        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = -0x1
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = size.toFloat()
        paint.textSize =
            size * size / paint.measureText((if (left < 10) left * 10 else left).toString() + "")
        val yPos = c.height / 2f - (paint.descent() + paint.ascent()) / 2
        c.drawText(left.toString() + "", size / 2f, yPos, paint)
        return b
    }

    companion object : OnStartListener {
        private var hasOngoingNotifications = false
        override fun onStart() {
            start()
            Times.map { it.any { it.ongoing } }.distinctUntilChanged().filter { it }.asLiveData()
                .observeForever { start() }
        }

        fun start() {
            if (Times.current.any { it.ongoing }) {
                val intent = Intent(
                    App.get(),
                    OngoingNotificationsService::class.java
                )
                App.get().startForegroundService(intent)
            } else {
                hasOngoingNotifications = false
                val notMan =
                    App.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                Times.current.map { it.buildNotificationId("ongoing") }
                    .forEach { notMan.cancel(it) }
            }
        }


    }


}