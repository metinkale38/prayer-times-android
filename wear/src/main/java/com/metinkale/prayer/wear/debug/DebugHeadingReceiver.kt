package com.metinkale.prayer.wear.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.metinkale.prayer.wear.BuildConfig

/**
 * Debug-only adb entry point for overriding the Qibla compass heading.
 *
 * Set:   adb shell am broadcast -a com.metinkale.prayer.wear.DEBUG_HEADING --ef angle 218
 * Reset: adb shell am broadcast -a com.metinkale.prayer.wear.DEBUG_HEADING --ez reset true
 */
class DebugHeadingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!BuildConfig.DEBUG) return
        if (intent.action != ACTION) return

        if (intent.getBooleanExtra(EXTRA_RESET, false)) {
            DebugHeadingOverride.reset()
            return
        }

        if (intent.hasExtra(EXTRA_ANGLE)) {
            val angle = intent.getFloatExtra(EXTRA_ANGLE, Float.NaN)
            if (!angle.isNaN()) {
                DebugHeadingOverride.set(angle)
            }
        }
    }

    companion object {
        const val ACTION = "com.metinkale.prayer.wear.DEBUG_HEADING"
        const val EXTRA_ANGLE = "angle"
        const val EXTRA_RESET = "reset"
    }
}
