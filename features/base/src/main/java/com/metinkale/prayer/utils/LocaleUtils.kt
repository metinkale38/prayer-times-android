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
package com.metinkale.prayer.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import androidx.annotation.Size
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.R
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.date.HijriDay
import com.metinkale.prayer.date.HijriMonth
import java.lang.IllegalStateException
import java.text.DateFormatSymbols
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.*

object LocaleUtils {


    @JvmStatic
    val locales: List<Locale>
        get() = (listOfNotNull(
            Preferences.LANGUAGE.takeIf { it != "system" }).map { Locale(it) } + LocaleListCompat.getDefault()
            .let { list -> (0 until list.size()).mapNotNull { list[it] } }).distinctBy { it.language }


    @JvmStatic
    val locale
        get() = locales.first()

    @JvmStatic
    fun init(c: Context) {
        CrashReporter.setCustomKey("lang", Preferences.LANGUAGE)
        CrashReporter.setCustomKey("digits", Preferences.DIGITS)
        val config = Configuration()
        val localeList = LocaleList(*locales.toTypedArray())
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        c.resources.updateConfiguration(config, c.resources.displayMetrics)
        c.applicationContext.resources.updateConfiguration(config, c.resources.displayMetrics)

        val localeListCompat = LocaleListCompat.create(*locales.toTypedArray())

        try {
            AppCompatDelegate.setApplicationLocales(localeListCompat)
        } catch (e: IllegalStateException) {
            // getting "Can't change activity type once set!"
            CrashReporter.recordException(e)
        }
    }

