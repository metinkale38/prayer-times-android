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
import android.graphics.drawable.Icon
import android.os.Build
import android.os.SystemClock
import android.text.Html
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Pair
import android.util.TypedValue
import android.widget.RemoteViews
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
    override fun onTimeTick() {
        val textColor = Preferences.ONGOING_TEXT_COLOR.get()
        val bgColor = Preferences.ONGOING_BG_COLOR.get()
        val notMan = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val cal = LocalDate.now()
        val notifications: MutableList<Pair<Int, Notification>> = ArrayList()
        for (t in Times.current) {
            if (!t.ongoing) {
                notMan.cancel(t.id)
                continue
            }
            val icon = Preferences.SHOW_ONGOING_ICON.get()
            val number = Preferences.SHOW_ONGOING_NUMBER.get()
            setCustomKey("showIcon", icon)
            setCustomKey("showNumber", number)
            val views = RemoteViews(context.packageName, R.layout.notification_layout)
            if (Build.MANUFACTURER.lowercase().contains("xiaomi")) {
                views.setViewPadding(R.id.notification, 0, 0, 0, 0)
            }
            if (bgColor != 0) {
                views.setInt(R.id.notification, "setBackgroundColor", bgColor)
            }
            views.setTextViewText(android.R.id.title, t.name)
            if (textColor != 0) views.setTextColor(android.R.id.title, textColor)
            val timeIds =
                intArrayOf(R.id.time0, R.id.time1, R.id.time2, R.id.time3, R.id.time4, R.id.time5)
            val vakitIds =
                intArrayOf(R.id.fajr, R.id.sun, R.id.zuhr, R.id.asr, R.id.maghrib, R.id.ishaa)
            var marker = t.getCurrentTime()
            if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") {
                marker += 1
            }
            for (vakit in Vakit.values()) {
                val time = t.getTime(cal, vakit.ordinal).toLocalTime()
                if (marker == vakit.ordinal) {
                    views.setTextViewText(
                        vakitIds[vakit.ordinal],
                        Html.fromHtml("<strong>" + vakit.string + "</strong>")
                    )
                    if (Preferences.CLOCK_12H.get()) {
                        val span = LocaleUtils.formatTimeForHTML(time) as Spannable
                        span.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            span.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        views.setTextViewText(timeIds[vakit.ordinal], span)
                    } else {
                        views.setTextViewText(
                            timeIds[vakit.ordinal],
                            Html.fromHtml("<strong>" + LocaleUtils.formatTimeForHTML(time) + "</strong>")
                        )
                    }
                } else {
                    views.setTextViewText(vakitIds[vakit.ordinal], vakit.string)
                    views.setTextViewText(
                        timeIds[vakit.ordinal],
                        LocaleUtils.formatTimeForHTML(time)
                    )
                }
                if (textColor != 0) {
                    views.setTextColor(timeIds[vakit.ordinal], textColor)
                    views.setTextColor(vakitIds[vakit.ordinal], textColor)
                }
            }
            val nextTime =
                t.getTime(cal, t.getNextTime()).atZone(ZoneId.systemDefault()).toInstant()
            if (Build.VERSION.SDK_INT >= 24 && Preferences.COUNTDOWN_TYPE.get() == Preferences.COUNTDOWN_TYPE_SHOW_SECONDS) {
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
            if (textColor != 0) {
                views.setTextColor(R.id.countdown, textColor)
            }
            val builder = Notification.Builder(context)
            builder.setContentIntent(getPendingIntent(t))
            if (!icon) {
                builder.setSmallIcon(R.drawable.ic_placeholder)
            } else if (number) {
                builder.setSmallIcon(Icon.createWithBitmap(getIconFromMinutes(t)))
            } else {
                builder.setSmallIcon(R.drawable.ic_abicon)
            }
            builder.setOngoing(true)
            builder.setWhen(if (icon) System.currentTimeMillis() else 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomContentView(views)
            } else {
                builder.setContent(views)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(NotificationUtils.getOngoingChannel(context))
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

    private fun getIconFromMinutes(t: Times): Bitmap {
        val left = Duration.between(LocalDateTime.now(), t.getTime(LocalDate.now(), t.getNextTime())).toMinutes()
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
        if (key == Preferences.SHOW_ONGOING_ICON.key || key == Preferences.SHOW_ONGOING_NUMBER.key || key == Preferences.ONGOING_TEXT_COLOR.key || key == Preferences.ONGOING_BG_COLOR.key) {
            onTimeTick()
        }
    }

    companion object {
        private const val FOREGROUND_NEEDY_ONGOING = "ongoing"
    }
}