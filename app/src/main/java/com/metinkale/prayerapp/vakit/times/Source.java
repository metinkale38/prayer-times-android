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