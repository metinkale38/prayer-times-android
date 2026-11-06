package com.metinkale.prayer.wear.debug

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Debug-only heading override for the Qibla compass. Updated via
 * [DebugHeadingReceiver] so the compass can be tested deterministically with
 * adb, independent of the emulator sensor listener timing.
 */
object DebugHeadingOverride {
    val heading = MutableStateFlow<Float?>(null)

    fun set(angle: Float) {
        heading.value = angle
    }

    fun reset() {
        heading.value = null
    }
}
