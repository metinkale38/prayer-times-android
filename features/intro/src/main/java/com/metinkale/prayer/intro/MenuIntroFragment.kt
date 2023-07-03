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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.metinkale.prayer.Module
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.TimesFragment

/**
 * Created by metin on 17.07.2017.
 */
class MenuIntroFragment : IntroFragment() {

    private var drawerLayout: DrawerLayout? = null
    private val open: () -> Unit = {
        drawerLayout?.openDrawer(GravityCompat.START)
        drawerLayout?.postDelayed(close, 3000)
    }
    private val close: () -> Unit = {
        drawerLayout?.closeDrawers()
        drawerLayout?.postDelayed(open, 2000)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_menu, container, false)
        val toolbar = v.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.appName)
        toolbar.setNavigationIcon(R.drawable.ic_action_menu)
        drawerLayout = v.findViewById(R.id.drawer)
        drawerLayout?.setBackgroundResource(R.color.background)
        val lv = v.findViewById<ListView>(R.id.base_nav)
        lv.adapter = buildNavAdapter(activity)
        return v
    }

    private fun buildNavAdapter(c: Context?): ArrayAdapter<Module> =
        object : ArrayAdapter<Module>(c!!, R.layout.drawer_list_item, Module.values().toList()) {
            override fun getView(pos: Int, v: View?, p: ViewGroup): View {
                var v = v
                if (v == null) {
                    v = LayoutInflater.from(c)
                        .inflate(com.metinkale.prayer.base.R.layout.drawer_list_item, p, false)
                }
                val item = getItem(pos)
                if (item!!.iconRes == 0 && item.titleRes == 0) {
                    v!!.visibility = View.GONE
                    return v
                }
                v!!.visibility = View.VISIBLE
                (v as TextView?)!!.setText(item.titleRes)
                if (c!!.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    (v as TextView?)!!.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, item.iconRes, 0
                    )
                } else {
                    (v as TextView?)!!.setCompoundDrawablesWithIntrinsicBounds(
                        item.iconRes, 0, 0, 0
                    )
                }
                return v
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frag: Fragment = TimesFragment()
        childFragmentManager.beginTransaction().replace(R.id.basecontent, frag).commit()
    }

    override fun onSelect() {
        drawerLayout?.postDelayed(open, 500)
    }

    override fun onEnter() {}
    override fun onExit() {
        drawerLayout?.removeCallbacks(open)
        drawerLayout?.removeCallbacks(close)
    }

    override fun shouldShow(): Boolean {
        return Preferences.SHOW_INTRO
    }
}