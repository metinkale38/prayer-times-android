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

package com.metinkale.prayerapp.names;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.names.Adapter.Item;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main extends BaseActivity implements OnQueryTextListener {

    private ListView listView;
    private Item[] values;

    @SuppressLint("NewApi")
    private static String normalize(CharSequence str) {
        String string = Normalizer.normalize(str, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "");
        return string.toLowerCase(Locale.ENGLISH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.names_main);

        listView = (ListView) findViewById(android.R.id.list);
        values = new Item[99];

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.names)));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\t");
                values[i] = new Item();
                values[i].arabic = p[0];
                values[i].name[0] = p[1];
                values[i].meaning[0] = p[2];
                values[i].name[1] = p[3];
                values[i].meaning[1] = p[4];
                values[i].name[2] = p[5];
                values[i].meaning[2] = p[6];
                i++;
            }
            br.close();
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        listView.setAdapter(new Adapter(this, values));
        listView.setFastScrollEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        List<Item> values = new ArrayList<>();
        for (Item val : this.values) {
            if (normalize(val.toString()).contains(normalize(newText))) {
                values.add(val);
            }
        }

        listView.setAdapter(new Adapter(this, values.toArray(new Item[values.size()])));
        return false;
    }

    @Override
    public boolean setNavBar() {
        return false;
    }

}
