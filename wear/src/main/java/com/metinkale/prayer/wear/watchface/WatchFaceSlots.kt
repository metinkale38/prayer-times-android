package com.metinkale.prayer.wear.watchface

import android.graphics.RectF

internal object WatchFaceSlots {
    const val Weather = 100
    const val Goal1 = 101
    const val Goal2 = 102
    const val Goal3 = 103
    const val Steps = 104

    val Bounds: Map<Int, RectF> = mapOf(
        Weather to RectF(0.37f, 0.045f, 0.63f, 0.185f),
        Goal1 to RectF(0.28f, 0.645f, 0.40f, 0.765f),
        Goal2 to RectF(0.44f, 0.645f, 0.56f, 0.765f),
        Goal3 to RectF(0.60f, 0.645f, 0.72f, 0.765f),
        Steps to RectF(0.22f, 0.778f, 0.78f, 0.848f)
    )

    val GoalIds = listOf(Goal1, Goal2, Goal3)
}
