package com.metinkale.prayerapp.vakit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.vakit.times.Cities;
import com.metinkale.prayerapp.vakit.times.Cities.Item;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.WebTimes;

import java.util.List;

public class AddCityLegacy extends BaseActivity implements OnItemClickListener {

    private ListView mListView;
    private MyAdapter mAdapter;
    private Thread mThread;
    private String mSource, mCountry, mState, mCity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.vakit_addcity);

        TextView legacy = (TextView) findViewById(R.id.legacySwitch);
        legacy.setText(R.string.newAddCity);
        legacy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AddCityLegacy.this.finish();
                startActivity(new Intent(AddCityLegacy.this, AddCity.class));

            }

        });

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(this);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);

        get("", "", "", "");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void get(final String source, final String country, final String state, final String city) {

        List<String> resp = Cities.list(source, country, state, city);
        if (!resp.isEmpty()) {
            mSource = source;
            mCountry = country;
            mState = state;
            mCity = city;
            mAdapter.clear();
            mAdapter.addAll(resp);
            mListView.scrollTo(0, 0);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (mCity != "") {
            get(mSource, mCountry, mState, "");
        } else if (mState != "") {
            get(mSource, mCountry, "", "");
        } else if (mCountry != "") {
            get(mSource, "", "", "");
        } else if (mSource != "") {
            get("", "", "", "");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long index) {
        if (mAdapter.getFullText(pos).contains(",")) {
            Item i = new Item();
            i.city = mAdapter.getItem(pos);
            i.country = mCountry;
            String[] s = mAdapter.getFullText(pos).split(",");
            i.id = s[1];
            i.lat = Double.parseDouble(s[2]);
            i.lng = Double.parseDouble(s[3]);
            i.source = Times.Source.valueOf(mSource);

            WebTimes.add(i.source, i.city, i.id, i.lat, i.lng);
            finish();
        }

        if (mSource == "") {
            get(mAdapter.getItem(pos), "", "", "");
        } else if (mCountry == "") {
            get(mSource, mAdapter.getItem(pos), "", "");
        } else if (mState == "") {
            get(mSource, mCountry, mAdapter.getItem(pos), "");
        } else if (mCity == "") {
            get(mSource, mCountry, mState, mAdapter.getItem(pos));
        }
    }

    @Override
    public boolean setNavBar() {
        setNavBarColor(0xffeeeeee);
        return true;
    }

    class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1);

        }

        public String getFullText(int pos) {
            return super.getItem(pos);
        }

        @Override
        public String getItem(int pos) {
            String s = super.getItem(pos);
            if (s.contains(","))
                s = s.substring(0, s.indexOf(","));

            return s;
        }

    }

}
