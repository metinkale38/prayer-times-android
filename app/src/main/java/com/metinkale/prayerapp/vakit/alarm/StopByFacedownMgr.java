package com.metinkale.prayerapp.vakit.alarm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.vakit.sounds.MyPlayer;

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
            Crashlytics.logException(e);
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
