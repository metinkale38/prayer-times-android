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

package com.metinkale.prayer.compass.time;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.utils.LocaleUtils;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

public class FragQiblaTime extends Fragment implements QiblaListener {
    
    private QiblaTimeView mQiblaTimeView;
    private TextView mQiblaAngle;
    private TextView mQiblaDistance;
    private double mLat;
    private double mLng;
    private double mAlt;
    private double mDistance;
    private double mAngle;
    
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_qiblatime, container, false);
        mQiblaTimeView = v.findViewById(R.id.qiblatime);
        
        mQiblaAngle = v.findViewById(R.id.angle);
        mQiblaDistance = v.findViewById(R.id.distance);
        
        View info = (View) mQiblaAngle.getParent();
        ViewCompat.setElevation(info, info.getPaddingTop());
        
        
        setQiblaAngle(mAngle);
        setQiblaDistance(mDistance);
        setUserLocation(mLat,mLng,mAlt);
        return v;
    }
    
    
    @Override
    public void setUserLocation(double lat, double lng, double alt) {
        mLat = lat;
        mLng = lng;
        mAlt = alt;
        if (mQiblaTimeView != null)
            mQiblaTimeView.setLocation(lat, lng, alt);
    }
    
    @Override
    public void setQiblaAngle(double angle) {
        mAngle=angle;
        if (mQiblaAngle == null)
            return;
        if (angle < 0) {
            angle += 360;
        }
        mQiblaAngle.setText(LocaleUtils.toArabicNrs(Math.round(angle) + "°"));
        mQiblaTimeView.setAngle(angle);
        
    }
    
    @Override
    public void setQiblaDistance(double distance) {
        mDistance=distance;
        if (mQiblaDistance == null)
            return;
        mQiblaDistance.setText(LocaleUtils.toArabicNrs(Math.round(distance) + "km"));
        
    }
}
