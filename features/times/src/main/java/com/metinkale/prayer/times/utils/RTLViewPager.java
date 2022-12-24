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

package com.metinkale.prayer.times.utils;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.TextUtilsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.Locale;

public class RTLViewPager extends ViewPager {

    private boolean swipeLocked;

    public RTLViewPager(Context context) {
        super(context);
    }

    public RTLViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeLocked(boolean swipeLocked) {
        this.swipeLocked = swipeLocked;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !swipeLocked && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(@Nullable MotionEvent event) {
        return !swipeLocked && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return !swipeLocked && super.canScrollHorizontally(direction);
    }

    public void setRTLSupportAdapter(FragmentManager fm, @NonNull FragmentPagerAdapter adapter) {
        super.setAdapter(isRTL() ? new RTLAdapterWrapper(fm, adapter) : adapter);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        throw new RuntimeException("not supported, please use setRTLSupportAdapter");
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        super.setOnPageChangeListener(isRTL() ? new RTLOnPageChangeListener(listener) : listener);
    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        super.addOnPageChangeListener(isRTL() ? new RTLOnPageChangeListener(listener) : listener);
    }

    @Override
    public void removeOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        throw new RuntimeException("not Supported");
    }


    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(isRTL() ? getAdapter().getCount() - 1 - item : item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(isRTL() ? getAdapter().getCount() - 1 - item : item, smoothScroll);
    }

    @Override
    public int getCurrentItem() {
        return isRTL() ? (getAdapter().getCount() - 1 - super.getCurrentItem()) : super.getCurrentItem();
    }

    private boolean isRTL() {
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL;
    }


    private static class RTLAdapterWrapper extends FragmentPagerAdapter {
        private final FragmentPagerAdapter adapter;

        @NonNull
        private final DataSetObserver observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetChanged();
            }
        };

        private RTLAdapterWrapper(FragmentManager fm, @NonNull FragmentPagerAdapter adapter) {
            super(fm);
            this.adapter = adapter;
            adapter.registerDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return adapter.getCount();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return adapter.isViewFromObject(view, object);
        }


        @Override
        public long getItemId(int position) {
            return adapter.getItemId(getCount() - 1 - position);
        }


        @Override
        public int getItemPosition(@NonNull Object object) {
            int pos = adapter.getItemPosition(object);
            if (pos == POSITION_NONE) return POSITION_NONE;
            return getCount() - 1 - pos;
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            return adapter.getItem(getCount() - 1 - position);
        }


    }

    private class RTLOnPageChangeListener implements OnPageChangeListener {
        private final OnPageChangeListener list;

        RTLOnPageChangeListener(OnPageChangeListener listener) {
            list = listener;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset == 0) positionOffset = 1;
            if (positionOffsetPixels == 0) positionOffsetPixels = getWidth();
            list.onPageScrolled(getAdapter().getCount() - position - 1, 1 - positionOffset, getWidth() - positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            list.onPageSelected(getAdapter().getCount() - position - 1);

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            list.onPageScrollStateChanged(state);
        }


    }
}