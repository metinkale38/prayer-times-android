package com.metinkale.prayer.wear.complications

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel

internal const val CUSTOM_PRAYER_TEXT_CONFIG_ACTION =
    "com.metinkale.prayer.wear.action.CONFIGURE_PRAYER_TEXT"

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

internal fun formatPrayerTextOption(
    context: Context,
    model: PrayerTileModel,
    option: PrayerTextOption
): String {
    val currentName = context.getString(model.current.labelRes)
    val currentTime = model.current.time.format(ComplicationTimeFormat)
    val nextName = context.getString(model.next.labelRes)
    val nextTime = model.next.time.format(ComplicationTimeFormat)
    fun prayerName(index: Int) = context.getString(model.today[index].labelRes)
    fun prayerTime(index: Int) = model.today[index].time.format(ComplicationTimeFormat)
    fun prayerCountdown(index: Int) = context.formatCountdown(
        model.prayerCountdownMinutes.getOrElse(index) { 0 }
    )
    fun prayerNameCountdown(index: Int) =
        "${prayerName(index)} ${context.formatCountdownIn(
            model.prayerCountdownMinutes.getOrElse(index) { 0 }
        )}"

    return when (option) {
        PrayerTextOption.HIJRI_DATE -> model.hijriDateText
        PrayerTextOption.HIJRI_MONTH -> model.hijriMonth
        PrayerTextOption.HIJRI_YEAR -> model.hijriYear
        PrayerTextOption.HIJRI_DAY -> model.hijriDay
        PrayerTextOption.CURRENT_PRAYER -> currentName
        PrayerTextOption.CURRENT_TIME -> currentTime
        PrayerTextOption.CURRENT_NAME_TIME -> "$currentName $currentTime"
        PrayerTextOption.NEXT_PRAYER -> nextName
        PrayerTextOption.NEXT_TIME -> nextTime
        PrayerTextOption.NEXT_NAME_TIME -> "$nextName $nextTime"
        PrayerTextOption.COUNTDOWN -> context.formatCountdown(model.countdownMinutes)
        PrayerTextOption.NEXT_NAME_COUNTDOWN ->
            "$nextName ${context.formatCountdownIn(model.countdownMinutes)}"

        PrayerTextOption.COMBINED_TIMES -> "$currentName $currentTime | $nextName $nextTime"
        PrayerTextOption.COMBINED_COUNTDOWN ->
            "$currentName $currentTime | $nextName ${context.formatCountdownIn(model.countdownMinutes)}"

        PrayerTextOption.FAJR_TIME -> prayerTime(0)
        PrayerTextOption.FAJR_COUNTDOWN -> prayerCountdown(0)
        PrayerTextOption.FAJR_NAME_TIME -> "${prayerName(0)} ${prayerTime(0)}"
        PrayerTextOption.FAJR_NAME_COUNTDOWN -> prayerNameCountdown(0)

        PrayerTextOption.SUN_TIME -> prayerTime(1)
        PrayerTextOption.SUN_COUNTDOWN -> prayerCountdown(1)
        PrayerTextOption.SUN_NAME_TIME -> "${prayerName(1)} ${prayerTime(1)}"
        PrayerTextOption.SUN_NAME_COUNTDOWN -> prayerNameCountdown(1)

        PrayerTextOption.DHUHR_TIME -> prayerTime(2)
        PrayerTextOption.DHUHR_COUNTDOWN -> prayerCountdown(2)
        PrayerTextOption.DHUHR_NAME_TIME -> "${prayerName(2)} ${prayerTime(2)}"
        PrayerTextOption.DHUHR_NAME_COUNTDOWN -> prayerNameCountdown(2)

        PrayerTextOption.ASR_TIME -> prayerTime(3)
        PrayerTextOption.ASR_COUNTDOWN -> prayerCountdown(3)
        PrayerTextOption.ASR_NAME_TIME -> "${prayerName(3)} ${prayerTime(3)}"
        PrayerTextOption.ASR_NAME_COUNTDOWN -> prayerNameCountdown(3)

        PrayerTextOption.MAGHRIB_TIME -> prayerTime(4)
        PrayerTextOption.MAGHRIB_COUNTDOWN -> prayerCountdown(4)
        PrayerTextOption.MAGHRIB_NAME_TIME -> "${prayerName(4)} ${prayerTime(4)}"
        PrayerTextOption.MAGHRIB_NAME_COUNTDOWN -> prayerNameCountdown(4)

        PrayerTextOption.ISHAA_TIME -> prayerTime(5)
        PrayerTextOption.ISHAA_COUNTDOWN -> prayerCountdown(5)
        PrayerTextOption.ISHAA_NAME_TIME -> "${prayerName(5)} ${prayerTime(5)}"
        PrayerTextOption.ISHAA_NAME_COUNTDOWN -> prayerNameCountdown(5)
    }
}

