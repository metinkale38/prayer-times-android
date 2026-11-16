package com.metinkale.prayer.wear.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.padding

private val EdgeText = Color(0xFFE7EEF2)
private val EdgeMutedText = Color(0xFFA9B0B4)

val EdgeTitleStyle = CurvedTextStyle(
    color = EdgeText,
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold
)

val EdgeBodyStyle = CurvedTextStyle(
    color = EdgeMutedText,
    fontSize = 11.sp,
    fontWeight = FontWeight.Normal
)

@Composable
fun CurvedEdgeLabel(
    text: String,
    alignTop: Boolean,
    style: CurvedTextStyle
) {
    CurvedLayout(
        modifier = Modifier.fillMaxSize(),
        anchor = if (alignTop) 270f else 90f,
        anchorType = AnchorType.Center,
        angularDirection = if (alignTop) {
            CurvedDirection.Angular.Normal
        } else {
            CurvedDirection.Angular.CounterClockwise
        }
    ) {
        basicCurvedText(
            text = text,
            modifier = CurvedModifier.padding(radial = 8.dp),
            style = style
        )
    }
}
