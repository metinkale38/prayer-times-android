package com.metinkale.prayer.wear.watchface

import android.graphics.Paint
import android.graphics.Typeface

internal object WatchFaceTheme {
    val Background = 0xFF000000.toInt()
    val TextPrimary = 0xFFF2F2F2.toInt()
    val TextSecondary = 0xFFA9B0B4.toInt()
    val Accent = 0xFF3F9BBF.toInt()
    val AccentBright = 0xFF7FB3C6.toInt()
    val Track = 0x59FFFFFF.toInt()
    val AmbientPrimary = 0xFFFFFFFF.toInt()
    val AmbientSecondary = 0xFF808080.toInt()

    fun text(size: Float, textColor: Int, ambient: Boolean, bold: Boolean = false): Paint =
        Paint().apply {
            isAntiAlias = !ambient
            color = textColor
            textSize = size
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(
                "sans-serif",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        }

    fun complicationDrawable(context: android.content.Context): androidx.wear.watchface.complications.rendering.ComplicationDrawable =
        androidx.wear.watchface.complications.rendering.ComplicationDrawable(context).apply {
            activeStyle.apply {
                textColor = TextPrimary
                titleColor = TextSecondary
                rangedValuePrimaryColor = Accent
                rangedValueSecondaryColor = Track
                iconColor = TextPrimary
                borderColor = android.graphics.Color.TRANSPARENT
            }
            ambientStyle.apply {
                textColor = AmbientPrimary
                titleColor = AmbientSecondary
                rangedValuePrimaryColor = AmbientPrimary
                rangedValueSecondaryColor = AmbientSecondary
                iconColor = AmbientPrimary
                borderColor = android.graphics.Color.TRANSPARENT
            }
        }

    fun arc(stroke: Float, arcColor: Int, ambient: Boolean): Paint =
        Paint().apply {
            isAntiAlias = !ambient
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            color = arcColor
        }
}
