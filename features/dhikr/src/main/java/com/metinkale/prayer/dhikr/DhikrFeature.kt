package com.metinkale.prayer.dhikr

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object DhikrFeature : MenuFeature {
    override val name: String = "dhikr"
    override val iconRes: Int = R.drawable.ic_menu_dhikr
    override val titleRes: Int = R.string.dhikr

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}