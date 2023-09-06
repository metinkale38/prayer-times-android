package com.metinkale.prayer.calendar

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object CalendarFeature : MenuFeature {
    override val name: String = "calendar"
    override val iconRes: Int = R.drawable.ic_menu_calendar
    override val titleRes: Int = R.string.calendar

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)

}