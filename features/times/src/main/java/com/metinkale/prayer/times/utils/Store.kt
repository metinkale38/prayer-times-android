package com.metinkale.prayer.times.utils

import android.util.Log
import com.metinkale.prayer.CrashReporter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * its a StateFlow which is updatable.
 *
 * In contrast to MutableStateFlow this Class supports creating Sub-UpdatableStateFlow's e.g. for reading and updating submodels
 */
interface Store<T> {
    val data: Flow<T>
    val current: T

    fun update(update: (T) -> T)

    fun <R> map(get: (T) -> R, set: (T, R) -> T): Store<R> =
        SubStore(this, get, set)
}


fun <T> MutableStateFlow<T>.asStore(): Store<T> =
    RootStore(this)

fun <T, I> Store<List<T>>.mapById(
    id: I,
    idProvider: (T) -> I
): Store<T?> = map(get = {
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


private class RootStore<T>(private val state: MutableStateFlow<T>) : Store<T> {

    val job = Job()
    val mutex = Mutex()

    override fun update(update: (T) -> T) {
        flow { emit(update) }.onEach { enqueue { d -> update(d) } }
            .catch { e -> CrashReporter.recordException(e) }
            .launchIn(MainScope() + job)
    }


    private suspend fun enqueue(update: (T) -> T) {
        mutex.withLock {
            state.value = update(state.value)

        }
    }

    override val data: Flow<T> = state
    override val current: T get() = state.value

}

private class SubStore<T, R>(
    val parent: Store<T>,
    val get: (T) -> R,
    val set: (T, R) -> T
) : Store<R> {
    override val data: Flow<R> = parent.data.map(get).distinctUntilChanged()
    override val current: R get() = get(parent.current)
    override fun update(update: (R) -> R) {
        parent.update { set(it, update(get(it))) }
    }
}

