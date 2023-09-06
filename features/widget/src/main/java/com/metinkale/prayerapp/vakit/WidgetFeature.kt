package com.metinkale.prayerapp.vakit

import com.metinkale.prayer.Feature

object WidgetFeature : Feature {
    override val name: String = "widget"

    override val appEventListeners = listOf(WidgetService)
}