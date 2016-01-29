package com.metinkale.prayerapp.compass.classes.rotation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Magnetometer / Accelerometer sensor fusion Smoothed by means of simple high
 * pass filter
 * <p/>
 * When it receives an event it will notify the constructor-specified delegate.
 *
 * @author Adam
 */
public class MagAccelListener implements SensorEventListener
{
    // smoothing factor - tune to taste
    private final float mFilterFactor = 0.05f;
    // smoothed accelerometer values
    public float[] mAccelVals = new float[]{0f, 0f, 9.8f};
    // smoothed magnetometer values
    public float[] mMagVals = new float[]{0.5f, 0f, 0f};
    private float[] mRotationM = new float[16];
    private boolean mIsReady = false;
    private RotationUpdateDelegate mRotationUpdateDelegate;

    public MagAccelListener(RotationUpdateDelegate rotationUpdateDelegate)
    {
        mRotationUpdateDelegate = rotationUpdateDelegate;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                smooth(event.values, mAccelVals, mAccelVals);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                smooth(event.values, mMagVals, mMagVals);
                mIsReady = true;
                break;
            default:
                break;
        }
        // wait until we have both a new accelerometer and magnetometer sample
        if(mIsReady)
        {
            mIsReady = false;
            fuseValues();
        }
    }

    private void fuseValues()
    {
        SensorManager.getRotationMatrix(mRotationM, null, mAccelVals, mMagVals);
        mRotationUpdateDelegate.onRotationUpdate(mRotationM);
    }

    private void smooth(float[] inv, float prevv[], float outv[])
    {
        float filterFactorInv = 1.0f - mFilterFactor;
        outv[0] = inv[0] * mFilterFactor + prevv[0] * filterFactorInv;
        outv[1] = inv[1] * mFilterFactor + prevv[1] * filterFactorInv;
        outv[2] = inv[2] * mFilterFactor + prevv[2] * filterFactorInv;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }
}