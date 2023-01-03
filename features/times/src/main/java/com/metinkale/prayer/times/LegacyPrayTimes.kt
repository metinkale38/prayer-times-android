/*
PrayTimes-Java: Prayer Times Java Calculator (ver 0.9)

Copyright (C) 2007-2011 PrayTimes.org (JS Code ver 2.3)
Copyright (C) 2017 Metin Kale (Java Code)

Developer JS: Hamid Zarrabi-Zadeh
Developer Java: Metin Kale

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/
package com.metinkale.prayer.times

import dev.metinkale.prayertimes.calc.HighLatsAdjustment
import dev.metinkale.prayertimes.calc.Midnight
import kotlinx.serialization.Serializable

@Serializable
data class LegacyPrayTimes(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val elv: Double = 0.0,
    val highLats: HighLatsAdjustment,
    val midnight: Midnight,
    val timeZone: String = "UTC",
    val minutes: List<Int> = List(LegacyTimes.values().size) { 0 },
    val angles: List<Double> = List(LegacyTimes.values().size) { 0.0 },
) {

}
enum class LegacyTimes {
    Imsak,
    Fajr,
    Sunrise,
    Zawal,
    Dhuhr,
    AsrShafi,
    AsrHanafi,
    Sunset,
    Maghrib,
    Ishaa,
    Midnight
}