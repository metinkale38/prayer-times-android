package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import java.time.Instant
import java.util.concurrent.TimeUnit

class CustomPrayerTextComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val model = model() ?: return noData()
        val option = PrayerTextSettings.option(this, request.complicationInstanceId)
        val text = formatPrayerTextOption(this, model, option)
        if (text.isBlank()) return noData()
        val countdown = countdownSpec(this, model, option)
        if (countdown != null) {
            return buildTextComplication(
                request.complicationType,
                countdownComplicationText(countdown),
                text,
                tapAction()
            )
        }
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

internal fun countdownComplicationText(spec: CountdownSpec): TimeDifferenceComplicationText =
    TimeDifferenceComplicationText.Builder(
        TimeDifferenceStyle.SHORT_DUAL_UNIT,
        CountDownTimeReference(Instant.ofEpochMilli(spec.targetEpochMillis))
    )
        .setMinimumTimeUnit(TimeUnit.MINUTES)
        .apply { spec.surroundingText?.let { setText(it) } }
        .build()

internal data class CountdownSpec(
    val targetEpochMillis: Long,
    val surroundingText: String?
)

internal fun countdownSpec(
    context: Context,
    model: PrayerTileModel,
    option: PrayerTextOption
): CountdownSpec? {
    val nextName = context.getString(model.next.labelRes)
    val currentName = context.getString(model.current.labelRes)
    fun prayerName(index: Int) = context.getString(model.today[index].labelRes)
    fun target(index: Int) = model.prayerNextEpochMillis.getOrElse(index) { 0L }
    return when (option) {
        PrayerTextOption.COUNTDOWN -> CountdownSpec(model.nextEpochMillis, null)
        PrayerTextOption.NEXT_NAME_COUNTDOWN ->
            CountdownSpec(model.nextEpochMillis, "$nextName in ^1")

        PrayerTextOption.COMBINED_COUNTDOWN ->
            CountdownSpec(
                model.nextEpochMillis,
                "$currentName ${model.current.time.format(ComplicationTimeFormat)} | $nextName in ^1"
            )

        PrayerTextOption.FAJR_COUNTDOWN -> CountdownSpec(target(0), null)
        PrayerTextOption.FAJR_NAME_COUNTDOWN -> CountdownSpec(target(0), "${prayerName(0)} in ^1")
        PrayerTextOption.SUN_COUNTDOWN -> CountdownSpec(target(1), null)
        PrayerTextOption.SUN_NAME_COUNTDOWN -> CountdownSpec(target(1), "${prayerName(1)} in ^1")
        PrayerTextOption.DHUHR_COUNTDOWN -> CountdownSpec(target(2), null)
        PrayerTextOption.DHUHR_NAME_COUNTDOWN -> CountdownSpec(target(2), "${prayerName(2)} in ^1")
        PrayerTextOption.ASR_COUNTDOWN -> CountdownSpec(target(3), null)
        PrayerTextOption.ASR_NAME_COUNTDOWN -> CountdownSpec(target(3), "${prayerName(3)} in ^1")
        PrayerTextOption.MAGHRIB_COUNTDOWN -> CountdownSpec(target(4), null)
        PrayerTextOption.MAGHRIB_NAME_COUNTDOWN -> CountdownSpec(target(4), "${prayerName(4)} in ^1")
        PrayerTextOption.ISHAA_COUNTDOWN -> CountdownSpec(target(5), null)
        PrayerTextOption.ISHAA_NAME_COUNTDOWN -> CountdownSpec(target(5), "${prayerName(5)} in ^1")
        else -> null
    }
}
