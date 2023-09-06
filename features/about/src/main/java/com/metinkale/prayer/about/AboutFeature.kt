package com.metinkale.prayer.about

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object AboutFeature : MenuFeature {
    override val name: String ="about"
    override val iconRes: Int = R.drawable.ic_menu_about
    override val titleRes = R.string.about


    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}