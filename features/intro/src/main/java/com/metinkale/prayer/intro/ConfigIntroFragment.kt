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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.AlarmsFragment.Companion.create
import com.metinkale.prayer.times.fragments.TimesFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.times.times.getTime
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by metin on 17.07.2017.
 */
class ConfigIntroFragment : IntroFragment() {
    private var mView: View? = null
    private var mTask = 0
    private var mPrefsFrag: Fragment? = null
    private val mDoTask: Runnable = object : Runnable {
        override fun run() {
            mView!!.postDelayed(this, 2000)
            if (mFragment == null || isDetached || !isAdded) {
                return
            }
            val fm = childFragmentManager
            if (fm.isStateSaved) return
            when (mTask % 6) {
                0 -> mFragment?.topSlider?.animateOpen()
                1 -> mFragment?.topSlider?.animateClose()
                2 -> mFragment?.bottomSlider?.animateOpen()
                3 -> mFragment?.bottomSlider?.animateClose()
                4 -> {
                    mMenuItem?.icon?.alpha = 100
                    mMenuItem?.icon?.invalidateSelf()
                    mFragment?.setFooterText("", false)
                    fm.beginTransaction()
                        .replace(
                            R.id.basecontent,
                            mPrefsFrag!!
                        ).addToBackStack("").commit()
                }
                5 -> {
                    mMenuItem?.icon?.alpha = 255
                    mMenuItem?.icon?.invalidateSelf()
                    mFragment?.setFooterText(getString(R.string.monthly), false)
                    childFragmentManager.popBackStack()
                }
            }
            mTask++
        }
    }
    private var mFragment: TimesFragment? = null
    private var mMenuItem: MenuItem? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.intro_config, container, false)
        val toolbar = mView!!.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.appName)
        toolbar.setNavigationIcon(R.drawable.ic_action_menu)
        try {
            requireActivity().menuInflater.inflate(R.menu.vakit, toolbar.menu)
            mMenuItem = toolbar.menu.getItem(0)
            mMenuItem!!.icon = mMenuItem!!.icon
        } catch (e: Exception) {
            recordException(e)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var city: Times? = null
        val cities: List<Times?> = Times.current
        for (time in cities) {
            if (city == null) city = time
            if (time?.getTime(LocalDate.now().withDayOfMonth(1), Vakit.DHUHR.ordinal)
                    ?.toLocalTime()
                    ?.equals(LocalTime.MIDNIGHT) == false
            ) {
                city = time
                break
            }
        }
        val bdl = Bundle()
        bdl.putInt("startCity", cities.indexOf(city))
        mFragment = TimesFragment()
        mFragment!!.arguments = bdl
        mPrefsFrag = create(city?.id ?: -1)
        childFragmentManager.beginTransaction().replace(R.id.basecontent, mFragment!!).commit()
    }

    override fun onSelect() {
        if (mView != null) {
            mView!!.removeCallbacks(mDoTask)
            mView!!.postDelayed(mDoTask, 500)
        }
    }

    override fun onEnter() {}
    override fun onExit() {
        if (mView != null) mView!!.removeCallbacks(mDoTask)
    }

    override fun shouldShow(): Boolean {
        return Preferences.SHOW_INTRO.get()
    }
}