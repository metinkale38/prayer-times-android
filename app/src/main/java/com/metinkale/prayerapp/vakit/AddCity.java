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

package com.metinkale.prayerapp.vakit;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.vakit.times.CalcTimes;
import com.metinkale.prayerapp.vakit.times.WebTimes;
import com.metinkale.prayerapp.vakit.times.Cities;
import com.metinkale.prayerapp.vakit.times.Cities.Item;
import com.metinkale.prayerapp.vakit.times.Source;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class AddCity extends BaseActivity implements OnItemClickListener, OnQueryTextListener, LocationListener, OnClickListener {
    private MyAdapter mAdapter;
    private FloatingActionButton mFab;
    private MenuItem mSearchItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vakit_addcity);

        mFab = (FloatingActionButton) findViewById(R.id.search);
        mFab.setOnClickListener(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(this);
        mAdapter = new MyAdapter(this);
        listView.setAdapter(mAdapter);

        TextView legacy = (TextView) findViewById(R.id.legacySwitch);
        legacy.setText(R.string.oldAddCity);
        legacy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(AddCity.this, AddCityLegacy.class));

            }

        });

        checkLocation();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.get(this).pLocation) {
            checkLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    public void checkLocation() {
        if (PermissionUtils.get(this).pLocation) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location loc = null;
            List<String> providers = lm.getProviders(true);
            for (String provider : providers) {
                Location last = lm.getLastKnownLocation(provider);
                // one hour==1meter in accuracy
                if ((last != null) && ((loc == null) || ((last.getAccuracy() - (last.getTime() / (1000 * 60 * 60))) < (loc.getAccuracy() - (loc.getTime() / (1000 * 60 * 60)))))) {
                    loc = last;
                }
            }

            if (loc != null)
                onLocationChanged(loc);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setSpeedRequired(false);
            String provider = lm.getBestProvider(criteria, true);
            if (provider != null) {
                lm.requestSingleUpdate(provider, this, null);
            }

        } else {
            PermissionUtils.get(this).needLocation(this);
        }
    }


    @Override
    public void onClick(View view) {
        if (view == mFab) {
            MenuItemCompat.collapseActionView(mSearchItem);
            MenuItemCompat.expandActionView(mSearchItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MaterialMenuInflater.with(this)
                .setDefaultColor(0xFFFFFFFF)
                .inflate(R.menu.search, menu);
        mSearchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.performClick();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long index) {
        Cities.Item i = mAdapter.getItem(pos);
        if (i != null) if (i.source == Source.Calc) {
            Bundle bdl = new Bundle();
            bdl.putString("city", i.city);
            bdl.putDouble("lat", i.lat);
            bdl.putDouble("lng", i.lng);
            CalcTimes.add(this, bdl);
        } else {
            WebTimes.add(i.source, i.city, i.id, i.lat, i.lng);
            finish();
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        Cities.search(query, new Cities.Callback() {
            @Override
            public void onResult(List result) {
                List<Item> items = result;
                if ((items != null) && !items.isEmpty()) {
                    mAdapter.clear();
                    mAdapter.addAll(items);
                }
                mAdapter.notifyDataSetChanged();
            }
        });


        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }


    @Override
    public void onLocationChanged(Location loc) {
        if ((mAdapter.getCount() <= 1)) {
            mAdapter.clear();
            Item item = new Item();
            item.city = "GPS";
            item.country = "GPS";
            item.source = Source.Calc;
            item.lat = loc.getLatitude();
            item.lng = loc.getLongitude();
            mAdapter.add(item);
            mAdapter.notifyDataSetChanged();

            Cities.search(item.lat + "," + item.lng, new Cities.Callback() {
                @Override
                public void onResult(List result) {
                    List<Item> items = result;
                    if ((items != null) && !items.isEmpty()) {
                        mAdapter.clear();
                        mAdapter.addAll(items);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    static class MyAdapter extends ArrayAdapter<Cities.Item> {

        public MyAdapter(Context context) {
            super(context, 0, 0);

        }

        @Override
        public void addAll(@NonNull Collection<? extends Item> collection) {
            super.addAll(collection);
            sort(new Comparator<Item>() {

                @Override
                public int compare(Item i0, Item i1) {
                    return i0.source.ordinal() - i1.source.ordinal();
                }

            });
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.vakit_addcity_row, null);
                vh = new ViewHolder();
                vh.city = (TextView) convertView.findViewById(R.id.city);
                vh.country = (TextView) convertView.findViewById(R.id.country);
                vh.sourcetxt = (TextView) convertView.findViewById(R.id.sourcetext);
                vh.source = (ImageView) convertView.findViewById(R.id.source);

                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            Cities.Item i = getItem(position);
            if (i.city != null && i.city.startsWith("BRUNSWICK")) {
                i.city = "Braunschweig"; //;)
            }
            vh.city.setText(i.city);
            vh.country.setText(i.country);

            vh.sourcetxt.setText(i.source.text);
            if (i.source.resId == 0) {
                vh.source.setVisibility(View.GONE);
            } else {
                vh.source.setImageResource(i.source.resId);
                vh.source.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        class ViewHolder {
            TextView country;
            TextView city;
            TextView sourcetxt;
            ImageView source;
        }

    }

}
