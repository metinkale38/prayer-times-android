/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.receiver

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import com.metinkale.prayer.CrashReporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

interface AppEventListener {}
fun interface OnPrefsChangedListener : AppEventListener {
    fun onPrefsChanged(key: String)
}

fun interface OnStartListener : AppEventListener {
    fun onStart()
}

fun interface OnTimeTickListener : AppEventListener {
    fun onTimeTick()
}


fun interface OnScreenOnListener : AppEventListener {
    fun onScreenOn()
}

fun interface OnScreenOffListener : AppEventListener {
    fun onScreenOff()
}

object AppEventManager {
    private val listeners: MutableSet<AppEventListener> = mutableSetOf()
    private val job = Job()

    fun register(listener: AppEventListener) {
        listeners.add(listener)
    }

    fun unregister(listener: AppEventListener) {
        listeners.remove(listener)
    }

    fun sendOnPrefsChanged(key: String) = (MainScope() + job).launch {
        listeners.mapNotNull { it as? OnPrefsChangedListener }.forEach {
            try {
                it.onPrefsChanged(key)
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }

    fun sendOnStart() = (MainScope() + job).launch {
        listeners.mapNotNull { it as? OnStartListener }.forEach {
            try {
                it.onStart()
            } catch (e: Throwable) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || e !is ForegroundServiceStartNotAllowedException)
                    CrashReporter.recordException(e)
            }
        }
    }

    fun sendTimeTick() = (MainScope() + job).launch {
        listeners.mapNotNull { it as? OnTimeTickListener }.forEach {
            try {
                it.onTimeTick()
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }

    fun sendScreenOn() = (MainScope() + job).launch {
        listeners.mapNotNull { it as? OnScreenOnListener }.forEach {
            try {
                it.onScreenOn()
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }

    fun sendScreenOff() = (MainScope() + job).launch {
        listeners.mapNotNull { it as? OnScreenOffListener }.forEach {
            try {
                it.onScreenOff()
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }
}

