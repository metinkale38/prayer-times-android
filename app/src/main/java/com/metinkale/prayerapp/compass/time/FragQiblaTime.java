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

package com.metinkale.prayerapp.compass.time;

import android.animation.TimeInterpolator;
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
import com.metinkale.prayerapp.compass.Main;
import com.metinkale.prayerapp.compass.Main.MyCompassListener;
import com.metinkale.prayerapp.utils.Utils;

public class FragQiblaTime extends Fragment implements MyCompassListener {
    private static final TimeInterpolator overshootInterpolator = new OvershootInterpolator();
    private static final TimeInterpolator accelerateInterpolator = new AccelerateInterpolator();

    private QiblaTimeView mQiblaTimeView;
    private TextView mAngle;
    private TextView mDist;
    private View mInfo;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_qiblatime, container, false);
        mQiblaTimeView = (QiblaTimeView) v.findViewById(R.id.qiblatime);

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
        if (mQiblaTimeView != null) {
            double angle = ((Main) getActivity()).getQiblaAngle();
            if (angle < 0) {
                angle += 360;
            }
            mAngle.setText(Utils.toArabicNrs(Math.round(angle) + "°"));
            mDist.setText(Utils.toArabicNrs(Math.round(((Main) getActivity()).getDistance()) + "km"));
            mQiblaTimeView.setLocation(((Main) getActivity()).getLocation(), ((Main) getActivity()).getQiblaAngle());
        }

    }

    @Override
    public void onUpdateSensors(float[] rot) {
        mQiblaTimeView.invalidate();
    }


}
