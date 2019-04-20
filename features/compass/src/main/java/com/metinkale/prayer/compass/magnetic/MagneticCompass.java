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

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.compass.magnetic.compass2D.Frag2D;
import com.metinkale.prayer.compass.magnetic.compass3D.Frag3D;
import com.metinkale.prayer.compass.magnetic.utils.OrientationCalculator;
import com.metinkale.prayer.compass.magnetic.utils.math.Matrix4;
import com.metinkale.prayer.compass.magnetic.utils.rotation.MagAccelListener;
import com.metinkale.prayer.compass.magnetic.utils.rotation.RotationUpdateDelegate;
import com.metinkale.prayer.utils.PermissionUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MagneticCompass extends Fragment implements QiblaListener, RotationUpdateDelegate {
    
    
    private MagAccelListener mMagAccel;
    @NonNull
    private Matrix4 mRotationMatrix = new Matrix4();
    private int mDisplayRotation;
    private SensorManager mSensorManager;
    
    
    @NonNull
    private OrientationCalculator mOrientationCalculator = new OrientationCalculator();
    private Frag2D mFrag2D = new Frag2D();
    private Frag3D mFrag3D = new Frag3D();
    
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
        fragmentTransaction.add(R.id.frag3D, mFrag3D, "3d");
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
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                break;
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
        
        if (PermissionUtils.get(getActivity()).pCamera) {
            if (deviceOrientation[1] > -55f) {
                if (!mFrag2D.isFragmentHidden()) {
                    mFrag2D.hide();
                }
            } else {
                if (mFrag2D.isFragmentHidden()) {
                    mFrag2D.show();
                }
            }
            
            if (deviceOrientation[1] > -30f) {
                mFrag3D.startCamera();
            } else {
                mFrag3D.stopCamera();
            }
        }
        mFrag3D.onUpdateSensors(deviceOrientation);
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
        mFrag3D.setUserLocation(lat, lng, alt);
    }
    
    @Override
    public void setQiblaAngle(double angle) {
        mFrag2D.setQiblaAngle(angle);
        mFrag3D.setQiblaAngle(angle);
    }
    
    @Override
    public void setQiblaDistance(double distance) {
        mFrag2D.setQiblaDistance(distance);
        mFrag3D.setQiblaDistance(distance);
    }
    
    
}