internal fun PrayerTextOption.label(context: Context): String = when (this) {
    PrayerTextOption.HIJRI_DATE -> context.getString(R.string.complication_option_hijri_date)
    PrayerTextOption.HIJRI_MONTH -> context.getString(R.string.complication_option_hijri_month)
    PrayerTextOption.HIJRI_YEAR -> context.getString(R.string.complication_option_hijri_year)
    PrayerTextOption.HIJRI_DAY -> context.getString(R.string.complication_option_hijri_day)
    PrayerTextOption.CURRENT_PRAYER -> context.getString(R.string.complication_option_current_prayer)
    PrayerTextOption.CURRENT_TIME -> context.getString(R.string.complication_option_current_time)
    PrayerTextOption.CURRENT_NAME_TIME ->
        context.getString(R.string.complication_option_current_name_time)

    PrayerTextOption.NEXT_PRAYER -> context.getString(R.string.complication_option_next_prayer)
    PrayerTextOption.NEXT_TIME -> context.getString(R.string.complication_option_next_time)
    PrayerTextOption.NEXT_NAME_TIME ->
        context.getString(R.string.complication_option_next_name_time)

    PrayerTextOption.COUNTDOWN -> context.getString(R.string.complication_option_countdown)
    PrayerTextOption.NEXT_NAME_COUNTDOWN ->
        context.getString(R.string.complication_option_next_countdown)

    PrayerTextOption.COMBINED_TIMES ->
        context.getString(R.string.complication_option_combined_times)

    PrayerTextOption.COMBINED_COUNTDOWN ->
        context.getString(R.string.complication_option_combined_countdown)

    PrayerTextOption.FAJR_TIME,
    PrayerTextOption.SUN_TIME,
    PrayerTextOption.DHUHR_TIME,
    PrayerTextOption.ASR_TIME,
    PrayerTextOption.MAGHRIB_TIME,
    PrayerTextOption.ISHAA_TIME -> context.getString(
        R.string.complication_option_prayer_time,
        prayerName(context, this)
    )

    PrayerTextOption.FAJR_COUNTDOWN,
    PrayerTextOption.SUN_COUNTDOWN,
    PrayerTextOption.DHUHR_COUNTDOWN,
    PrayerTextOption.ASR_COUNTDOWN,
    PrayerTextOption.MAGHRIB_COUNTDOWN,
    PrayerTextOption.ISHAA_COUNTDOWN -> context.getString(
        R.string.complication_option_prayer_countdown,
        prayerName(context, this)
    )

    PrayerTextOption.FAJR_NAME_TIME,
    PrayerTextOption.SUN_NAME_TIME,
    PrayerTextOption.DHUHR_NAME_TIME,
    PrayerTextOption.ASR_NAME_TIME,
    PrayerTextOption.MAGHRIB_NAME_TIME,
    PrayerTextOption.ISHAA_NAME_TIME -> context.getString(
        R.string.complication_option_prayer_name_time,
        prayerName(context, this)
    )

    PrayerTextOption.FAJR_NAME_COUNTDOWN,
    PrayerTextOption.SUN_NAME_COUNTDOWN,
    PrayerTextOption.DHUHR_NAME_COUNTDOWN,
    PrayerTextOption.ASR_NAME_COUNTDOWN,
    PrayerTextOption.MAGHRIB_NAME_COUNTDOWN,
    PrayerTextOption.ISHAA_NAME_COUNTDOWN -> context.getString(
        R.string.complication_option_prayer_name_countdown,
        prayerName(context, this)
    )
}

private fun prayerName(context: Context, option: PrayerTextOption): String = when (option) {
    PrayerTextOption.FAJR_TIME,
    PrayerTextOption.FAJR_COUNTDOWN,
    PrayerTextOption.FAJR_NAME_TIME,
    PrayerTextOption.FAJR_NAME_COUNTDOWN -> context.getString(R.string.fajr)

    PrayerTextOption.SUN_TIME,
    PrayerTextOption.SUN_COUNTDOWN,
    PrayerTextOption.SUN_NAME_TIME,
    PrayerTextOption.SUN_NAME_COUNTDOWN -> context.getString(R.string.sun)

    PrayerTextOption.DHUHR_TIME,
    PrayerTextOption.DHUHR_COUNTDOWN,
    PrayerTextOption.DHUHR_NAME_TIME,
    PrayerTextOption.DHUHR_NAME_COUNTDOWN -> context.getString(R.string.zuhr)

    PrayerTextOption.ASR_TIME,
    PrayerTextOption.ASR_COUNTDOWN,
    PrayerTextOption.ASR_NAME_TIME,
    PrayerTextOption.ASR_NAME_COUNTDOWN -> context.getString(R.string.asr)

    PrayerTextOption.MAGHRIB_TIME,
    PrayerTextOption.MAGHRIB_COUNTDOWN,
    PrayerTextOption.MAGHRIB_NAME_TIME,
    PrayerTextOption.MAGHRIB_NAME_COUNTDOWN -> context.getString(R.string.maghrib)

    PrayerTextOption.ISHAA_TIME,
    PrayerTextOption.ISHAA_COUNTDOWN,
    PrayerTextOption.ISHAA_NAME_TIME,
    PrayerTextOption.ISHAA_NAME_COUNTDOWN -> context.getString(R.string.ishaa)

    else -> ""
}

internal fun Context.formatCountdownIn(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (hours > 0) {
        getString(R.string.complication_in_hours_minutes, hours, remaining)
    } else {
        getString(R.string.complication_in_minutes, remaining)
    }
}
