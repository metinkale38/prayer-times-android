package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel

class CustomPrayerTextComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val model = model() ?: return noData()
        val option = PrayerTextSettings.option(this, request.complicationInstanceId)
        val text = formatPrayerTextOption(this, model, option)
        if (text.isBlank()) return noData()
        return buildTextComplication(
            request.complicationType,
            TextComplicationContent(
                shortText = text,
                longText = text,
                contentDescription = text
            ),
            tapAction()
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val text = "Ishaa 18:22 | Fajr 04:00"
        return buildTextComplication(
            type,
            TextComplicationContent(
                shortText = text,
                longText = text,
                contentDescription = text
            )
        )
    }
}

