package com.metinkale.prayer.wear.qibla

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
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

private val CompassFace = Color(0xFFFFFFFF)
private val CompassRing = Color(0xFF4A768A)
private val CompassText = Color(0xFF000000)
private val CompassNorth = Color(0xFF888888)

@Composable
fun QiblaCompass(
    headingDegrees: Float,
    qiblaBearingDegrees: Float?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val kaabaPainter = painterResource(R.drawable.compass_kaabe)

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        // Same reference as the phone CompassView: half of the square canvas.
        val compassCenter = min(size.width, size.height) / 2f
        // Leave room for the curved labels at the top and bottom edge.
        val radius = (compassCenter - 24.dp.toPx()).coerceAtLeast(1f)
        val faceRadius = radius * 19f / 20f

        drawCircle(color = CompassFace, radius = faceRadius, center = center)
        drawCircle(
            color = CompassRing,
            radius = faceRadius,
            center = center,
            style = Stroke(width = compassCenter / 15f)
        )

        val heading = QiblaMath.normalizeDegrees(headingDegrees).roundToInt()
        val headingLayout = textMeasurer.measure(
            text = AnnotatedString(heading.toString()),
            style = TextStyle(
                color = CompassText,
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
                color = CompassText,
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

        // The whole north/marker layer rotates against the current heading.
        rotate(degrees = -headingDegrees, pivot = center) {
            val northArrow = buildArrowPath(center, faceRadius)
            drawPath(path = northArrow, color = CompassNorth)
            drawTextAtBaseline(
                textMeasurer = textMeasurer,
                text = "N",
                xCenter = center.x,
                baselineY = center.y - faceRadius * 11f / 20f,
                style = TextStyle(
                    color = CompassNorth,
                    fontSize = (faceRadius / 5f).toSp(),
                    fontWeight = FontWeight.Normal
                )
            )

            if (qiblaBearingDegrees != null) {
                rotate(degrees = qiblaBearingDegrees, pivot = center) {
                    drawKaabaIcon(kaabaPainter, center, faceRadius)
                    drawPath(path = northArrow, color = CompassText)
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

private fun DrawScope.drawKaabaIcon(
    painter: Painter,
    center: Offset,
    faceRadius: Float
) {
    val iconSize = faceRadius / 5f
    val iconCenter = Offset(
        x = center.x,
        y = center.y - faceRadius * 11f / 20f
    )
    translate(
        left = iconCenter.x - iconSize / 2f,
        top = iconCenter.y - iconSize / 2f
    ) {
        with(painter) {
            draw(size = Size(iconSize, iconSize))
        }
    }
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
