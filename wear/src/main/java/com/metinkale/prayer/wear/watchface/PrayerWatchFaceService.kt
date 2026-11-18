package com.metinkale.prayer.wear.watchface

import android.content.Intent
import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.TapType
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import com.metinkale.prayer.wear.MainActivity

class PrayerWatchFaceService : WatchFaceService() {
    override fun createUserStyleSchema(): UserStyleSchema = UserStyleSchema(emptyList())

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager =
        ComplicationSlotsManager(createSlots(), currentUserStyleRepository)

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = PrayerWatchFaceRenderer(
            this,
            surfaceHolder,
            currentUserStyleRepository,
            watchState,
            complicationSlotsManager.complicationSlots.values.toList()
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
            .setTapListener(object : WatchFace.TapListener {
                override fun onTapEvent(
                    tapType: Int,
                    tapEvent: TapEvent,
                    complicationSlot: ComplicationSlot?
                ) {
                    if (tapType != TapType.UP) return
                    val action = complicationSlot?.complicationData?.value?.tapAction
                    if (action != null) {
                        runCatching { action.send() }
                    } else {
                        val intent = Intent(this@PrayerWatchFaceService, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { startActivity(intent) }
                    }
                }
            })
    }

    private fun createSlots(): List<ComplicationSlot> = listOf(
        createSlot(
            WatchFaceSlots.Weather,
            WatchFaceSlots.Bounds.getValue(WatchFaceSlots.Weather),
            listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.RANGED_VALUE,
                ComplicationType.SMALL_IMAGE,
                ComplicationType.EMPTY
            )
        ),
        createSlot(WatchFaceSlots.Goal1, WatchFaceSlots.Bounds.getValue(WatchFaceSlots.Goal1), GoalTypes),
        createSlot(WatchFaceSlots.Goal2, WatchFaceSlots.Bounds.getValue(WatchFaceSlots.Goal2), GoalTypes),
        createSlot(WatchFaceSlots.Goal3, WatchFaceSlots.Bounds.getValue(WatchFaceSlots.Goal3), GoalTypes),
        createSlot(
            WatchFaceSlots.Steps,
            WatchFaceSlots.Bounds.getValue(WatchFaceSlots.Steps),
            listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.EMPTY
            )
        )
    )

    private fun createSlot(
        id: Int,
        bounds: RectF,
        types: List<ComplicationType>
    ): ComplicationSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = id,
        canvasComplicationFactory = { watchState, invalidateCallback ->
            CanvasComplicationDrawable(
                WatchFaceTheme.complicationDrawable(this),
                watchState,
                invalidateCallback
            )
        },
        supportedTypes = types,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(),
        bounds = ComplicationSlotBounds(
            ComplicationType.values().associateWith { RectF(bounds) },
            ComplicationType.values().associateWith { RectF() }
        )
    ).build()

    companion object {
        private val GoalTypes = listOf(
            ComplicationType.GOAL_PROGRESS,
            ComplicationType.RANGED_VALUE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
            ComplicationType.EMPTY
        )
    }
}
