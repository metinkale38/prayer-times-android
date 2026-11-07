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
import androidx.wear.protolayout.material3.Typography.BODY_EXTRA_SMALL
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.Typography.BODY_MEDIUM
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_LARGE
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.LayoutColor
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

private const val RESOURCES_VERSION = "1"
private const val REFRESH_INTERVAL_MILLIS = 60_000L
private const val CELL_HEIGHT_DP = 42f
private const val ROW_HEIGHT_DP = 42f
private const val ROW_SPACING_DP = 5f
private const val GRID_SPACING_DP = 5f
private const val GRID_SIDE_PADDING_DP = 16f
private const val GRID_TOP_PADDING_DP = 22f
private const val GRID_BOTTOM_PADDING_DP = 18f
private const val BACKGROUND_ARGB = 0xFF121212.toInt()
private const val CELL_ARGB = 0xFF242424.toInt()
private const val HIGHLIGHT_ARGB = 0xFF4A768A.toInt()
private const val TEXT_PRIMARY_ARGB = 0xFFF2F2F2.toInt()
private const val TEXT_SECONDARY_ARGB = 0xFFA9B0B4.toInt()
private const val ACTIVE_TEXT_ARGB = 0xFFFFFFFF.toInt()

private val TileTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

class NextPrayerTileService : PrayerTileService() {
    override suspend fun buildTile(
        context: Context,
        requestParams: RequestBuilders.TileRequest
    ): Tile {
        val model = loadPrayerTileModel(context)
        val layout = materialScope(context, requestParams.deviceConfiguration) {
            nextPrayerLayout(context, model)
        }
        return tile(layout)
    }
}

