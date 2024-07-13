/*
 * Copyright (c) 2013-2023 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.metinkale.prayer.compass.magnetic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.metinkale.prayer.compass.R

class CalibrationFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accuracy: TextView
    private var accelerometerAccuracy = 0
    private var magneticAccuracy = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calibration_dialog, container, false)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
        accuracy = view.findViewById(R.id.accuracy_text)
        accuracy.setText(
            Html.fromHtml(
                String.format(
                    "%s: <font color='red'>%s</font>",
                    getString(R.string.accuracy),
                    getString(R.string.accuracy_low)
                )
            )
        )
        view.findViewById<View>(R.id.close).setOnClickListener { v: View? ->
            parentFragment?.childFragmentManager?.beginTransaction()
                ?.remove(this@CalibrationFragment)?.commit()
        }
        return view
    }

    override fun onDestroyView() {
        sensorManager.unregisterListener(this)
        super.onDestroyView()
    }

    override fun onSensorChanged(event: SensorEvent) {}
    override fun onAccuracyChanged(sensor: Sensor, sensorAccuracy: Int) {
        var accuracy = sensorAccuracy
        if (accuracy == 0) accuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW
        when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerAccuracy = accuracy
            Sensor.TYPE_MAGNETIC_FIELD -> magneticAccuracy = accuracy
            else -> {}
        }
        if (accelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || magneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) this.accuracy.text =
            Html.fromHtml(
                String.format(
                    "%s: <font color='#ff0000'>%s</font>",
                    getString(R.string.accuracy),
                    getString(R.string.accuracy_low)
                )
            ) else if (accelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ||
            magneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        ) {
            this.accuracy.text = Html.fromHtml(
                String.format(
                    "%s: <font color='#e6e600'>%s</font>",
                    getString(R.string.accuracy),
                    getString(R.string.accuracy_medium)
                )
            )
        } else if (accelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH &&
            magneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        ) {
            this.accuracy.text = Html.fromHtml(
                String.format(
                    "%s: <font color='#00ff00'>%s</font>",
                    getString(R.string.accuracy),
                    getString(R.string.accuracy_high)
                )
            )
            this.accuracy.postDelayed({
                if (!isStateSaved) parentFragment?.childFragmentManager?.beginTransaction()
                    ?.remove(this@CalibrationFragment)?.commit()
            }, 3000)
        }
    }
}