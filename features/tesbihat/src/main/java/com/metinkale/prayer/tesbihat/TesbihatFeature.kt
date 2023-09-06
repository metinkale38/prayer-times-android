package com.metinkale.prayer.tesbihat

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object TesbihatFeature : MenuFeature {
    override val name: String = "tesbihat"
    override val iconRes: Int = R.drawable.ic_menu_tesbihat
    override val titleRes: Int = R.string.tesbihat

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)

}