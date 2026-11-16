package com.metinkale.prayer.wear.tiles

import android.content.ComponentName
import android.content.Context
import androidx.wear.protolayout.ActionBuilders.launchAction
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.PrimaryLayoutMargins
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.metinkale.prayer.wear.MainActivity
import com.metinkale.prayer.wear.R
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal const val RESOURCES_VERSION = "3"
internal const val REFRESH_INTERVAL_MILLIS = 60_000L
abstract class PrayerTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    protected abstract suspend fun buildTile(
        context: Context,
        requestParams: RequestBuilders.TileRequest
    ): Tile

    final override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<Tile> {
        val future = SettableFuture.create<Tile>()
        scope.launch {
            runCatching {
                buildTile(applicationContext, requestParams)
            }.onSuccess(future::set)
                .onFailure(future::setException)
        }
        return future
    }

    final override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(requestParams.version)
                .build()
        )

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

class PrayerTimesTileService : PrayerTileService() {
    override suspend fun buildTile(
        context: Context,
        requestParams: RequestBuilders.TileRequest
    ): Tile {
        val model = loadPrayerTileModel(context)
        val device = requestParams.deviceConfiguration
        val shortestDp = minOf(device.screenWidthDp, device.screenHeightDp).toFloat()
        val largeScreen = device.screenWidthDp >= 192
        val scale = (shortestDp / 192f).coerceIn(1f, 1.25f)
        val layout = materialScope(context, device) {
            prayerTimesLayout(context, model, largeScreen, scale)
        }
        return tile(layout)
    }
}

private fun tile(layout: LayoutElementBuilders.LayoutElement): Tile =
    Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setFreshnessIntervalMillis(REFRESH_INTERVAL_MILLIS)
        .setTileTimeline(Timeline.fromLayoutElement(layout))
        .build()

