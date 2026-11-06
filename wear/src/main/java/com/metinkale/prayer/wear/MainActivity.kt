package com.metinkale.prayer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.VerticalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OpenOnPhoneDialogContent
import androidx.wear.compose.material3.OpenOnPhoneDialogDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.data.DhikrRepository
import com.metinkale.prayer.wear.data.WearTimesRepository
import com.metinkale.prayer.wear.dhikr.DhikrPage
import com.metinkale.prayer.wear.qibla.QiblaPage
import com.metinkale.prayer.wear.ui.CurvedEdgeLabel
import com.metinkale.prayer.wear.ui.EdgeTitleStyle
import com.metinkale.prayer.wear.sync.WearDhikrDataLayer
import com.metinkale.prayer.wear.sync.WearTimesDataLayer
import dev.metinkale.openprayertimes.hijri.HijriDate
import dev.metinkale.openprayertimes.hijri.HijriMonth
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate as KLocalDate

private val Background = Color(0xFF121212)
private val TileBackground = Color(0xFF242424)
private val Highlight = Color(0xFF4A768A)
private val HighlightBright = Color(0xFF7FB3C6)
private val IndicatorInactive = Color(0xFF6B757B)
private val TextPrimary = Color(0xFFF2F2F2)
private val TextSecondary = Color(0xFFA9B0B4)

private const val PAGE_COUNT = 10001
private const val TODAY_PAGE = 5000
private const val TOP_LEVEL_PAGE_COUNT = 3
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private data class PrayerUi(
    val labelRes: Int,
    val time: String,
    val isCurrent: Boolean,
    val isNext: Boolean
)

private data class PrayerTimes(
    val fajr: LocalTime,
    val sun: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val ishaa: LocalTime
) {
    fun asList(): List<LocalTime> = listOf(fajr, sun, dhuhr, asr, maghrib, ishaa)
}

private fun WearDayTimes.toPrayerTimes(): PrayerTimes? = runCatching {
    PrayerTimes(
        fajr = LocalTime.parse(fajr),
        sun = LocalTime.parse(sun),
        dhuhr = LocalTime.parse(dhuhr),
        asr = LocalTime.parse(asr),
        maghrib = LocalTime.parse(maghrib),
        ishaa = LocalTime.parse(ishaa)
    )
}.getOrNull()

private fun hijriMonthRes(month: HijriMonth): Int = when (month) {
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

private fun nextPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
    val nextIndex = times.asList().indexOfFirst { it > now }
    return if (nextIndex < 0) 0 else nextIndex
}

private fun currentPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
    if (now < times.fajr) return -1
    if (!now.isBefore(times.ishaa)) return 5
    val nextIndex = times.asList().indexOfFirst { it > now }
    return if (nextIndex <= 0) 5 else nextIndex - 1
}

private fun countdownText(times: PrayerTimes, now: LocalTime): String {
    val next = times.asList().firstOrNull { it > now } ?: times.fajr
    var seconds = Duration.between(now, next).seconds
    if (seconds < 0) seconds += 24 * 60 * 60

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
}

