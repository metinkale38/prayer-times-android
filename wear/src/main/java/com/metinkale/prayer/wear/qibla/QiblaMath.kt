package com.metinkale.prayer.wear.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Pure Kotlin Qibla calculations, shared by the compass and map screens.
 *
 * The bearing uses the same great-circle atan2 formula as the phone app. It is
 * intentionally free of Android dependencies so it can be unit tested easily.
 */
object QiblaMath {
    const val KAABA_LATITUDE = 21.42247
    const val KAABA_LONGITUDE = 39.826198

    private const val EARTH_RADIUS_KM = 6371.0088

    fun normalizeDegrees(degrees: Float): Float {
        var value = degrees % 360f
        if (value < 0f) value += 360f
        return value
    }

    fun normalizeDegrees(degrees: Double): Double {
        var value = degrees % 360.0
        if (value < 0.0) value += 360.0
        return value
    }

    /**
     * Initial true bearing from [latitude]/[longitude] to the Kaaba in degrees
     * clockwise from true north.
     */
    fun bearingDegrees(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLng = Math.toRadians(KAABA_LONGITUDE - longitude)

        val y = sin(deltaLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLng)
        return normalizeDegrees(Math.toDegrees(atan2(y, x)))
    }

    /**
     * Great-circle distance to the Kaaba in kilometres.
     */
    fun distanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLat = lat2 - lat1
        val deltaLng = Math.toRadians(KAABA_LONGITUDE - longitude)

        val a = (sin(deltaLat / 2).let { it * it } +
            cos(lat1) * cos(lat2) * sin(deltaLng / 2).let { it * it })
            .coerceIn(0.0, 1.0)
        return 2.0 * EARTH_RADIUS_KM * atan2(sqrt(a), sqrt(1.0 - a))
    }

    /**
     * Converts a magnetic heading reported by the sensors to true north using
     * the local magnetic declination (positive east).
     */
    fun trueHeadingDegrees(magneticHeadingDegrees: Float, declinationDegrees: Float): Float =
        normalizeDegrees(magneticHeadingDegrees + declinationDegrees)
}

/**
 * Low-pass filter for compass headings with handling for the 0/360 wrap-around.
 */
class HeadingLowPassFilter(private val alpha: Float = 0.15f) {
    private var initialized = false
    private var value = 0f

    fun filter(inputDegrees: Float): Float {
        val input = QiblaMath.normalizeDegrees(inputDegrees)
        if (!initialized) {
            value = input
            initialized = true
            return value
        }

        var delta = input - QiblaMath.normalizeDegrees(value)
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        value = QiblaMath.normalizeDegrees(value + alpha * delta)
        return value
    }
}
