package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel

internal object PrayerTextSettings {
    private const val PREFS = "prayer_text_complications"

    private fun key(instanceId: Int) = "option_$instanceId"

    fun option(context: Context, instanceId: Int): PrayerTextOption {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key(instanceId), null) ?: return PrayerTextOption.COMBINED_TIMES
        return runCatching { PrayerTextOption.valueOf(raw) }
            .getOrDefault(PrayerTextOption.COMBINED_TIMES)
    }

    fun setOption(context: Context, instanceId: Int, option: PrayerTextOption) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key(instanceId), option.name)
            .apply()
    }
}

