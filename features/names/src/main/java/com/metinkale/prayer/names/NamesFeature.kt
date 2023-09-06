package com.metinkale.prayer.names

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object NamesFeature : MenuFeature {
    override val name: String = "names"
    override val iconRes: Int = R.drawable.ic_menu_names
    override val titleRes: Int = R.string.names
    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}