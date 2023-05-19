package com.metinkale.prayer.date

import com.metinkale.prayer.App
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.R
import com.metinkale.prayer.utils.FastTokenizer
import java.time.LocalDate

class HijriDate(val year: Int, val month: HijriMonth, val day: Int) {

    val monthValue get() = month.value

    constructor(year: Int, monthValue: Int, day: Int)
            : this(year, HijriMonth.values()[monthValue - 1], day)


    fun toLocalDate(): LocalDate {
        val last = entries.last { year <= it.hy && monthValue <= it.hm && day <= it.hd }
        var date =LocalDate.of(last.gy, last.gm, last.gd).plusDays((day - last.hd).toLong())
        val hfix = Preferences.HIJRI_FIX.get()
        if (hfix != 0) {
            date = date.plusDays(hfix.toLong())
        }
        return date
    }

    companion object {

        private data class Entry(
            val hd: Int,
            val hm: Int,
            val hy: Int,
            val gd: Int,
            val gm: Int,
            val gy: Int
        )

        private lateinit var entries: List<Entry>

        val MIN_GREG_YEAR: Int by lazy { entries.minOf { it.gy } }
        val MAX_GREG_YEAR: Int by lazy { entries.maxOf { it.gy } }
        val MIN_HIJRI_YEAR: Int by lazy { entries.minOf { it.hy } }
        val MAX_HIJRI_YEAR: Int by lazy { entries.maxOf { it.hy } }

        init {
            App.get().resources.openRawResource(R.raw.hijri).bufferedReader().useLines { lines ->
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
                }
            }
        }
    }
}