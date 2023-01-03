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

package com.metinkale.prayer.compass.magnetic;

import androidx.annotation.NonNull;

public class DegreeLowPassFilter extends LowPassFilter {
    
    public DegreeLowPassFilter() {
        super();
    }
    
    public DegreeLowPassFilter(float alpha) {
        super(alpha);
    }
    
    @NonNull
    public float[] filter(@NonNull float... input) {
        if (last == null) {
            return super.filter(input);
        }
        
        for (int i = 0; i < input.length; i++) {
            while (input[i] - last[i] > 180) {
                last[i] += 360;
            }
            while (input[i] - last[i] < -180) {
                last[i] -= 360;
            }
        }
        return super.filter(input);
    }
}