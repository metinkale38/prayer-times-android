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

package com.metinkale.prayerapp.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import org.joda.time.LocalDate;


public class Main extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_main);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(LocalDate.now().getYear() - HicriDate.MIN_YEAR);
    }


    public static class YearFragment extends Fragment implements OnItemClickListener {

        public static final String YEAR = "year";
        private int year;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.calendar_frag, container, false);

            ListView listView = (ListView) view.findViewById(android.R.id.list);

            year = getArguments().getInt(YEAR);
            listView.setAdapter(new Adapter(getActivity(), year));

            listView.setOnItemClickListener(this);

            return view;

        }

        @Override
        public void onItemClick(AdapterView<?> arg0, @NonNull View v, int pos, long arg3) {
            String asset = Utils.getAssetForHolyday((Integer) v.getTag());
            if ((asset != null) && !"en".equals(Prefs.getLanguage()) && !"ar".equals(Prefs.getLanguage())) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("asset", asset);
                startActivity(i);
            }
        }
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if ("ar".equals(Prefs.getLanguage())) position = getCount() - position - 1;
            Fragment fragment = new YearFragment();
            Bundle args = new Bundle();
            args.putInt(YearFragment.YEAR, position + HicriDate.MIN_YEAR);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return HicriDate.MAX_YEAR - HicriDate.MIN_YEAR;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if ("ar".equals(Prefs.getLanguage())) position = getCount() - position - 1;
            return Utils.toArabicNrs(position + HicriDate.MIN_YEAR);
        }
    }

}
