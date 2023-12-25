package com.metinkale.prayer.times

import dev.metinkale.prayertimes.core.sources.Source


val Source.drawableId: Int?
    get() = when (this) {
        Source.Diyanet -> R.drawable.ic_ditib
        Source.IGMG -> R.drawable.ic_igmg
        Source.Semerkand -> R.drawable.ic_semerkand
        Source.NVC -> R.drawable.ic_namazvakticom
        else -> null
    }



