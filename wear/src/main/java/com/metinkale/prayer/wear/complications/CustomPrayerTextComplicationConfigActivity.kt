package com.metinkale.prayer.wear.complications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.metinkale.prayer.wear.R

class CustomPrayerTextComplicationConfigActivity : ComponentActivity() {
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
                PrayerTextConfig(instanceId)
            }
        }
    }

    @Composable
    private fun PrayerTextConfig(instanceId: Int) {
        PrayerTextOptionSelector(
            options = PrayerTextOption.entries,
            title = stringResource(R.string.complication_custom_name)
        ) { option ->
            PrayerTextSettings.setOption(this, instanceId, option)
            PrayerComplicationsUpdater.requestUpdate(
                this,
                CustomPrayerTextComplicationService::class.java,
                instanceId
            )
            setResult(RESULT_OK)
            finish()
        }
    }
}
