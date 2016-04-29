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

package com.metinkale.prayerapp.vakit.times;

import com.metinkale.prayer.R;

/**
 * Created by metin on 03.04.2016.
 */
public enum Source {
    Calc("Hesaplanmis", 0), Diyanet("Diyanet", R.drawable.ic_ditib), Fazilet("Fazilet Takvimi", R.drawable.ic_fazilet), IGMG("IGMG", R.drawable.ic_igmg), Semerkand("Semerkand", R.drawable.ic_semerkand), NVC("NamazVakti.com", R.drawable.ic_namazvakticom);

    public int resId;
    public String text;

    Source(String text, int resId) {
        this.text = text;
        this.resId = resId;
    }
}