package com.metinkale.prayer.wear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.metinkale.prayer.wear.MainActivity
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import com.metinkale.prayer.wear.tiles.loadPrayerTileModel
import java.time.Instant
import java.time.format.DateTimeFormatter

private val ComplicationTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal object PrayerTimeComplicationSettings {
    private const val PREFS = "prayer_time_complications"

    private fun key(instanceId: Int) = "prayer_index_$instanceId"

    fun selectedIndex(context: Context, instanceId: Int): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(key(instanceId), 0)
            .coerceIn(0, 5)

    fun setSelectedIndex(context: Context, instanceId: Int, index: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(key(instanceId), index.coerceIn(0, 5))
            .apply()
    }
}

object PrayerComplicationsUpdater {
    private val services = listOf(
        NextPrayerComplicationService::class.java,
        PrayerCountdownComplicationService::class.java,
        PrayerProgressComplicationService::class.java,
        HijriDateComplicationService::class.java,
        PrayerTimeComplicationService::class.java
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

class NextPrayerComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null
        val model = model() ?: return noData()
        val name = getString(model.next.labelRes)
        val time = model.next.time.format(ComplicationTimeFormat)
        return ShortTextComplicationData.Builder(
            text = plain(time),
            contentDescription = plain(
                getString(R.string.complication_next_description, name, time)
            )
        )
            .setTitle(plain(name))
            .setTapAction(tapAction())
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) {
            ShortTextComplicationData.Builder(
                text = plain("05:30"),
                contentDescription = plain("Next prayer: Fajr at 05:30")
            )
                .setTitle(plain("Fajr"))
                .build()
        } else {
            null
        }
}

class PrayerCountdownComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null
        val model = model() ?: return noData()
        val name = getString(model.next.labelRes)
        val countdown = TimeDifferenceComplicationText.Builder(
            TimeDifferenceStyle.SHORT_DUAL_UNIT,
            CountDownTimeReference(Instant.ofEpochMilli(model.nextEpochMillis))
        ).build()
        return ShortTextComplicationData.Builder(
            text = countdown,
            contentDescription = plain(
                getString(R.string.complication_countdown_description, name)
            )
        )
            .setTitle(plain(name))
            .setTapAction(tapAction())
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) {
            ShortTextComplicationData.Builder(
                text = plain("2h 15m"),
                contentDescription = plain("Countdown to Fajr")
            )
                .setTitle(plain("Fajr"))
                .build()
        } else {
            null
        }
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
        val description = plain(
            getString(R.string.complication_hijri_description, model.hijriText)
        )
        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = plain(model.hijriDay),
                contentDescription = description
            )
                .setTitle(plain(model.hijriMonthYear))
                .setTapAction(tapAction())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = plain(model.hijriText),
                contentDescription = description
            )
                .setTitle(plain(getString(R.string.complication_hijri_title)))
                .setTapAction(tapAction())
                .build()

            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? = when (type) {
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = plain("27"),
            contentDescription = plain("Hijri date: 27. Jumada al-awwal 1448")
        )
            .setTitle(plain("Jumada al-awwal 1448"))
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = plain("27. Jumada al-awwal 1448"),
            contentDescription = plain("Hijri date: 27. Jumada al-awwal 1448")
        )
            .setTitle(plain("Hijri"))
            .build()

        else -> null
    }
}

class PrayerTimeComplicationService : PrayerComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return null
        val model = model() ?: return noData()
        val index = PrayerTimeComplicationSettings.selectedIndex(
            this,
            request.complicationInstanceId
        )
        val entry = model.today.getOrNull(index) ?: return noData()
        val name = getString(entry.labelRes)
        val time = entry.time.format(ComplicationTimeFormat)
        return ShortTextComplicationData.Builder(
            text = plain(time),
            contentDescription = plain(
                getString(R.string.complication_prayer_time_description, name, time)
            )
        )
            .setTitle(plain(name))
            .setTapAction(tapAction())
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) {
            ShortTextComplicationData.Builder(
                text = plain("05:30"),
                contentDescription = plain("Fajr at 05:30")
            )
                .setTitle(plain("Fajr"))
                .build()
        } else {
            null
        }
}
