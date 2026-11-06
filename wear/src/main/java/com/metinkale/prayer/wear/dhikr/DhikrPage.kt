package com.metinkale.prayer.wear.dhikr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Text
import com.metinkale.prayer.sync.DhikrDefaults
import com.metinkale.prayer.sync.WearDhikr
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.data.DhikrRepository
import com.metinkale.prayer.wear.ui.CurvedEdgeLabel
import com.metinkale.prayer.wear.ui.EdgeBodyStyle
import com.metinkale.prayer.wear.ui.EdgeTitleStyle
import kotlin.math.min
import kotlinx.coroutines.launch

private val DhikrBackground = Color(0xFF121212)
private val DhikrTile = Color(0xFF242424)
private val DhikrText = Color(0xFFF2F2F2)
private val DhikrSecondary = Color(0xFFA9B0B4)
private val DhikrAccent = Color(0xFF3E6B7A)

private data class DhikrPreset(val title: String, val target: Int)

private val DhikrPresets = listOf(
    DhikrPreset("Tesbih", 33),
    DhikrPreset("Salawat", 100),
    DhikrPreset("Istighfar", 100),
    DhikrPreset("SubhanAllah", 33),
    DhikrPreset("Alhamdulillah", 33),
    DhikrPreset("Allahu Akbar", 34)
)

@Composable
fun DhikrPage(repository: DhikrRepository, modifier: Modifier = Modifier) {
    val dhikrs by repository.dhikrs.collectAsStateWithLifecycle(initialValue = emptyList())
    val pagerState = rememberPagerState(pageCount = { dhikrs.size + 1 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(dhikrs.size) {
        if (pagerState.currentPage > dhikrs.size) {
            pagerState.scrollToPage(dhikrs.size)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        if (page < dhikrs.size) {
            val dhikr = dhikrs[page]
            DhikrCard(
                dhikr = dhikr,
                onIncrement = { scope.launch { repository.increment(dhikr.id) } },
                onReset = { scope.launch { repository.reset(dhikr.id) } },
                onDelete = { scope.launch { repository.delete(dhikr.id) } }
            )
        } else {
            AddDhikrPage(
                onAdd = { title, target ->
                    val newIndex = dhikrs.size
                    scope.launch {
                        repository.add(title, target)
                        pagerState.animateScrollToPage(newIndex)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DhikrCard(
    dhikr: WearDhikr,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val target = dhikr.target.coerceAtLeast(1)
    val current = dhikr.count % target
    val rounds = dhikr.count / target
    val accent = dhikrAccent(dhikr)
    val progress = current.toFloat() / target

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DhikrBackground)
            .combinedClickable(
                onClick = {
                    if ((dhikr.count + 1) % target == 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onIncrement()
                },
                onLongClick = { showActions = true }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val compassCenter = min(size.width, size.height) / 2f
            val radius = (compassCenter - 24.dp.toPx()).coerceAtLeast(1f)
            val faceRadius = radius * 19f / 20f
            val ringStroke = compassCenter / 15f
            val topLeft = Offset(center.x - faceRadius, center.y - faceRadius)
            val arcSize = Size(faceRadius * 2f, faceRadius * 2f)

            drawArc(
                color = accent.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            if (progress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current.toString(),
                color = DhikrText,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "/ $target",
                color = DhikrSecondary,
                fontSize = 18.sp
            )
            if (rounds > 0) {
                Text(
                    text = stringResource(R.string.dhikr_rounds, rounds),
                    color = DhikrSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.dhikr_tap_hint),
                color = DhikrSecondary,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        CurvedEdgeLabel(
            text = dhikr.title,
            alignTop = true,
            style = EdgeTitleStyle
        )

        CurvedEdgeLabel(
            text = stringResource(R.string.dhikr_hold_hint),
            alignTop = false,
            style = EdgeBodyStyle
        )
    }

    if (showActions) {
        DhikrActionsDialog(
            dhikr = dhikr,
            onReset = onReset,
            onDelete = onDelete,
            onDismiss = { showActions = false }
        )
    }
}

@Composable
private fun DhikrActionsDialog(
    dhikr: WearDhikr,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DhikrTile, RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dhikr.title,
                color = DhikrText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            ActionRow(
                text = stringResource(R.string.dhikr_reset),
                color = DhikrText,
                onClick = {
                    onReset()
                    onDismiss()
                }
            )
            ActionRow(
                text = stringResource(R.string.dhikr_delete),
                color = Color(0xFFFF8A80),
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
            ActionRow(
                text = stringResource(R.string.dhikr_cancel),
                color = DhikrSecondary,
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun ActionRow(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        color = color,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AddDhikrPage(onAdd: (String, Int) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("33") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DhikrBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.dhikr_new),
            color = DhikrText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        DhikrPresets.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(DhikrTile, RoundedCornerShape(10.dp))
                            .clickable {
                                title = preset.title
                                target = preset.target.toString()
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset.title} · ${preset.target}",
                            color = DhikrText,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        FieldLabel(text = stringResource(R.string.dhikr_name))
        BasicTextField(
            value = title,
            onValueChange = { if (it.length <= 20) title = it },
            singleLine = true,
            textStyle = TextStyle(color = DhikrText, fontSize = 15.sp),
            cursorBrush = SolidColor(DhikrAccent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DhikrTile, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.dhikr_name_hint),
                            color = DhikrSecondary,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        FieldLabel(text = stringResource(R.string.dhikr_target))
        BasicTextField(
            value = target,
            onValueChange = { value ->
                if (value.length <= 5 && value.all(Char::isDigit)) target = value
            },
            singleLine = true,
            textStyle = TextStyle(color = DhikrText, fontSize = 15.sp),
            cursorBrush = SolidColor(DhikrAccent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DhikrTile, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    if (target.isEmpty()) {
                        Text(
                            text = stringResource(R.string.dhikr_target_hint),
                            color = DhikrSecondary,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val canAdd = title.isNotBlank()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (canAdd) DhikrAccent else DhikrTile,
                    RoundedCornerShape(12.dp)
                )
                .clickable(enabled = canAdd) {
                    val count = target.toIntOrNull()?.coerceIn(1, 100_000) ?: 33
                    onAdd(title.trim(), count)
                    title = ""
                    target = "33"
                }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.dhikr_add),
                color = DhikrText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        color = DhikrSecondary,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(3.dp))
}

private fun dhikrAccent(dhikr: WearDhikr): Color =
    if (dhikr.color == 0) Color(DhikrDefaults.color) else Color(dhikr.color)
