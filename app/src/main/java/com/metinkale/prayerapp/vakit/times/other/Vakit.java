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

package com.metinkale.prayerapp.vakit.times.other;

import android.support.annotation.NonNull;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

public enum Vakit {
    IMSAK(R.string.fajr, 0, "الإمساك"), SABAH(R.string.morningPrayer, 0, "فجر"), GUNES(R.string.sun, 1, "شروق"), OGLE(R.string.zuhr, 2, "ظهر"), IKINDI(R.string.asr, 3, "عصر"), AKSAM(R.string.maghrib, 4, "مغرب"), YATSI(R.string.ishaa, 5, "عشاء");

    public int index;

    private String arabic;
    private String name;
    private int resId;

    Vakit(int id, int index, String arabic) {
        resId = id;
        this.index = index;
        this.arabic = arabic;
    }

    @NonNull
    public static Vakit getByIndex(int index) {
        switch (index) {
            case 0:
                if (Prefs.useArabic()) {
                    return SABAH;
                }
                return IMSAK;
            case 1:
                return GUNES;
            case 2:
                return OGLE;
            case 3:
                return IKINDI;
            case 4:
                return AKSAM;
            case 5:
            case -1:

                return YATSI;
        }
        return IMSAK;
    }

    public String getString() {
        if (Prefs.useArabic()) {
            return arabic;
        }
        if (name == null) {
            name = App.get().getString(resId);
        }

        return name;
    }
}