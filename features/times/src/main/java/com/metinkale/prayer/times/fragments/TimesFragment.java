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

package com.metinkale.prayer.times.fragments;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Module;
import com.metinkale.prayer.date.HijriDate;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.TimesBase;
import com.metinkale.prayer.times.utils.MultipleOrientationSlidingDrawer;
import com.metinkale.prayer.times.utils.RTLViewPager;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.UUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimesFragment extends BaseActivity.MainFragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public MyAdapter mAdapter;
    public FloatingActionButton mAddCityFab;
    private RTLViewPager mPager;
    private SettingsFragment mSettingsFrag;
    private ImsakiyeFragment mImsakiyeFrag;
    private TextView mFooterText;
    private MultipleOrientationSlidingDrawer mTopSlider;
    private MultipleOrientationSlidingDrawer mBottomSlider;

    @Nullable
    public static PendingIntent getPendingIntent(@Nullable Times t) {
        if (t == null) {
            return null;
        }
        Context context = App.get();
        Intent intent = Module.TIMES.buildIntent(context);
        intent.putExtra("startCity", Times.getTimes().indexOf(t));
        return PendingIntent.getActivity(context, UUID.asInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public MultipleOrientationSlidingDrawer getTopSlider() {
        return mTopSlider;
    }

    public MultipleOrientationSlidingDrawer getBottomSlider() {
        return mBottomSlider;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vakit_main, container, false);
        mFooterText = v.findViewById(R.id.footerText);
        mPager = v.findViewById(R.id.pager);
        mAdapter = new MyAdapter(this, getChildFragmentManager());

        mSettingsFrag = new SettingsFragment();
        mImsakiyeFrag = new ImsakiyeFragment();

        getChildFragmentManager().beginTransaction().replace(R.id.imsakiyeContainer, mImsakiyeFrag).replace(R.id.settingsContainer, mSettingsFrag)
                .commit();


        mTopSlider = v.findViewById(R.id.topSlider);
        mBottomSlider = v.findViewById(R.id.bottomSlider);

        mAddCityFab = v.findViewById(R.id.addCity);
        mAddCityFab.setOnClickListener(this);

        mPager.setRTLSupportAdapter(getChildFragmentManager(), mAdapter);

        int holyday = HijriDate.isHolyday();
        if (holyday != 0) {
            TextView tv = v.findViewById(R.id.holyday);
            tv.setVisibility(View.VISIBLE);
            tv.setText(LocaleUtils.getHolyday(holyday));
        }

        mPager.addOnPageChangeListener(this);


        mTopSlider.setOnDrawerScrollListener(new MultipleOrientationSlidingDrawer.OnDrawerScrollListener() {
            @Override
            public void onScrollStarted() {
            }

            @Override
            public void onScrolling(int pos) {
                mPager.setTranslationY(pos);
            }

            @Override
            public void onScrollEnded() {

            }
        });

        mTopSlider.setOnDrawerOpenListener(() -> {
            int position = mPager.getCurrentItem();
            if (position != 0) {
                Times t = Times.getTimes(mAdapter.getItemId(position));
                mSettingsFrag.setTimes(t);
            }
            mBottomSlider.lock();
        });

        mTopSlider.setOnDrawerCloseListener(() -> {
            mBottomSlider.unlock();

            Fragment page = getChildFragmentManager()
                    .findFragmentByTag("android:switcher:" + mPager.getId() + ":" + mAdapter.getItemId(mPager.getCurrentItem()));
            if (page instanceof CityFragment) {
                ((CityFragment) page).update();
            }
        });


        mBottomSlider.setOnDrawerOpenListener(() -> mTopSlider.lock());

        mBottomSlider.setOnDrawerCloseListener(() -> mTopSlider.unlock());
        v.findViewById(R.id.topSliderCloseHandler).setOnTouchListener((view, motionEvent) -> {
            if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mTopSlider.isOpened()) {
                mTopSlider.animateClose();
                return true;
            }
            return false;
        });


        v.findViewById(R.id.bottomSliderCloseHandler).setOnTouchListener((view, motionEvent) -> {
            if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mBottomSlider.isOpened()) {
                mBottomSlider.animateClose();
                return true;
            }
            return false;
        });

        int startPos = 1;
        if (getArguments() != null) {
            startPos = getArguments().getInt("startCity", -1) + 1;
        } else
            startPos = 1;

        if (startPos <= 0) {
            startPos = 1;
        }
        startPos = Math.min(startPos, mAdapter.getCount() - 1);

        mPager.setCurrentItem(startPos);
        onPageScrolled(startPos, 0, 0);
        onPageSelected(startPos);
        onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
        return v;
    }


    @Override
    public void onClick(View v) {
        if (v == mAddCityFab) {
            moveToFrag(new SearchCityFragment());
        }

    }


    @Override
    public boolean onBackPressed() {
        Fragment frag = getChildFragmentManager().findFragmentByTag("notPrefs");
        if (frag != null) {
            setFooterText(getString(R.string.monthly), true);
            getChildFragmentManager().beginTransaction().remove(frag).commit();
            return true;
        } else if (mBottomSlider.isOpened()) {
            mBottomSlider.animateClose();
            return true;
        } else if (mTopSlider.isOpened()) {
            mTopSlider.animateClose();
            return true;
        }
        return false;
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int pos = mPager.getCurrentItem();
            if (pos != 0) {
                Times t = Times.getTimes(mAdapter.getItemId(pos));
                mImsakiyeFrag.setTimes(t);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0) {
            mTopSlider.lock();
            mBottomSlider.lock();
        } else {
            mTopSlider.unlock();
            mBottomSlider.unlock();
        }
        if (mAddCityFab != null)
            if ((position == 0) && (positionOffset == 0)) {
                mAddCityFab.show();
            } else {
                mAddCityFab.hide();
            }


    }

    public void setFooterText(@NonNull CharSequence txt, boolean enablePane) {
        mFooterText.setVisibility(txt.toString().isEmpty() ? View.GONE : View.VISIBLE);
        mFooterText.setText(txt);
        mPager.setSwipeLocked(!enablePane);

    }

    @Override
    public void onPageSelected(int pos) {
        mFooterText.setText((pos == 0) ? R.string.cities : R.string.monthly);
    }

    public void onItemClick(int pos) {
        mPager.setCurrentItem(pos + 1, true);

    }

    public static class MyAdapter extends FragmentPagerAdapter implements Observer<List<Times>> {

        private List<Times> mTimes;

        MyAdapter(LifecycleOwner lco, FragmentManager fm) {
            super(fm);

            Times.getTimes().observe(lco, this);
            this.onChanged(Times.getTimes());
        }


        @Override
        public void onChanged(@NonNull List<Times> times) {
            mTimes = new ArrayList<>(times);
            Collections.sort(mTimes, Comparator.comparingInt(TimesBase::getSortId));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTimes.size() + 1;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return 0;
            }

            return mTimes.get(position - 1).getID();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            if (object instanceof SortFragment) {
                return 0;
            } else {
                CityFragment frag = (CityFragment) object;
                int pos = mTimes.indexOf(frag.getTimes());
                if (pos >= 0) {
                    return pos + 1;
                }
            }
            return POSITION_NONE;
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position > 0) {
                CityFragment frag = new CityFragment();
                Bundle bdl = new Bundle();
                bdl.putLong("city", getItemId(position));
                frag.setArguments(bdl);

                return frag;
            } else {
                return new SortFragment();
            }
        }


    }
}
