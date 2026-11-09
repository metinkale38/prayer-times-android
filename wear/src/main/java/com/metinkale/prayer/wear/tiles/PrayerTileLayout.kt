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

internal const val BACKGROUND_ARGB = 0xFF121212.toInt()
internal const val CELL_ARGB = 0xFF242424.toInt()
internal const val HIGHLIGHT_ARGB = 0xFF4A768A.toInt()
internal const val TEXT_PRIMARY_ARGB = 0xFFF2F2F2.toInt()
internal const val TEXT_SECONDARY_ARGB = 0xFFA9B0B4.toInt()
internal const val ACTIVE_TEXT_ARGB = 0xFFFFFFFF.toInt()

internal val TileTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun openApp(context: Context) = clickable(
    action = launchAction(
        ComponentName(context, MainActivity::class.java),
        emptyMap()
    )
)

internal fun colorProp(argb: Int): ColorBuilders.ColorProp =
    ColorBuilders.ColorProp.Builder(argb).build()


internal fun textWithSize(
    text: String,
    sizeSp: Float,
    color: Int,
    maxLines: Int = 1
): LayoutElementBuilders.Text =
    LayoutElementBuilders.Text.Builder()
        .setText(text)
        .setFontStyle(
            LayoutElementBuilders.FontStyle.Builder()
                .setSize(DimensionBuilders.sp(sizeSp))
                .setColor(colorProp(color))
                .setPreferredFontFamilies("roboto")
                .build()
        )
        .setMaxLines(maxLines)
        .build()

private fun verticalSpacer(dp: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setHeight(DimensionBuilders.dp(dp))
        .build()

private fun weightedVerticalSpacer(weight: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setHeight(DimensionBuilders.weight(weight))
        .build()

internal fun horizontalSpacer(dp: Float): LayoutElementBuilders.LayoutElement =
    LayoutElementBuilders.Spacer.Builder()
        .setWidth(DimensionBuilders.dp(dp))
        .build()

private fun countdownClock(minutes: Int): String =
    String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)


internal fun MaterialScope.prayerTimesLayout(
    context: Context,
    model: PrayerTileModel?,
    largeScreen: Boolean,
    scale: Float
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
    val countdown = countdownClock(model.countdownMinutes)
    val sidePadding = (if (largeScreen) 16f else 24f) * scale
    val topPadding = (if (largeScreen) 22f else 28f) * scale
    val bottomPadding = (if (largeScreen) 5f else 24f) * scale
    val gridSpacing = (if (largeScreen) 5f else 4f) * scale
    val dateSize = (if (largeScreen) 12f else 11f) * scale
    val hijriSize = (if (largeScreen) 9f else 8f) * scale
    val countdownSize = (if (largeScreen) 16f else 15f) * scale
    val untilSize = (if (largeScreen) 9f else 8f) * scale
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
                                .setStart(DimensionBuilders.dp(sidePadding))
                                .setEnd(DimensionBuilders.dp(sidePadding))
                                .setTop(DimensionBuilders.dp(topPadding))
                                .setBottom(DimensionBuilders.dp(bottomPadding))
                                .build()
                        )
                        .build()
                )
                .addContent(
                    textWithSize(
                        model.dateText,
                        sizeSp = dateSize,
                        color = TEXT_PRIMARY_ARGB
                    )
                )
                .apply {
                    if (model.hijriText.isNotEmpty()) {
                        addContent(verticalSpacer(1f * scale))
                        addContent(
                            textWithSize(
                                model.hijriText,
                                sizeSp = hijriSize,
                                color = TEXT_SECONDARY_ARGB
                            )
                        )
                    }
                }
                .addContent(
                    if (largeScreen) {
                        verticalSpacer(2.5f * scale)
                    } else {
                        weightedVerticalSpacer(1f)
                    }
                )
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .setWidth(DimensionBuilders.expand())
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                        .addContent(prayerRow(context, model, model.today.take(3), 0, largeScreen, scale))
                        .addContent(verticalSpacer(gridSpacing))
                        .addContent(prayerRow(context, model, model.today.drop(3), 3, largeScreen, scale))
                        .build()
                )
                .addContent(weightedVerticalSpacer(1f))
                .addContent(
                    textWithSize(
                        countdown,
                        sizeSp = countdownSize,
                        color = TEXT_PRIMARY_ARGB
                    )
                )
                .addContent(verticalSpacer(1f * scale))
                .addContent(
                    textWithSize(
                        context.getString(R.string.wear_until, nextName),
                        sizeSp = untilSize,
                        color = TEXT_SECONDARY_ARGB
                    )
                )
                .build()
        )
        .build()
}
