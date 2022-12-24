/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Cities;
import com.metinkale.prayer.times.times.Entry;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.sources.WebTimes;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class SelectCityFragment extends BaseActivity.MainFragment implements OnItemClickListener {

    private ListView mListView;
    private MyAdapter mAdapter;
    @NonNull
    private final Stack<Long> mBackStack = new Stack<>();
    @Nullable
    private final Cities mCities = Cities.get();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vakit_addcity, container, false);

        v.findViewById(R.id.search).setVisibility(View.GONE);
        v.findViewById(R.id.autoLocation).setVisibility(View.GONE);
        TextView legacy = v.findViewById(R.id.legacySwitch);
        legacy.setText(R.string.newAddCity);
        legacy.setOnClickListener(view -> {
            back();
            moveToFrag(new SearchCityFragment());
        });

        mListView = v.findViewById(R.id.listView);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(this);
        mAdapter = new MyAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        get(0);
        return v;
    }

    public void get(long id) {
        mBackStack.add(id);
        mCities.list(id, new Cities.Callback<List<Entry>>() {
            @Override
            public void onResult(@NonNull List<Entry> result) {
                if (!result.isEmpty()) {
                    Collections.sort(result, Comparator.comparing(Entry::getName));
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
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(entry.getName());
            tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, entry.getKey() == null ? R.drawable.ic_chevron_right : 0, 0);

            return convertView;
        }
    }

}
