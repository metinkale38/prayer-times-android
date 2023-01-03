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

package com.metinkale.prayer.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.date.HijriDate;
import com.metinkale.prayer.utils.LocaleUtils;


import org.joda.time.LocalDate;

import java.util.Locale;


public class CalendarFragment extends BaseActivity.MainFragment implements OnItemClickListener {
    private static final String[] ASSETS = {"/dinigunler/hicriyil.html", "/dinigunler/asure.html", "/dinigunler/mevlid.html", "/dinigunler/3aylar.html", "/dinigunler/regaib.html", "/dinigunler/mirac.html", "/dinigunler/berat.html", "/dinigunler/ramazan.html", "/dinigunler/kadir.html", "/dinigunler/arefe.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/arefe.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html"};
    private PagerAdapter adapter;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calendar_main, container, false);

        adapter = new HijriPagerAdapter(getChildFragmentManager());

        viewPager = v.findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(HijriDate.now().getYear() - HijriDate.getMinHijriYear());
        return v;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.switchHijriGreg) {
            if (adapter instanceof HijriPagerAdapter) {
                adapter = new GregorianPagerAdapter(getChildFragmentManager());
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(LocalDate.now().getYear() - HijriDate.getMinGregYear());
            } else {
                adapter = new HijriPagerAdapter(getChildFragmentManager());
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(HijriDate.now().getYear() - HijriDate.getMinHijriYear());
            }
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    public static String getAssetForHolyday(int pos) {
        if (pos == 0) return null;
        return LocaleUtils.getLanguage("en", "de", "tr") + ASSETS[pos - 1];
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, @NonNull View v, int pos, long arg3) {
        String asset = getAssetForHolyday((Integer) v.getTag());

        Locale lang = LocaleUtils.getLocale();
        if ((asset != null) && (new Locale("de").getLanguage().equals(lang.getLanguage())
                || new Locale("tr").getLanguage().equals(lang.getLanguage()))) {
            Bundle bdl = new Bundle();
            bdl.putString("asset", asset);
            Fragment frag = new WebViewFragment();
            frag.setArguments(bdl);
            moveToFrag(frag);
        }
    }

    public static class YearFragment extends Fragment {

        public static final String YEAR = "year";
        public static final String IS_HIJRI = "isHijri";
        private int year;
        private boolean isHijri;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.calendar_frag, container, false);

            ListView listView = view.findViewById(android.R.id.list);

            year = getArguments().getInt(YEAR);
            isHijri = getArguments().getBoolean(IS_HIJRI);
            listView.setAdapter(new Adapter(getActivity(), year, isHijri));

            listView.setOnItemClickListener((OnItemClickListener) getParentFragment());

            return view;

        }


    }

    public static class GregorianPagerAdapter extends FragmentPagerAdapter {

        GregorianPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (LocaleUtils.getLocale().getLanguage().equals(new Locale("ar").getLanguage()))
                position = getCount() - position - 1;
            Fragment fragment = new YearFragment();
            Bundle args = new Bundle();
            args.putInt(YearFragment.YEAR, position + HijriDate.getMinGregYear());
            args.putBoolean(YearFragment.IS_HIJRI, false);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return HijriDate.getMaxGregYear() - HijriDate.getMinGregYear() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (LocaleUtils.getLocale().getLanguage().equals(new Locale("ar").getLanguage()))
                position = getCount() - position - 1;
            return LocaleUtils.formatNumber(position + HijriDate.getMinGregYear());
        }

        @Override
        public long getItemId(int position) {
            return position + HijriDate.getMinGregYear();
        }
    }

    public static class HijriPagerAdapter extends FragmentPagerAdapter {

        HijriPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (LocaleUtils.getLocale().getLanguage().equals(new Locale("ar").getLanguage()))
                position = getCount() - position - 1;
            Fragment fragment = new YearFragment();
            Bundle args = new Bundle();
            args.putInt(YearFragment.YEAR, position + HijriDate.getMinHijriYear());
            args.putBoolean(YearFragment.IS_HIJRI, true);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return HijriDate.getMaxHijriYear() - HijriDate.getMinHijriYear() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (LocaleUtils.getLocale().getLanguage().equals(new Locale("ar").getLanguage()))
                position = getCount() - position - 1;
            return LocaleUtils.formatNumber(position + HijriDate.getMinHijriYear());
        }

        @Override
        public long getItemId(int position) {
            return position + HijriDate.getMinHijriYear();
        }
    }

}
