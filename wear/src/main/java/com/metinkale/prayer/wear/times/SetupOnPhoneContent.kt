package com.metinkale.prayer.wear.times

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Text
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.ui.Background
import com.metinkale.prayer.wear.ui.CurvedEdgeLabel
import com.metinkale.prayer.wear.ui.EdgeTitleStyle
import com.metinkale.prayer.wear.ui.Highlight
import com.metinkale.prayer.wear.ui.HighlightBright
import com.metinkale.prayer.wear.ui.TextPrimary
import com.metinkale.prayer.wear.ui.TextSecondary
import com.metinkale.prayer.wear.ui.TileBackground
import dev.metinkale.openprayertimes.hijri.HijriDate
import dev.metinkale.openprayertimes.hijri.HijriMonth
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate as KLocalDate

@Composable
internal fun SetupOnPhoneContent(
    hasPayload: Boolean,
    isSyncing: Boolean,
    onRetry: () -> Unit
) {
    val sectionTitle = stringResource(R.string.wear_prayer_times)
    val headline = stringResource(
        if (hasPayload) R.string.wear_no_times_title else R.string.wear_no_connection_title
    )
    val body = stringResource(
        if (hasPayload) R.string.wear_no_times_body else R.string.wear_no_connection_body
    )
    val action = stringResource(
        if (isSyncing) R.string.wear_syncing else R.string.wear_sync_action
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        CurvedEdgeLabel(
            text = sectionTitle,
            alignTop = true,
            style = EdgeTitleStyle
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Highlight.copy(alpha = 0.22f),
                        radius = size.minDimension / 2f - 2.dp.toPx(),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = headline,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                color = TextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(30.dp)
                    .background(
                        if (isSyncing) TileBackground else Highlight,
                        RoundedCornerShape(15.dp)
                    )
                    .clickable(enabled = !isSyncing, onClick = onRetry),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action,
                    color = if (isSyncing) TextSecondary else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