    fun formatTimeForHTML(localTime: LocalTime?): CharSequence {
        var time = formatTime(localTime)
        if (!Preferences.CLOCK_12H) {
            return time
        }
        val d = time.indexOf(" ")
        if (d < 0) return time
        time = time.replace(" ", "")
        val s = time.length
        val span: Spannable = SpannableString(time)
        span.setSpan(SuperscriptSpan(), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(RelativeSizeSpan(0.5f), d, s, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    fun formatTime(localTime: LocalTime?): String {
        var time =
            if (localTime == null) "00:00" else localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        if (Preferences.CLOCK_12H && time.contains(":")) {
            time = try {
                val fix = time.substring(0, time.indexOf(":"))
                val suffix = time.substring(time.indexOf(":"))
                val hour = fix.toInt()
                if (hour == 0) {
                    "00$suffix AM"
                } else if (hour < 12) {
                    az(hour) + suffix + " AM"
                } else if (hour == 12) {
                    "12$suffix PM"
                } else {
                    az(hour - 12) + suffix + " PM"
                }
            } catch (e: Exception) {
                CrashReporter.recordException(e)
                return time
            }
        }
        return formatNumber(time)
    }


    @JvmStatic
    fun getLanguage(@Size(min = 1) vararg allow: String): String {
        val lang = locale
        val locales = arrayOfNulls<Locale>(allow.size)
        for (i in allow.indices) {
            locales[i] = Locale(allow[i])
        }
        for (i in locales.indices) {
            if (lang.language == locales[i]!!.language) return allow[i]
        }
        return allow[0]
    }

    @JvmStatic
    fun getHolyday(which: HijriDay): String? =
        which.resId?.let { App.get().resources.getString(it) }


    private fun getGregMonth(which: Month): String {
        return if (Preferences.LANGUAGE == "system")
            DateFormatSymbols(locale).months[which.ordinal]
        else App.get().resources.getString(which.resId)
    }

    private fun getHijriMonth(which: HijriMonth): String {
        return App.get().resources.getString(which.resId)
    }

    fun az(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else i.toString() + ""
    }

    private fun getDateFormat(hicri: Boolean): String {
        return if (hicri) Preferences.HIJRI_DATE_FORMAT else Preferences.DATE_FORMAT
    }

    @JvmStatic
    fun formatDate(date: HijriDate): String {
        var format = getDateFormat(true)
        format = format.replace("DD", az(date.day, 2))
        if (format.contains("MMM")) {
            format = try {
                format.replace("MMM", getHijriMonth(date.month))
            } catch (ex: ArrayIndexOutOfBoundsException) {
                CrashReporter.recordException(ex)
                return ""
            }
        }
        format = format.replace("MM", az(date.month.value, 2))
        format = format.replace("YYYY", az(date.year, 4))
        format = format.replace("YY", az(date.year, 2))
        return formatNumber(format)
    }

    @JvmStatic
    fun formatDate(date: LocalDate): String {
        var format = getDateFormat(false)
        format = format.replace("DD", az(date.dayOfMonth, 2))
        format = try {
            format.replace("MMM", getGregMonth(date.month))
        } catch (ex: ArrayIndexOutOfBoundsException) {
            CrashReporter.recordException(ex)
            return ""
        }
        format = format.replace("MM", az(date.monthValue, 2))
        format = format.replace("YYYY", az(date.year, 4))
        format = format.replace("YY", az(date.year, 2))
        return formatNumber(format)
    }

    private fun az(Int: Int, num: Int): String {
        var ret = StringBuilder(Int.toString() + "")
        if (ret.length < num) {
            for (i in ret.length until num) {
                ret.insert(0, "0")
            }
        } else if (ret.length > num) {
            ret = StringBuilder(ret.substring(ret.length - num, ret.length))
        }
        return ret.toString()
    }

    @JvmStatic
    fun formatNumber(number: String): String {
        var str = number
        if (Preferences.DIGITS == "normal") return str
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        if (Preferences.DIGITS == "farsi") {
            arabicChars[4] = '۴'
            arabicChars[5] = '۵'
            arabicChars[6] = '۶'
        }
        str = str.replace("AM", "ص").replace("PM", "م")
        val builder = StringBuilder()
        str.forEach { char ->
            if (char in '0' until '9') {
                builder.append(arabicChars[char.code - 48])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    @JvmStatic
    fun formatNumber(nr: Int): String {
        return formatNumber(nr.toString() + "")
    }

    @JvmStatic
    fun formatNumber(doub: Double): String {
        return formatNumber(String.format(locale, "%f", doub))
    }

    @JvmStatic
    fun wrapContext(context: Context): Context {
        val res = context.resources
        val configuration = res.configuration

        return ContextWrapper(
            run {
                configuration.setLocale(locale)
                val localeList = LocaleList(*locales.toTypedArray())
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context.createConfigurationContext(configuration)
            }
        )
    }

    @JvmStatic
    fun getSupportedLanguages(c: Context): List<Translation> {
        val languages = c.resources.getStringArray(R.array.languages)
        val translations: MutableList<Translation> = ArrayList()
        for (_lang in languages) {
            var lang = _lang
            val divider = lang.indexOf("|")
            val progress = lang.substring(divider + 1).toInt()
            lang = lang.substring(0, divider)
            if (lang == "kur") lang = "ku"
            if (progress > 50) {
                translations.add(Translation(lang, progress))
            }
        }
        translations.sortWith { t1: Translation, t2: Translation ->
            -t1.progress.compareTo(t2.progress)
        }
        translations.add(0, Translation("system", -1))
        return translations
    }


    fun formatPeriod(from: Temporal, to: Temporal, showSecs: Boolean = false): String {
        val d = Duration.between(from, to)
        return formatNumber(
            if (showSecs) {
                String.format(
                    "%02d:%02d:%02d",
                    d.toHoursPart(),
                    d.toMinutesPart(),
                    d.toSecondsPart()
                )
            } else {
                val duration =
                    if (Preferences.COUNTDOWN_TYPE == Preferences.COUNTDOWN_TYPE_FLOOR) d
                    else d.plusSeconds(59)
                String.format("%02d:%02d", duration.toHoursPart(), duration.toMinutesPart())
            }
        )
    }

    fun readableSize(bytes: Int): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "kMGTPE"[exp - 1]
        return String.format(
            Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre
        )
    }

    data class Translation(val language: String, val progress: Int) {

        val displayLanguage: String
            get() {
                if (language == "system") return App.get().resources.getString(R.string.systemLanguage)
                if (language == "ku") return "Kurdî"
                val locale = Locale(language)
                return locale.getDisplayLanguage(locale)
            }
        val displayText: CharSequence
            get() = if (progress < 0) displayLanguage
            else Html.fromHtml("$displayLanguage&nbsp;<small>($progress%)</small>")
    }


}

val Month.resId: Int
    get() = when (this) {
        Month.JANUARY -> R.string.gmonth1
        Month.FEBRUARY -> R.string.gmonth2
        Month.MARCH -> R.string.gmonth3
        Month.APRIL -> R.string.gmonth4
        Month.MAY -> R.string.gmonth5
        Month.JUNE -> R.string.gmonth6
        Month.JULY -> R.string.gmonth7
        Month.AUGUST -> R.string.gmonth8
        Month.SEPTEMBER -> R.string.gmonth9
        Month.OCTOBER -> R.string.gmonth10
        Month.NOVEMBER -> R.string.gmonth11
        Month.DECEMBER -> R.string.gmonth12
    }
