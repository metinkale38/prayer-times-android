package com.metinkale.prayer.times

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import com.metinkale.prayer.App
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R
import com.metinkale.prayer.receiver.OnStartListener
import dev.metinkale.prayertimes.core.Configuration

object TimesFeature : MenuFeature, OnStartListener {
    override val iconRes: Int = R.drawable.ic_menu_times
    override val titleRes: Int = R.string.appName

    override val name: String = "times"

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)


    override val appEventListeners = listOf(
        TimesBroadcastReceiver, OngoingNotificationsService.Companion, this
    )

    override fun onStart() {
        Configuration.IGMG_API_KEY = App.get().getString(R.string.IGMG_API_KEY)
        Configuration.LONDON_PRAYER_TIMES_API_KEY =
            App.get().getString(R.string.LONDON_PRAYER_TIMES_API_KEY)
        Configuration.languages = Resources.getSystem().configuration.locales.let { locales ->
            (0 until locales.size()).map { locales[it].language }
        }
    }
}