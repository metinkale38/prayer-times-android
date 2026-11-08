package com.metinkale.prayer.wear.complications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.tiles.PrayerTileModel
import com.metinkale.prayer.wear.tiles.loadPrayerTileModel

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

private fun sampleValues(): PrayerTextValues =
    PrayerTextValues("Ishaa", "18:22", "Fajr", "03:44", "2h 2m")

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
        val type = runCatching {
            ComplicationType.fromWireType(
                intent.getIntExtra(
                    ComplicationDataSourceService.EXTRA_CONFIG_COMPLICATION_TYPE,
                    -1
                )
            )
        }.getOrNull()

        setContent {
            MaterialTheme {
                AppScaffold {
                    PrayerTextConfig(
                        instanceId = instanceId,
                        type = type,
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
private fun PrayerTextConfig(
    instanceId: Int,
    type: ComplicationType?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<PrayerTileModel?>(null) }
    var tokens by remember {
        mutableStateOf(CustomPrayerTextSettings.tokens(context, instanceId))
    }
    var removedToken by remember {
        mutableStateOf<Pair<Int, PrayerTextToken>?>(null)
    }
    var showBuilder by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        model = loadPrayerTileModel(context)
    }

    BackHandler(enabled = showBuilder) {
        showBuilder = false
    }

    fun apply(newTokens: List<PrayerTextToken>) {
        CustomPrayerTextSettings.setTokens(context, instanceId, newTokens)
        PrayerComplicationsUpdater.requestUpdate(
            context,
            CustomPrayerTextComplicationService::class.java,
            instanceId
        )
    }

    val values = model?.let { prayerTextValues(context, it) } ?: sampleValues()

    if (showBuilder) {
        BuilderScreen(
            tokens = tokens,
            values = values,
            type = type,
            onToken = {
                tokens = tokens + it
                removedToken = null
            },
            onRemove = { index ->
                removedToken = index to tokens[index]
                tokens = tokens.filterIndexed { i, _ -> i != index }
            },
            canUndoRemove = removedToken != null,
            onUndoRemove = {
                removedToken?.let { (index, token) ->
                    tokens = tokens.toMutableList().apply {
                        add(index.coerceIn(0, size), token)
                    }
                }
                removedToken = null
            },
            onClear = {
                tokens = emptyList()
                removedToken = null
            },
            onDone = {
                if (tokens.isNotEmpty()) {
                    apply(tokens)
                    onDone()
                }
            }
        )
    } else {
        TemplateScreen(
            values = values,
            type = type,
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
    values: PrayerTextValues,
    type: ComplicationType?,
    onTemplate: (PrayerTextTemplate) -> Unit,
    onCustom: () -> Unit
) {
    val context = LocalContext.current
    val state = rememberScalingLazyListState()
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
                    text = stringResource(R.string.complication_custom_config_title),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            item {
                Text(
                    text = stringResource(R.string.complication_templates),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(Templates) { _, template ->
                Card(
                    onClick = { onTemplate(template) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(template.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (type != ComplicationType.LONG_TEXT) {
                        Text(
                            text = "${stringResource(R.string.complication_short_preview)}: " +
                                    formatShortPrayerText(template.tokens, values),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (type != ComplicationType.SHORT_TEXT) {
                        Text(
                            text = "${stringResource(R.string.complication_long_preview)}: " +
                                    formatPrayerText(context, template.tokens, values),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            item {
                FilledTonalButton(
                    onClick = onCustom,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.complication_custom),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BuilderScreen(
    tokens: List<PrayerTextToken>,
    values: PrayerTextValues,
    type: ComplicationType?,
    onToken: (PrayerTextToken) -> Unit,
    onRemove: (Int) -> Unit,
    canUndoRemove: Boolean,
    onUndoRemove: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val shortText = formatShortPrayerText(tokens, values)
    val longText = formatPrayerText(context, tokens, values)
    val state = rememberScalingLazyListState()
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
                    text = stringResource(R.string.complication_custom_config_title),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            if (type != ComplicationType.LONG_TEXT) {
                item {
                    PreviewCard(
                        label = stringResource(R.string.complication_short_preview),
                        text = shortText.ifEmpty { stringResource(R.string.complication_empty) },
                        maxLines = 1
                    )
                }
            }
            if (type != ComplicationType.SHORT_TEXT) {
                item {
                    PreviewCard(
                        label = stringResource(R.string.complication_long_preview),
                        text = longText.ifEmpty { stringResource(R.string.complication_empty) },
                        maxLines = 2
                    )
                }
            }
            item {
                FilledTonalButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tokens.isNotEmpty(),
                    label = {
                        Text(
                            text = stringResource(R.string.complication_done),
                            maxLines = 1
                        )
                    }
                )
            }
            item {
                Text(
                    text = stringResource(R.string.complication_selected),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(tokens) { index, token ->
                val label = stringResource(token.labelRes)
                val removeLabel = stringResource(R.string.complication_remove_token, label)
                OutlinedButton(
                    onClick = { onRemove(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = removeLabel },
                    label = {
                        Text(
                            text = "× $label",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            item {
                Button(
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tokens.isNotEmpty(),
                    label = {
                        Text(
                            text = stringResource(R.string.complication_clear),
                            maxLines = 1
                        )
                    }
                )
            }
            item {
                Button(
                    onClick = onUndoRemove,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canUndoRemove,
                    label = {
                        Text(
                            text = stringResource(R.string.complication_undo),
                            maxLines = 1
                        )
                    }
                )
            }
            item {
                Text(
                    text = stringResource(R.string.complication_blocks),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(PrayerTextToken.entries) { token ->
                Button(
                    onClick = { onToken(token) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = stringResource(token.labelRes), maxLines = 1)
                    }
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(label: String, text: String, maxLines: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}
