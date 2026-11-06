package com.metinkale.prayer.wear.qibla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.padding
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import com.metinkale.prayer.sync.WearCity
import com.metinkale.prayer.wear.BuildConfig
import com.metinkale.prayer.wear.R
import com.metinkale.prayer.wear.debug.DebugHeadingOverride
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private val StatusText = Color(0xFF8D989E)
private val EdgeText = Color(0xFFE7EEF2)
private val QiblaBackground = Color(0xFF121212)

private val AngleTextStyle = CurvedTextStyle(
    color = EdgeText,
    fontSize = 14.sp,
    fontWeight = FontWeight.SemiBold
)
private val DistanceTextStyle = CurvedTextStyle(
    color = EdgeText,
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium
)
private val StatusTextStyle = CurvedTextStyle(
    color = StatusText,
    fontSize = 10.sp,
    fontWeight = FontWeight.Normal
)

@Composable
fun QiblaPage(
    fallbackCity: WearCity?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val permission = rememberLocationPermissionState()

    LaunchedEffect(isActive, permission.granted, permission.requestedOnce) {
        if (isActive && !permission.granted && !permission.requestedOnce) {
            permission.request()
        }
    }

    val locationState = rememberUserLocation(
        permissionGranted = permission.granted,
        trackingEnabled = isActive,
        fallbackCity = fallbackCity
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { QIBLA_PAGE_COUNT }
    )
    val currentPage = pagerState.currentPage
    val compassActive = isActive && currentPage == 0
    val headingState = rememberMagneticHeading(enabled = compassActive)

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> QiblaCompassPage(
                headingState = headingState,
                locationState = locationState
            )

            1 -> QiblaMapPage(
                locationState = locationState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun QiblaCompassPage(
    headingState: CompassHeadingState,
    locationState: UserLocationState,
    modifier: Modifier = Modifier
) {
    val location = locationState.location
    val latitude = location?.latitude
    val longitude = location?.longitude
    val altitude = location?.altitudeMeters

    val debugHeading by DebugHeadingOverride.heading.collectAsStateWithLifecycle()
    val sensorHeading = headingState.headingDegrees
    val effectiveHeading = if (BuildConfig.DEBUG) {
        debugHeading ?: sensorHeading
    } else {
        sensorHeading
    }

    val declination = remember(latitude, longitude, altitude) {
        if (latitude != null && longitude != null) {
            magneticDeclinationDegrees(
                latitude = latitude,
                longitude = longitude,
                altitudeMeters = altitude ?: 0.0
            )
        } else {
            0f
        }
    }

    val debugHeadingValue = debugHeading
    val trueHeading = if (BuildConfig.DEBUG && debugHeadingValue != null) {
        QiblaMath.normalizeDegrees(debugHeadingValue)
    } else {
        QiblaMath.trueHeadingDegrees(
            magneticHeadingDegrees = effectiveHeading,
            declinationDegrees = declination
        )
    }

    val qiblaBearing = remember(latitude, longitude) {
        if (latitude != null && longitude != null) {
            QiblaMath.bearingDegrees(latitude, longitude).toFloat()
        } else {
            null
        }
    }

    val distanceKm = remember(latitude, longitude) {
        if (latitude != null && longitude != null) {
            QiblaMath.distanceToKaabaKm(latitude, longitude)
        } else {
            null
        }
    }

    val qiblaAngle = qiblaBearing?.roundToInt()
    val distanceText = distanceKm?.let(::formatDistance)

    val statusText = when {
        !locationState.permissionGranted -> stringResource(R.string.location_permission)
        locationState.isSearchingDeviceLocation -> stringResource(R.string.location_searching)
        location != null && !location.isDeviceLocation && location.cityName != null ->
            stringResource(R.string.location_from_city, location.cityName)

        location != null -> stringResource(R.string.location_ready)
        else -> stringResource(R.string.location_searching)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QiblaBackground)
    ) {
        QiblaCompass(
            headingDegrees = trueHeading,
            qiblaBearingDegrees = qiblaBearing,
            modifier = Modifier.fillMaxSize()
        )

        if (qiblaAngle != null) {
            CurvedEdgeLabel(
                text = stringResource(R.string.qibla_angle, qiblaAngle),
                alignTop = true,
                style = AngleTextStyle
            )
        }

        if (distanceText != null) {
            CurvedEdgeLabel(
                text = stringResource(R.string.qibla_distance, distanceText),
                alignTop = false,
                style = DistanceTextStyle
            )
        } else {
            CurvedEdgeLabel(
                text = statusText,
                alignTop = false,
                style = StatusTextStyle
            )
        }
    }
}

@Composable
private fun CurvedEdgeLabel(
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
            modifier = CurvedModifier.padding(radial = 4.dp),
            style = style
        )
    }
}

private fun formatDistance(distanceKm: Double): String {
    val rounded = distanceKm.roundToLong()
    return "${rounded}km"
}

private const val QIBLA_PAGE_COUNT = 2
