package com.metinkale.prayer.times.times

import com.metinkale.prayer.R


enum class Source(val fullName: String, val drawableId: Int? = null) {
    Diyanet("Diyanet", R.drawable.ic_times_ditib),
    IGMG("IGMG", R.drawable.ic_times_igmg),
    London("LondonPrayerTimes.com"),
    NVC("NamazVakti.com", R.drawable.ic_times_namazvakticom),
    Semerkand("SemerkandTakvimi", R.drawable.ic_times_semerkand),
    Calc("Calc"),
    CSV("CSV");
}
