package com.metinkale.prayer.wear.qibla

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.metinkale.prayer.wear.R

private val MapBackground = Color(0xFF0E1113)
private val MapText = Color(0xFFF2F2F2)
private val MapSecondary = Color(0xFFA9B0B4)
private val MapLine = Color(0xFF3BB2D0)
private val MapAction = Color(0xFF4A768A)
private val MapAccuracyFill = Color(0x333BB2D0)

@Composable
fun QiblaMapPage(
    locationState: UserLocationState,
    onShowCompass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiKey = remember(context) { context.getString(R.string.GOOGLE_API_KEY) }

    if (apiKey.isBlank()) {
        MapMessage(
            title = stringResource(R.string.map),
            body = stringResource(R.string.map_unavailable_body),
            actionLabel = stringResource(R.string.open_compass),
            onAction = onShowCompass,
            modifier = modifier
        )
        return
    }

    val location = locationState.location
    if (location == null) {
        MapMessage(
            title = stringResource(R.string.map),
            body = if (locationState.permissionGranted) {
                stringResource(R.string.location_searching)
            } else {
                stringResource(R.string.location_permission)
            },
            actionLabel = stringResource(R.string.open_compass),
            onAction = onShowCompass,
            modifier = modifier
        )
        return
    }

    val userPosition = remember(location.latitude, location.longitude) {
        LatLng(location.latitude, location.longitude)
    }
    val kaabaPosition = remember {
        LatLng(QiblaMath.KAABA_LATITUDE, QiblaMath.KAABA_LONGITUDE)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userPosition, MAP_ZOOM)
    }

    LaunchedEffect(userPosition) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(userPosition, MAP_ZOOM)
        )
    }

    val userMarkerState = rememberUpdatedMarkerState(position = userPosition)
    val kaabaMarkerState = rememberUpdatedMarkerState(position = kaabaPosition)

    val userMarkerTitle = stringResource(R.string.your_location)
    val kaabaMarkerTitle = stringResource(R.string.kaaba)

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            isMyLocationEnabled = false,
            isTrafficEnabled = false,
            mapType = MapType.HYBRID
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false
        )
    ) {
        Polyline(
            points = listOf(userPosition, kaabaPosition),
            geodesic = true,
            color = MapLine,
            width = 4f,
            zIndex = 1f
        )

        location.accuracyMeters?.takeIf { it > 0f }?.let { accuracy ->
            Circle(
                center = userPosition,
                fillColor = MapAccuracyFill,
                radius = accuracy.toDouble(),
                strokeColor = MapLine,
                strokeWidth = 2f,
                zIndex = 0f
            )
        }

        Marker(
            state = userMarkerState,
            title = userMarkerTitle,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            anchor = Offset(0.5f, 0.5f),
            zIndex = 2f
        )

        Marker(
            state = kaabaMarkerState,
            title = kaabaMarkerTitle,
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_compass_kaabe),
            anchor = Offset(0.5f, 0.5f),
            zIndex = 3f
        )
    }
}

@Composable
private fun MapMessage(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MapBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = MapText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                color = MapSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = actionLabel,
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .background(MapAction, RoundedCornerShape(50))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

private const val MAP_ZOOM = 14f
