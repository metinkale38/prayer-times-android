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
import com.metinkale.prayer.times.alarm.Alarm
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SoundSerializer::class)
sealed interface Sound {
    val name: String?
    val shortName: String?
    fun createMediaPlayer(alarm: Alarm?): MediaPlayer?
    val isDownloaded: Boolean
    val size: Int
    val id: Int
    val appSounds: Set<AppSound?>
}

object SoundSerializer : KSerializer<Sound> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SoundSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Sound) {
        return if (value is UserSound) {
            encoder.encodeString(value.uri.toString())
        } else encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): Sound {
        return runCatching {
            Sounds.getSound(decoder.decodeInt())
        }.getOrNull() ?: runCatching {
            Sounds.getSound(decoder.decodeString())
        }.getOrNull()!!
    }
}