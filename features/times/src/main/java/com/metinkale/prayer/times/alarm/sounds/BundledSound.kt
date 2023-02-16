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
package com.metinkale.prayer.times.alarm.sounds

import android.media.MediaPlayer
import androidx.collection.ArraySet
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getCurrentTime
import kotlinx.serialization.Serializable

data class BundledSound(
    override val name: String,
    val subSounds: MutableMap<SoundType, Sound> = mutableMapOf()
) : Sound {
    init {
        Sounds.addSound(this)
    }

    enum class SoundType {
        Default, Fajr, FajrShort, Zuhr, Asr, Magrib, Ishaa, Dua, Takbir, Sela
    }

    fun addSound(type: SoundType, sound: Sound) {
        subSounds[type] = sound
    }

    override val appSounds: Set<AppSound?>
        get() {
            val set = ArraySet<AppSound?>()
            for (sound in subSounds.values) {
                set.addAll(sound.appSounds)
            }
            return set
        }
    override val shortName: String
        get() = name

    private fun <K, V> Map<K, V>.getFirstOfOrFirst(vararg key: K): V? =
        key.firstNotNullOfOrNull { get(it) } ?: values.firstOrNull()

    override fun createMediaPlayer(alarm: Alarm?): MediaPlayer? {
        if (!isDownloaded) return null

        val prio = mutableListOf(
            SoundType.Fajr,
            SoundType.Zuhr,
            SoundType.Asr,
            SoundType.Magrib,
            SoundType.Ishaa,
            SoundType.Takbir,
            SoundType.Default
        )
        return if (alarm == null) {
            subSounds.getFirstOfOrFirst(*prio.toTypedArray())
        } else {
            prio.removeLast()
            when (Vakit.getByIndex(alarm.city.getCurrentTime())) {
                Vakit.FAJR -> subSounds.getFirstOfOrFirst(*prio.toTypedArray())
                Vakit.SUN, Vakit.DHUHR -> subSounds.getFirstOfOrFirst(*prio.drop(1).toTypedArray())
                Vakit.ASR -> subSounds.getFirstOfOrFirst(*prio.drop(2).toTypedArray())
                Vakit.MAGHRIB -> subSounds.getFirstOfOrFirst(*prio.drop(3).toTypedArray())
                Vakit.ISHAA -> subSounds.getFirstOfOrFirst(*prio.drop(4).toTypedArray())
            }
        }?.createMediaPlayer(alarm)
    }

    override val isDownloaded: Boolean
        get() {
            var dled = true
            for (sound in subSounds.values) {
                if (!sound.isDownloaded) {
                    dled = false
                    break
                }
            }
            return dled
        }
    override val size: Int
        get() {
            var size = 0
            for (sound in subSounds.values) {
                size += sound.size
            }
            return size
        }
    override val id: Int
        get() = name.hashCode()
}