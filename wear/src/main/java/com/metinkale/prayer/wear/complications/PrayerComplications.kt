package com.metinkale.prayer.wear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.metinkale.prayer.wear.MainActivity
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import com.metinkale.prayer.wear.tiles.loadPrayerTileModel
import java.time.format.DateTimeFormatter

internal val ComplicationTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

object PrayerComplicationsUpdater {
    private val services = listOf(
        PrayerProgressComplicationService::class.java,
        HijriDateComplicationService::class.java,
        CustomPrayerTextComplicationService::class.java
    )

    fun requestUpdates(context: Context) {
        val appContext = context.applicationContext
        services.forEach { service ->
            runCatching {
                ComplicationDataSourceUpdateRequester.create(
                    appContext,
                    ComponentName(appContext, service)
                ).requestUpdateAll()
            }
        }
    }

    fun requestUpdate(context: Context, service: Class<*>, instanceId: Int) {
        runCatching {
            ComplicationDataSourceUpdateRequester.create(
                context.applicationContext,
                ComponentName(context.applicationContext, service)
            ).requestUpdate(instanceId)
        }
    }
}

private fun plain(text: CharSequence): PlainComplicationText =
    PlainComplicationText.Builder(text).build()

internal data class TextComplicationContent(
    val shortText: CharSequence,
    val shortTitle: CharSequence? = null,
    val longText: CharSequence,
    val longTitle: CharSequence? = null,
    val contentDescription: CharSequence
)

internal fun buildTextComplication(
    type: ComplicationType,
    content: TextComplicationContent,
    tapAction: PendingIntent? = null
): ComplicationData? = when (type) {
    ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
        text = plain(content.shortText),
        contentDescription = plain(content.contentDescription)
    ).apply {
        content.shortTitle?.let { setTitle(plain(it)) }
        tapAction?.let { setTapAction(it) }
    }.build()

    ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
        text = plain(content.longText),
        contentDescription = plain(content.contentDescription)
    ).apply {
        content.longTitle?.let { setTitle(plain(it)) }
        tapAction?.let { setTapAction(it) }
    }.build()

    else -> null
}

internal fun Context.formatCountdown(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (hours > 0) {
        getString(R.string.complication_duration_hours_minutes, hours, remaining)
    } else {
        getString(R.string.complication_duration_minutes, remaining)
    }
}

private fun Context.openAppPendingIntent(): PendingIntent =
    PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

abstract class PrayerComplicationService : SuspendingComplicationDataSourceService() {
    internal suspend fun model(): PrayerTileModel? = loadPrayerTileModel(this)

    protected fun tapAction(): PendingIntent = openAppPendingIntent()

    protected fun noData(): ComplicationData = NoDataComplicationData()
}

class PrayerProgressComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.RANGED_VALUE) return null
        val model = model() ?: return noData()
        val now = System.currentTimeMillis()
        val duration = model.periodEndEpochMillis - model.periodStartEpochMillis
        val progress = if (duration <= 0L) {
            0f
        } else {
            ((now - model.periodStartEpochMillis).toFloat() / duration).coerceIn(0f, 1f)
        }
        val currentLabelRes = model.today.getOrNull(model.currentIndex)?.labelRes
            ?: R.string.ishaa
        val currentName = getString(currentLabelRes)
        val nextName = getString(model.next.labelRes)
        return RangedValueComplicationData.Builder(
            value = progress * 100f,
            min = 0f,
            max = 100f,
            contentDescription = plain(
                getString(R.string.complication_progress_description, currentName, nextName)
            )
        )
            .setValueType(RangedValueComplicationData.TYPE_PERCENTAGE)
            .setTitle(plain(getString(R.string.complication_progress_title)))
            .setText(plain(getString(R.string.complication_progress_text, currentName, nextName)))
            .setTapAction(tapAction())
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.RANGED_VALUE) {
            RangedValueComplicationData.Builder(
                value = 50f,
                min = 0f,
                max = 100f,
                contentDescription = plain("Prayer progress from Asr to Maghrib")
            )
                .setValueType(RangedValueComplicationData.TYPE_PERCENTAGE)
                .setTitle(plain("Prayer"))
                .setText(plain("Asr → Maghrib"))
                .build()
        } else {
            null
        }
}

class HijriDateComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val model = model() ?: return noData()
        return buildTextComplication(
            request.complicationType,
            TextComplicationContent(
                shortText = model.hijriDay,
                shortTitle = model.hijriMonthYear,
                longText = model.hijriText,
                contentDescription = getString(
                    R.string.complication_hijri_description,
                    model.hijriText
                )
            ),
            tapAction()
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        buildTextComplication(
            type,
            TextComplicationContent(
                shortText = "27",
                shortTitle = "Jumada al-awwal 1448",
                longText = "27. Jumada al-awwal 1448",
                contentDescription = "Hijri date: 27. Jumada al-awwal 1448"
            )
        )
}
