/*
 * Copyright (c) 2013-2017 Metin Kale
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

package com.metinkale.prayer.compass.magnetic.compass3D;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.compass.magnetic.DegreeLowPassFilter;
import com.metinkale.prayer.compass.magnetic.LowPassFilter;
import com.otaliastudios.cameraview.CameraView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Frag3D extends Fragment implements QiblaListener {
    
    private CompassView mCompassView;
    private CameraView mCamera;
    private double mQiblaAngle;
    private double mQiblaDistance;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_3d, container, false);
        mCompassView = v.findViewById(R.id.compass);
        
        mCamera = v.findViewById(R.id.camera);
        
        setQiblaAngle(mQiblaAngle);
        setQiblaDistance(mQiblaDistance);
        return v;
        
    }
    
    public void startCamera() {
        if (!mCamera.isStarted()) {
            mCamera.start();
        }
    }
    
    public void stopCamera() {
        if (mCamera.isStarted()) {
            mCamera.stop();
        }
    }
    
    
    @Override
    public void onDestroyView() {
        if (mCamera.isStarted())
            mCamera.stop();
        super.onDestroyView();
    }
    
    private LowPassFilter lowPassFilter = new DegreeLowPassFilter();
    
    public void onUpdateSensors(float[] rot) {
        if (mCompassView != null) {
            rot = lowPassFilter.filter(rot);
            
            mCompassView.setAngle(rot[0], rot[1], rot[2]);
        }
    }
    
    
    @Override
    public void setUserLocation(double lat, double lng, double alt) {
    
    }
    
    @Override
    public void setQiblaAngle(double angle) {
        mQiblaAngle = angle;
        if (mCompassView != null)
            mCompassView.setQiblaAngle(angle);
    }
    
    @Override
    public void setQiblaDistance(double distance) {
        mQiblaDistance = distance;
        if (mCompassView != null)
            mCompassView.setQiblaDistance(distance);
    }
}
