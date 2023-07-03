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
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.fragments.TimesFragment

/**
 * Created by metin on 17.07.2017.
 */
class PagerIntroFragment : IntroFragment() {
    private var mPager: ViewPager? = null
    private var mView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.intro_pager, container, false)
        val toolbar = mView!!.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.appName)
        toolbar.setNavigationIcon(R.drawable.ic_action_menu)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val frag: Fragment = TimesFragment()
        childFragmentManager.beginTransaction().replace(R.id.basecontent, frag).commit()
    }

    private val mAction: Runnable = object : Runnable {
        override fun run() {
            if (mPager == null) {
                if (view == null) return
                mPager = view!!.findViewById(R.id.pager)
                if (mPager == null) return else {
                    trySlowDownViewPager(mPager!!)
                }
            }
            val item = (mPager!!.currentItem + 1) % mPager!!.adapter!!.count
            mPager!!.setCurrentItem(item, true)
            mPager!!.postDelayed(this, if (item == 0) 5000 else 2000.toLong())
        }
    }

    override fun onSelect() {
        if (mView != null) {
            mView!!.removeCallbacks(mAction)
            mView!!.postDelayed(mAction, 500)
        }
    }

    override fun onEnter() {}
    override fun onExit() {
        if (mView != null) mView!!.removeCallbacks(mAction)
    }

    override fun shouldShow() = Preferences.SHOW_INTRO

    companion object {
        private fun trySlowDownViewPager(pager: ViewPager) {
            try {
                val scroller = ViewPager::class.java.getDeclaredField("mScroller")
                scroller.isAccessible = true
                val interpolator = ViewPager::class.java.getDeclaredField("sInterpolator")
                interpolator.isAccessible = true
                scroller[pager] =
                    object : Scroller(pager.context, interpolator[pager] as Interpolator) {
                        override fun startScroll(
                            startX: Int, startY: Int, dx: Int, dy: Int,
                            duration: Int
                        ) {
                            super.startScroll(startX, startY, dx, dy, duration * 10)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                //ignore
            }
        }
    }
}