@Composable
private fun ScaledMaterialTheme(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val shortestDp = minOf(
        configuration.screenWidthDp,
        configuration.screenHeightDp
    ).toFloat()
    val scale = (shortestDp / 192f).coerceIn(1f, 1.25f)
    val scaledDensity = Density(
        density = baseDensity.density * scale,
        fontScale = baseDensity.fontScale
    )
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(content = content)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaledMaterialTheme {
                val context = LocalContext.current
                val repository = remember(context) { WearTimesRepository.get(context) }
                val dhikrRepository = remember(context) { DhikrRepository.get(context) }
                val payload by repository.payload.collectAsStateWithLifecycle(initialValue = null)
                val prayerScope = rememberCoroutineScope()
                var prayerSyncing by remember { mutableStateOf(false) }

                LaunchedEffect(repository) {
                    prayerSyncing = true
                    WearTimesDataLayer.requestSync(context)
                    WearTimesDataLayer.fetchExisting(context)?.let { repository.save(it) }
                    delay(500)
                    prayerSyncing = false
                }

                LaunchedEffect(dhikrRepository) {
                    dhikrRepository.ensureSyncIds()
                    WearDhikrDataLayer.fetchExisting(context)?.let { payload ->
                        dhikrRepository.mergeRemote(payload.dhikrs)
                    }
                }

                LaunchedEffect(dhikrRepository) {
                    dhikrRepository.allDhikrs.collectLatest { dhikrs ->
                        delay(500)
                        WearDhikrDataLayer.push(context, dhikrs)
                    }
                }

                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { TOP_LEVEL_PAGE_COUNT }
                )
                val currentPage = pagerState.currentPage

                Box(modifier = Modifier.fillMaxSize()) {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> PrayerTimesPage(
                                payload = payload,
                                isSyncing = prayerSyncing,
                                onRetry = {
                                    prayerScope.launch {
                                        prayerSyncing = true
                                        WearTimesDataLayer.requestSync(context)
                                        WearTimesDataLayer.fetchExisting(context)?.let { repository.save(it) }
                                        delay(500)
                                        prayerSyncing = false
                                    }
                                }
                            )
                            1 -> QiblaPage(
                                fallbackCity = payload?.city,
                                isActive = currentPage == 1,
                                modifier = Modifier.fillMaxSize()
                            )

                            2 -> DhikrPage(
                                repository = dhikrRepository,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    PageIndicator(
                        pageCount = TOP_LEVEL_PAGE_COUNT,
                        currentPage = currentPage,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (selected) 5.dp else 3.dp)
                    .background(
                        if (selected) HighlightBright else IndicatorInactive,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PrayerTimesPage(
    payload: WearTimesPayload?,
    isSyncing: Boolean,
    onRetry: () -> Unit
) {
    val syncedDays = remember(payload) {
        payload?.days?.associateBy { it.date } ?: emptyMap()
    }
    val baseDate = remember { LocalDate.now() }
    val hasData = payload != null && payload.days.isNotEmpty()
    val now = rememberCurrentTime()
    val baseTimes = remember(syncedDays, baseDate) {
        syncedDays[baseDate.toString()]?.toPrayerTimes()
    }
    val nextIsTomorrow = baseTimes != null && !now.isBefore(baseTimes.ishaa)
    val nextIndex = baseTimes?.let {
        if (nextIsTomorrow) 0 else nextPrayerIndex(it, now)
    } ?: -1
    val nextDate = baseTimes?.let {
        if (nextIsTomorrow) baseDate.plusDays(1) else baseDate
    }
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = TODAY_PAGE,
        pageCount = { PAGE_COUNT }
    )
    val largeScreen = LocalConfiguration.current.screenWidthDp >= 200

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        if (!hasData) {
            SetupOnPhoneContent(
                hasPayload = payload != null,
                isSyncing = isSyncing,
                onRetry = onRetry
            )
        } else {
            val dayOffset = page - TODAY_PAGE
            val date = baseDate.plusDays(dayOffset.toLong())

            DayCard(
                date = date,
                isToday = date == baseDate,
                now = if (date == baseDate) now else null,
                nextDate = nextDate,
                nextIndex = nextIndex,
                syncedDay = syncedDays[date.toString()],
                largeScreen = largeScreen,
                onBackToToday = {
                    scope.launch {
                        pagerState.animateScrollToPage(TODAY_PAGE)
                    }
                }
            )
        }
    }
}

@Composable
private fun SetupOnPhoneContent(
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

@Composable
private fun MissingDataCard(dateText: String, hijriText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dateText,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Light
        )
        Text(
            text = hijriText,
            color = TextSecondary.copy(alpha = 0.8f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.wear_no_data),
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    isToday: Boolean,
    now: LocalTime?,
    nextDate: LocalDate?,
    nextIndex: Int,
    syncedDay: WearDayTimes?,
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
    val countdown = if (isToday && now != null) countdownText(times, now) else null

    val prayers = listOf(
        PrayerUi(R.string.fajr, times.fajr.format(TimeFormat), currentIndex == 0, nextDate == date && nextIndex == 0),
        PrayerUi(R.string.sun, times.sun.format(TimeFormat), currentIndex == 1, nextDate == date && nextIndex == 1),
        PrayerUi(R.string.zuhr, times.dhuhr.format(TimeFormat), currentIndex == 2, nextDate == date && nextIndex == 2),
        PrayerUi(R.string.asr, times.asr.format(TimeFormat), currentIndex == 3, nextDate == date && nextIndex == 3),
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
    val bottomPadding = if (largeScreen) 16.dp else 24.dp
    val gridSpacing = if (largeScreen) 5.dp else 4.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        if (!isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .width(120.dp)
                    .height(44.dp)
                    .clickable(onClick = onBackToToday),
                contentAlignment = Alignment.TopCenter
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = topPadding,
                    bottom = bottomPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
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

            Spacer(modifier = Modifier.height(if (largeScreen) 3.dp else 2.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(if (largeScreen) 3.dp else 2.dp))

            if (isToday && countdown != null && nextPrayer != null) {
                Countdown(label = countdownLabel, text = countdown, largeScreen = largeScreen)
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun RowScope.PrayerTile(
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

@Composable
private fun Countdown(label: String, text: String, largeScreen: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = if (largeScreen) 16.sp else 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = if (largeScreen) 9.sp else 8.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun rememberCurrentTime(): LocalTime {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = LocalTime.now()
        }
    }
    return now
}
