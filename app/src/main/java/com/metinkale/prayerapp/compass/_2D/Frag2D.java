/*
 * Copyright (c) 2016 Metin Kale
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
 *
 */

package com.metinkale.prayerapp.compass._2D;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.compass.LowPassFilter;
import com.metinkale.prayerapp.compass.Main;
import com.metinkale.prayerapp.compass.Main.MyCompassListener;

public class Frag2D extends Fragment implements MyCompassListener {
    private static final TimeInterpolator overshootInterpolator = new OvershootInterpolator();
    private static final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private CompassView mCompassView;
    private TextView mAngle;
    private TextView mDist;
    private View mInfo;
    private float[] mGravity = new float[3];
    private float[] mGeo = new float[3];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_2d, container, false);
        mCompassView = (CompassView) v.findViewById(R.id.compass);

        mAngle = (TextView) v.findViewById(R.id.angle);
        mDist = (TextView) v.findViewById(R.id.distance);
        mInfo = v.findViewById(R.id.infobox);

        View info = (View) mAngle.getParent();
        ViewCompat.setElevation(info, info.getPaddingTop());
        onUpdateDirection();
        return v;
    }

    @Override
    public void onUpdateDirection() {
        if (mCompassView != null) {
            mCompassView.setQiblaAngle((int) Main.getQiblaAngle());
            mAngle.setText(Utils.toArabicNrs(Math.round(mCompassView.getQiblaAngle()) + "°"));
            mDist.setText(Utils.toArabicNrs(Math.round(Main.getDistance()) + "km"));
        }

    }

    @Override
    public void onUpdateSensors(float[] rot) {
        if (mCompassView != null && getActivity() != null) {
            // mCompassView.setAngle(rot[0]);
            mGravity = LowPassFilter.filter(((Main) getActivity()).mMagAccel.mAccelVals, mGravity);
            mGeo = LowPassFilter.filter(((Main) getActivity()).mMagAccel.mMagVals, mGeo);

            if ((mGravity != null) && (mGeo != null)) {
                float[] R = new float[9];
                float[] I = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeo);
                if (success) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    mCompassView.setAngle((int) Math.toDegrees(orientation[0]));

                }
            }
        }

    }

    public boolean mHidden;

    public void show() {
        mHidden = false;
        mCompassView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 0, 1);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 0, 1);

                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 0, 1);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 0, 1);
                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2);
                animSetXY.setInterpolator(overshootInterpolator);
                animSetXY.setDuration(300);
                animSetXY.start();
            }
        });

    }

    public void hide() {
        mHidden = true;
        mCompassView.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCompassView, "scaleX", 1, 0);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCompassView, "scaleY", 1, 0);

                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mInfo, "scaleX", 1, 0);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mInfo, "scaleY", 1, 0);

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(scaleX, scaleY, scaleX2, scaleY2);
                animSetXY.setInterpolator(accelerateInterpolator);
                animSetXY.setDuration(300);
                animSetXY.start();
            }
        });

    }

}
