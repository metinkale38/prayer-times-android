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

package com.metinkale.prayerapp.vakit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.MainActivity;
import com.metinkale.prayerapp.vakit.times.Cities;
import com.metinkale.prayerapp.vakit.times.Entry;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class SelectCityFragment extends MainActivity.MainFragment implements OnItemClickListener {

    private ListView mListView;
    private MyAdapter mAdapter;
    @NonNull
    private Stack<Integer> mBackStack = new Stack<>();
    @Nullable
    private Cities mCities = Cities.get();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vakit_addcity, container, false);

        v.findViewById(R.id.search).setVisibility(View.GONE);

        TextView legacy = (TextView) v.findViewById(R.id.legacySwitch);
        legacy.setText(R.string.newAddCity);
        legacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
                moveToFrag(new SearchCityFragment());
            }
        });

        mListView = (ListView) v.findViewById(R.id.listView);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(this);
        mAdapter = new MyAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        get(0);
        return v;
    }

    public void get(int id) {
        mBackStack.add(id);
        mCities.list(id, new Cities.Callback<List<Entry>>() {
            @Override
            public void onResult(@NonNull List<Entry> result) {
                if (!result.isEmpty()) {
                    Collections.sort(result, new Comparator<Entry>() {
                        @Override
                        public int compare(Entry e1, Entry e2) {
                            return e1.getName().compareTo(e2.getName());
                        }
                    });
                    mAdapter.clear();
                    mAdapter.addAll(result);
                    mListView.scrollTo(0, 0);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long index) {
        Entry entry = mAdapter.getItem(pos);

        if (entry.getKey() != null) {
            Source source = entry.getSource();


            WebTimes.add(source, entry.getName(), entry.getKey(), entry.getLat(), entry.getLng());
            backToMain();
            return;
        }

        get(entry.getId());
    }

    @Override
    public boolean onBackPressed() {
        if (mBackStack.size() > 1) {
            mBackStack.pop();
            get(mBackStack.pop());
            return true;
        } else return super.onBackPressed();
    }

    static class MyAdapter extends ArrayAdapter<Entry> {

        MyAdapter(@NonNull Context context) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            Entry entry = getItem(position);
            TextView tv = ((TextView) convertView.findViewById(android.R.id.text1));
            tv.setText(entry.getName());
            tv.setCompoundDrawables(null, null, entry.getKey() == null ?
                    MaterialDrawableBuilder.with(getContext()).setIcon(MaterialDrawableBuilder.IconValue.CHEVRON_RIGHT).build() : null, null);

            return convertView;
        }
    }

}
