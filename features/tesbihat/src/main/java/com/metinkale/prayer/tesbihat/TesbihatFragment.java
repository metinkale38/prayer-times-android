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

package com.metinkale.prayer.tesbihat;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.LocaleUtils;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TesbihatFragment extends BaseActivity.MainFragment {

    private static int mTextSize;
    @Nullable
    private static Locale mLocale;
    private FragmentStatePagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tesbihat_main, container, false);
        mLocale = LocaleUtils.getLocale();

        PagerSlidingTabStrip indicator = v.findViewById(R.id.indicator);

        mViewPager = v.findViewById(R.id.pager);

        if (new Locale("tr").getLanguage().equals(mLocale.getLanguage())) {
            mSectionsPagerAdapter = new TurkishPagerAdapter(getChildFragmentManager());
        } else {
            mSectionsPagerAdapter = new OtherPagerAdapter(getChildFragmentManager());
            indicator.setVisibility(View.GONE);
        }
        mViewPager.setAdapter(mSectionsPagerAdapter);
        indicator.setViewPager(mViewPager);
        indicator.setTextColor(0xffffffff);
        indicator.setDividerColor(0x0);
        indicator.setIndicatorColor(0xffffffff);

        if ((Times.getCount() != 0) && new Locale("tr").getLanguage().equals(mLocale.getLanguage())) {
            int current = Times.getTimes(Times.getIds().get(0)).getCurrentTime();
            switch (Vakit.getByIndex(current)) {
                case FAJR:
                    mViewPager.setCurrentItem(0);
                    break;
                case SUN:
                case DHUHR:
                    mViewPager.setCurrentItem(1);
                    break;
                case ASR:
                    mViewPager.setCurrentItem(2);
                    break;
                case MAGHRIB:
                    mViewPager.setCurrentItem(3);
                    break;
                case ISHAA:
                    mViewPager.setCurrentItem(4);
                    break;

            }
        }

        mTextSize = Preferences.TESBIHAT_TEXTSIZE.get();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tesbihat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == R.id.zoomIn) {
            mTextSize++;
            Preferences.TESBIHAT_TEXTSIZE.set(mTextSize);
            int i = mViewPager.getCurrentItem();
            mViewPager.invalidate();
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(i);

        } else if (i1 == R.id.zoomOut) {
            mTextSize--;

            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("tesbihatTextSize", mTextSize).apply();
            int i = mViewPager.getCurrentItem();
            mViewPager.invalidate();
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(i);


        }

        return super.onOptionsItemSelected(item);
    }


    public static class PageFragment extends Fragment {

        private int pos;

        @NonNull
        public static PageFragment create(int pos) {
            PageFragment frag = new PageFragment();
            frag.pos = pos;
            return frag;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.webview, container, false);
            WebView wv = rootView.findViewById(R.id.webview);
            wv.getSettings().setTextZoom((int) (102.38 * Math.pow(1.41, mTextSize)));
            if (new Locale("tr").getLanguage().equals(mLocale.getLanguage())) {
                wv.loadUrl("file:///android_asset/tr/tesbihat/" + getAssetDir(pos));
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
            return PageFragment.create(position);
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
                    return getString(R.string.fajr);
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
            return PageFragment.create(position);

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
