package com.metinkale.prayerapp.calendar;

import android.content.Intent;
import android.os.Bundle;
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
        viewPager.setCurrentItem(LocalDate.now().getYear());
    }

    @Override
    public boolean setNavBar() {
        return false;
    }

    public static class YearFragment extends Fragment implements OnItemClickListener {

        public static final String YEAR = "year";
        private int year;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.calendar_frag, container, false);

            ListView listView = (ListView) view.findViewById(android.R.id.list);

            year = getArguments().getInt(YEAR);
            listView.setAdapter(new Adapter(getActivity(), year));

            listView.setOnItemClickListener(this);

            return view;

        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
            String asset = Utils.getAssetForHolyday((Integer) v.getTag());
            if ((asset != null) && !"en".equals(Prefs.getLanguage())) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("asset", asset);
                startActivity(i);
            }
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new YearFragment();
            Bundle args = new Bundle();
            args.putInt(YearFragment.YEAR, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3000;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position + "";
        }
    }

}
