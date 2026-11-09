package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel

internal enum class PrayerTextOption {
    HIJRI_DATE,
    HIJRI_MONTH,
    HIJRI_YEAR,
    HIJRI_DAY,
    CURRENT_PRAYER,
    CURRENT_TIME,
    CURRENT_NAME_TIME,
    NEXT_PRAYER,
    NEXT_TIME,
    NEXT_NAME_TIME,
    COUNTDOWN,
    NEXT_NAME_COUNTDOWN,
    COMBINED_TIMES,
    COMBINED_COUNTDOWN,
    FAJR_TIME,
    FAJR_COUNTDOWN,
    FAJR_NAME_TIME,
    FAJR_NAME_COUNTDOWN,
    SUN_TIME,
    SUN_COUNTDOWN,
    SUN_NAME_TIME,
    SUN_NAME_COUNTDOWN,
    DHUHR_TIME,
    DHUHR_COUNTDOWN,
    DHUHR_NAME_TIME,
    DHUHR_NAME_COUNTDOWN,
    ASR_TIME,
    ASR_COUNTDOWN,
    ASR_NAME_TIME,
    ASR_NAME_COUNTDOWN,
    MAGHRIB_TIME,
    MAGHRIB_COUNTDOWN,
    MAGHRIB_NAME_TIME,
    MAGHRIB_NAME_COUNTDOWN,
    ISHAA_TIME,
    ISHAA_COUNTDOWN,
    ISHAA_NAME_TIME,
    ISHAA_NAME_COUNTDOWN
}

