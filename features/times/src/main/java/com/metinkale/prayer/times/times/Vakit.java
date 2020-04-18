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

package com.metinkale.prayer.times.times;

import androidx.annotation.NonNull;

import com.metinkale.prayer.App;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.R;

public enum Vakit {
    FAJR(new int[]{R.string.imsak, R.string.fajr}, new String[]{"الإمساك", "الفجر"}),
    SUN(R.string.sun, "الشروق"),
    DHUHR(R.string.zuhr, "الظهر"),
    ASR(new int[]{R.string.asr, R.string.asrSani}, new String[]{"العصر الثاني", "العصر"}),
    MAGHRIB(R.string.maghrib, "المغرب"),
    ISHAA(R.string.ishaa, "العِشاء");


    public static final int LENGTH = values().length;
    private final String[] arabic;
    private final int[] resId;

    Vakit(int[] id, String[] arabic) {
        resId = id;
        this.arabic = arabic;
    }

    Vakit(int id, String arabic) {
        resId = new int[]{id};
        this.arabic = new String[]{arabic};
    }

    @NonNull
    public static Vakit getByIndex(int i) {
        while (i < 0) {
            i += 6;
        }
        while (i > 5) {
            i -= 6;
        }
        switch (i) {
            case 0:
                return FAJR;
            case 1:
                return SUN;
            case 2:
                return DHUHR;
            case 3:
                return ASR;
            case 4:
                return MAGHRIB;
            case 5:
            default:
                return ISHAA;
        }
    }


    public String getString() {
        // TR: Imsak (Default) - Sabah
        // Other: Imsak - Fajr (Default)
        // Background: some sources give two seperate times for imsak/fajr, to make sure, neither fasting, nor prayer gets invalid due to calculation errors

        if (this == FAJR) {
            if (!Preferences.USE_ARABIC.get() && Preferences.LANGUAGE.get().equals("tr")) {
                return getString(0);
            }
            return getString(1);
        }

        return getString(0);
    }

    public String getString(int index) {
        if (Preferences.USE_ARABIC.get()) {
            return arabic[index];
        }

        return App.get().getString(resId[index]);
    }


    public Vakit nextTime() {
        return Vakit.values()[(ordinal() + 1) % 6];
    }


    public Vakit prevTime() {
        return Vakit.values()[(ordinal() + 5) % 6];
    }
}