/*
 * Copyright (c) 2013-2026 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.metinkale.prayer.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.Module
import com.metinkale.prayer.Preferences.CHANGELOG_VERSION
import com.metinkale.prayer.Preferences.SHOW_INTRO
import com.metinkale.prayer.R
import com.metinkale.prayer.utils.AboutShortcuts.github
import com.metinkale.prayer.utils.AboutShortcuts.mail
import com.metinkale.prayer.utils.AboutShortcuts.rate
import com.metinkale.prayer.utils.AboutShortcuts.reportBug
import com.metinkale.prayer.utils.AboutShortcuts.share
import com.metinkale.prayer.utils.AboutShortcuts.translate

/**
 * Created by metin on 30.10.16.
 */
class AboutFragment : BaseActivity.MainFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.about_main, container, false)

        try {
            val pInfo = requireActivity().getPackageManager()
                .getPackageInfo(requireActivity().getPackageName(), 0)
            (v.findViewById<View?>(R.id.version) as TextView).text =
                "${pInfo.versionName} (${pInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        v.findViewById<View>(R.id.mail).setOnClickListener(this)
        v.findViewById<View>(R.id.libLicences).setOnClickListener(this)
        v.findViewById<View>(R.id.licenses).setOnClickListener(this)
        v.findViewById<View>(R.id.reportBug).setOnClickListener(this)
        v.findViewById<View>(R.id.translate).setOnClickListener(this)
        v.findViewById<View>(R.id.rate).setOnClickListener(this)
        v.findViewById<View>(R.id.github).setOnClickListener(this)
        v.findViewById<View>(R.id.share).setOnClickListener(this)
        v.findViewById<View>(R.id.showIntro).setOnClickListener(this)
        return v
    }

    override fun onClick(v: View) {
        val i = v.getId()
        if (i == R.id.mail) {
            mail(requireActivity())
        } else if (i == R.id.libLicences) {
            libLicences()
        } else if (i == R.id.licenses) {
            licenses(requireActivity())
        } else if (i == R.id.reportBug) {
            reportBug(requireActivity())
        } else if (i == R.id.translate) {
            translate(requireActivity())
        } else if (i == R.id.rate) {
            rate(requireActivity())
        } else if (i == R.id.github) {
            github(requireActivity())
        } else if (i == R.id.share) {
            share(requireActivity())
        } else if (i == R.id.showIntro) {
            SHOW_INTRO = true
            CHANGELOG_VERSION = 0
            Module.INTRO.launch(requireActivity())
        }
    }


    fun libLicences() {
        moveToFrag(AboutLibsFragment())
    }


    companion object {
        fun licenses(ctx: Context) {
            val wv = WebView(ctx)
            wv.loadUrl("file:///android_asset/license.html")
            val builder = AlertDialog.Builder(ctx)
            builder.setTitle(ctx.getResources().getString(R.string.license)).setView(wv)
                .setCancelable(false)
            builder.setNegativeButton(ctx.getResources().getString(R.string.ok), null)
            builder.show()
        }
    }
}
