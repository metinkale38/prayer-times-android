/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.alarm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.times.alarm.sounds.MyPlayer;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class StopByFacedownMgr implements SensorEventListener {
    private final MyPlayer player;
    private final SensorManager sensorManager;
    private int mIsFaceDown = 1;

    private StopByFacedownMgr(SensorManager sensorManager, MyPlayer player) {
        this.sensorManager = sensorManager;
        this.player = player;
    }

    public static void start(Context ctx, MyPlayer myPlayer) {
        try {
            SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.registerListener(new StopByFacedownMgr(sensorManager, myPlayer), sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        } catch (Exception e) {
            //do not crash, but report issue
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        if (!player.isPlaying()) {
            sensorManager.unregisterListener(this);
            return;
        }

        if (event.values[2] < -3) {
            if (mIsFaceDown != 1) {//ignore if already was off
                mIsFaceDown += 2;
                if (mIsFaceDown >= 15) {//prevent accident
                    player.stop();
                    sensorManager.unregisterListener(this);
                }
            }
        } else {
            mIsFaceDown = 0;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
