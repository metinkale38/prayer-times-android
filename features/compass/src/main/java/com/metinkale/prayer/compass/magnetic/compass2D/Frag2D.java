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

package com.metinkale.prayer.compass.magnetic.compass2D;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.compass.magnetic.DegreeLowPassFilter;
import com.metinkale.prayer.utils.LocaleUtils;

public class Frag2D extends Fragment implements QiblaListener {
    private CompassView mCompassView;
    private TextView mAngleTV;
    private TextView mDistanceTV;
    private int mAngle;
    private double mQiblaDistance;
    private double mQiblaAngle;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_2d, container, false);
        mCompassView = v.findViewById(R.id.compass);

        
        mAngleTV = v.findViewById(R.id.angle);
        mDistanceTV = v.findViewById(R.id.distance);
        View info = (View) mAngleTV.getParent();
        ViewCompat.setElevation(info, info.getPaddingTop());
        
        setAngle(mAngle);
        setQiblaAngle(mQiblaAngle);
        setQiblaDistance(mQiblaDistance);
        return v;
    }
    

    @Override
    public void setUserLocation(double lat, double lng, double alt) {
    
    }
    
    @Override
    public void setQiblaAngle(double angle) {
        mQiblaAngle = angle;
        if (mAngleTV != null) {
            mAngleTV.setText(LocaleUtils.formatNumber(Math.round(angle) + "°"));
            mCompassView.setQiblaAngle((int) Math.round(angle));
        }
    }
    
    @Override
    public void setQiblaDistance(double distance) {
        mQiblaDistance = distance;
        if (mDistanceTV != null)
            mDistanceTV.setText(LocaleUtils.formatNumber(Math.round(distance) + "km"));
    }
    
    private final DegreeLowPassFilter lowPassFilter = new DegreeLowPassFilter();
    
    public void setAngle(int angle) {
        mAngle = angle;
        if (mCompassView == null)
            return;
        mCompassView.setAngle(lowPassFilter.filter(angle)[0]);
    }
    
}
