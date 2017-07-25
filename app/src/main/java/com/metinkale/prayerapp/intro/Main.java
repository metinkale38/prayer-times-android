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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;

import java.util.Locale;

/**
 * Created by metin on 17.07.2017.
 */

public class Main extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager mPager;
    private int[] mColors = {
            0xFF3F51B5,
            0xFF00BCD4,
            0xFF673AB7,
            App.get().getColor(R.color.colorPrimary),
            App.get().getColor(R.color.indicator),
            App.get().getColor(R.color.colorPrimaryDark),
            App.get().getColor(R.color.colorPrimaryLight)
    };
    private IntroFragment[] mFragments = {
            new MenuIntroFragment(),
            new PagerIntroFragment(),
            new ConfigIntroFragment(),
            new MenuIntroFragment()
    };
    private MyAdapter mAdapter;
    private View mMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_main);
        mPager = (ViewPager) findViewById(R.id.pager);
        mMain = findViewById(R.id.main);

        if (Times.getCount() < 2) {
            if (Locale.getDefault().equals(new Locale("tr"))) {
                WebTimes.add(Source.Diyanet, "Mekke", "D_64_16309", 21.4260221, 39.8296538, -1);
                WebTimes.add(Source.Diyanet, "Kayseri", "D_2_546_9620", 38.7333333333333, 35.4833333333333, -2);
            } else if (Locale.getDefault().equals(new Locale("de"))) {
                WebTimes.add(Source.Diyanet, "Mekka", "D_64_16309", 21.4260221, 39.8296538, -1);
                WebTimes.add(Source.Diyanet, "Braunschweig", "D_13_16754", 52.2666666666667, 10.5166666666667, -2);
            } else if (Locale.getDefault().equals(new Locale("fr"))) {
                WebTimes.add(Source.Diyanet, "Mecque", "D_64_16309", 21.4260221, 39.8296538, -1);
                WebTimes.add(Source.Diyanet, "Paris", "D_21_13382", 48.8566101, 2.3514992, -2);
            } else {
                WebTimes.add(Source.Diyanet, "Mecca", "D_64_16309", 21.4260221, 39.8296538, -1);
                WebTimes.add(Source.Diyanet, "London", "D_33_617_8928", 51.5073219, -0.1276473, -2);
            }
        }

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(this);


    }

    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int color1 = mColors[position % mColors.length];
        int color2 = mColors[position + 1 % mColors.length];
        mMain.setBackgroundColor(blendColors(color1, color2, 1 - positionOffset));

        if (position > 0 && positionOffset == 0)
            mFragments[position].setPagerPosition(1);
        mFragments[position].setPagerPosition(positionOffset);
        if (mFragments.length > position + 1)
            mFragments[position + 1].setPagerPosition(1 - positionOffset);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }
}
