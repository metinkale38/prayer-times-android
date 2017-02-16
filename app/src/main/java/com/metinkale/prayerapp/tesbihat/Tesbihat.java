/*
 * Copyright (c) 2016 Metin Kale
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

package com.metinkale.prayerapp.tesbihat;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.webkit.WebView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.utils.PagerSlidingTabStrip;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Times;

public class Tesbihat extends BaseActivity {

    private static int mTextSize;
    @Nullable
    private static String mLang;
    private FragmentStatePagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tesbihat_main);
        mLang = Prefs.getLanguage();

        PagerSlidingTabStrip indicator = (PagerSlidingTabStrip) findViewById(R.id.indicator);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        if ("tr".equals(mLang)) {
            mSectionsPagerAdapter = new TurkishPagerAdapter(getSupportFragmentManager());

        } else {
            mSectionsPagerAdapter = new OtherPagerAdapter(getSupportFragmentManager());
            indicator.setVisibility(View.GONE);
        }
        mViewPager.setAdapter(mSectionsPagerAdapter);
        indicator.setViewPager(mViewPager);
        indicator.setTextColor(0xffffffff);
        indicator.setDividerColor(0x0);
        indicator.setIndicatorColor(0xffffffff);

        if ((Times.getCount() != 0) && "tr".equals(mLang)) {
            int next = Times.getTimes(Times.getIds().get(0)).getNext();

            switch (next) {
                case 0:
                case 6:
                    mViewPager.setCurrentItem(4);
                    break;
                case 1:
                case 2:
                    mViewPager.setCurrentItem(0);
                    break;
                case 3:
                    mViewPager.setCurrentItem(1);
                    break;
                case 4:
                    mViewPager.setCurrentItem(2);
                    break;
                case 5:
                    mViewPager.setCurrentItem(3);
                    break;

            }
        }

        mTextSize = Prefs.getTesbihatTextSize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.tesbihat, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.zoomIn: {
                mTextSize++;

                Prefs.setTesbihatTextSize(mTextSize);
                int i = mViewPager.getCurrentItem();
                mViewPager.invalidate();
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.setCurrentItem(i);
            }
            break;
            case R.id.zoomOut:
                mTextSize--;

                PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("tesbihatTextSize", mTextSize).apply();
                int i = mViewPager.getCurrentItem();
                mViewPager.invalidate();
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.setCurrentItem(i);

                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class TesbihatFragment extends Fragment {

        private int pos;

        @NonNull
        public static TesbihatFragment create(int pos) {
            TesbihatFragment frag = new TesbihatFragment();
            frag.pos = pos;
            return frag;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.webview, container, false);
            WebView wv = (WebView) rootView.findViewById(R.id.webview);
            wv.getSettings().setTextZoom((int) (102.38 * Math.pow(1.41, mTextSize)));
            if ("tr".equals(mLang)) {
                wv.loadUrl("file:///android_asset/" + mLang + "/tesbihat/" + getAssetDir(pos));
            } else {
                wv.loadUrl("file:///android_asset/en/tasbihat.html");
            }
            return rootView;
        }

        @Nullable
        public String getAssetDir(int position) {
            switch (position) {
                case 0:
                    return "sabah.htm";
                case 1:
                    return "ogle.htm";
                case 2:
                    return "ikindi.htm";
                case 3:
                    return "aksam.htm";
                case 4:
                    return "yatsi.htm";
            }
            return null;
        }

    }

    public class TurkishPagerAdapter extends FragmentStatePagerAdapter {

        public TurkishPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return TesbihatFragment.create(position);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.morningPrayer);
                case 1:
                    return getString(R.string.zuhr);
                case 2:
                    return getString(R.string.asr);
                case 3:
                    return getString(R.string.maghrib);
                case 4:
                    return getString(R.string.ishaa);
            }
            return null;
        }
    }

    public static class OtherPagerAdapter extends FragmentStatePagerAdapter {

        public OtherPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return TesbihatFragment.create(position);

        }

        @Override
        public int getCount() {
            return 1;
        }

        @NonNull
        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

}
