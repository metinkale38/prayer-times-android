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

internal data class PrayerTextValues(
    val currentName: String,
    val currentTime: String,
    val nextName: String,
    val nextTime: String,
    val countdown: String
)

class CustomPrayerTextComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val model = model() ?: return noData()
        val tokens = CustomPrayerTextSettings.tokens(this, request.complicationInstanceId)
        if (tokens.isEmpty()) return noData()
        val values = prayerTextValues(this, model)
        val shortText = formatShortPrayerText(tokens, values)
        val longText = formatPrayerText(this, tokens, values)
        if (longText.isBlank()) return noData()
        return buildTextComplication(
            request.complicationType,
            TextComplicationContent(
                shortText = shortText.ifEmpty { longText },
                longText = longText,
                contentDescription = longText
            ),
            tapAction()
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val values = PrayerTextValues("Ishaa", "18:22", "Fajr", "05:30", "2h 8m")
        val tokens = CustomPrayerTextSettings.defaultTokens
        return buildTextComplication(
            type,
            TextComplicationContent(
                shortText = formatShortPrayerText(tokens, values),
                longText = formatPrayerText(this, tokens, values),
                contentDescription = formatPrayerText(this, tokens, values)
            )
        )
    }
}

internal fun prayerTextValues(context: Context, model: PrayerTileModel) = PrayerTextValues(
    currentName = context.getString(model.current.labelRes),
    currentTime = model.current.time.format(ComplicationTimeFormat),
    nextName = context.getString(model.next.labelRes),
    nextTime = model.next.time.format(ComplicationTimeFormat),
    countdown = context.formatCountdown(model.countdownMinutes)
)

internal fun formatPrayerText(
    context: Context,
    tokens: List<PrayerTextToken>,
    values: PrayerTextValues
): String = tokens.joinToString(" ") { token ->
    when (token) {
        PrayerTextToken.CURRENT_NAME -> values.currentName
        PrayerTextToken.NEXT_NAME -> values.nextName
        PrayerTextToken.CURRENT_TIME -> values.currentTime
        PrayerTextToken.NEXT_TIME -> values.nextTime
        PrayerTextToken.COUNTDOWN -> values.countdown
        PrayerTextToken.SEPARATOR -> "|"
        PrayerTextToken.UNTIL -> context.getString(R.string.complication_until)
    }
}

internal fun formatShortPrayerText(
    tokens: List<PrayerTextToken>,
    values: PrayerTextValues
): String {
    val hasTime = PrayerTextToken.CURRENT_TIME in tokens ||
        PrayerTextToken.NEXT_TIME in tokens
    if (PrayerTextToken.COUNTDOWN in tokens && !hasTime) return values.countdown

    tokens.forEachIndexed { index, token ->
        when (token) {
            PrayerTextToken.CURRENT_NAME -> {
                return if (tokens.getOrNull(index + 1) == PrayerTextToken.CURRENT_TIME) {
                    "${values.currentName} ${values.currentTime}"
                } else {
                    values.currentName
                }
            }

            PrayerTextToken.NEXT_NAME -> {
                return if (tokens.getOrNull(index + 1) == PrayerTextToken.NEXT_TIME) {
                    "${values.nextName} ${values.nextTime}"
                } else {
                    values.nextName
                }
            }

            PrayerTextToken.CURRENT_TIME -> return values.currentTime
            PrayerTextToken.NEXT_TIME -> return values.nextTime
            PrayerTextToken.COUNTDOWN -> return values.countdown
            PrayerTextToken.SEPARATOR, PrayerTextToken.UNTIL -> Unit
        }
    }
    return ""
}
