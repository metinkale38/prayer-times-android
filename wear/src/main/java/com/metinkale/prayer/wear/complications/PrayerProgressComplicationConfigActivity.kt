package com.metinkale.prayer.wear.complications

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.metinkale.prayer.wear.R

internal val ProgressTextOptions = listOf(
    PrayerTextOption.CURRENT_PRAYER,
    PrayerTextOption.CURRENT_TIME,
    PrayerTextOption.CURRENT_NAME_TIME,
    PrayerTextOption.NEXT_PRAYER,
    PrayerTextOption.NEXT_TIME,
    PrayerTextOption.NEXT_NAME_TIME,
    PrayerTextOption.COUNTDOWN,
    PrayerTextOption.NEXT_NAME_COUNTDOWN,
    PrayerTextOption.COMBINED_TIMES,
    PrayerTextOption.COMBINED_COUNTDOWN
)

internal object PrayerProgressTextSettings {
    private const val PREFS = "prayer_progress_complications"

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

class PrayerProgressComplicationConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val instanceId = intent.getIntExtra(
            ComplicationDataSourceService.EXTRA_CONFIG_COMPLICATION_ID,
            -1
        )
        if (instanceId < 0) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                PrayerProgressConfig(instanceId)
            }
        }
    }

    @Composable
    private fun PrayerProgressConfig(instanceId: Int) {
        PrayerTextOptionSelector(
            options = ProgressTextOptions,
            title = stringResource(R.string.complication_progress_name)
        ) { option ->
            PrayerProgressTextSettings.setOption(this, instanceId, option)
            PrayerComplicationsUpdater.requestUpdate(
                this,
                PrayerProgressComplicationService::class.java,
                instanceId
            )
            setResult(RESULT_OK)
            finish()
        }
    }
}
