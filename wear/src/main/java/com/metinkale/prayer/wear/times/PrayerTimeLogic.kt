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

internal fun hijriMonthRes(month: HijriMonth): Int = when (month) {
    HijriMonth.MUHARRAM -> R.string.hmonth1
    HijriMonth.SAFAR -> R.string.hmonth2
    HijriMonth.RABIAL_AWWAL -> R.string.hmonth3
    HijriMonth.RABIAL_AKHIR -> R.string.hmonth4
    HijriMonth.JUMADAAL_AWWAL -> R.string.hmonth5
    HijriMonth.JUMADAAL_AKHIR -> R.string.hmonth6
    HijriMonth.RAJAB -> R.string.hmonth7
    HijriMonth.SHABAN -> R.string.hmonth8
    HijriMonth.RAMADAN -> R.string.hmonth9
    HijriMonth.SHAWWAL -> R.string.hmonth10
    HijriMonth.DHUL_QADA -> R.string.hmonth11
    HijriMonth.DHUL_HIJJA -> R.string.hmonth12
}

internal fun nextPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
    val nextIndex = times.asList().indexOfFirst { it > now }
    return if (nextIndex < 0) 0 else nextIndex
}

internal fun currentPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
    if (now < times.fajr) return -1
    if (!now.isBefore(times.ishaa)) return 5
    val nextIndex = times.asList().indexOfFirst { it > now }
    return if (nextIndex <= 0) 5 else nextIndex - 1
}

internal fun countdownText(
    times: PrayerTimes,
    now: LocalTime,
    nextDayFajr: LocalTime?
): String {
    val next = times.asList().firstOrNull { it > now }
    val target = next ?: nextDayFajr ?: times.fajr
    var seconds = Duration.between(now, target).seconds
    if (next == null && seconds < 0) seconds += 24 * 60 * 60

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
}
