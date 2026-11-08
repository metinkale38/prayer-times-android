package com.metinkale.prayer.wear.complications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
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
private val ConfigAccent = Color(0xFF4A768A)
private val ConfigText = Color(0xFFF2F2F2)

private data class PrayerTextTemplate(
    val labelRes: Int,
    val tokens: List<PrayerTextToken>
)

private val Templates = listOf(
    PrayerTextTemplate(
        R.string.complication_template_current_next,
        listOf(
            PrayerTextToken.CURRENT_NAME,
            PrayerTextToken.CURRENT_TIME,
            PrayerTextToken.SEPARATOR,
            PrayerTextToken.NEXT_NAME,
            PrayerTextToken.NEXT_TIME
        )
    ),
    PrayerTextTemplate(
        R.string.complication_template_countdown,
        listOf(
            PrayerTextToken.CURRENT_NAME,
            PrayerTextToken.SEPARATOR,
            PrayerTextToken.COUNTDOWN,
            PrayerTextToken.UNTIL,
            PrayerTextToken.NEXT_NAME
        )
    ),
    PrayerTextTemplate(
        R.string.complication_template_current,
        listOf(PrayerTextToken.CURRENT_NAME, PrayerTextToken.CURRENT_TIME)
    ),
    PrayerTextTemplate(
        R.string.complication_template_next,
        listOf(PrayerTextToken.NEXT_NAME, PrayerTextToken.NEXT_TIME)
    ),
    PrayerTextTemplate(
        R.string.complication_template_counter,
        listOf(PrayerTextToken.COUNTDOWN)
    ),
    PrayerTextTemplate(
        R.string.complication_template_current_time,
        listOf(PrayerTextToken.CURRENT_TIME)
    ),
    PrayerTextTemplate(
        R.string.complication_template_next_time,
        listOf(PrayerTextToken.NEXT_TIME)
    )
)

private val PrayerTextToken.labelRes: Int
    get() = when (this) {
        PrayerTextToken.CURRENT_NAME -> R.string.complication_token_current_name
        PrayerTextToken.NEXT_NAME -> R.string.complication_token_next_name
        PrayerTextToken.CURRENT_TIME -> R.string.complication_token_current_time
        PrayerTextToken.NEXT_TIME -> R.string.complication_token_next_time
        PrayerTextToken.COUNTDOWN -> R.string.complication_token_counter
        PrayerTextToken.SEPARATOR -> R.string.complication_token_separator
        PrayerTextToken.UNTIL -> R.string.complication_until
    }

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
                PrayerTextBuilder(
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

@Composable
private fun PrayerTextBuilder(instanceId: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    var tokens by remember {
        mutableStateOf(CustomPrayerTextSettings.tokens(context, instanceId))
    }

    fun apply(newTokens: List<PrayerTextToken>) {
        tokens = newTokens
        CustomPrayerTextSettings.setTokens(context, instanceId, newTokens)
        PrayerComplicationsUpdater.requestUpdate(
            context,
            CustomPrayerTextComplicationService::class.java,
            instanceId
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ConfigBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = stringResource(R.string.complication_custom_config_title),
            color = ConfigText,
            fontSize = 12.sp
        )

        Text(
            text = stringResource(R.string.complication_templates),
            color = ConfigText,
            fontSize = 10.sp
        )
        Templates.forEach { template ->
            BuilderButton(stringResource(template.labelRes)) {
                apply(template.tokens)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.complication_blocks),
            color = ConfigText,
            fontSize = 10.sp
        )
        PrayerTextToken.entries.forEach { token ->
            BuilderButton(stringResource(token.labelRes)) {
                apply(tokens + token)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.complication_preview),
            color = ConfigText,
            fontSize = 10.sp
        )
        Text(
            text = tokens.map { stringResource(it.labelRes) }.joinToString(" "),
            color = ConfigAccent,
            fontSize = 10.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .background(ConfigTile, RoundedCornerShape(8.dp))
                    .clickable { apply(emptyList()) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.complication_clear),
                    color = ConfigText,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .background(ConfigAccent, RoundedCornerShape(8.dp))
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.complication_done),
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun BuilderButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(ConfigTile, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = ConfigText, fontSize = 10.sp)
    }
}
