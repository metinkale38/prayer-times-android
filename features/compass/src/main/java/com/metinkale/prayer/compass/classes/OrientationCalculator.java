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

package com.metinkale.prayer.compass.classes;

import com.metinkale.prayer.compass.classes.math.Matrix4;

public interface OrientationCalculator {
    /**
     * Given a rotation matrix and the current device screen rotation, produce
     * values for azimuth, altitude, roll (yaw, pitch, roll)
     *
     * @param rotationMatrix - device rotation
     * @param screenRotation - device screen rotation
     * @param out            - array of float[3] to dump values into
     */
    void getOrientation(Matrix4 rotationMatrix, int screenRotation, float[] out);
}
