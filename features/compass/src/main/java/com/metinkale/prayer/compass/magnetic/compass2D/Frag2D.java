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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.metinkale.prayer.compass.QiblaListener;
import com.metinkale.prayer.compass.R;
import com.metinkale.prayer.compass.magnetic.DegreeLowPassFilter;
import com.metinkale.prayer.utils.LocaleUtils;

public class Frag2D extends Fragment implements QiblaListener {
    private static final TimeInterpolator overshootInterpolator = new OvershootInterpolator();
    private static final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private CompassView mCompassView;
    private TextView mAngleTV;
    private TextView mDistanceTV;
    private View mInfo;
    private View mBG;
    private boolean mHidden;
    private int mAngle;
    private double mQiblaDistance;
    private double mQiblaAngle;
    
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_2d, container, false);
        mCompassView = v.findViewById(R.id.compass);
        if (mHidden) {
            mCompassView.setScaleX(0);
            mCompassView.setScaleY(0);
        }
        
        mAngleTV = v.findViewById(R.id.angle);
        mDistanceTV = v.findViewById(R.id.distance);
        mInfo = v.findViewById(R.id.infobox);
        mBG = v.findViewById(R.id.background);
        View info = (View) mAngleTV.getParent();
        ViewCompat.setElevation(info, info.getPaddingTop());
        
        setAngle(mAngle);
        setQiblaAngle(mQiblaAngle);
        setQiblaDistance(mQiblaDistance);
        return v;
    }
    
    
    public void show() {
        mHidden = false;
        mCompassView.post(() -> {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 0, 1);

            ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 0, 1);
            ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 0, 1);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(mBG, "alpha", 0, 1);

            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2, alpha);
            animSetXY.setInterpolator(overshootInterpolator);
            animSetXY.setDuration(300);
            animSetXY.start();
        });
        
    }
    
    public boolean isFragmentHidden() {
        return mHidden;
    }
    
    public void hide() {
        mHidden = true;
        if (mCompassView != null)
            mCompassView.post(() -> {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 1, 0);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 1, 0);

                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 1, 0);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 1, 0);

                ObjectAnimator alpha = ObjectAnimator.ofFloat(mBG, "alpha", 1, 0);


                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2, alpha);
                animSetXY.setInterpolator(accelerateInterpolator);
                animSetXY.setDuration(300);
                animSetXY.start();
            });
        
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
