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

package com.metinkale.prayer.times.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.utils.Utils;

import org.joda.time.LocalDate;

/**
 * Created by Metin on 21.07.2015.
 */
public class ImsakiyeFragment extends Fragment {
    private ImsakiyeAdapter mAdapter;
    private Times mTimes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        ListView lv = new ListView(getActivity());
        mAdapter = new ImsakiyeAdapter(getActivity());
        lv.setAdapter(mAdapter);
        setTimes(mTimes);
        TextView addMore = new TextView(getActivity());
        addMore.setText("\n" + getString(R.string.showMore) + "\n");
        addMore.setGravity(Gravity.CENTER);
        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.daysInMonth += 7;
                mAdapter.notifyDataSetInvalidated();
            }
        });
        lv.addFooterView(addMore);
        lv.setBackgroundResource(R.color.background);
        return lv;
    }

    public void setTimes(Times t) {
        mTimes = t;
        if (mAdapter != null) {
            mAdapter.times = mTimes;
            mAdapter.notifyDataSetChanged();
            mAdapter.daysInMonth = ((LocalDate) mAdapter.getItem(1)).dayOfMonth().getMaximumValue();

        }
    }

    public class ImsakiyeAdapter extends BaseAdapter {
        private Times times;
        private int daysInMonth;
        private LocalDate date;
        private int today;
        private LayoutInflater inflater;

        public ImsakiyeAdapter(Context context) {
            LocalDate now = LocalDate.now();
            today = now.getDayOfMonth();
            date = now.withDayOfMonth(1);
            daysInMonth = date.dayOfMonth().getMaximumValue();


            inflater = LayoutInflater.from(context);
        }

        @Override
        public long getItemId(int position) {
            return position + ((times == null) ? 0 : times.getID());
        }

        @NonNull
        @Override
        public Object getItem(int position) {
            return date.plusDays(position);
        }

        @Nullable
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.vakit_imsakiye, parent, false);
            }
            ViewGroup v = (ViewGroup) convertView;
            String[] a;
            if (position == 0) {
                a = new String[]{getString(R.string.date), getString(R.string.fajr), getString(R.string.sun), getString(R.string.zuhr), getString(R.string.asr), getString(R.string.maghrib), getString(R.string.ishaa)};
            } else if (times == null) {
                a = new String[]{"00:00", "00:00", "00:00", "00:00", "00:00", "00:00", "00:00"};
            } else {
                LocalDate cal = (LocalDate) getItem(position - 1);

                String[] daytimes = {times.getTime(cal, 0), times.getTime(cal, 1), times.getTime(cal, 2), times.getTime(cal, 3), times.getTime(cal, 4), times.getTime(cal, 5)};

                a = new String[]{cal.toString("dd.MM"), daytimes[0], daytimes[1], daytimes[2], daytimes[3], daytimes[4], daytimes[5]};
            }

            for (int i = 0; i < 7; i++) {
                TextView tv = (TextView) v.getChildAt(i);
                if (i != 0)
                    tv.setText(Utils.fixTimeForHTML(a[i]));
                else
                    tv.setText(Utils.toArabicNrs(a[i]));
            }
            if (position == today) {
                v.setBackgroundResource(R.color.colorPrimary);
            } else if (position == 0) {
                v.setBackgroundResource(R.color.indicator);
            } else if ((position % 2) == 0) {
                v.setBackgroundResource(R.color.colorPrimaryLight);
            } else {
                v.setBackgroundResource(R.color.background);
            }

            return v;
        }

        @Override
        public int getCount() {
            return daysInMonth + 1;
        }

    }
}
