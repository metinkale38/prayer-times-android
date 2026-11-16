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
internal fun RowScope.PrayerTile(
    prayer: PrayerUi,
    modifier: Modifier,
    largeScreen: Boolean
) {
    val isSunrise = prayer.labelRes == R.string.sun
    val active = prayer.isCurrent && !isSunrise
    val next = prayer.isNext && !isSunrise && !active

    Card(
        modifier = modifier
            .height(if (largeScreen) 42.dp else 38.dp)
            .then(
                if (next) {
                    Modifier.border(1.dp, Highlight, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) Highlight else TileBackground,
            contentColor = if (active) Color.White else TextPrimary
        ),
        contentPadding = PaddingValues(
            horizontal = if (largeScreen) 5.dp else 4.dp,
            vertical = if (largeScreen) 4.dp else 3.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(prayer.labelRes),
                color = if (active) Color.White else TextSecondary,
                fontSize = if (largeScreen) 9.sp else 8.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = prayer.time,
                color = when {
                    active -> Color.White
                    isSunrise -> TextSecondary
                    else -> TextPrimary
                },
                fontSize = if (largeScreen) 15.sp else 14.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Light,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

