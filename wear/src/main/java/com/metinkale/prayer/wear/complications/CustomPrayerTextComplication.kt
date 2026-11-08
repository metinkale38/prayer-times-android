package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel

internal const val CUSTOM_PRAYER_TEXT_CONFIG_ACTION =
    "com.metinkale.prayer.wear.action.CONFIGURE_PRAYER_TEXT"

internal enum class PrayerTextToken {
    CURRENT_NAME,
    NEXT_NAME,
    CURRENT_TIME,
    NEXT_TIME,
    COUNTDOWN,
    SEPARATOR,
    UNTIL
}

internal object CustomPrayerTextSettings {
    private const val PREFS = "custom_prayer_text_complications"

    private fun key(instanceId: Int) = "blocks_$instanceId"

    val defaultTokens = listOf(
        PrayerTextToken.CURRENT_NAME,
        PrayerTextToken.CURRENT_TIME,
        PrayerTextToken.SEPARATOR,
        PrayerTextToken.NEXT_NAME,
        PrayerTextToken.NEXT_TIME
    )

    fun tokens(context: Context, instanceId: Int): List<PrayerTextToken> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key(instanceId), null) ?: return defaultTokens
        if (raw.isEmpty()) return emptyList()
        return raw.split(',').mapNotNull {
            runCatching { PrayerTextToken.valueOf(it) }.getOrNull()
        }
    }

    fun setTokens(context: Context, instanceId: Int, tokens: List<PrayerTextToken>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key(instanceId), tokens.joinToString(","))
            .apply()
    }
}

class CustomPrayerTextComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val model = model() ?: return noData()
        val tokens = CustomPrayerTextSettings.tokens(this, request.complicationInstanceId)
        if (tokens.isEmpty()) return noData()
        val text = buildPrayerText(this, model, tokens)
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
        val text = "Ishaa 18:22 | Fajr 05:30"
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

internal fun buildPrayerText(
    context: Context,
    model: PrayerTileModel,
    tokens: List<PrayerTextToken>
): String = tokens.joinToString(" ") { token ->
    when (token) {
        PrayerTextToken.CURRENT_NAME -> context.getString(model.current.labelRes)
        PrayerTextToken.NEXT_NAME -> context.getString(model.next.labelRes)
        PrayerTextToken.CURRENT_TIME ->
            model.current.time.format(ComplicationTimeFormat)

        PrayerTextToken.NEXT_TIME -> model.next.time.format(ComplicationTimeFormat)
        PrayerTextToken.COUNTDOWN -> context.formatCountdown(model.countdownMinutes)
        PrayerTextToken.SEPARATOR -> "|"
        PrayerTextToken.UNTIL -> context.getString(R.string.complication_until)
    }
}
