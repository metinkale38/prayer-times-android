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
package com.metinkale.prayer.times.utils

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.text.TextUtilsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

open class RTLViewPager : ViewPager {
    private var swipeLocked = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setSwipeLocked(swipeLocked: Boolean) {
        this.swipeLocked = swipeLocked
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return !swipeLocked && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return !swipeLocked && super.onInterceptTouchEvent(event)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return !swipeLocked && super.canScrollHorizontally(direction)
    }

    fun setRTLSupportAdapter(fm: FragmentManager, adapter: FragmentPagerAdapter) {
        super.setAdapter(if (isRTL) RTLAdapterWrapper(fm, adapter) else adapter)
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        throw RuntimeException("not supported, please use setRTLSupportAdapter")
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        super.setOnPageChangeListener(if (isRTL) RTLOnPageChangeListener(listener) else listener)
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        super.addOnPageChangeListener(if (isRTL) RTLOnPageChangeListener(listener) else listener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        throw RuntimeException("not Supported")
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(if (isRTL) adapter!!.count - 1 - item else item)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(if (isRTL) adapter!!.count - 1 - item else item, smoothScroll)
    }

    override fun getCurrentItem(): Int {
        return if (isRTL) adapter!!.count - 1 - super.getCurrentItem() else super.getCurrentItem()
    }

    private val isRTL: Boolean
        private get() = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL

    private class RTLAdapterWrapper(
        fm: FragmentManager,
        private val adapter: FragmentPagerAdapter
    ) : FragmentPagerAdapter(fm) {
        private val observer: DataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onInvalidated() {
                notifyDataSetChanged()
            }
        }

        init {
            adapter.registerDataSetObserver(observer)
        }

        override fun getCount(): Int {
            return adapter.count
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return adapter.isViewFromObject(view, `object`)
        }

        override fun getItemId(position: Int): Long {
            return adapter.getItemId(count - 1 - position)
        }

        override fun getItemPosition(`object`: Any): Int {
            val pos = adapter.getItemPosition(`object`)
            return if (pos == POSITION_NONE) POSITION_NONE else count - 1 - pos
        }

        override fun getItem(position: Int): Fragment {
            return adapter.getItem(count - 1 - position)
        }
    }

    private inner class RTLOnPageChangeListener internal constructor(private val list: OnPageChangeListener) :
        OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            var positionOffset = positionOffset
            var positionOffsetPixels = positionOffsetPixels
            if (positionOffset == 0f) positionOffset = 1f
            if (positionOffsetPixels == 0) positionOffsetPixels = width
            list.onPageScrolled(
                adapter!!.count - position - 1,
                1 - positionOffset,
                width - positionOffsetPixels
            )
        }

        override fun onPageSelected(position: Int) {
            list.onPageSelected(adapter!!.count - position - 1)
        }

        override fun onPageScrollStateChanged(state: Int) {
            list.onPageScrollStateChanged(state)
        }
    }
}