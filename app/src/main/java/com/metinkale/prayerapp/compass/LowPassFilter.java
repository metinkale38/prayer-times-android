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

package com.metinkale.prayerapp.compass;

import android.support.annotation.Nullable;

public class LowPassFilter {

    /*
     * com.metinkale.calendar.Time smoothing constant for low-pass filter 0 ≤ α ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private static final float ALPHA = 0.1f;

    private LowPassFilter() {
    }

    /**
     * Filter the given input against the previous values and return a low-pass
     * filtered result.
     *
     * @param input float array to smooth.
     * @param prev  float array representing the previous values.
     * @return float array smoothed with a low-pass filter.
     */
    @Nullable
    public static float[] filter(@Nullable float[] input, @Nullable float[] prev) {
        if ((input == null) || (prev == null)) {
            throw new NullPointerException("input and prev float arrays must be non-NULL");
        }
        if (input.length != prev.length) {
            throw new IllegalArgumentException("input and prev must be the same length");
        }

        for (int i = 0; i < input.length; i++) {
            prev[i] += ALPHA * (input[i] - prev[i]);
        }
        return prev;
    }
}