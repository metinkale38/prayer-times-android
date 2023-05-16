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
package com.metinkale.prayer.times.fragments

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.metinkale.prayer.App
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.Module
import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.utils.MultipleOrientationSlidingDrawer
import com.metinkale.prayer.times.utils.RTLViewPager
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.UUID
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.min

class TimesFragment : BaseActivity.MainFragment(), OnPageChangeListener, View.OnClickListener {
    private lateinit var adapter: MyAdapter
    private lateinit var addCityFab: FloatingActionButton
    private lateinit var pager: RTLViewPager
    private lateinit var settingsFrag: SettingsFragment
    private lateinit var imsakiyeFrag: ImsakiyeFragment
    private lateinit var footerText: TextView
    lateinit var topSlider: MultipleOrientationSlidingDrawer
        private set
    lateinit var bottomSlider: MultipleOrientationSlidingDrawer
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_main, container, false)
        footerText = v.findViewById(R.id.footerText)
        pager = v.findViewById(R.id.pager)
        adapter = MyAdapter(this, childFragmentManager)
        settingsFrag = SettingsFragment()
        imsakiyeFrag = ImsakiyeFragment()
        childFragmentManager.beginTransaction().replace(R.id.imsakiyeContainer, imsakiyeFrag)
            .replace(R.id.settingsContainer, settingsFrag)
            .commit()
        topSlider = v.findViewById(R.id.topSlider)
        bottomSlider = v.findViewById(R.id.bottomSlider)
        addCityFab = v.findViewById(R.id.addCity)
        addCityFab.setOnClickListener(this)
        pager.setRTLSupportAdapter(childFragmentManager, adapter)
        val holyday = HijriDate.isHolyday()
        if (holyday != 0) {
            val tv = v.findViewById<TextView>(R.id.holyday)
            tv.visibility = View.VISIBLE
            tv.text = LocaleUtils.getHolyday(holyday)
        }
        pager.addOnPageChangeListener(this)
        topSlider.setOnDrawerScrollListener(object :
            MultipleOrientationSlidingDrawer.OnDrawerScrollListener {
            override fun onScrollStarted() {}
            override fun onScrolling(pos: Int) {
                pager.translationY = pos.toFloat()
            }

            override fun onScrollEnded() {}
        })
        topSlider.setOnDrawerOpenListener {
            val position = pager.currentItem
            if (position != 0 && Times.current.isNotEmpty()) {
                val (ID) = Times.current[position - 1]
                settingsFrag.timesId = ID
            }
            bottomSlider.lock()
        }
        topSlider.setOnDrawerCloseListener { bottomSlider.unlock() }
        bottomSlider.setOnDrawerOpenListener { topSlider.lock() }
        bottomSlider.setOnDrawerCloseListener { topSlider.unlock() }
        v.findViewById<View>(R.id.topSliderCloseHandler)
            .setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN && topSlider.isOpened) {
                    topSlider.animateClose()
                    return@setOnTouchListener true
                }
                false
            }
        v.findViewById<View>(R.id.bottomSliderCloseHandler)
            .setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN && bottomSlider.isOpened) {
                    bottomSlider.animateClose()
                    return@setOnTouchListener true
                }
                false
            }
        var startPos = (arguments?.getInt("startCity", -1) ?: 0) + 1

        startPos = min(startPos, adapter.count - 1)
        pager.currentItem = startPos
        onPageScrolled(startPos, 0f, 0)
        onPageSelected(startPos)
        onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE)
        return v
    }

    override fun onClick(v: View) {
        if (v === addCityFab) {
            moveToFrag(SearchCityFragment())
        }
    }

    override fun onBackPressed(): Boolean {
        val frag = childFragmentManager.findFragmentByTag("notPrefs")
        if (frag != null) {
            setFooterText(getString(R.string.monthly), true)
            childFragmentManager.beginTransaction().remove(frag).commit()
            return true
        } else if (bottomSlider.isOpened) {
            bottomSlider.animateClose()
            return true
        } else if (topSlider.isOpened) {
            topSlider.animateClose()
            return true
        }
        return false
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            val pos = pager.currentItem
            if (pos != 0 && Times.current.isNotEmpty()) {
                val t: Times = Times.current[pos - 1]
                imsakiyeFrag.setTimes(t)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (position == 0) {
            topSlider.lock()
            bottomSlider.lock()
        } else {
            topSlider.unlock()
            bottomSlider.unlock()
        }
        if (position == 0 && positionOffset == 0f) {
            addCityFab.show()
        } else {
            addCityFab.hide()
        }
    }

    fun setFooterText(txt: CharSequence, enablePane: Boolean) {
        footerText.visibility =
            if (txt.toString().isEmpty()) View.GONE else View.VISIBLE
        footerText.text = txt
        pager.setSwipeLocked(!enablePane)
    }

    override fun onPageSelected(pos: Int) {
        footerText.setText(if (pos == 0) R.string.cities else R.string.monthly)
    }

    fun onItemClick(pos: Int) {
        pager.setCurrentItem(pos + 1, true)
    }

    class MyAdapter internal constructor(lco: LifecycleOwner, fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {


        private var size: Int = Times.current.size

        init {
            Times.map { it.size }.distinctUntilChanged().asLiveData().observe(lco) {
                size = it
                notifyDataSetChanged()
            }
        }

        override fun getCount(): Int {
            return size + 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Fragment {
            return if (position > 0) {
                val frag = CityFragment()
                val bdl = Bundle()
                bdl.putInt("index", position - 1)
                frag.arguments = bdl
                frag
            } else {
                SortFragment()
            }
        }
    }

    companion object {
        @JvmStatic
        fun getPendingIntent(t: Times): PendingIntent? {
            val context: Context = App.get()
            val intent = Module.TIMES.buildIntent(context)
            intent.putExtra("startCity", Times.current.indexOfFirst { it.id == t.id })
            return PendingIntent.getActivity(
                context,
                UUID.asInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}