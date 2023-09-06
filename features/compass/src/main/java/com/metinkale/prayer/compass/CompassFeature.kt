package com.metinkale.prayer.compass

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object CompassFeature : MenuFeature {
    override val name: String = "feature"
    override val iconRes: Int = R.drawable.ic_menu_compass
    override val titleRes: Int = R.string.compass

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}