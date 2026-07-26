package com.metinkale.prayer.times.times

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BooleanLegacy", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return runCatching {
            decoder.decodeBoolean()
        }.getOrNull() ?: runCatching {
            decoder.decodeInt() == 1
        }.getOrNull() ?: runCatching {
            decoder.decodeString() == "true"
        }.getOrNull() ?: false
    }
}