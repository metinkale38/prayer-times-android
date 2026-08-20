package com.metinkale.prayer.calendar

import com.metinkale.prayer.R
import dev.metinkale.openprayertimes.hijri.HijriEvent
import dev.metinkale.openprayertimes.hijri.HijriMonth

val HijriMonth.nameRes: Int
    get() = when (this) {
        HijriMonth.MUHARRAM -> R.string.hmonth1
        HijriMonth.SAFAR -> R.string.hmonth2
        HijriMonth.RABIAL_AWWAL -> R.string.hmonth3
        HijriMonth.RABIAL_AKHIR -> R.string.hmonth4
        HijriMonth.JUMADAAL_AWWAL -> R.string.hmonth5
        HijriMonth.JUMADAAL_AKHIR -> R.string.hmonth6
        HijriMonth.RAJAB -> R.string.hmonth7
        HijriMonth.SHABAN -> R.string.hmonth8
        HijriMonth.RAMADAN -> R.string.hmonth9
        HijriMonth.SHAWWAL -> R.string.hmonth10
        HijriMonth.DHUL_QADA -> R.string.hmonth11
        HijriMonth.DHUL_HIJJA -> R.string.hmonth12
    }
val HijriEvent.assetPath: String?
    get() = when (this) {
        HijriEvent.MONTH -> null
        HijriEvent.ISLAMIC_NEW_YEAR -> "/dinigunler/hicriyil.html"
        HijriEvent.ASHURA -> "/dinigunler/asure.html"
        HijriEvent.MAWLID_AL_NABI -> "/dinigunler/mevlid.html"
        HijriEvent.THREE_MONTHS -> "/dinigunler/3aylar.html"
        HijriEvent.RAGAIB -> "/dinigunler/regaib.html"
        HijriEvent.MIRAJ -> "/dinigunler/mirac.html"
        HijriEvent.BARAAH -> "/dinigunler/berat.html"
        HijriEvent.RAMADAN_BEGIN -> "/dinigunler/ramazan.html"
        HijriEvent.LAYLATALQADR -> "/dinigunler/kadir.html"
        HijriEvent.LAST_RAMADAN -> "/dinigunler/arefe.html"
        HijriEvent.EID_AL_FITR_DAY1 -> "/dinigunler/ramazanbay.html"
        HijriEvent.EID_AL_FITR_DAY2 -> "/dinigunler/ramazanbay.html"
        HijriEvent.EID_AL_FITR_DAY3 -> "/dinigunler/ramazanbay.html"
        HijriEvent.ARAFAT -> "/dinigunler/arefe.html"
        HijriEvent.EID_AL_ADHA_DAY1 -> "/dinigunler/kurban.html"
        HijriEvent.EID_AL_ADHA_DAY2 -> "/dinigunler/kurban.html"
        HijriEvent.EID_AL_ADHA_DAY3 -> "/dinigunler/kurban.html"
        HijriEvent.EID_AL_ADHA_DAY4 -> "/dinigunler/kurban.html"
    }


val HijriEvent.nameRes: Int?
    get() = when (this) {
        HijriEvent.MONTH -> null
        HijriEvent.ISLAMIC_NEW_YEAR -> R.string.holyday1
        HijriEvent.ASHURA -> R.string.holyday2
        HijriEvent.MAWLID_AL_NABI -> R.string.holyday3
        HijriEvent.THREE_MONTHS -> R.string.holyday4
        HijriEvent.RAGAIB -> R.string.holyday5
        HijriEvent.MIRAJ -> R.string.holyday6
        HijriEvent.BARAAH -> R.string.holyday7
        HijriEvent.RAMADAN_BEGIN -> R.string.holyday8
        HijriEvent.LAYLATALQADR -> R.string.holyday9
        HijriEvent.LAST_RAMADAN -> R.string.holyday10
        HijriEvent.EID_AL_FITR_DAY1 -> R.string.holyday11
        HijriEvent.EID_AL_FITR_DAY2 -> R.string.holyday12
        HijriEvent.EID_AL_FITR_DAY3 -> R.string.holyday13
        HijriEvent.ARAFAT -> R.string.holyday14
        HijriEvent.EID_AL_ADHA_DAY1 -> R.string.holyday15
        HijriEvent.EID_AL_ADHA_DAY2 -> R.string.holyday16
        HijriEvent.EID_AL_ADHA_DAY3 -> R.string.holyday17
        HijriEvent.EID_AL_ADHA_DAY4 -> R.string.holyday18
    }

