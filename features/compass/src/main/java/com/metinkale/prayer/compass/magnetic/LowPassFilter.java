/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.compass.magnetic;

import androidx.annotation.NonNull;

public class LowPassFilter {
    
    /*
     * smoothing constant for low-pass filter 0 ≤ α ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private final float alpha;
    protected float[] last;
    
    public LowPassFilter() {
        alpha = 0.1f;
    }
    
    public LowPassFilter(float alpha) {
        this.alpha = alpha;
    }
    
    
    /**
     * Filter the given input against the previous values and return a low-pass
     * filtered result.
     *
     * @param input float array to smooth.
     * @return float array smoothed with a low-pass filter.
     */
    @NonNull
    public float[] filter(@NonNull float... input) {
        if (last == null) {
            last = new float[input.length];
            System.arraycopy(input, 0, last, 0, input.length);
            return input;
        }
        
        for (int i = 0; i < input.length; i++) {
            last[i] += alpha * (input[i] - last[i]);
        }
        return last;
    }
}