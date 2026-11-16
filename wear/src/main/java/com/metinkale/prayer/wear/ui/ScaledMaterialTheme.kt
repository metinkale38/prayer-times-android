package com.metinkale.prayer.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.wear.compose.material3.MaterialTheme

@Composable
internal fun ScaledMaterialTheme(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val shortestDp = minOf(
        configuration.screenWidthDp,
        configuration.screenHeightDp
    ).toFloat()
    val scale = (shortestDp / 192f).coerceIn(1f, 1.25f)
    val scaledDensity = Density(
        density = baseDensity.density * scale,
        fontScale = baseDensity.fontScale
    )
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(content = content)
    }
}
