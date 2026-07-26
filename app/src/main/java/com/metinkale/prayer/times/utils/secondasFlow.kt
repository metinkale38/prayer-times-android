package com.metinkale.prayer.times.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


val secondsFlow: Flow<Int> = flow {
    val firstTime = System.currentTimeMillis()
    while (true) {
        val time = System.currentTimeMillis() - firstTime
        emit((time / 1000).toInt())
        delay(1000 - time % 1000)
    }
}

