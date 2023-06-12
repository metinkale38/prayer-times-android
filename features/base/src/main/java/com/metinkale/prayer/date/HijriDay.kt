package com.metinkale.prayer.date

import com.metinkale.prayer.base.R


enum class HijriDay(val resId: Int) {
    MONTH(0), // 0
    ISLAMIC_NEW_YEAR(R.string.holyday1), // 1
    ASHURA(R.string.holyday2), // 2
    MAWLID_AL_NABI(R.string.holyday3), // 3
    THREE_MONTHS(R.string.holyday4), // 4
    RAGAIB(R.string.holyday5), // 5
    MIRAJ(R.string.holyday6), // 6
    BARAAH(R.string.holyday7), // 7
    RAMADAN_BEGIN(R.string.holyday8), // 8
    LAYLATALQADR(R.string.holyday9), // 9
    LAST_RAMADAN(R.string.holyday10), // 10
    EID_AL_FITR_DAY1(R.string.holyday11), // 11
    EID_AL_FITR_DAY2(R.string.holyday12), // 12
    EID_AL_FITR_DAY3(R.string.holyday13), // 13
    ARAFAT(R.string.holyday14), // 14
    EID_AL_ADHA_DAY1(R.string.holyday15), // 15
    EID_AL_ADHA_DAY2(R.string.holyday16), // 16
    EID_AL_ADHA_DAY3(R.string.holyday17), // 17
    EID_AL_ADHA_DAY4(R.string.holyday18) // 18
}