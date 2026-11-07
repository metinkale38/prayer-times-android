package com.metinkale.prayer.wear.complications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.metinkale.prayer.wear.R

private val ConfigBackground = Color(0xFF121212)
private val ConfigTile = Color(0xFF242424)
private val ConfigText = Color(0xFFF2F2F2)
private val ConfigAccent = Color(0xFF4A768A)

private val PrayerIndexes = listOf(
    R.string.fajr,
    R.string.sun,
    R.string.zuhr,
    R.string.asr,
    R.string.maghrib,
    R.string.ishaa
)

class PrayerTimeComplicationConfigActivity : ComponentActivity() {
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
                PrayerSelector(
                    instanceId = instanceId,
                    onSelected = { index ->
                        PrayerTimeComplicationSettings.setSelectedIndex(this, instanceId, index)
                        PrayerComplicationsUpdater.requestUpdate(
                            this,
                            PrayerTimeComplicationService::class.java,
                            instanceId
                        )
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun PrayerSelector(instanceId: Int, onSelected: (Int) -> Unit) {
    val context = LocalContext.current
    var selected by remember {
        mutableIntStateOf(PrayerTimeComplicationSettings.selectedIndex(context, instanceId))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ConfigBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.complication_prayer_time_config_title),
            color = ConfigText,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        PrayerIndexes.forEachIndexed { index, labelRes ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(
                        if (index == selected) ConfigAccent else ConfigTile,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        selected = index
                        onSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(labelRes),
                    color = if (index == selected) Color.White else ConfigText,
                    fontSize = 12.sp
                )
            }
        }
    }
}
