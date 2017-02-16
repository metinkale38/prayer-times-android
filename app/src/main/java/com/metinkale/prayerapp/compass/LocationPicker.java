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

package com.metinkale.prayerapp.compass;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Geocoder;

public class LocationPicker extends Activity implements TextWatcher, OnItemClickListener {
    private ArrayAdapter<Geocoder.Result> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass_location);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(this);

        mAdapter = new ArrayAdapter<Geocoder.Result>(this, android.R.layout.simple_list_item_1, android.R.id.text1) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
        };

        list.setAdapter(mAdapter);

        EditText city = (EditText) findViewById(R.id.location);
        city.addTextChangedListener(this);
    }

    @Override
    public void afterTextChanged(@NonNull Editable txt) {
        Geocoder.search(txt.toString(), results -> {
            if (results == null) return;
            mAdapter.clear();
            mAdapter.addAll(results);
        });

    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        Geocoder.Result a = mAdapter.getItem(pos);
        Prefs.setCompassPos((float) a.lat, (float) a.lng);

        finish();

    }

}
