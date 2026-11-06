package com.metinkale.prayer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.wear.data.WearTimesRepository
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
import kotlinx.datetime.LocalDate as KLocalDate

private val Background = Color(0xFF121212)
private val TileBackground = Color(0xFF242424)
private val Highlight = Color(0xFF3E6B7A)
private val TextPrimary = Color(0xFFF2F2F2)
private val TextSecondary = Color(0xFFA9B0B4)

private const val PAGE_COUNT = 10001
private const val TODAY_PAGE = 5000
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private data class PrayerUi(
    val labelRes: Int,
    val time: String,
    val isCurrent: Boolean
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

private fun mockPrayerCycle(): List<PrayerTimes> = (0 until 30).map { index ->
    val offset = index - 4
    PrayerTimes(
        fajr = LocalTime.of(5, 24).plusMinutes((offset / 3).toLong()),
        sun = LocalTime.of(7, 13).plusMinutes((offset / 3).toLong()),
        dhuhr = LocalTime.of(12, 7).plusMinutes((offset / 5).toLong()),
        asr = LocalTime.of(14, 20).plusMinutes((offset / 4).toLong()),
        maghrib = LocalTime.of(16, 50).minusMinutes((offset / 4).toLong()),
        ishaa = LocalTime.of(18, 26).minusMinutes((offset / 3).toLong())
    )
}

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

private fun currentPrayerIndex(times: PrayerTimes, now: LocalTime): Int {
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val repository = remember(context) { WearTimesRepository.get(context) }
                val payload by repository.payload.collectAsStateWithLifecycle(initialValue = null)

                LaunchedEffect(repository) {
                    WearTimesDataLayer.fetchExisting(context)?.let { repository.save(it) }
                }

                val syncedDays = remember(payload) {
                    payload?.days?.associateBy { it.date } ?: emptyMap()
                }
                val baseDate = remember { LocalDate.now() }
                val prayerCycle = remember { mockPrayerCycle() }
                val pagerState = rememberPagerState(
                    initialPage = TODAY_PAGE,
                    pageCount = { PAGE_COUNT }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val dayOffset = page - TODAY_PAGE
                    val date = baseDate.plusDays(dayOffset.toLong())
                    val cycleIndex = ((dayOffset + 4) % 30 + 30) % 30

                    DayCard(
                        date = date,
                        isToday = date == baseDate,
                        fallbackTimes = prayerCycle[cycleIndex],
                        syncedDay = syncedDays[date.toString()]
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    isToday: Boolean,
    fallbackTimes: PrayerTimes,
    syncedDay: WearDayTimes?
) {
    val times = syncedDay?.toPrayerTimes() ?: fallbackTimes
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateText = String.format(Locale.US, "%02d. %s %d", date.dayOfMonth, monthName, date.year)

    val hijri = runCatching {
        HijriDate.fromLocalDate(KLocalDate(date.year, date.monthValue, date.dayOfMonth))
    }.getOrNull()
    val hijriText = hijri?.let {
        "${it.day}. " + stringResource(hijriMonthRes(it.month)) + " ${it.year}"
    } ?: ""

    val now = if (isToday) rememberCurrentTime() else null
    val currentIndex = if (now != null) currentPrayerIndex(times, now) else -1
    val countdown = if (now != null) countdownText(times, now) else null

    val prayers = listOf(
        PrayerUi(R.string.fajr, times.fajr.format(TimeFormat), currentIndex == 0),
        PrayerUi(R.string.sun, times.sun.format(TimeFormat), currentIndex == 1),
        PrayerUi(R.string.zuhr, times.dhuhr.format(TimeFormat), currentIndex == 2),
        PrayerUi(R.string.asr, times.asr.format(TimeFormat), currentIndex == 3),
        PrayerUi(R.string.maghrib, times.maghrib.format(TimeFormat), currentIndex == 4),
        PrayerUi(R.string.ishaa, times.ishaa.format(TimeFormat), currentIndex == 5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = dateText,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = hijriText,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                prayers.take(3).forEach { prayer ->
                    PrayerTile(prayer, Modifier.weight(1f))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                prayers.drop(3).forEach { prayer ->
                    PrayerTile(prayer, Modifier.weight(1f))
                }
            }
        }

        if (isToday && countdown != null) {
            Countdown(countdown)
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RowScope.PrayerTile(prayer: PrayerUi, modifier: Modifier) {
    Card(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isCurrent) Highlight else TileBackground,
            contentColor = if (prayer.isCurrent) Color.White else TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(prayer.labelRes),
                color = if (prayer.isCurrent) Color.White else TextSecondary,
                fontSize = 8.sp,
                fontWeight = if (prayer.isCurrent) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = prayer.time,
                color = if (prayer.isCurrent) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (prayer.isCurrent) FontWeight.Medium else FontWeight.Light,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Countdown(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Light
    )
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
