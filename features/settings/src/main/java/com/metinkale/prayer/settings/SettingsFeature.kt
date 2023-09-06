package com.metinkale.prayer.settings

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object SettingsFeature : MenuFeature {
    override val iconRes: Int = R.drawable.ic_menu_settings
    override val titleRes: Int = R.string.settings
    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
    override val name: String = "settings"
}