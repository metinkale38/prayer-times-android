/*
 * Copyright (c) 2013-2026 Metin Kale
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
package com.metinkale.prayer.times.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.alarm.sounds.MyPlayer

class StopByFacedownMgr private constructor(
    private val sensorManager: SensorManager,
    private val player: MyPlayer
) : SensorEventListener {
    private var isFaceDown = 1
    override fun onSensorChanged(event: SensorEvent) {
        // Note: the player is intentionally checked via AlarmService.isInterrupted()
        // rather than player.isPlaying, since the alarm now repeats in a loop and
        // briefly stops playing between cycles while still very much active.
        if (AlarmService.isInterrupted()) {
            sensorManager.unregisterListener(this)
            return
        }
        if (event.values[2] < -3) {
            if (isFaceDown != 1) { //ignore if already was off
                isFaceDown += 2
                if (isFaceDown >= 15) { //prevent accident
                    AlarmService.interrupt()
                    player.stop()
                    sensorManager.unregisterListener(this)
                }
            }
        } else {
            isFaceDown = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        fun start(ctx: Context, myPlayer: MyPlayer) {
            try {
                val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                sensorManager.registerListener(
                    StopByFacedownMgr(sensorManager, myPlayer), sensorManager.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER
                    ), SensorManager.SENSOR_DELAY_UI
                )
            } catch (e: Exception) {
                //do not crash, but report issue
                recordException(e)
            }
        }
    }
}