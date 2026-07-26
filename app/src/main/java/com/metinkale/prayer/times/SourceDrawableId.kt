package com.metinkale.prayer.times
import com.metinkale.prayer.R

import dev.metinkale.openprayertimes.sources.Source


val Source.drawableId: Int?
    get() = when (this) {
        Source.Diyanet -> R.drawable.ic_times_ditib
        Source.IGMG -> R.drawable.ic_times_igmg
        Source.Semerkand -> R.drawable.ic_times_semerkand
        Source.NVC -> R.drawable.ic_times_namazvakticom
        else -> null
    }



