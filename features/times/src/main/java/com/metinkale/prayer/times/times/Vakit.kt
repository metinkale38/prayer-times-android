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
package com.metinkale.prayer.times.times

import com.metinkale.prayer.App
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.R

enum class Vakit {
    FAJR(intArrayOf(R.string.imsak, R.string.fajr), arrayOf("الإمساك", "الفجر")), SUN(
        R.string.sun,
        "الشروق"
    ),
    DHUHR(R.string.zuhr, "الظهر"), ASR(
        intArrayOf(R.string.asr, R.string.asrSani),
        arrayOf("العصر", "العصر الثاني")
    ),
    MAGHRIB(R.string.maghrib, "المغرب"), ISHAA(R.string.ishaa, "العِشاء");

    private val arabic: Array<String>
    private val resId: IntArray

    constructor(id: IntArray, arabic: Array<String>) {
        resId = id
        this.arabic = arabic
    }

    constructor(id: Int, arabic: String) {
        resId = intArrayOf(id)
        this.arabic = arrayOf(arabic)
    }

    // TR: Imsak (Default) - Sabah
    // Other: Imsak - Fajr (Default)
    // Background: some sources give two seperate times for imsak/fajr, to make sure, neither fasting, nor prayer gets invalid due to calculation errors
    val string: String
        get() =// TR: Imsak (Default) - Sabah
        // Other: Imsak - Fajr (Default)
            // Background: some sources give two seperate times for imsak/fajr, to make sure, neither fasting, nor prayer gets invalid due to calculation errors
            if (this == FAJR) {
                if (!Preferences.USE_ARABIC && Preferences.LANGUAGE == "tr") {
                    getString(0)
                } else getString(1)
            } else getString(0)

    fun getString(index: Int): String {
        return if (Preferences.USE_ARABIC) {
            arabic[index]
        } else App.get().getString(resId[index])
    }

    companion object {
        val LENGTH = values().size
        @JvmStatic
        fun getByIndex(i: Int): Vakit {
            var i = i
            while (i < 0) {
                i += 6
            }
            while (i > 5) {
                i -= 6
            }
            return when (i) {
                0 -> FAJR
                1 -> SUN
                2 -> DHUHR
                3 -> ASR
                4 -> MAGHRIB
                5 -> ISHAA
                else -> ISHAA
            }
        }
    }
}