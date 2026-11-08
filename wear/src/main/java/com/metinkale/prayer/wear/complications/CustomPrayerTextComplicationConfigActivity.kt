package com.metinkale.prayer.wear.complications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import com.metinkale.prayer.wear.tiles.loadPrayerTileModel

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
                AppScaffold {
                    PrayerTextConfig(
                        instanceId = instanceId,
                        onDone = {
                            setResult(RESULT_OK)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTextConfig(instanceId: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<PrayerTileModel?>(null) }
    val state = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        model = loadPrayerTileModel(context)
    }

    ScreenScaffold(scrollState = state) { padding ->
        ScalingLazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.complication_custom_name),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(PrayerTextOption.entries) { option ->
                Card(
                    onClick = {
                        PrayerTextSettings.setOption(context, instanceId, option)
                        PrayerComplicationsUpdater.requestUpdate(
                            context,
                            CustomPrayerTextComplicationService::class.java,
                            instanceId
                        )
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = option.label(context),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = model?.let { formatPrayerTextOption(context, it, option) }
                            ?: stringResource(R.string.complication_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
