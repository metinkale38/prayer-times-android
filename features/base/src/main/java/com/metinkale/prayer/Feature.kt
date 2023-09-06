package com.metinkale.prayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.metinkale.prayer.receiver.AppEventListener
import com.metinkale.prayer.receiver.AppEventManager


interface ActivityFeature : Feature {
    fun buildIntent(c: Context): Intent

    fun launch(c: Context, extras: Bundle? = null) {
        val intent = buildIntent(c)
        if (extras != null) intent.putExtras(extras)
        c.startActivity(intent)
    }
}

interface MenuFeature : ActivityFeature {
    val iconRes: Int
    val titleRes: Int
}

interface Feature {
    val name: String
    val appEventListeners: List<AppEventListener>
        get() = emptyList()

    companion object {
        var FEATURES: List<Feature> = emptyList()
            private set

        fun init(vararg feature: Feature) {
            FEATURES = feature.toList()
            FEATURES.flatMap { it.appEventListeners }.forEach { AppEventManager.register(it) }
        }
    }
}