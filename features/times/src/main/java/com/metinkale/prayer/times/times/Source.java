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

import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;

import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.sources.CSVTimes;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.times.times.sources.DiyanetTimes;
import com.metinkale.prayer.times.times.sources.FaziletTimes;
import com.metinkale.prayer.times.times.sources.IGMGTimes;
import com.metinkale.prayer.times.times.sources.IndonesiaTimes;
import com.metinkale.prayer.times.times.sources.LondonTimes;
import com.metinkale.prayer.times.times.sources.MalaysiaTimes;
import com.metinkale.prayer.times.times.sources.MoroccoTimes;
import com.metinkale.prayer.times.times.sources.NVCTimes;
import com.metinkale.prayer.times.times.sources.SemerkandTimes;
import com.metinkale.prayer.utils.LocaleUtils;

import java.util.Locale;

/**
 * Created by metin on 03.04.2016.
 */
public enum Source {
    Calc(R.string.calculated, 0, CalcTimes.class), Diyanet("Diyanet.gov.tr", R.drawable.ic_ditib, R.raw.diyanet, DiyanetTimes.class, "tr"),
    @Deprecated Fazilet("FaziletTakvimi.com", R.drawable.ic_fazilet, 0, FaziletTimes.class),
    IGMG("IGMG.org", R.drawable.ic_igmg, R.raw.igmg, IGMGTimes.class),
    Semerkand("SemerkandTakvimi.com", R.drawable.ic_semerkand, R.raw.semerkand, SemerkandTimes.class),
    NVC("NamazVakti.com", R.drawable.ic_namazvakticom, R.raw.nvc, NVCTimes.class),
    @Deprecated Morocco("habous.gov.ma", R.drawable.ic_morocco, 0, MoroccoTimes.class, "ma"),
    @Deprecated Malaysia("e-solat.gov.my", R.drawable.ic_malaysia, 0, MalaysiaTimes.class, "my"),
    @Deprecated Indonesia("Kemenag.go.id", R.drawable.ic_indonesia, 0, IndonesiaTimes.class, "id"),
    London("Londonprayertimes.com", 0, R.raw.london, LondonTimes.class, "gb"), CSV("CSV", 0, 0, CSVTimes.class);

    public final Class<? extends Times> clz;
    public final int drawableId;
    public final String name;
    public final int citiesId;
    public final String[] prioCountries;

    Source(String name, @DrawableRes int drawableId, @RawRes int citiesId, Class<? extends Times> clz, String... prio) {
        this.name = name;
        this.drawableId = drawableId;
        this.clz = clz;
        this.citiesId = citiesId;
        this.prioCountries = prio;
    }

    Source(int resTxt, int resIcon, Class<? extends Times> clz, String... prio) {
        Locale.setDefault(LocaleUtils.getLocale());
        this.name = App.get().getString(resTxt);
        this.drawableId = resIcon;
        this.clz = clz;
        this.prioCountries = prio;
        citiesId = 0;
    }
}