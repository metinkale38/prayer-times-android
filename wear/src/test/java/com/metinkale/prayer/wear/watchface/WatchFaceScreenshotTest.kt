package com.metinkale.prayer.wear.watchface

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import com.metinkale.prayer.sync.WearCity
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.tiles.buildPrayerTileModel
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w450dp-h450dp-xxhdpi")
class WatchFaceScreenshotTest {
    private val zone = ZoneId.of("Europe/Berlin")

    private fun day(date: String) = WearDayTimes(
        date = date,
        fajr = "04:30",
        sun = "06:10",
        dhuhr = "13:15",
        asr = "17:00",
        maghrib = "19:45",
        ishaa = "21:15"
    )

    private val payload = WearTimesPayload(
        generatedAtEpochMillis = 0L,
        city = WearCity(1, "Berlin", "Diyanet", 2.0, 52.52, 13.405),
        days = (14..21).map { day("2026-04-$it") }
    )

    private fun now(hour: Int, minute: Int) =
        LocalDateTime.of(2026, 4, 16, hour, minute).atZone(zone)

    @Test
    fun renderScreenshots() {
        val context = RuntimeEnvironment.getApplication()
        val painter = PrayerWatchFaceCanvas(context.resources)
        val output = File(System.getProperty("user.dir"), "build/watchface-shots").apply { mkdirs() }
        val times = listOf(
            "0300" to now(3, 0),
            "0645" to now(6, 45),
            "1330" to now(13, 30),
            "1900" to now(19, 0),
            "2130" to now(21, 30)
        )
        for ((name, time) in times) {
            save(
                painter,
                context,
                time,
                false,
                File(output, "face-$name.png"),
                drawComplications = true
            )
        }
        save(
            painter,
            context,
            now(13, 30),
            true,
            File(output, "face-ambient.png"),
            drawComplications = false
        )
    }

    private fun save(
        painter: PrayerWatchFaceCanvas,
        context: android.content.Context,
        time: ZonedDateTime,
        ambient: Boolean,
        file: File,
        drawComplications: Boolean
    ) {
        val bitmap = Bitmap.createBitmap(450, 450, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = Rect(0, 0, 450, 450)
        val model = buildPrayerTileModel(context, payload, time.toLocalDateTime())
        val dial = buildPrayerDial(payload, time)
        painter.draw(canvas, bounds, time, model, dial, ambient, true)
        if (drawComplications) {
            complications(context, time, ambient).forEach { (id, drawable) ->
                drawable.bounds = slotBounds(id, bounds)
                drawable.draw(canvas)
            }
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SCREENSHOT " + file.absolutePath)
    }

    private fun complications(
        context: android.content.Context,
        time: ZonedDateTime,
        ambient: Boolean
    ): Map<Int, ComplicationDrawable> = buildMap {
        fun drawable(data: androidx.wear.watchface.complications.data.ComplicationData) =
            WatchFaceTheme.complicationDrawable(context).apply {
                currentTime = time.toInstant()
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
