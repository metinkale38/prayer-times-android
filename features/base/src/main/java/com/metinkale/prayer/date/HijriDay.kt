package com.metinkale.prayer.date

import com.metinkale.prayer.base.R


enum class HijriDay(val resId: Int? = null, val assetPath: String? = null) {
    MONTH, // 0
    ISLAMIC_NEW_YEAR(R.string.holyday1, "/dinigunler/hicriyil.html"), // 1
    ASHURA(R.string.holyday2, "/dinigunler/asure.html"), // 2
    MAWLID_AL_NABI(R.string.holyday3, "/dinigunler/mevlid.html"), // 3
    THREE_MONTHS(R.string.holyday4, "/dinigunler/3aylar.html"), // 4
    RAGAIB(R.string.holyday5, "/dinigunler/regaib.html"), // 5
    MIRAJ(R.string.holyday6, "/dinigunler/mirac.html"), // 6
    BARAAH(R.string.holyday7, "/dinigunler/berat.html"), // 7
    RAMADAN_BEGIN(R.string.holyday8, "/dinigunler/ramazan.html"), // 8
    LAYLATALQADR(R.string.holyday9, "/dinigunler/kadir.html"), // 9
    LAST_RAMADAN(R.string.holyday10, "/dinigunler/arefe.html"), // 10
    EID_AL_FITR_DAY1(R.string.holyday11, "/dinigunler/ramazanbay.html"), // 11
    EID_AL_FITR_DAY2(R.string.holyday12, "/dinigunler/ramazanbay.html"), // 12
    EID_AL_FITR_DAY3(R.string.holyday13, "/dinigunler/ramazanbay.html"), // 13
    ARAFAT(R.string.holyday14, "/dinigunler/arefe.html"), // 14
    EID_AL_ADHA_DAY1(R.string.holyday15, "/dinigunler/kurban.html"), // 15
    EID_AL_ADHA_DAY2(R.string.holyday16, "/dinigunler/kurban.html"), // 16
    EID_AL_ADHA_DAY3(R.string.holyday17, "/dinigunler/kurban.html"), // 17
    EID_AL_ADHA_DAY4(R.string.holyday18, "/dinigunler/kurban.html") // 18
}