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

package com.metinkale.prayer.compass.magnetic;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.compass.magnetic.compass2D.Frag2D;
import com.metinkale.prayer.compass.magnetic.utils.OrientationCalculator;
import com.metinkale.prayer.compass.magnetic.utils.math.Matrix4;
import com.metinkale.prayer.compass.magnetic.utils.rotation.MagAccelListener;
import com.metinkale.prayer.compass.magnetic.utils.rotation.RotationUpdateDelegate;

public class MagneticCompass extends Fragment implements QiblaListener, RotationUpdateDelegate {


    private MagAccelListener mMagAccel;
    @NonNull
    private final Matrix4 mRotationMatrix = new Matrix4();
    private int mDisplayRotation;
    private SensorManager mSensorManager;


    @NonNull
    private final OrientationCalculator mOrientationCalculator = new OrientationCalculator();
    private final Frag2D mFrag2D = new Frag2D();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.compass_main, container, false);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);


        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplayRotation = display.getRotation();

        // sensor listeners
        mMagAccel = new MagAccelListener(this);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag2D, mFrag2D, "2d");
        fragmentTransaction.commit();


        return v;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.unregisterListener(mMagAccel);

        mSensorManager.registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mMagAccel, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mMagAccel);
        super.onPause();
    }


    // RotationUpdateDelegate methods
    @Override
    public void onRotationUpdate(@NonNull float[] newMatrix) {
        // remap matrix values according to display rotation, as in
        // SensorManager documentation.
        switch (mDisplayRotation) {
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(newMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, newMatrix);
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(newMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, newMatrix);
                break;
            default:
                break;
        }
        mRotationMatrix.set(newMatrix);

        float[] deviceOrientation = new float[3];
        mOrientationCalculator.getOrientation(mRotationMatrix, mDisplayRotation, deviceOrientation);

        float[] orientation = new float[3];
        SensorManager.getOrientation(newMatrix, orientation);
        mFrag2D.setAngle((int) Math.toDegrees(orientation[0]));
    }

    private boolean mCalibrationStarted;

    @Override
    public void onAccuracyChanged(int accuracy) {
        if (accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH && !mCalibrationStarted) {
            mCalibrationStarted = true;
            getChildFragmentManager().beginTransaction().replace(R.id.calib, new CalibrationFragment(), "calibration").commit();
        }
    }


    @Override
    public void setUserLocation(double lat, double lng, double alt) {
        mFrag2D.setUserLocation(lat, lng, alt);
    }

    @Override
    public void setQiblaAngle(double angle) {
        mFrag2D.setQiblaAngle(angle);
    }

    @Override
    public void setQiblaDistance(double distance) {
        mFrag2D.setQiblaDistance(distance);
    }


}
