/*
 * Copyright (c) 2013-2017 Metin Kale
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

package com.metinkale.prayerapp.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.fragments.CityFragment;
import com.metinkale.prayerapp.vakit.fragments.SortFragment;
import com.metinkale.prayerapp.vakit.times.Times;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by metin on 17.07.2017.
 */

public class ConfigIntroFragment extends IntroFragment implements ViewPager.OnPageChangeListener {

    private ViewPager mPager;
    private FloatingActionButton mAddCityFab;
    private TextView mFooterText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_pager, container, false);


        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.appName);
        toolbar.setNavigationIcon(MaterialDrawableBuilder.with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.MENU)
                .setColor(Color.WHITE)
                .setToActionbarSize()
                .build());

        mPager = (ViewPager) v.findViewById(R.id.pager);
        mPager.setBackgroundColor(Color.WHITE);
        mPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) return new SortFragment();
                else {
                    Times t = Times.getTimesAt(position - 1);
                    Bundle bdl = new Bundle();
                    bdl.putLong("city", t.getID());
                    Fragment frag = new CityFragment();
                    frag.setArguments(bdl);
                    return frag;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        mPager.addOnPageChangeListener(this);


        mPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem((mPager.getCurrentItem() + 1) % 3, true);
                mPager.postDelayed(this, 2500);
            }
        }, 2500);

        mAddCityFab = (FloatingActionButton) v.findViewById(R.id.addCity);
        mFooterText = (TextView) v.findViewById(R.id.footerText);
        return v;
    }


    @Override
    protected void onSelect() {
    }

    @Override
    protected void onEnter() {
    }

    @Override
    protected void onExit() {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mAddCityFab != null)
            if ((position == 0) && (positionOffset == 0)) {
                mAddCityFab.show();
                mFooterText.setText(R.string.cities);
            } else {
                mAddCityFab.hide();
                mFooterText.setText(R.string.monthly);
            }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
