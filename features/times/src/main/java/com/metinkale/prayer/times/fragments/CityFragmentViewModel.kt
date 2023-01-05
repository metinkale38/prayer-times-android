package com.metinkale.prayer.times.fragments

import com.metinkale.prayer.App
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.times.drawableId
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.utils.secondsFlow
import com.metinkale.prayer.utils.LocaleUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.joda.time.DateTime
import org.joda.time.LocalDate

data class CityFragmentViewModel(
    val date: String,
    val city: String,
    val hijri: String,
    val fajrTitle: String,
    val fajrTime: CharSequence,
    val sunTitle: String,
    val sunTime: CharSequence,
    val dhuhrTitle: String,
    val dhuhrTime: CharSequence,
    val asrTitle: String,
    val asrTime: CharSequence,
    val maghribTitle: String,
    val maghribTime: CharSequence,
    val ishaTitle: String,
    val ishaTime: CharSequence,
    val countdown: String,
    val icon: Int,
    val hoverLine: Int,
    val autolocation: Boolean,
    val isKerahat: Boolean
) {
    companion object {
        fun from(times: Flow<Times>) =
            combine(times, secondsFlow) { it, secs ->
                val daytimes = it.dayTimes.get(LocalDate.now())

                if (daytimes == null && App.isOnline() && secs % 60 == 0) {
                    (it.dayTimes as DayTimesWebProvider).syncAsync()
                }

                val hasSabah = daytimes?.sabah != null
                val hasAsr2 = daytimes?.asrHanafi != null
                val alt = (secs / 3) % 2 == 0
                CityFragmentViewModel(
                    date = LocaleUtils.formatDate(LocalDate.now()),
                    city = it.name,
                    hijri = LocaleUtils.formatDate(HijriDate.now()),
                    fajrTitle = if (!hasSabah) Vakit.FAJR.string
                    else if (!alt) Vakit.FAJR.getString(0)
                    else Vakit.FAJR.getString(1),
                    dhuhrTitle = Vakit.DHUHR.string,
                    sunTitle = Vakit.SUN.string,
                    asrTitle = if (!hasAsr2) Vakit.ASR.string
                    else if (!alt) Vakit.ASR.getString(0)
                    else Vakit.ASR.getString(1),
                    maghribTitle = Vakit.MAGHRIB.string,
                    ishaTitle = Vakit.ISHAA.string,
                    fajrTime = LocaleUtils.formatTimeForHTML(if (hasSabah && alt) daytimes?.sabah else daytimes?.fajr),
                    sunTime = LocaleUtils.formatTimeForHTML(daytimes?.sun),
                    dhuhrTime = LocaleUtils.formatTimeForHTML(daytimes?.dhuhr),
                    asrTime = LocaleUtils.formatTimeForHTML(if (hasSabah && alt) daytimes?.asrHanafi else daytimes?.asr),
                    maghribTime = LocaleUtils.formatTimeForHTML(daytimes?.maghrib),
                    ishaTime = LocaleUtils.formatTimeForHTML(daytimes?.ishaa),
                    countdown = LocaleUtils.formatPeriod(
                        DateTime.now(),
                        it.getTime(LocalDate.now(), it.getNextTime()).toDateTime(),
                        true
                    ),
                    icon = it.source.drawableId ?: 0,
                    hoverLine = it.getNextTime() + if (Preferences.VAKIT_INDICATOR_TYPE.get() == "next") 0 else -1,
                    autolocation = it.autoLocation,
                    isKerahat = it.isKerahat()
                )
            }
    }
}