class PrayerTimesTileService : PrayerTileService() {
    override suspend fun buildTile(
        context: Context,
        requestParams: RequestBuilders.TileRequest
    ): Tile {
        val model = loadPrayerTileModel(context)
        val layout = materialScope(context, requestParams.deviceConfiguration) {
            prayerTimesLayout(context, model)
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

private fun openApp(context: Context) = clickable(
    action = launchAction(
        ComponentName(context, MainActivity::class.java),
        emptyMap()
    )
)

private fun LayoutColor.toColorProp(): ColorBuilders.ColorProp =
    ColorBuilders.ColorProp.Builder(staticArgb).apply {
        dynamicArgb?.let(::setDynamicValue)
    }.build()

private fun colorProp(argb: Int): ColorBuilders.ColorProp =
    ColorBuilders.ColorProp.Builder(argb).build()

private fun textColor(argb: Int): LayoutColor = LayoutColor(argb)

private fun verticalSpacer(dp: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setHeight(DimensionBuilders.dp(dp))
        .build()

private fun weightedVerticalSpacer(weight: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setHeight(DimensionBuilders.weight(weight))
        .build()

private fun horizontalSpacer(dp: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setWidth(DimensionBuilders.dp(dp))
        .build()

private fun countdownText(context: Context, minutes: Int): String = when {
    minutes <= 0 -> context.getString(R.string.wear_now)
    minutes < 60 -> context.getString(R.string.wear_countdown_minutes, minutes)
    minutes % 60 == 0 -> context.getString(R.string.wear_countdown_hours, minutes / 60)
    else -> context.getString(
        R.string.wear_countdown_hours_minutes,
        minutes / 60,
        minutes % 60
    )
}

private fun countdownClock(minutes: Int): String =
    String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)

private fun MaterialScope.nextPrayerLayout(
    context: Context,
    model: PrayerTileModel?
): LayoutElementBuilders.LayoutElement {
    val header: String
    val name: String
    val countdown: String
    val time: String

    if (model == null) {
        header = context.getString(R.string.wear_tile_next_prayer)
        name = context.getString(R.string.wear_no_times_title)
        countdown = context.getString(R.string.wear_tile_open_app)
        time = ""
    } else if (model.justStarted) {
        val current = model.today.getOrNull(model.currentIndex)
        header = context.getString(R.string.wear_now)
        name = context.getString(current?.labelRes ?: model.next.labelRes)
        countdown = context.getString(R.string.wear_now)
        time = (current?.time ?: model.next.time).format(TileTimeFormat)
    } else {
        header = if (model.nextIsTomorrow) {
            context.getString(R.string.wear_tile_next_tomorrow)
        } else {
            context.getString(R.string.wear_tile_next_prayer)
        }
        name = context.getString(model.next.labelRes)
        countdown = context.getString(
            R.string.wear_tile_in,
            countdownText(context, model.countdownMinutes)
        )
        time = model.next.time.format(TileTimeFormat)
    }

    return primaryLayout(
        titleSlot = {
            text(
                header.layoutString,
                typography = LABEL_MEDIUM,
                color = colorScheme.onSurfaceVariant
            )
        },
        mainSlot = {
            LayoutElementBuilders.Box.Builder()
                .setWidth(DimensionBuilders.expand())
                .setHeight(DimensionBuilders.expand())
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                        .apply {
                            addContent(
                                text(
                                    name.layoutString,
                                    typography = if (model == null) TITLE_MEDIUM else TITLE_LARGE,
                                    color = colorScheme.onSurface
                                )
                            )
                            addContent(verticalSpacer(2f))
                            addContent(
                                text(
                                    countdown.layoutString,
                                    typography = if (model == null) BODY_MEDIUM else TITLE_LARGE,
                                    color = if (model == null) {
                                        colorScheme.onSurfaceVariant
                                    } else {
                                        colorScheme.primary
                                    }
                                )
                            )
                            if (time.isNotEmpty()) {
                                addContent(verticalSpacer(2f))
                                addContent(
                                    text(
                                        time.layoutString,
                                        typography = BODY_LARGE,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        .build()
                )
                .build()
        },
        onClick = openApp(context)
    )
}

private fun MaterialScope.prayerTimesLayout(
    context: Context,
    model: PrayerTileModel?
): LayoutElementBuilders.LayoutElement {
    if (model == null) {
        return primaryLayout(
            titleSlot = {
                text(
                    context.getString(R.string.wear_today).layoutString,
                    typography = LABEL_MEDIUM,
                    color = colorScheme.onSurfaceVariant
                )
            },
            mainSlot = {
                LayoutElementBuilders.Box.Builder()
                    .setWidth(DimensionBuilders.expand())
                    .setHeight(DimensionBuilders.expand())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                    .addContent(
                        LayoutElementBuilders.Column.Builder()
                            .setWidth(DimensionBuilders.expand())
                            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                            .addContent(
                                text(
                                    context.getString(R.string.wear_no_times_title).layoutString,
                                    typography = TITLE_MEDIUM,
                                    color = colorScheme.onSurface
                                )
                            )
                            .addContent(verticalSpacer(2f))
                            .addContent(
                                text(
                                    context.getString(R.string.wear_tile_open_app).layoutString,
                                    typography = BODY_MEDIUM,
                                    color = colorScheme.onSurfaceVariant
                                )
                            )
                            .build()
                    )
                    .build()
            },
            onClick = openApp(context),
            margins = PrimaryLayoutMargins.MIN_PRIMARY_LAYOUT_MARGIN
        )
    }

    val nextName = context.getString(model.next.labelRes)
    return LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.expand())
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setClickable(openApp(context))
                .setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(colorProp(BACKGROUND_ARGB))
                        .build()
                )
                .build()
        )
        .addContent(
            LayoutElementBuilders.Column.Builder()
                .setWidth(DimensionBuilders.expand())
                .setHeight(DimensionBuilders.expand())
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .setModifiers(
                    ModifiersBuilders.Modifiers.Builder()
                        .setPadding(
                            ModifiersBuilders.Padding.Builder()
                                .setStart(DimensionBuilders.dp(GRID_SIDE_PADDING_DP))
                                .setEnd(DimensionBuilders.dp(GRID_SIDE_PADDING_DP))
                                .setTop(DimensionBuilders.dp(GRID_TOP_PADDING_DP))
                                .setBottom(DimensionBuilders.dp(GRID_BOTTOM_PADDING_DP))
                                .build()
                        )
                        .build()
                )
                .addContent(
                    text(
                        model.dateText.layoutString,
                        typography = BODY_SMALL,
                        color = textColor(TEXT_PRIMARY_ARGB)
                    )
                )
                .apply {
                    if (model.hijriText.isNotEmpty()) {
                        addContent(
                            text(
                                model.hijriText.layoutString,
                                typography = BODY_EXTRA_SMALL,
                                color = textColor(TEXT_SECONDARY_ARGB)
                            )
                        )
                    }
                }
                .addContent(weightedVerticalSpacer(1f))
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .setWidth(DimensionBuilders.expand())
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                        .addContent(prayerRow(context, model, model.today.take(3), 0))
                        .addContent(verticalSpacer(GRID_SPACING_DP))
                        .addContent(prayerRow(context, model, model.today.drop(3), 3))
                        .build()
                )
                .addContent(weightedVerticalSpacer(1f))
                .addContent(
                    text(
                        countdownClock(model.countdownMinutes).layoutString,
                        typography = TITLE_MEDIUM,
                        color = textColor(TEXT_PRIMARY_ARGB)
                    )
                )
                .addContent(
                    text(
                        context.getString(R.string.wear_until, nextName).layoutString,
                        typography = BODY_EXTRA_SMALL,
                        color = textColor(TEXT_SECONDARY_ARGB)
                    )
                )
                .build()
        )
        .build()
}

private fun MaterialScope.prayerRow(
    context: Context,
    model: PrayerTileModel,
    entries: List<PrayerTimeEntry>,
    offset: Int
): LayoutElementBuilders.Row =
    LayoutElementBuilders.Row.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.dp(ROW_HEIGHT_DP))
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .apply {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    addContent(horizontalSpacer(ROW_SPACING_DP))
                }
                addContent(prayerCell(context, model, entry, offset + index))
            }
        }
        .build()

private fun MaterialScope.prayerCell(
    context: Context,
    model: PrayerTileModel,
    entry: PrayerTimeEntry,
    index: Int
): LayoutElementBuilders.Box {
    val isSun = entry.labelRes == R.string.sun
    val isCurrent = !isSun && index == model.currentIndex
    val isNext = !isSun && index == model.nextIndex && !isCurrent
    val containerColor = if (isCurrent) HIGHLIGHT_ARGB else CELL_ARGB
    val labelColor = if (isCurrent) ACTIVE_TEXT_ARGB else TEXT_SECONDARY_ARGB
    val timeColor = when {
        isCurrent -> ACTIVE_TEXT_ARGB
        isSun -> TEXT_SECONDARY_ARGB
        else -> TEXT_PRIMARY_ARGB
    }

    val modifiers = ModifiersBuilders.Modifiers.Builder()
        .setBackground(
            ModifiersBuilders.Background.Builder()
                .setColor(colorProp(containerColor))
                .setCorner(shapes.small)
                .build()
        )
    if (isNext) {
        modifiers.setBorder(
            ModifiersBuilders.Border.Builder()
                .setColor(colorProp(HIGHLIGHT_ARGB))
                .setWidth(DimensionBuilders.dp(1f))
                .build()
        )
    }

    return LayoutElementBuilders.Box.Builder()
        .setWidth(DimensionBuilders.weight(1f))
        .setHeight(DimensionBuilders.dp(CELL_HEIGHT_DP))
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .setModifiers(modifiers.build())
        .addContent(
            LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(
                    text(
                        context.getString(entry.labelRes).layoutString,
                        typography = BODY_EXTRA_SMALL,
                        color = textColor(labelColor)
                    )
                )
                .addContent(
                    text(
                        entry.time.format(TileTimeFormat).layoutString,
                        typography = LABEL_MEDIUM,
                        color = textColor(timeColor)
                    )
                )
                .build()
        )
        .build()
}
