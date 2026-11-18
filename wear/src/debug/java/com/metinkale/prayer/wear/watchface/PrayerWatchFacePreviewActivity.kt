package com.metinkale.prayer.wear.watchface

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.data.WearTimesRepository
import com.metinkale.prayer.wear.tiles.buildPrayerTileModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrayerWatchFacePreviewActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        val instant = intent.getLongExtra("el", 0L).takeIf { it > 0L }?.let(Instant::ofEpochMilli)
            ?: Instant.now()
        val ambient = intent.getBooleanExtra("ambient", false)
        val now = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        scope.launch {
            val payload = withContext(Dispatchers.IO) {
                runCatching { WearTimesRepository.get(this@PrayerWatchFacePreviewActivity).payload.first() }
                    .getOrNull()
            }
            setContentView(PrayerWatchFacePreviewView(this@PrayerWatchFacePreviewActivity, payload, now, ambient))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

private class PrayerWatchFacePreviewView(
    context: Context,
    private val payload: WearTimesPayload?,
    private val now: ZonedDateTime,
    private val ambient: Boolean
) : View(context) {
    private val painter = PrayerWatchFaceCanvas(context.resources)
    private val drawables = createComplications()

    override fun onDraw(canvas: Canvas) {
        val bounds = Rect(0, 0, width, height)
        val model = payload?.let {
            buildPrayerTileModel(context, it, now.toLocalDateTime())
        }
        val dial = payload?.let { buildPrayerDial(it, now) }
        painter.draw(canvas, bounds, now, model, dial, ambient, true)
        if (!ambient) {
            drawables.forEach { (slotId, drawable) ->
                drawable.bounds = slotBounds(slotId, bounds)
                drawable.draw(canvas)
            }
        }
    }

    private fun createComplications(): Map<Int, ComplicationDrawable> = buildMap {
        fun drawable(data: ComplicationData): ComplicationDrawable =
            WatchFaceTheme.complicationDrawable(context).apply {
                currentTime = now.toInstant()
                isInAmbientMode = ambient
                setComplicationData(data, false)
            }

        put(
            WatchFaceSlots.Weather,
            drawable(
                ShortTextComplicationData.Builder(
                    PlainComplicationText.Builder("32°").build(),
                    PlainComplicationText.Builder("32°").build()
                ).build()
            )
        )
        listOf(
            WatchFaceSlots.Goal1,
            WatchFaceSlots.Goal2,
            WatchFaceSlots.Goal3
        ).forEach { id ->
            put(
                id,
                drawable(
                    RangedValueComplicationData.Builder(
                        100f,
                        0f,
                        100f,
                        PlainComplicationText.Builder("100").build()
                    ).setText(PlainComplicationText.Builder("100%").build()).build()
                )
            )
        }
        put(
            WatchFaceSlots.Steps,
            drawable(
                ShortTextComplicationData.Builder(
                    PlainComplicationText.Builder("1000").build(),
                    PlainComplicationText.Builder("1000").build()
                ).setTitle(PlainComplicationText.Builder("Schritte").build()).build()
            )
        )
    }

    private fun slotBounds(slotId: Int, bounds: Rect): Rect {
        val fraction = WatchFaceSlots.Bounds.getValue(slotId)
        val size = minOf(bounds.width(), bounds.height()).toFloat()
        val left = bounds.exactCenterX() - size / 2f
        val top = bounds.exactCenterY() - size / 2f
        return Rect(
            (left + fraction.left * size).toInt(),
            (top + fraction.top * size).toInt(),
            (left + fraction.right * size).toInt(),
            (top + fraction.bottom * size).toInt()
        )
    }
}
