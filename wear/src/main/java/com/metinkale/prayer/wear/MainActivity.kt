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
import androidx.wear.tiles.TileService
import androidx.wear.compose.material3.openOnPhoneDialogCurvedText
import com.metinkale.prayer.sync.WearDayTimes
import com.metinkale.prayer.sync.WearTimesPayload
import com.metinkale.prayer.wear.data.DhikrRepository
import com.metinkale.prayer.wear.data.WearTimesRepository
import com.metinkale.prayer.wear.dhikr.DhikrPage
import com.metinkale.prayer.wear.qibla.QiblaPage
import com.metinkale.prayer.wear.times.PrayerTimesPage
import com.metinkale.prayer.wear.ui.HighlightBright
import com.metinkale.prayer.wear.ui.ScaledMaterialTheme
import com.metinkale.prayer.wear.ui.TopLevelPageIndicator
import com.metinkale.prayer.wear.ui.IndicatorInactive
import com.metinkale.prayer.wear.tiles.PrayerTimesTileService
import com.metinkale.prayer.wear.ui.CurvedEdgeLabel
import com.metinkale.prayer.wear.ui.EdgeTitleStyle
import com.metinkale.prayer.wear.sync.WearDhikrDataLayer
import com.metinkale.prayer.wear.sync.WearTimesDataLayer
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate as KLocalDate

private const val TOP_LEVEL_PAGE_COUNT = 3

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            TileService.getUpdater(this).requestUpdate(PrayerTimesTileService::class.java)
        }
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

                    TopLevelPageIndicator(
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
