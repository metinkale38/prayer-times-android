package com.metinkale.prayer.date

import com.metinkale.prayer.App
import com.metinkale.prayer.base.R
import com.metinkale.prayer.utils.FastTokenizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.chrono.HijrahEra
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class HijriDate private constructor(val year: Int, val monthValue: Int, val day: Int) :
    Comparable<HijriDate> {

    init {
        if (year > MAX_HIJRI_YEAR) throw java.lang.RuntimeException("cannot create Hijri-Date after $MAX_HIJRI_YEAR")
        if (monthValue !in 1..12) throw java.lang.RuntimeException("Hijri-month must be within 1..12")
        if (day !in 1..30) throw java.lang.RuntimeException("Hijri-day must be within 1..30")
    }

    val month get() = HijriMonth.values()[monthValue - 1]

    fun toLocalDate(): LocalDate {
        return entries.lastOrNull { it.hijri <= hashCode() }?.let { last ->
            LocalDate.of(last.gy, last.gm, last.gd).plusDays((day - last.hd).toLong())
        } ?: run {
            // we have no data prior 1433, use java
            val date = HijrahChronology.INSTANCE.date(HijrahEra.AH, year, monthValue, day)
            LocalDate.from(date)
        }
    }

    fun plusDays(days: Int): HijriDate {
        return if (day + days in 1..29) {
            of(year, month, day + days)
        } else
            toLocalDate().plusDays(days.toLong()).toHijriDate()
    }

    fun minusDays(days: Int): HijriDate {
        return if (day - days in 1..29) {
            of(year, month, day - days)
        } else toLocalDate().minusDays(days.toLong()).toHijriDate()
    }

    fun getHijriDay(): HijriDay? = getHolydaysForHijriYear(year).find { it.first == this }?.second


    override fun equals(other: Any?): Boolean =
        other is HijriDate && other.day == day && other.month == month && other.year == year

    override fun hashCode(): Int = year * 10000 + monthValue * 100 + day

    override fun toString(): String {
        return "HijriDate(year=$year, month=$month($monthValue), day=$day)"
    }


    override fun compareTo(other: HijriDate): Int = hashCode().compareTo(other.hashCode())

    companion object {
        @JvmStatic
        fun getHolydaysForHijriYear(year: Int): Sequence<Pair<HijriDate, HijriDay>> = sequence {
            // do not use map here, one day might be RAJAB and THREE_MONTHS at the same time
            yield(of(year, HijriMonth.MUHARRAM, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.MUHARRAM, 1) to HijriDay.ISLAMIC_NEW_YEAR)
            yield(of(year, HijriMonth.MUHARRAM, 10) to HijriDay.ASHURA)
            yield(of(year, HijriMonth.SAFAR, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.RABIAL_AWWAL, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.RABIAL_AWWAL, 11) to HijriDay.MAWLID_AL_NABI)
            yield(of(year, HijriMonth.RABIAL_AKHIR, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.JUMADAAL_AWWAL, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.JUMADAAL_AKHIR, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.RAJAB, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.RAJAB, 1) to HijriDay.THREE_MONTHS)

            val ragaib = of(year, HijriMonth.RAJAB, 1).toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY))
                .toHijriDate()
                .let { if (it.month < HijriMonth.RAJAB) it.plusDays(7) else it }
                .minusDays(1)
            yield(ragaib to HijriDay.RAGAIB)

            yield(of(year, HijriMonth.RAJAB, 26) to HijriDay.MIRAJ)
            yield(of(year, HijriMonth.SHABAN, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.SHABAN, 14) to HijriDay.BARAAH)
            yield(of(year, HijriMonth.RAMADAN, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.RAMADAN, 1) to HijriDay.RAMADAN_BEGIN)
            yield(of(year, HijriMonth.RAMADAN, 26) to HijriDay.LAYLATALQADR)
            yield(of(year, HijriMonth.SHAWWAL, 1).plusDays(-1) to HijriDay.LAST_RAMADAN)
            yield(of(year, HijriMonth.SHAWWAL, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.SHAWWAL, 1) to HijriDay.EID_AL_FITR_DAY1)
            yield(of(year, HijriMonth.SHAWWAL, 2) to HijriDay.EID_AL_FITR_DAY2)
            yield(of(year, HijriMonth.SHAWWAL, 3) to HijriDay.EID_AL_FITR_DAY3)
            yield(of(year, HijriMonth.DHUL_QADA, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.DHUL_HIJJA, 9) to HijriDay.ARAFAT)
            yield(of(year, HijriMonth.DHUL_HIJJA, 1) to HijriDay.MONTH)
            yield(of(year, HijriMonth.DHUL_HIJJA, 10) to HijriDay.EID_AL_ADHA_DAY1)
            yield(of(year, HijriMonth.DHUL_HIJJA, 11) to HijriDay.EID_AL_ADHA_DAY2)
            yield(of(year, HijriMonth.DHUL_HIJJA, 12) to HijriDay.EID_AL_ADHA_DAY3)
            yield(of(year, HijriMonth.DHUL_HIJJA, 13) to HijriDay.EID_AL_ADHA_DAY4)
        }

        @JvmStatic
        fun getHolydaysForGregYear(year: Int): Sequence<Pair<HijriDate, HijriDay>> {
            val min = LocalDate.of(year, 1, 1).toHijriDate()
            val max = LocalDate.of(year, 12, 31).toHijriDate()

            return sequenceOf(min.year, max.year).distinct().flatMap { getHolydaysForHijriYear(it) }
                .filter { it.first >= min }.filter { it.first <= max }
        }


        private data class Entry(
            val hd: Int, val hm: Int, val hy: Int, val gd: Int, val gm: Int, val gy: Int
        ) {
            val hijri = hy * 10000 + hm * 100 + hd
            val greg = gy * 10000 + gm * 100 + gd
        }

        /**
         * Overwrite for unit-tests
         */
        var openTSVFile = { App.get().resources.openRawResource(R.raw.hijri) }


        private val entries: List<Entry> by lazy {
            openTSVFile().bufferedReader().useLines { lines ->
                lines.filter { !it.contains("HD") }.map { line ->
                    val ft = FastTokenizer(line, "\t")
                    Entry(
                        ft.nextInt(),
                        ft.nextInt(),
                        ft.nextInt(),
                        ft.nextInt(),
                        ft.nextInt(),
                        ft.nextInt()
                    )
                }.toList()
            }
        }

        private val MIN_GREG_YEAR: Int by lazy { entries.minOf { it.gy } }

        @JvmStatic
        val MAX_GREG_YEAR: Int by lazy { entries.maxOf { it.gy } }

        private val MIN_HIJRI_YEAR: Int by lazy { entries.minOf { it.hy } }

        @JvmStatic
        val MAX_HIJRI_YEAR: Int by lazy { entries.maxOf { it.hy } }

        @JvmStatic
        fun of(year: Int, monthValue: Int, day: Int) =
            HijriDate(year, monthValue, day)

        @JvmStatic
        fun of(year: Int, month: HijriMonth, day: Int) = HijriDate(year, month.value, day)

        @JvmStatic
        fun now() = fromLocalDate(LocalDate.now())

        @JvmStatic
        fun fromLocalDate(localDate: LocalDate): HijriDate = localDate.run {
            val greg = year * 10000 + monthValue * 100 + dayOfMonth
            return entries.lastOrNull { it.greg <= greg }?.let { last ->
                val ld = LocalDate.of(last.gy, last.gm, last.gd)
                of(last.hy, last.hm, last.hd - ChronoUnit.DAYS.between(this, ld).toInt())
            } ?: run { // we have no data prior 1433, let java convert it
                val hijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(localDate)
                of(
                    hijrahDate.getLong(ChronoField.YEAR_OF_ERA).toInt(),
                    hijrahDate.getLong(ChronoField.MONTH_OF_YEAR).toInt(),
                    hijrahDate.getLong(ChronoField.DAY_OF_MONTH).toInt()
                )
            }
        }
    }

}

fun LocalDate.toHijriDate() = HijriDate.fromLocalDate(this)