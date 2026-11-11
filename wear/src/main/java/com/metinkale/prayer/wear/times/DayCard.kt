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
internal fun DayCard(
    date: LocalDate,
    isToday: Boolean,
    now: LocalTime?,
    nextDate: LocalDate?,
    nextIndex: Int,
    syncedDay: WearDayTimes?,
    nextDay: WearDayTimes?,
    largeScreen: Boolean,
    onBackToToday: () -> Unit
) {
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateText = String.format(Locale.US, "%02d. %s %d", date.dayOfMonth, monthName, date.year)

    val hijri = runCatching {
        HijriDate.fromLocalDate(KLocalDate(date.year, date.monthValue, date.dayOfMonth))
    }.getOrNull()
    val hijriText = hijri?.let {
        "${it.day}. " + stringResource(hijriMonthRes(it.month)) + " ${it.year}"
    } ?: ""

    val times = syncedDay?.toPrayerTimes()
    if (times == null) {
        MissingDataCard(dateText, hijriText)
        return
    }

    val currentIndex = if (isToday && now != null) currentPrayerIndex(times, now) else -1
    val nextDayFajr = nextDay?.toPrayerTimes()?.fajr
    val countdown = if (isToday && now != null) {
        countdownText(times, now, nextDayFajr)
    } else {
        null
    }
    val alternate = isToday && now != null
    val alt = alternate && (now.second / 3) % 2 == 0
    val fajrLabel = if (times.sabah != null && alternate && !alt) R.string.imsak else R.string.fajr
    val fajrTime = if (times.sabah != null && alternate && alt) times.sabah else times.fajr
    val asrLabel = if (times.asrHanafi != null && alternate && alt) R.string.asrSani else R.string.asr
    val asrTime = if (times.asrHanafi != null && alternate && alt) times.asrHanafi else times.asr

    val prayers = listOf(
        PrayerUi(fajrLabel, fajrTime.format(TimeFormat), currentIndex == 0, nextDate == date && nextIndex == 0),
        PrayerUi(R.string.sun, times.sun.format(TimeFormat), currentIndex == 1, nextDate == date && nextIndex == 1),
        PrayerUi(R.string.zuhr, times.dhuhr.format(TimeFormat), currentIndex == 2, nextDate == date && nextIndex == 2),
        PrayerUi(asrLabel, asrTime.format(TimeFormat), currentIndex == 3, nextDate == date && nextIndex == 3),
        PrayerUi(R.string.maghrib, times.maghrib.format(TimeFormat), currentIndex == 4, nextDate == date && nextIndex == 4),
        PrayerUi(R.string.ishaa, times.ishaa.format(TimeFormat), currentIndex == 5, nextDate == date && nextIndex == 5)
    )
    val nextPrayer = prayers.getOrNull(nextIndex)
    val countdownLabel = if (isToday && nextPrayer != null) {
        stringResource(R.string.wear_until, stringResource(nextPrayer.labelRes))
    } else {
        ""
    }
    val horizontalPadding = if (largeScreen) 16.dp else 24.dp
    val topPadding = if (largeScreen) 22.dp else 28.dp
    val bottomPadding = if (largeScreen) 10.dp else 24.dp
    val gridSpacing = if (largeScreen) 5.dp else 4.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = topPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = dateText,
                color = TextPrimary,
                fontSize = if (largeScreen) 12.sp else 11.sp,
                lineHeight = if (largeScreen) 13.sp else 12.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = hijriText,
                color = TextSecondary,
                fontSize = if (largeScreen) 9.sp else 8.sp,
                lineHeight = if (largeScreen) 10.sp else 9.sp,
                fontWeight = FontWeight.Light
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                prayers.take(3).forEach { prayer ->
                    PrayerTile(prayer, Modifier.weight(1f), largeScreen)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                prayers.drop(3).forEach { prayer ->
                    PrayerTile(prayer, Modifier.weight(1f), largeScreen)
                }
            }
        }

        if (isToday && countdown != null && nextPrayer != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPadding),
                contentAlignment = Alignment.Center
            ) {
                Countdown(label = countdownLabel, text = countdown, largeScreen = largeScreen)
            }
        } else if (!isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .width(120.dp)
                    .height(38.dp)
                    .clickable(onClick = onBackToToday),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(66.dp)
                        .height(22.dp)
                        .background(TileBackground, RoundedCornerShape(50))
                        .border(1.dp, Highlight, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹ " + stringResource(R.string.wear_today),
                        color = HighlightBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
