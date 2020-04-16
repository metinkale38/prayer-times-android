/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.intro;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.metinkale.prayer.times.utils.RTLViewPager;

import java.lang.reflect.Field;

/**
 * Created by metin on 25.07.17.
 */

public class MyViewPager extends RTLViewPager {
    public MyViewPager(Context context) {
        super(context);
        postInitViewPager();
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        IntroFragment frag = (IntroFragment) ((FragmentPagerAdapter) getAdapter()).getItem(getCurrentItem());
        if (frag.allowTouch()) return super.onInterceptTouchEvent(ev);
        return true;
    }


    private void postInitViewPager() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);


            scroller.set(this, new Scroller(getContext(), (Interpolator) interpolator.get(this)) {
                @Override
                public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                    super.startScroll(startX, startY, dx, dy, duration * 3);
                }
            });
        } catch (Exception ignored) {
        }
    }

}
