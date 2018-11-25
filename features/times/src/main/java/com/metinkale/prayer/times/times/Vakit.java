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

package com.metinkale.prayer.times.times;

import com.metinkale.prayer.App;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.R;

import androidx.annotation.NonNull;

public enum Vakit {
    IMSAK(R.string.fajr, "فجر"), GUNES(R.string.sun, "شروق"), OGLE(R.string.zuhr, "ظهر"), IKINDI(R.string.asr, "عصر"),
    AKSAM(R.string.maghrib, "مغرب"), YATSI(R.string.ishaa, "عشاء");
    
    
    private static int SIZE = values().length;
    
    private final String arabic;
    private String name;
    private final int resId;
    
    Vakit(int id, String arabic) {
        resId = id;
        this.arabic = arabic;
    }
    
    @NonNull
    public static Vakit getByIndex(int index) {
        while (index < 0) {
            index += SIZE;
        }
        
        while (index >= SIZE) {
            index -= SIZE;
        }
        return values()[index];
    }
    
    public String getString() {
        if (Preferences.USE_ARABIC.get()) {
            return arabic;
        }
        
        return App.get().getString(resId);
    }
    
    
    
    public Vakit nextTime() {
        return Vakit.values()[(ordinal() + 1) % 6];
    }
    
    
    public Vakit prevTime() {
        return Vakit.values()[(ordinal() + 5) % 6];
    }
}