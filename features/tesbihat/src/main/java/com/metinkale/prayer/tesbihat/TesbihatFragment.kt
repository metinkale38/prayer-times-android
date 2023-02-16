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
package com.metinkale.prayer.tesbihat

import com.metinkale.prayer.utils.LocaleUtils.locale
import com.metinkale.prayer.times.times.Vakit.Companion.getByIndex
import com.metinkale.prayer.BaseActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import com.metinkale.prayer.times.times.Vakit
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.getCurrentTime
import java.util.*

class TesbihatFragment : BaseActivity.MainFragment() {
    private lateinit var pagerAdapter: FragmentStatePagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.tesbihat_main, container, false)
        LOCALE = locale
        val indicator = v.findViewById<PagerSlidingTabStrip>(R.id.indicator)
        viewPager = v.findViewById(R.id.pager)
        if (Locale("tr").language == LOCALE!!.language) {
            pagerAdapter = TurkishPagerAdapter(childFragmentManager)
        } else {
            pagerAdapter = OtherPagerAdapter(childFragmentManager)
            indicator.visibility = View.GONE
        }
        viewPager.adapter = pagerAdapter
        indicator.setViewPager(viewPager)
        indicator.textColor = -0x1
        indicator.dividerColor = 0x0
        indicator.indicatorColor = -0x1
        if (Times.current.isNotEmpty() && Locale("tr").language == LOCALE!!.language) {
            val current: Int = Times.getTimesByIndex(0).current?.getCurrentTime() ?: 0
            when (getByIndex(current)) {
                Vakit.FAJR -> viewPager.currentItem = 0
                Vakit.SUN, Vakit.DHUHR -> viewPager.currentItem = 1
                Vakit.ASR -> viewPager.currentItem = 2
                Vakit.MAGHRIB -> viewPager.currentItem = 3
                Vakit.ISHAA -> viewPager.currentItem = 4
            }
        }
        TEXT_SIZE = Preferences.TESBIHAT_TEXTSIZE.get()
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tesbihat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i1 = item.itemId
        if (i1 == R.id.zoomIn) {
            TEXT_SIZE++
            Preferences.TESBIHAT_TEXTSIZE.set(TEXT_SIZE)
            val i = viewPager.currentItem
            viewPager.invalidate()
            viewPager.adapter = pagerAdapter
            viewPager.currentItem = i
        } else if (i1 == R.id.zoomOut) {
            TEXT_SIZE--
            PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putInt("tesbihatTextSize", TEXT_SIZE).apply()
            val i = viewPager.currentItem
            viewPager.invalidate()
            viewPager.adapter = pagerAdapter
            viewPager.currentItem = i
        }
        return super.onOptionsItemSelected(item)
    }

    class PageFragment : Fragment() {
        private var pos = 0
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.webview, container, false)
            val wv = rootView.findViewById<WebView>(R.id.webview)
            wv.settings.textZoom =
                (102.38 * Math.pow(1.41, TEXT_SIZE.toDouble())).toInt()
            if (Locale("tr").language == LOCALE!!.language) {
                wv.loadUrl("file:///android_asset/tr/tesbihat/" + getAssetDir(pos))
            } else {
                wv.loadUrl("file:///android_asset/en/tasbihat.html")
            }
            return rootView
        }

        fun getAssetDir(position: Int): String? {
            when (position) {
                0 -> return "sabah.htm"
                1 -> return "ogle.htm"
                2 -> return "ikindi.htm"
                3 -> return "aksam.htm"
                4 -> return "yatsi.htm"
            }
            return null
        }

        companion object {
            fun create(pos: Int): PageFragment {
                val frag = PageFragment()
                frag.pos = pos
                return frag
            }
        }
    }

    inner class TurkishPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(
        fm!!
    ) {
        override fun getItem(position: Int): Fragment {
            return PageFragment.create(position)
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.fajr)
                1 -> return getString(R.string.zuhr)
                2 -> return getString(R.string.asr)
                3 -> return getString(R.string.maghrib)
                4 -> return getString(R.string.ishaa)
            }
            return null
        }
    }

    class OtherPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(
        fm!!
    ) {
        override fun getItem(position: Int): Fragment {
            return PageFragment.create(position)
        }

        override fun getCount(): Int {
            return 1
        }

        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }
    }

    companion object {
        private var TEXT_SIZE = 0
        private var LOCALE: Locale? = null
    }
}