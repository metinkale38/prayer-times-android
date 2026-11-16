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
import com.metinkale.prayer.wear.ui.KeepScreenOn
import com.metinkale.prayer.wear.ui.EdgeBodyStyle
import com.metinkale.prayer.wear.ui.EdgeTitleStyle
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DhikrPage(
    repository: DhikrRepository,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    KeepScreenOn(isActive)
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
