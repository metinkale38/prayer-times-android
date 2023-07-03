/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.intro

import com.metinkale.prayer.utils.AboutShortcuts.mail
import com.metinkale.prayer.utils.AboutShortcuts.translate
import com.metinkale.prayer.utils.AboutShortcuts.rate
import com.metinkale.prayer.utils.AboutShortcuts.share
import com.metinkale.prayer.intro.IntroFragment
import android.widget.CompoundButton
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.metinkale.prayer.intro.R
import android.widget.TextView
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.utils.AboutShortcuts

/**
 * Created by metin on 25.07.17.
 */
class LastFragment : IntroFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_last, container, false)
        try {
            val pInfo = requireActivity().packageManager.getPackageInfo(
                requireActivity().packageName, 0
            )
            (v.findViewById<View>(R.id.version) as TextView).text =
                pInfo.versionName + " (" + pInfo.versionCode + ")"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val email = v.findViewById<Button>(R.id.mail)
        val vote = v.findViewById<Button>(R.id.vote)
        val showIntro = v.findViewById<Button>(R.id.showIntro)
        val share = v.findViewById<Button>(R.id.share)
        val translate = v.findViewById<Button>(R.id.translate)
        email.setOnClickListener(this)
        vote.setOnClickListener(this)
        showIntro.setOnClickListener(this)
        share.setOnClickListener(this)
        translate.setOnClickListener(this)
        email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mail, 0, 0, 0)
        vote.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0)
        showIntro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_intro, 0, 0, 0)
        share.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_share, 0, 0, 0)
        translate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_translate, 0, 0, 0)
        return v
    }

    override fun onSelect() {}
    override fun onEnter() {}
    override fun onExit() {}
    override fun allowTouch(): Boolean {
        return true
    }

    override fun shouldShow(): Boolean {
        return Preferences.SHOW_INTRO
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        if (!b) return
        val newlang = compoundButton.tag as String
        if (Preferences.LANGUAGE.equals(newlang)) return
        Preferences.LANGUAGE = newlang
        requireActivity().recreate()
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.mail) {
            mail(requireActivity())
        } else if (i == R.id.translate) {
            translate(requireActivity())
        } else if (i == R.id.vote) {
            rate(requireActivity())
        } else if (i == R.id.showIntro) {
            Preferences.SHOW_INTRO = true
            Preferences.CHANGELOG_VERSION = 0
            requireActivity().recreate()
            share(requireActivity())
        } else if (i == R.id.share) {
            share(requireActivity())
        }
    }
}