package com.metinkale.prayer.wear.qibla

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.metinkale.prayer.sync.WearCity

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val isDeviceLocation: Boolean,
    val cityName: String? = null
)

data class UserLocationState(
    val location: UserLocation?,
    val permissionGranted: Boolean,
    val isSearchingDeviceLocation: Boolean
)

class LocationPermissionState(
    val granted: Boolean,
    val requestedOnce: Boolean,
    val request: () -> Unit
)

fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasLocationPermission(context)) }
    var requestedOnce by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        granted = hasLocationPermission(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = hasLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val request: () -> Unit = {
        requestedOnce = true
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    return LocationPermissionState(
        granted = granted,
        requestedOnce = requestedOnce,
        request = request
    )
}

@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocation(
    permissionGranted: Boolean,
    trackingEnabled: Boolean,
    fallbackCity: WearCity?
): UserLocationState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val deviceLocation = remember { mutableStateOf<UserLocation?>(null) }
    val isSearching = remember { mutableStateOf(false) }

    DisposableEffect(permissionGranted, trackingEnabled, lifecycleOwner) {
        if (!permissionGranted || !trackingEnabled) {
            isSearching.value = false
            return@DisposableEffect onDispose {}
        }

        val locationManager = context.getSystemService(LocationManager::class.java)
        if (locationManager == null) {
            isSearching.value = false
            return@DisposableEffect onDispose {}
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                deviceLocation.value = location.toUserLocation()
                isSearching.value = false
            }

            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        fun updateFromLastKnownLocation() {
            val lastKnown = runCatching {
                locationManager.allProviders
                    .mapNotNull { locationManager.getLastKnownLocation(it) }
                    .maxByOrNull { it.time }
            }.getOrNull()

            if (lastKnown != null) {
                deviceLocation.value = lastKnown.toUserLocation()
                isSearching.value = false
            }
        }

        fun startTracking() {
            updateFromLastKnownLocation()

            val providers = PROVIDERS.filter { provider ->
                runCatching { locationManager.isProviderEnabled(provider) }
                    .getOrDefault(false)
            }
            isSearching.value = deviceLocation.value == null

            providers.forEach { provider ->
                runCatching {
                    locationManager.requestLocationUpdates(
                        provider,
                        LOCATION_INTERVAL_MS,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            }
        }

        fun stopTracking() {
            runCatching { locationManager.removeUpdates(listener) }
            isSearching.value = false
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> startTracking()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> stopTracking()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            startTracking()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopTracking()
        }
    }

    val fallback = remember(fallbackCity) { fallbackCity?.toUserLocation() }
    val effectiveLocation = if (permissionGranted) {
        deviceLocation.value ?: fallback
    } else {
        fallback
    }

    return UserLocationState(
        location = effectiveLocation,
        permissionGranted = permissionGranted,
        isSearchingDeviceLocation = permissionGranted &&
            trackingEnabled &&
            deviceLocation.value == null
    )
}

private fun Location.toUserLocation(): UserLocation = UserLocation(
    latitude = latitude,
    longitude = longitude,
    altitudeMeters = altitude,
    isDeviceLocation = true
)

private fun WearCity.toUserLocation(): UserLocation = UserLocation(
    latitude = latitude,
    longitude = longitude,
    altitudeMeters = 0.0,
    isDeviceLocation = false,
    cityName = name
)

private const val LOCATION_INTERVAL_MS = 1_000L
private val PROVIDERS = listOf(
    LocationManager.GPS_PROVIDER,
    LocationManager.NETWORK_PROVIDER,
    LocationManager.PASSIVE_PROVIDER
)
