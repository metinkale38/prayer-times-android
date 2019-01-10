package com.metinkale.prayer.compass.magnetic;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.compass.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CalibrationFragment extends Fragment implements SensorEventListener {
    private SensorManager mSensorManager;
    private int mAccelerometerAccuracy;
    private int mMagneticAccuracy;
    private TextView mAccuracy;
    
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calibration_dialog, container, false);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mAccuracy = view.findViewById(R.id.accuracy_text);
        mAccuracy.setText(
                Html.fromHtml(String.format("%s: <font color='red'>%s</font>", getString(R.string.accuracy), getString(R.string.accuracy_low))));
        return view;
    }
    
    
    @Override
    public void onDestroyView() {
        mSensorManager.unregisterListener(this);
        super.onDestroyView();
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
    
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == 0)
            accuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW;
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerAccuracy = accuracy;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticAccuracy = accuracy;
                break;
            default:
                break;
        }
        if (mAccelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || mMagneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            mAccuracy.setText(Html.fromHtml(
                    String.format("%s: <font color='#ff0000'>%s</font>", getString(R.string.accuracy), getString(R.string.accuracy_low))));
        else if (mAccelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ||
                mMagneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            mAccuracy.setText(Html.fromHtml(
                    String.format("%s: <font color='#e6e600'>%s</font>", getString(R.string.accuracy), getString(R.string.accuracy_medium))));
        } else if (mAccelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH &&
                mMagneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            mAccuracy.setText(Html.fromHtml(
                    String.format("%s: <font color='#00ff00'>%s</font>", getString(R.string.accuracy), getString(R.string.accuracy_high))));
            
            mAccuracy.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getParentFragment().getChildFragmentManager().beginTransaction().remove(CalibrationFragment.this).commit();
                }
            }, 3000);
            
        }
    }
}
