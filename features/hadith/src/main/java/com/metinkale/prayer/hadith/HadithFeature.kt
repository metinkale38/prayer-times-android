package com.metinkale.prayer.hadith

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.MenuFeature
import com.metinkale.prayer.base.R

object HadithFeature : MenuFeature {
    override val name: String = "hadith"
    override val iconRes: Int = R.drawable.ic_menu_hadith
    override val titleRes: Int = R.string.hadith

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}