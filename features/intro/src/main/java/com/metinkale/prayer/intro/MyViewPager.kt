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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.metinkale.prayer.times.utils.RTLViewPager

/**
 * Created by metin on 25.07.17.
 */
class MyViewPager : RTLViewPager {
    constructor(context: Context?) : super(context!!) {
        postInitViewPager()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        postInitViewPager()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val frag = (adapter as FragmentPagerAdapter?)?.getItem(currentItem) as? IntroFragment
        return if (frag?.allowTouch() == true) super.onInterceptTouchEvent(ev) else true
    }

    private fun postInitViewPager() {
        try {
            val scroller = ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            val interpolator = ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolator.isAccessible = true
            scroller[this] = object : Scroller(context, interpolator[this] as Interpolator) {
                override fun startScroll(
                    startX: Int,
                    startY: Int,
                    dx: Int,
                    dy: Int,
                    duration: Int
                ) {
                    super.startScroll(startX, startY, dx, dy, duration * 3)
                }
            }
        } catch (ignored: Exception) {
        }
    }
}