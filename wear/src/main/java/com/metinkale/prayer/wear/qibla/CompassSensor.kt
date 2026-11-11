package com.metinkale.prayer.wear.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

data class CompassHeadingState(
    val headingDegrees: Float,
    val hasSensor: Boolean
)

/**
 * Local magnetic declination for converting the sensor's magnetic heading to
 * true north. Positive values mean magnetic north is east of true north.
 */
fun magneticDeclinationDegrees(
    latitude: Double,
    longitude: Double,
    altitudeMeters: Double = 0.0,
    timeMillis: Long = System.currentTimeMillis()
): Float = GeomagneticField(
    latitude.toFloat(),
    longitude.toFloat(),
    altitudeMeters.toFloat(),
    timeMillis
).declination

/**
 * Observes the device heading with TYPE_ROTATION_VECTOR and falls back to the
 * accelerometer + magnetometer combination on devices without a rotation
 * vector sensor. Listeners are registered in a DisposableEffect and removed
 * when the composable leaves composition.
 */
@Composable
fun rememberMagneticHeading(enabled: Boolean): CompassHeadingState {
    val context = LocalContext.current
    val sensorManager = remember(context) {
        context.getSystemService(SensorManager::class.java)
    }
    val configuration = LocalConfiguration.current

    val heading = remember { mutableFloatStateOf(0f) }
    val hasSensor = remember { mutableStateOf(false) }

    DisposableEffect(sensorManager, enabled, configuration) {
        if (sensorManager == null || !enabled) {
            hasSensor.value = false
            return@DisposableEffect onDispose {}
        }

        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val usesRotationVector = rotationVector != null
        val usesAccelMag = !usesRotationVector && accelerometer != null && magnetometer != null

        hasSensor.value = usesRotationVector || usesAccelMag
        if (!hasSensor.value) {
            return@DisposableEffect onDispose {}
        }

        val displayRotation = currentDisplayRotation(context)
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        val filter = HeadingLowPassFilter()

        fun applyOrientation() {
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            heading.floatValue = filter.filter(azimuth)
        }

        fun updateFromAccelMag() {
            if (!hasGravity || !hasGeomagnetic) return
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                remapForDisplay(rotationMatrix, displayRotation)
                applyOrientation()
            }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(
                            rotationMatrix,
                            event.values
                        )
                        remapForDisplay(rotationMatrix, displayRotation)
                        applyOrientation()
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(gravity, endIndex = minOf(3, event.values.size))
                        hasGravity = true
                        updateFromAccelMag()
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        event.values.copyInto(geomagnetic, endIndex = minOf(3, event.values.size))
                        hasGeomagnetic = true
                        updateFromAccelMag()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (usesRotationVector) {
            sensorManager.registerListener(
                listener,
                rotationVector,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            accelerometer?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
            magnetometer?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return CompassHeadingState(
        headingDegrees = heading.floatValue,
        hasSensor = hasSensor.value
    )
}

private fun remapForDisplay(rotationMatrix: FloatArray, displayRotation: Int) {
    when (displayRotation) {
        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_Y,
            SensorManager.AXIS_MINUS_X,
            rotationMatrix
        )

        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_MINUS_X,
            SensorManager.AXIS_MINUS_Y,
            rotationMatrix
        )

        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_MINUS_Y,
            SensorManager.AXIS_X,
            rotationMatrix
        )
    }
}

private fun currentDisplayRotation(context: Context): Int = runCatching {
    context.display?.rotation
        ?: (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.rotation
}.getOrDefault(Surface.ROTATION_0)
