package com.metinkale.prayer.wear.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.metinkale.prayer.wear.R
import kotlin.math.min
import kotlin.math.roundToInt

private val CompassFaceCenter = Color(0xFF232323)
private val CompassFaceEdge = Color(0xFF171717)
private val CompassFaceBorder = Color(0xFF2C3A40)
private val CompassRing = Color(0xFF4A768A)
private val CompassRingHighlight = Color(0x4D6FA7BB)
private val CompassHeading = Color(0xFFE7EEF2)
private val CompassDegree = Color(0xFFA9B0B4)
private val CompassSecondary = Color(0xFFA9B0B4)
private val CompassAligned = Color(0xFF7FB3C6)
private val CompassNorthNeedle = Color(0xFFC7D0D5)
private val CompassNLabel = Color(0xFFA9B0B4)
private val CompassQiblaArrow = Color(0xFFE7EEF2)
private val CompassKaabaGold = Color(0xFFC9A24B)
private val CompassKaabaDoor = Color(0xFF4A768A)

@Composable
fun QiblaCompass(
    headingDegrees: Float,
    qiblaBearingDegrees: Float?,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val headingLabel = stringResource(
        if (isAligned) R.string.qibla_aligned else R.string.heading
    )
    val headingLabelColor = if (isAligned) CompassAligned else CompassSecondary

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        // Same reference as the phone CompassView: half of the square canvas.
        val compassCenter = min(size.width, size.height) / 2f
        // Leave room for the curved labels at the top and bottom edge.
        val radius = (compassCenter - 24.dp.toPx()).coerceAtLeast(1f)
        val faceRadius = radius * 19f / 20f

        val ringWidth = compassCenter / 15f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CompassFaceCenter, CompassFaceEdge),
                center = center,
                radius = faceRadius
            ),
            radius = faceRadius,
            center = center
        )
        drawCircle(
            color = CompassFaceBorder,
            radius = faceRadius - ringWidth / 2f + 1.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = CompassRing,
            radius = faceRadius,
            center = center,
            style = Stroke(width = ringWidth)
        )
        drawCircle(
            color = CompassRingHighlight,
            radius = faceRadius - ringWidth / 2f + 1.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        val heading = QiblaMath.normalizeDegrees(headingDegrees).roundToInt()
        val headingLayout = textMeasurer.measure(
            text = AnnotatedString(heading.toString()),
            style = TextStyle(
                color = CompassHeading,
                fontSize = (compassCenter * 0.28f).toSp(),
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1
        )
        val headingTopLeft = Offset(
            x = center.x - headingLayout.size.width / 2f,
            y = center.y - headingLayout.size.height / 2f
        )
        drawText(textLayoutResult = headingLayout, topLeft = headingTopLeft)

        val degreeLayout = textMeasurer.measure(
            text = AnnotatedString("°"),
            style = TextStyle(
                color = CompassDegree,
                fontSize = (compassCenter * 0.22f).toSp(),
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1
        )
        val numberBox = headingLayout.getBoundingBox(0)
        val lastNumberBox = headingLayout.getBoundingBox(
            headingLayout.layoutInput.text.length - 1
        )
        val degreeBox = degreeLayout.getBoundingBox(0)
        val degreeGap = compassCenter * 0.012f
        drawText(
            textLayoutResult = degreeLayout,
            topLeft = Offset(
                x = headingTopLeft.x + lastNumberBox.right + degreeGap - degreeBox.left,
                y = headingTopLeft.y + numberBox.top - degreeBox.top
            )
        )

        val headingLabelLayout = textMeasurer.measure(
            text = AnnotatedString(headingLabel),
            style = TextStyle(
                color = headingLabelColor,
                fontSize = (compassCenter * 0.065f).toSp(),
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        drawText(
            textLayoutResult = headingLabelLayout,
            topLeft = Offset(
                x = center.x - headingLabelLayout.size.width / 2f,
                y = headingTopLeft.y + headingLayout.size.height + 2.dp.toPx()
            )
        )

        // The whole north/marker layer rotates against the current heading.
        rotate(degrees = -headingDegrees, pivot = center) {
            val northArrow = buildArrowPath(center, faceRadius)
            drawPath(path = northArrow, color = CompassNorthNeedle)
            drawTextAtBaseline(
                textMeasurer = textMeasurer,
                text = "N",
                xCenter = center.x,
                baselineY = center.y - faceRadius * 11f / 20f,
                style = TextStyle(
                    color = CompassNLabel,
                    fontSize = (faceRadius / 5f).toSp(),
                    fontWeight = FontWeight.Normal
                )
            )

            if (qiblaBearingDegrees != null) {
                rotate(degrees = qiblaBearingDegrees, pivot = center) {
                    drawKaabaMarker(center, faceRadius)
                    drawPath(path = northArrow, color = CompassQiblaArrow)
                }
            }
        }
    }
}

private fun buildArrowPath(center: Offset, faceRadius: Float): Path = Path().apply {
    moveTo(center.x, center.y - faceRadius * 7f / 8f)
    lineTo(center.x - faceRadius / 4f, center.y - faceRadius * 2f / 3f)
    lineTo(center.x, center.y - faceRadius * 3f / 4f)
    lineTo(center.x + faceRadius / 4f, center.y - faceRadius * 2f / 3f)
    close()
}

private fun DrawScope.drawKaabaMarker(center: Offset, faceRadius: Float) {
    val iconSize = faceRadius / 7f
    val half = iconSize / 2f
    val iconCenter = Offset(
        x = center.x,
        y = center.y - faceRadius * 11f / 20f
    )
    val topLeft = Offset(iconCenter.x - half, iconCenter.y - half)
    val corner = CornerRadius(2.dp.toPx())

    drawRoundRect(
        color = CompassFaceCenter,
        topLeft = topLeft,
        size = Size(iconSize, iconSize),
        cornerRadius = corner
    )
    drawRoundRect(
        color = CompassQiblaArrow,
        topLeft = topLeft,
        size = Size(iconSize, iconSize),
        cornerRadius = corner,
        style = Stroke(width = 1.dp.toPx())
    )
    drawRect(
        color = CompassKaabaGold,
        topLeft = Offset(
            topLeft.x + 1.dp.toPx(),
            topLeft.y + iconSize * 0.32f
        ),
        size = Size(iconSize - 2.dp.toPx(), 1.5.dp.toPx())
    )
    drawRect(
        color = CompassKaabaDoor,
        topLeft = Offset(
            iconCenter.x - iconSize * 0.09f,
            topLeft.y + iconSize * 0.68f
        ),
        size = Size(iconSize * 0.18f, iconSize * 0.25f)
    )
}

private fun DrawScope.drawTextAtBaseline(
    textMeasurer: TextMeasurer,
    text: String,
    xCenter: Float,
    baselineY: Float,
    style: TextStyle
) {
    val layout = textMeasurer.measure(
        text = AnnotatedString(text),
        style = style,
        maxLines = 1
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            x = xCenter - layout.size.width / 2f,
            y = baselineY - layout.getLineBaseline(0)
        )
    )
}
