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

@Composable
internal fun DhikrCard(
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

private fun dhikrAccent(dhikr: WearDhikr): Color =
    if (dhikr.color == 0) Color(DhikrDefaults.color) else Color(dhikr.color)
