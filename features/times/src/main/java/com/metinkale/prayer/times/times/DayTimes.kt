package com.metinkale.prayer.times.times

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class DayTimes(
    /** Date */
    @Serializable(LocalDateSerializer::class)
    val date: LocalDate,
    /** Time for fajr prayer */
    @Serializable(LocalTimeSerializer::class)
    val fajr: LocalTime,
    /** Sunrise */
    @Serializable(LocalTimeSerializer::class)
    val sun: LocalTime,
    /** Time for Dhuhr Prayer */
    @Serializable(LocalTimeSerializer::class)
    val dhuhr: LocalTime,
    /** Time for Asr Prayer */
    @Serializable(LocalTimeSerializer::class)
    val asr: LocalTime,
    /** Time for Maghrib Prayer */
    @Serializable(LocalTimeSerializer::class)
    val maghrib: LocalTime,
    /** Time for Ishaa Prayer */
    @Serializable(LocalTimeSerializer::class)
    val ishaa: LocalTime,
    /** OPTIONAL: asr according to Hanafi () */
    @Serializable(LocalTimeSerializer::class)
    val asrHanafi: LocalTime? = null,
    /** OPTIONAL: slightly after fajr */
    @Serializable(LocalTimeSerializer::class)
    val sabah: LocalTime? = null,
) {
    companion object {
        fun from(dayTimes: dev.metinkale.prayertimes.core.DayTimes) = DayTimes(
            date = dayTimes.date.toJavaLocalDate(),
            fajr = dayTimes.fajr.toJavaLocalTime(),
            sun = dayTimes.sun.toJavaLocalTime(),
            dhuhr = dayTimes.dhuhr.toJavaLocalTime(),
            asr = dayTimes.asr.toJavaLocalTime(),
            maghrib = dayTimes.maghrib.toJavaLocalTime(),
            ishaa = dayTimes.ishaa.toJavaLocalTime(),
            asrHanafi = dayTimes.asrHanafi?.toJavaLocalTime(),
            sabah = dayTimes.sabah?.toJavaLocalTime()
        )
    }
}


// TODO check whether needed
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}


// TODO check whether needed
object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString())
    }
}