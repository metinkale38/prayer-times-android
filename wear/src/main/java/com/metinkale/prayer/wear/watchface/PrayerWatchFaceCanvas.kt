package com.metinkale.prayer.wear.watchface

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal class PrayerWatchFaceCanvas(private val resources: Resources) {
    private val dateFormat = DateTimeFormatter.ofPattern("EEE, d MMMM yyyy", Locale.getDefault())
    private val clock24Format = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val clock12Format = DateTimeFormatter.ofPattern("h:mm", Locale.US)

    fun draw(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        model: PrayerTileModel?,
        dial: PrayerDial?,
        ambient: Boolean,
        is24Hour: Boolean
    ) {
        canvas.drawColor(WatchFaceTheme.Background)

        val size = min(bounds.width(), bounds.height()).toFloat()
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val left = centerX - size / 2f
        val top = centerY - size / 2f

        fun px(fraction: Float) = left + fraction * size
        fun py(fraction: Float) = top + fraction * size

        dial?.let { drawDial(canvas, it, centerX, centerY, size, ambient) }

        val primary = if (ambient) WatchFaceTheme.AmbientPrimary else WatchFaceTheme.TextPrimary
        val secondary =
            if (ambient) WatchFaceTheme.AmbientSecondary else WatchFaceTheme.TextSecondary
        val accent = if (ambient) WatchFaceTheme.AmbientSecondary else WatchFaceTheme.Accent

        val clock = zonedDateTime.format(if (is24Hour) clock24Format else clock12Format)
        canvas.drawText(
            clock,
            centerX,
            py(0.475f),
            WatchFaceTheme.text(0.22f * size, primary, ambient, bold = true)
        )

        model?.let {
            if (!ambient) {
                canvas.drawText(
                    zonedDateTime.format(dateFormat),
                    centerX,
                    py(0.235f),
                    WatchFaceTheme.text(0.058f * size, primary, ambient)
                )
            }
            if (it.hijriDateText.isNotEmpty()) {
                canvas.drawText(
                    it.hijriDateText,
                    centerX,
                    py(0.545f),
                    WatchFaceTheme.text(0.058f * size, accent, ambient)
                )
            }
            if (!ambient) {
                val countdown = resources.getString(
                    R.string.watch_face_countdown,
                    resources.getString(it.current.labelRes),
                    formatMinutes(it.countdownMinutes),
                    resources.getString(it.next.labelRes)
                )
                canvas.drawText(
                    countdown,
                    centerX,
                    py(0.605f),
                    WatchFaceTheme.text(0.052f * size, secondary, ambient)
                )
            }
        }
    }

    private fun drawDial(
        canvas: Canvas,
        dial: PrayerDial,
        centerX: Float,
        centerY: Float,
        size: Float,
        ambient: Boolean
    ) {
        val radius = size * 0.455f
        val stroke = size * 0.020f
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        val trackPaint = WatchFaceTheme.arc(
            stroke,
            if (ambient) WatchFaceTheme.AmbientSecondary else WatchFaceTheme.Track,
            ambient
        )
        val fillPaint = WatchFaceTheme.arc(stroke, WatchFaceTheme.Accent, ambient)
        val currentPaint = WatchFaceTheme.arc(stroke, WatchFaceTheme.AccentBright, ambient)

        dial.segments.forEach { segment ->
            canvas.drawArc(rect, segment.startAngle - 90f, segment.sweepAngle, false, trackPaint)
            if (!ambient && segment.fillSweepAngle > 0.5f) {
                val running = segment.fillSweepAngle < segment.sweepAngle
                canvas.drawArc(
                    rect,
                    segment.startAngle - 90f,
                    segment.fillSweepAngle,
                    false,
                    if (running) currentPaint else fillPaint
                )
            }
        }

        dial.segments.forEach { segment ->
            val label = segment.label ?: return@forEach
            val running = segment.fillSweepAngle > 0f && segment.fillSweepAngle < segment.sweepAngle
            val nameColor = if (running && !ambient) {
                WatchFaceTheme.TextPrimary
            } else {
                WatchFaceTheme.TextSecondary
            }
            val flipped = segment.labelAngle in 90f..270f
            val rotation = if (flipped) segment.labelAngle + 180f else segment.labelAngle
            drawLabel(
                canvas, centerX, centerY, size, segment.labelAngle, rotation,
                resources.getString(label.labelRes),
                size * if (flipped) 0.368f else 0.415f, 0.038f * size, nameColor, ambient
            )
            drawLabel(
                canvas, centerX, centerY, size, segment.labelAngle, rotation,
                label.timeText,
                size * if (flipped) 0.415f else 0.368f, 0.032f * size, nameColor, ambient
            )
        }
    }

    private fun drawLabel(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        angle: Float,
        rotation: Float,
        text: String,
        radius: Float,
        textSize: Float,
        textColor: Int,
        ambient: Boolean
    ) {
        val rad = Math.toRadians((angle - 90f).toDouble())
        val x = centerX + (radius * cos(rad)).toFloat()
        val y = centerY + (radius * sin(rad)).toFloat()
        canvas.save()
        canvas.rotate(rotation, x, y)
        canvas.drawText(text, x, y + textSize * 0.35f, WatchFaceTheme.text(textSize, textColor, ambient))
        canvas.restore()
    }

    private fun formatMinutes(minutes: Int): String {
        val hours = minutes / 60
        val remaining = minutes % 60
        return String.format(Locale.US, "%02d:%02d", hours, remaining)
    }
}
