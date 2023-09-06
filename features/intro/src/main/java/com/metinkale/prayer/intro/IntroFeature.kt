package com.metinkale.prayer.intro

import android.content.Context
import android.content.Intent
import com.metinkale.prayer.ActivityFeature

object IntroFeature : ActivityFeature {
    override val name: String = "intro"

    override fun buildIntent(c: Context): Intent = Intent(c, MainActivity::class.java)
}