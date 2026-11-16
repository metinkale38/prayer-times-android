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

private val DhikrTargetTemplates = listOf(33, 50, 100, 200, 500, 1000)



@Composable
internal fun AddDhikrPage(
    onAdd: (String, Int) -> Unit,
    onCancel: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("33") }
    val defaultTitle = stringResource(R.string.dhikr)
    val parsedTarget = target.toIntOrNull()?.takeIf { it in 1..100_000 }
    val invalidTarget = target.isNotEmpty() && parsedTarget == null
    val canAdd = parsedTarget != null
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DhikrBackground)
    ) {
        CurvedEdgeLabel(
            text = stringResource(R.string.dhikr_new),
            alignTop = true,
            style = EdgeTitleStyle
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 26.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                val templateRows = DhikrTargetTemplates.chunked(3)
                templateRows.forEachIndexed { index, targets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        targets.forEach { value ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .background(DhikrTile, RoundedCornerShape(8.dp))
                                    .clickable { target = value.toString() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value.toString(),
                                    color = DhikrText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    if (index < templateRows.lastIndex) {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

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
                        Spacer(modifier = Modifier.height(1.dp))
                        CompactField(
                            value = title,
                            onValueChange = { if (it.length <= 20) title = it },
                            placeholder = defaultTitle,
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
                        Spacer(modifier = Modifier.height(1.dp))
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

            Text(
                text = stringResource(R.string.dhikr_cancel),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 12.dp, vertical = 1.dp),
                color = DhikrSecondary,
                fontSize = 9.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(30.dp)
                        .background(
                            if (canAdd) DhikrAccent else DhikrTile,
                            RoundedCornerShape(17.dp)
                        )
                        .clickable(enabled = canAdd) {
                            val count = parsedTarget ?: return@clickable
                            onAdd(title.trim().ifBlank { defaultTitle }, count)
                            title = ""
                            target = "33"
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
