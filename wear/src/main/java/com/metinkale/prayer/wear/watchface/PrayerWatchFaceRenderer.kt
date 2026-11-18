package com.metinkale.prayer.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.text.format.DateFormat
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.data.WearTimesRepository
import com.metinkale.prayer.wear.tiles.buildPrayerTileModel
import java.time.ZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class PrayerWatchFaceRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState,
    private val slots: List<ComplicationSlot>
) : Renderer.CanvasRenderer2<PrayerWatchFaceRenderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    CanvasType.SOFTWARE,
    0L,
    true
) {
    internal object SharedAssets : Renderer.SharedAssets {
        override fun onDestroy() {}
    }

    private val painter = PrayerWatchFaceCanvas(context.resources)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var payload: WearTimesPayload? = null

    init {
        scope.launch {
            WearTimesRepository.get(context).payload.collect { value ->
                payload = value
                invalidate()
            }
        }
    }

    override suspend fun createSharedAssets(): SharedAssets = SharedAssets

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val data = payload
        val model = data?.let {
            buildPrayerTileModel(context, it, zonedDateTime.toLocalDateTime())
        }
        val dial = data?.let { buildPrayerDial(it, zonedDateTime) }
        val ambient = renderParameters.drawMode == DrawMode.AMBIENT

        painter.draw(
            canvas,
            bounds,
            zonedDateTime,
            model,
            dial,
            ambient,
            DateFormat.is24HourFormat(context)
        )

        if (!ambient) {
            slots.forEach { it.render(canvas, zonedDateTime, renderParameters) }
        }
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        if (renderParameters.drawMode == DrawMode.AMBIENT) return
        canvas.drawColor(Color.TRANSPARENT)
        render(canvas, bounds, zonedDateTime, sharedAssets)
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
