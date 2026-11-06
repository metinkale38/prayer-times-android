package com.metinkale.prayer.sync

import kotlinx.serialization.Serializable

@Serializable
data class WearDhikr(
    val id: Long,
    val title: String,
    val target: Int = 33,
    val count: Int = 0,
    val position: Int = 0,
    val color: Int = DhikrDefaults.color
)

object DhikrDefaults {
    val color: Int = 0xFF33B5E5.toInt()
}
