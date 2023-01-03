package com.metinkale.prayer.times.utils

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * its a StateFlow which is updatable.
 *
 * In contrast to MutableStateFlow this Class supports creating Sub-UpdatableStateFlow's e.g. for reading and updating submodels
 */
interface UpdatableStateFlow<T> : StateFlow<T> {
    fun update(update: (T) -> T)

    fun <R> map(get: (T) -> R, set: (T, R) -> T): UpdatableStateFlow<R> =
        SubUpdatableStateFlow(this, get, set)
}


fun <T> MutableStateFlow<T>.asStore(): UpdatableStateFlow<T> = BaseUpdatableStateFlow(this)

fun <T, I> UpdatableStateFlow<List<T>>.mapById(id: I, idProvider: (T) -> I): UpdatableStateFlow<T?> = map(get = {
    it.firstOrNull { idProvider(it) == id }
}, set = { parent, item ->
    if (item == null) {
        parent.filter { idProvider(it) != id }
    } else if (parent.any { id == idProvider(it) }) {
        parent.map { if (id == idProvider(it)) item else it }
    } else {
        parent + item
    }
})

private class MappedStateFlow<T, R>(val stateFlow: StateFlow<T>, val map: (T) -> R) : StateFlow<R> {
    override val replayCache: List<R>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing =
        stateFlow.collect { value -> collector.emit(map(value)) }

    override val value: R
        get() = map(stateFlow.value)

}

private class BaseUpdatableStateFlow<T>(val flow: MutableStateFlow<T>) : UpdatableStateFlow<T>,
    StateFlow<T> by flow {
    override fun update(update: (T) -> T) = flow.update(update)
}

private class SubUpdatableStateFlow<T, R>(
    val parent: UpdatableStateFlow<T>,
    val get: (T) -> R,
    val set: (T, R) -> T
) : UpdatableStateFlow<R>, StateFlow<R> by MappedStateFlow(parent, get) {
    override fun update(update: (R) -> R) = parent.update { set(it, update(get(it))) }
}

