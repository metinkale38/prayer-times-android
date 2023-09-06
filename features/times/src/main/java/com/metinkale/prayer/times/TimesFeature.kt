package com.metinkale.prayer.times

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object TimesFeature : MenuFeature {
    override val iconRes: Int = R.drawable.ic_menu_times
    override val titleRes: Int = R.string.appName

    override val name: String = "times"

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)

    override val appEventListeners =
        listOf(
            TimesBroadcastReceiver,
            OngoingNotificationsService.Companion,
            LocationService.Companion
        )
}