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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Pair
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.text.toSpannable
import com.metinkale.prayer.CrashReporter.setCustomKey
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.receiver.InternalBroadcastReceiver.OnPrefsChangedListener
import com.metinkale.prayer.receiver.InternalBroadcastReceiver.OnTimeTickListener
import com.metinkale.prayer.service.ForegroundService
import com.metinkale.prayer.times.fragments.TimesFragment.Companion.getPendingIntent
import com.metinkale.prayer.times.times.*
import com.metinkale.prayer.times.utils.NotificationUtils
import com.metinkale.prayer.utils.LocaleUtils
import java.time.*

class OngoingNotificationsReceiver : InternalBroadcastReceiver(), OnTimeTickListener,
    OnPrefsChangedListener {
    private val textColor get() = Preferences.ONGOING_TEXT_COLOR.takeIf { it != 0 }
    private val bgColor get() = Preferences.ONGOING_BG_COLOR.takeIf { it != 0 }
    private val icon get() = Preferences.SHOW_ONGOING_ICON
    private val number get() = Preferences.SHOW_ONGOING_NUMBER

    override fun onTimeTick() {

        val notMan = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifications: MutableList<Pair<Int, Notification>> = ArrayList()
        for (t in Times.current) {
            if (!t.ongoing) {
                notMan.cancel(t.id)
                continue
            }
            setCustomKey("showIcon", icon)
            setCustomKey("showNumber", number)


            val channelId = NotificationUtils.getOngoingChannel(context)
            val builder = NotificationCompat.Builder(context, channelId)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                builder.setCustomContentView(buildSmallRemoteView(t))
                builder.setCustomBigContentView(buildLargeRemoteView(t))
                builder.setShowWhen(false)
            } else {
                builder.setContent(buildLargeRemoteView(t))
            }
            val noti = builder.build()
            noti.priority = Notification.PRIORITY_LOW
            notifications.add(Pair(t.id, noti))
        }
        if (notifications.isNotEmpty()) {
            for (i in notifications.indices) {
                val pair = notifications[i]
                if (i == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ForegroundService.addNeedy(
                        context,
                        FOREGROUND_NEEDY_ONGOING,
                        pair.second,
                        pair.first
                    )
                } else {
                    notMan.notify(pair.first, pair.second)
                }
            }
        } else {
            ForegroundService.removeNeedy(context, FOREGROUND_NEEDY_ONGOING)
        }
    }


    fun RemoteViews.initCommon(t: Times) = let { views ->

        val today = LocalDate.now()

        bgColor?.let { views.setInt(R.id.notification, "setBackgroundColor", it) }

        // Title
        views.setTextViewText(android.R.id.title, t.name)
        textColor?.let { views.setTextColor(android.R.id.title, it) }

        // Countdown
        val nextTime = t.getTime(today, t.getNextTime()).atZone(ZoneId.systemDefault()).toInstant()
        if (Build.VERSION.SDK_INT >= 24 && Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) {
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


        val views = RemoteViews(context.packageName, R.layout.notification_layout_large)
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


    private fun buildSmallRemoteView(t: Times): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.notification_layout_small)
        views.initCommon(t)

        // Times
        views.setViewVisibility(R.id.times, View.GONE)
        views.setTextViewText(
            R.id.countdownprefix,
            Vakit.getByIndex(t.getCurrentTime()).string + " | "
        )
        return views
    }

    private fun getIconFromMinutes(t: Times): Bitmap {
        val left =
            Duration.between(LocalDateTime.now(), t.getTime(LocalDate.now(), t.getNextTime()))
                .toMinutes()
        val r = context.resources
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

    override fun onPrefsChanged(key: String) {
        if (key == "ongoingIcon" ||
            key == "ongoingNumber" ||
            key == "ongoingTextColor"
            || key == "ongoingBGColor"
        ) {
            onTimeTick()
        }
    }

    companion object {
        private const val FOREGROUND_NEEDY_ONGOING = "ongoing"
    }
}