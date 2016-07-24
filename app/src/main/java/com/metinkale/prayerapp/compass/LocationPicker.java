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

package com.metinkale.prayerapp.compass;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.custom.Geocoder;
import com.metinkale.prayerapp.settings.Prefs;

import java.util.List;

public class LocationPicker extends Activity implements TextWatcher, OnItemClickListener {
    private EditText mCity;
    private ArrayAdapter<String> mAdapter;
    private Thread mThread;
    private List<Geocoder.Address> mAddresses;
    private Runnable mSearch = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //expected to happen sometimes
            }

            String query = mCity.getText().toString();

            mAddresses = Geocoder.from(query, 5);

            if (mAddresses != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        for (Geocoder.Address a : mAddresses) {

                            mAdapter.add(a.city + " - " + a.country + " - " + a.state);
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass_location);

        mCity = (EditText) findViewById(R.id.location);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(this);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);

        list.setAdapter(mAdapter);

        mCity.addTextChangedListener(this);
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        if ((mThread != null) && mThread.isAlive()) {
            mThread.interrupt();
        }
        mThread = new Thread(mSearch);
        mThread.start();
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (mAddresses == null) {
            return;
        }
        Geocoder.Address a = mAddresses.get(pos);
        Prefs.setCompassPos((float) a.lat, (float) a.lng);

        finish();

    }

}
