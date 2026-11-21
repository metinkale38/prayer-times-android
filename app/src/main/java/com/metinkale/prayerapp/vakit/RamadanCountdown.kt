/*
 * Copyright (c) 2013-2026 Metin Kale
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
package com.metinkale.prayerapp.vakit

import java.time.LocalDateTime

internal data class RamadanCountdownTarget(
    val target: LocalDateTime,
    val isIftar: Boolean
)

/**
 * Picks the next boundary for the Ramadan widget.
 *
 * During the day the widget counts down to iftar (maghrib). From maghrib until
 * the next imsak (fajr) it counts down to imsak, including the time after
 * midnight before the next fast begins.
 *
 * The previous day's iftar is part of the candidates as well. At very high
 * latitudes maghrib can fall after midnight, in which case the fast still ends
 * at that time and not at the following imsak.
 */
internal fun ramadanCountdownTarget(
    now: LocalDateTime,
    previousIftar: LocalDateTime,
    fajr: LocalDateTime,
    iftar: LocalDateTime,
    nextFajr: LocalDateTime
): RamadanCountdownTarget {
    val candidates = listOf(
        RamadanCountdownTarget(previousIftar, true),
        RamadanCountdownTarget(fajr, false),
        RamadanCountdownTarget(iftar, true),
        RamadanCountdownTarget(nextFajr, false)
    )
    return candidates.filter { it.target.isAfter(now) }
        .minByOrNull { it.target }
        ?: RamadanCountdownTarget(nextFajr, false)
}
