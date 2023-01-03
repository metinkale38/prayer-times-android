package com.metinkale.prayer.times.utils

import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.joda.time.LocalDate


inline fun <V : View, T> V.bind(
    lifecycle: Lifecycle,
    flow: Flow<T>,
    crossinline binding: V.(T) -> Unit
) = flow.distinctUntilChanged().asLiveData().observe({ lifecycle }) { binding(it) }

fun TextView.bindText(lifecycle: Lifecycle, text: Flow<CharSequence>) =
    bind(lifecycle, text) { this.text = it }


val dateFlow: Flow<LocalDate> = flow {
    var today = LocalDate.now()
    emit(today)

    while (true) {
        val next = today.plusDays(1)
        val wait = next.toDateTimeAtStartOfDay().toDateTime().millis - System.currentTimeMillis()
        delay(wait)
        today = next
        emit(today)
    }
}.shareIn(MainScope(), SharingStarted.Companion.Lazily, 1)

val secondsFlow: Flow<Int> = flow {
    val firstTime = System.currentTimeMillis()
    while (true) {
        val time = System.currentTimeMillis() - firstTime
        emit((time / 1000).toInt())
        delay(1000 - time % 1000)
    }
}

