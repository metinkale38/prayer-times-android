package com.metinkale.prayer.wear.dhikr

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DhikrBackground = Color(0xFF121212)
private val DhikrTile = Color(0xFF242424)
private val DhikrText = Color(0xFFF2F2F2)
private val DhikrSecondary = Color(0xFFA9B0B4)
private val DhikrAccent = Color(0xFF4A768A)
private val DhikrAccentBright = Color(0xFF7FB3C6)
private val DhikrIndicatorInactive = Color(0xFF6B757B)

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

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = pagerState.currentPage < dhikrs.size
        ) { page ->
            if (page < dhikrs.size) {
                val dhikr = dhikrs[page]
                DhikrCard(
                    dhikr = dhikr,
                    onIncrement = { scope.launch { repository.increment(dhikr.syncId) } },
                    onReset = { scope.launch { repository.reset(dhikr.syncId) } },
                    onDelete = { scope.launch { repository.delete(dhikr.syncId) } }
                )
            } else {
                AddDhikrPage(
                    onAdd = { title, target ->
                        val newIndex = dhikrs.size
                        scope.launch {
                            repository.add(title, target)
                            pagerState.animateScrollToPage(newIndex)
                        }
                    },
                    onCancel = {
                        scope.launch {
                            pagerState.animateScrollToPage((dhikrs.size - 1).coerceAtLeast(0))
                        }
                    }
                )
            }
        }

        DhikrSubPageIndicator(
            pageCount = dhikrs.size + 1,
            dhikrCount = dhikrs.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp)
        )
    }
}

@Composable
private fun DhikrSubPageIndicator(
    pageCount: Int,
    dhikrCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 4) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val selected = index == currentPage
                if (index == dhikrCount) {
                    Text(
                        text = "+",
                        color = if (selected) DhikrAccentBright else DhikrIndicatorInactive,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (selected) 4.dp else 3.dp)
                            .background(
                                if (selected) DhikrAccentBright else DhikrIndicatorInactive,
                                CircleShape
                            )
                    )
                }
            }
        }
    } else {
        Text(
            text = if (currentPage >= dhikrCount) {
                "+"
            } else {
                "${currentPage + 1}/$dhikrCount +"
            },
            modifier = modifier,
            color = DhikrSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "dhikrPress"
    )
    val target = dhikr.target.coerceAtLeast(1)
    val current = dhikr.count % target
    val rounds = dhikr.count / target
    val accent = dhikrAccent(dhikr)
    val progress = current.toFloat() / target

    var lastRounds by remember { mutableStateOf(rounds) }
    var roundPulse by remember { mutableStateOf(false) }
    LaunchedEffect(dhikr.count, target) {
        val newRounds = dhikr.count / target
        if (newRounds > lastRounds) {
            roundPulse = true
            delay(350)
            roundPulse = false
        }
        lastRounds = newRounds
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(DhikrBackground)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
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

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF232323), Color(0xFF171717)),
                    center = center,
                    radius = faceRadius
                ),
                radius = faceRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF2C3A40),
                radius = faceRadius - ringStroke / 2f + 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            drawArc(
                color = accent.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            val displayProgress = if (roundPulse) 1f else progress
            drawCircle(
                color = accent.copy(alpha = 0.22f),
                radius = faceRadius - ringStroke / 2f + 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            if (displayProgress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * displayProgress.coerceIn(0f, 1f),
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
                fontSize = 48.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = "/ $target",
                color = DhikrSecondary,
                fontSize = 16.sp
            )
            if (rounds > 0) {
                Text(
                    text = stringResource(R.string.dhikr_round, rounds),
                    color = if (roundPulse) accent else DhikrSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DhikrBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
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
private fun AddDhikrPage(
    onAdd: (String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var customMode by rememberSaveable { mutableStateOf(false) }
    var selectedPreset by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("33") }
    val selected = DhikrPresets.firstOrNull { it.title == selectedPreset }
    val parsedTarget = target.toIntOrNull()?.takeIf { it in 1..100_000 }
    val invalidTarget = customMode && target.isNotEmpty() && parsedTarget == null
    val canAdd = if (customMode) {
        title.isNotBlank() && parsedTarget != null
    } else {
        selected != null
    }
    val scrollState = rememberScrollState()

    LaunchedEffect(customMode) {
        scrollState.scrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DhikrBackground)
    ) {
        CurvedEdgeLabel(
            text = stringResource(if (customMode) R.string.dhikr_custom else R.string.dhikr_new),
            alignTop = true,
            style = EdgeTitleStyle
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 30.dp)
        ) {
            if (!customMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dhikr_templates),
                        color = DhikrSecondary,
                        fontSize = 9.sp
                    )
                    Text(
                        text = stringResource(R.string.dhikr_custom),
                        modifier = Modifier.clickable {
                            customMode = true
                            selectedPreset = null
                            title = ""
                            target = "33"
                        },
                        color = DhikrAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                if (!customMode) {
                    DhikrPresets.forEach { preset ->
                        val isSelected = selectedPreset == preset.title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    if (isSelected) DhikrAccent else DhikrTile,
                                    RoundedCornerShape(9.dp)
                                )
                                .clickable {
                                    selectedPreset = preset.title
                                    title = preset.title
                                    target = preset.target.toString()
                                }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                modifier = Modifier.weight(1f),
                                color = if (isSelected) Color.White else DhikrText,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = preset.target.toString(),
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else DhikrSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(
                                text = stringResource(R.string.dhikr_name),
                                color = DhikrSecondary,
                                fontSize = 7.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            CompactField(
                                value = title,
                                onValueChange = { if (it.length <= 20) title = it },
                                placeholder = stringResource(R.string.dhikr_name_hint),
                                keyboardType = KeyboardType.Text,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.dhikr_target),
                                color = DhikrSecondary,
                                fontSize = 7.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            CompactField(
                                value = target,
                                onValueChange = { value ->
                                    if (value.length <= 5 && value.all(Char::isDigit)) target = value
                                },
                                placeholder = stringResource(R.string.dhikr_target_hint),
                                keyboardType = KeyboardType.Number,
                                invalid = invalidTarget,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (invalidTarget) {
                        Text(
                            text = stringResource(R.string.dhikr_invalid_target),
                            color = Color(0xFFFF8A80),
                            fontSize = 8.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.dhikr_cancel),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        if (customMode) {
                            customMode = false
                        } else {
                            onCancel()
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = DhikrSecondary,
                fontSize = 9.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(32.dp)
                        .background(
                            if (canAdd) DhikrAccent else DhikrTile,
                            RoundedCornerShape(17.dp)
                        )
                        .clickable(enabled = canAdd) {
                            if (customMode) {
                                val count = parsedTarget ?: return@clickable
                                onAdd(title.trim(), count)
                                title = ""
                                target = "33"
                            } else {
                                val preset = selected ?: return@clickable
                                onAdd(preset.title, preset.target)
                                selectedPreset = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dhikr_add),
                        color = if (canAdd) Color.White else DhikrSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    invalid: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = DhikrText, fontSize = 13.sp),
        cursorBrush = SolidColor(DhikrAccent),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(DhikrTile, RoundedCornerShape(9.dp))
                    .then(
                        if (invalid) {
                            Modifier.border(1.dp, Color(0xFFFF8A80), RoundedCornerShape(9.dp))
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 9.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = DhikrSecondary,
                        fontSize = 12.sp
                    )
                }
                innerTextField()
            }
        }
    )
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
