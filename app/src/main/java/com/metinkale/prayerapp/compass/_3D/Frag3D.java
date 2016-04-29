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
 */

package com.metinkale.prayerapp.compass._3D;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.compass.Main;
import com.metinkale.prayerapp.compass.Main.MyCompassListener;

public class Frag3D extends Fragment implements MyCompassListener {

    private CompassView mCompassView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.compass_3d, container, false);
        mCompassView = (CompassView) v.findViewById(R.id.compass);
        onUpdateDirection();
        return v;

    }

    @Override
    public void onUpdateDirection() {
        if (mCompassView != null) {
            mCompassView.setQibla(Main.getQiblaAngle(), Main.getDistance());
        }

    }

    @Override
    public void onUpdateSensors(float[] rot) {
        if (mCompassView != null) {
            mCompassView.setAngle(rot[0], rot[1], rot[2]);
        }
    }
}
