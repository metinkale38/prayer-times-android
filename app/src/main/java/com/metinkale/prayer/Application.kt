package com.metinkale.prayer

import android.content.Context
import androidx.multidex.MultiDex
import com.metinkale.prayer.about.AboutFeature
import com.metinkale.prayer.calendar.CalendarFeature
import com.metinkale.prayer.compass.CompassFeature
import com.metinkale.prayer.dhikr.DhikrFeature
import com.metinkale.prayer.hadith.HadithFeature
import com.metinkale.prayer.intro.IntroFeature
import com.metinkale.prayer.missedprayers.MissedPrayersFeature
import com.metinkale.prayer.names.NamesFeature
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.settings.SettingsFeature
import com.metinkale.prayer.tesbihat.TesbihatFeature
import com.metinkale.prayer.times.TimesFeature
import com.metinkale.prayerapp.vakit.WidgetFeature

class Application : App() {
    init {
        Feature.init(
            AppFeature,
            TimesFeature,
            CompassFeature,
            NamesFeature,
            CalendarFeature,
            TesbihatFeature,
            HadithFeature,
            MissedPrayersFeature,
            DhikrFeature,
            SettingsFeature,
            AboutFeature,
            IntroFeature,
            WidgetFeature
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}