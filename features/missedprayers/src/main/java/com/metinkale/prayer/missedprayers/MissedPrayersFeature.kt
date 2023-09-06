package com.metinkale.prayer.missedprayers

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object MissedPrayersFeature : MenuFeature {
    override val name: String = "missedprayers"
    override val iconRes: Int = R.drawable.ic_menu_missed
    override val titleRes: Int = R.string.missedPrayers
    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}