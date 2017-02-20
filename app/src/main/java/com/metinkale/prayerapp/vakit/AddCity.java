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
 *
 */

package com.metinkale.prayerapp.vakit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.utils.FileChooser;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.vakit.times.CalcTimes;
import com.metinkale.prayerapp.vakit.times.Cities;
import com.metinkale.prayerapp.vakit.times.Entry;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.WebTimes;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class AddCity extends BaseActivity implements OnItemClickListener, OnQueryTextListener, LocationListener, OnClickListener {
    private MyAdapter mAdapter;
    private FloatingActionButton mFab;
    private MenuItem mSearchItem;
    @Nullable
    private Cities mCities = Cities.get();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vakit_addcity);

        mFab = (FloatingActionButton) findViewById(R.id.search);
        mFab.setOnClickListener(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setFastScrollEnabled(true);
        listView.setOnItemClickListener(this);
        listView.addFooterView(View.inflate(this, R.layout.vakit_addcity_addcsv, null));
        mAdapter = new MyAdapter(this);
        listView.setAdapter(mAdapter);

        TextView legacy = (TextView) findViewById(R.id.legacySwitch);
        legacy.setText(R.string.oldAddCity);
        legacy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(AddCity.this, AddCityLegacy.class));
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocation();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onPause() {
        if (PermissionUtils.get(this).pLocation) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.removeUpdates(this);
        }
        super.onPause();
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
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setSpeedRequired(true);
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
        Entry i = mAdapter.getItem(pos);
        if (i != null) if (i.getSource() == Source.Calc) {
            Bundle bdl = new Bundle();
            bdl.putString("city", i.getName());
            bdl.putDouble("lat", i.getLat());
            bdl.putDouble("lng", i.getLng());
            CalcTimes.add(this, bdl);
        } else {
            WebTimes.add(i.getSource(), i.getName(), i.getKey(), i.getLat(), i.getLng());
            finish();
        }

    }

    @Override
    public boolean onQueryTextSubmit(@Nullable String query) {

        mCities.search(query == null ? query : query.trim().replace(" ", "+"), new Cities.Callback<List<Entry>>() {
            @Override
            public void onResult(@Nullable List<Entry> items) {
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
    public void onLocationChanged(@NonNull Location loc) {
        if ((mAdapter.getCount() <= 1)) {
            mAdapter.clear();
            Entry item = new Entry();
            item.setName("GPS");
            item.setCountry("");
            item.setKey("C_");
            item.setLat(loc.getLatitude());
            item.setLng(loc.getLongitude());
            mAdapter.add(item);
            mAdapter.notifyDataSetChanged();

            mCities.search(item.getLat(), item.getLng(), new Cities.Callback<List<Entry>>() {
                @Override
                public void onResult(@Nullable List<Entry> items) {
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

    public void addFromCSV(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.addFromCSV)
                .setItems(R.array.addFromCSV, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            FileChooser chooser = new FileChooser(AddCity.this);
                            chooser.setExtension("csv");
                            chooser.showDialog();
                            chooser.setFileListener(new FileChooser.FileSelectedListener() {
                                @Override
                                public void fileSelected(File file) {
                                    String name = file.getName();
                                    if (name.contains("."))
                                        name = name.substring(0, name.lastIndexOf("."));

                                    WebTimes.add(Source.CSV, name, file.toURI().toString(), 0, 0);
                                    finish();
                                }
                            });
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(AddCity.this);
                            final EditText editText = new EditText(AddCity.this);
                            editText.setHint("http(s)://example.com/prayertimes.csv");
                            alert.setView(editText);
                            alert.setTitle(R.string.csvFromURL);
                            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String url = editText.getText().toString();
                                    String name = url.substring(url.lastIndexOf("/") + 1);
                                    if (name.contains("."))
                                        name = name.substring(0, name.lastIndexOf("."));
                                    WebTimes.add(Source.CSV, name, url, 0, 0);
                                    finish();
                                }
                            });
                            alert.setNegativeButton(R.string.cancel, null);
                            alert.show();

                        }
                    }
                });
        builder.show();
    }


    static class MyAdapter extends ArrayAdapter<Entry> {

        public MyAdapter(@NonNull Context context) {
            super(context, 0, 0);

        }

        @Override
        public void addAll(@NonNull Collection<? extends Entry> collection) {
            super.addAll(collection);
            sort(new Comparator<Entry>() {
                @Override
                public int compare(Entry e1, Entry e2) {
                    return e1.getSource().ordinal() - e2.getSource().ordinal();
                }
            });
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
            Entry i = getItem(position);

            vh.city.setText(i.getName());
            vh.country.setText(i.getCountry());

            vh.sourcetxt.setText(i.getSource().text);
            if (i.getSource().resId == 0) {
                vh.source.setVisibility(View.GONE);
            } else {
                vh.source.setImageResource(i.getSource().resId);
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
