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
import androidx.compose.runtime.LaunchedEffect
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
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import com.metinkale.prayer.wear.tiles.loadPrayerTileModel

private val ConfigBackground = Color(0xFF121212)
private val ConfigTile = Color(0xFF242424)
private val ConfigAccent = Color(0xFF4A768A)
private val ConfigText = Color(0xFFF2F2F2)

private data class PrayerTextTemplate(
    val tokens: List<PrayerTextToken>,
    val fallback: String
)

private val Templates = listOf(
    PrayerTextTemplate(
        listOf(
            PrayerTextToken.CURRENT_NAME,
            PrayerTextToken.CURRENT_TIME,
            PrayerTextToken.SEPARATOR,
            PrayerTextToken.NEXT_NAME,
            PrayerTextToken.NEXT_TIME
        ),
        "Ishaa 18:22 | Fajr 03:44"
    ),
    PrayerTextTemplate(
        listOf(
            PrayerTextToken.CURRENT_NAME,
            PrayerTextToken.SEPARATOR,
            PrayerTextToken.COUNTDOWN,
            PrayerTextToken.UNTIL,
            PrayerTextToken.NEXT_NAME
        ),
        "Ishaa | 2h 2m until Fajr"
    ),
    PrayerTextTemplate(
        listOf(PrayerTextToken.CURRENT_NAME, PrayerTextToken.CURRENT_TIME),
        "Ishaa 18:22"
    ),
    PrayerTextTemplate(
        listOf(PrayerTextToken.NEXT_NAME, PrayerTextToken.NEXT_TIME),
        "Fajr 03:44"
    ),
    PrayerTextTemplate(
        listOf(PrayerTextToken.COUNTDOWN),
        "2h 2m"
    ),
    PrayerTextTemplate(
        listOf(PrayerTextToken.CURRENT_TIME),
        "18:22"
    ),
    PrayerTextTemplate(
        listOf(PrayerTextToken.NEXT_TIME),
        "03:44"
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

@Composable
private fun PrayerTextConfig(instanceId: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<PrayerTileModel?>(null) }
    var tokens by remember {
        mutableStateOf(CustomPrayerTextSettings.tokens(context, instanceId))
    }
    var showBuilder by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        model = loadPrayerTileModel(context)
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

    fun preview(template: PrayerTextTemplate): String =
        model?.let { buildPrayerText(context, it, template.tokens) } ?: template.fallback

    fun livePreview(): String =
        model?.let { buildPrayerText(context, it, tokens) } ?: "Ishaa 18:22 | Fajr 03:44"

    if (showBuilder) {
        BuilderScreen(
            tokens = tokens,
            preview = livePreview(),
            onToken = { apply(tokens + it) },
            onClear = { apply(emptyList()) },
            onBack = { showBuilder = false },
            onDone = onDone
        )
    } else {
        TemplateScreen(
            previews = Templates.map { preview(it) },
            onTemplate = {
                apply(it.tokens)
                onDone()
            },
            onCustom = { showBuilder = true }
        )
    }
}

@Composable
private fun TemplateScreen(
    previews: List<String>,
    onTemplate: (PrayerTextTemplate) -> Unit,
    onCustom: () -> Unit
) {
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
        Templates.forEachIndexed { index, template ->
            BuilderButton(previews[index]) { onTemplate(template) }
        }
        Spacer(Modifier.height(4.dp))
        BuilderButton(stringResource(R.string.complication_custom), onClick = onCustom)
    }
}

@Composable
private fun BuilderScreen(
    tokens: List<PrayerTextToken>,
    preview: String,
    onToken: (PrayerTextToken) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
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
            text = stringResource(R.string.complication_preview),
            color = ConfigText,
            fontSize = 10.sp
        )
        Text(
            text = preview,
            color = ConfigAccent,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.complication_blocks),
            color = ConfigText,
            fontSize = 10.sp
        )
        PrayerTextToken.entries.forEach { token ->
            BuilderButton(stringResource(token.labelRes)) { onToken(token) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ActionButton(stringResource(R.string.complication_back), Modifier.weight(1f), onBack)
            ActionButton(stringResource(R.string.complication_clear), Modifier.weight(1f), onClear)
            ActionButton(
                stringResource(R.string.complication_done),
                Modifier.weight(1f),
                onDone,
                accent = true
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    accent: Boolean = false
) {
    Box(
        modifier = modifier
            .height(26.dp)
            .background(if (accent) ConfigAccent else ConfigTile, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (accent) Color.White else ConfigText,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun BuilderButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(ConfigTile, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = ConfigText, fontSize = 10.sp)
    }
}
