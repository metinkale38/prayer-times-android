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

internal fun MaterialScope.prayerRow(
    context: Context,
    model: PrayerTileModel,
    entries: List<PrayerTimeEntry>,
    offset: Int,
    largeScreen: Boolean,
    scale: Float
): LayoutElementBuilders.Row {
    val cellHeight = (if (largeScreen) 42f else 38f) * scale
    val spacing = (if (largeScreen) 5f else 4f) * scale
    return LayoutElementBuilders.Row.Builder()
        .setWidth(DimensionBuilders.expand())
        .setHeight(DimensionBuilders.dp(cellHeight))
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .apply {
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    addContent(horizontalSpacer(spacing))
                }
                addContent(prayerCell(context, model, entry, offset + index, largeScreen, scale))
            }
        }
        .build()
}

internal fun MaterialScope.prayerCell(
    context: Context,
    model: PrayerTileModel,
    entry: PrayerTimeEntry,
    index: Int,
    largeScreen: Boolean,
    scale: Float
): LayoutElementBuilders.Box {
    val cellHeight = (if (largeScreen) 42f else 38f) * scale
    val labelSize = (if (largeScreen) 9f else 8f) * scale
    val timeSize = (if (largeScreen) 15f else 14f) * scale
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
        .setHeight(DimensionBuilders.dp(cellHeight))
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        .setModifiers(modifiers.build())
        .addContent(
            LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .addContent(
                    textWithSize(
                        context.getString(entry.labelRes),
                        sizeSp = labelSize,
                        color = labelColor
                    )
                )
                .addContent(
                    textWithSize(
                        entry.time.format(TileTimeFormat),
                        sizeSp = timeSize,
                        color = timeColor
                    )
                )
                .build()
        )
        .build()
}
