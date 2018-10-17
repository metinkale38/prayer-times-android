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

import androidx.lifecycle.Observer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.alarm.AlarmService;
import com.metinkale.prayer.times.times.Times;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import org.joda.time.LocalDateTime;

public class AlarmsFragment extends Fragment implements Observer<Times> {
    private Times mTimes;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private AlarmsAdapter mAdapter;
    private boolean mManuallyTriggered = false;

    @NonNull
    public static AlarmsFragment create(@NonNull Times t) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", t.getID());
        AlarmsFragment frag = new AlarmsFragment();
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.vakit_notprefs, container, false);

        mTimes = Times.getTimes(getArguments().getLong("city", 0));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AlarmsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mTimes.observe(this, this);
        onChanged(mTimes);

        setHasOptionsMenu(true);
        return mRecyclerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MaterialMenuInflater.with(getActivity()).inflate(R.menu.alarms, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.add) {
            Alarm alarm = new Alarm();
            alarm.setCity(mTimes);
            mTimes.getUserAlarms().add(alarm);
            AlarmConfigFragment.create(alarm).show(getChildFragmentManager(), "alarmconfig");
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().setTitle(getString(R.string.appName));
        if (BuildConfig.DEBUG && !mManuallyTriggered) {
            mManuallyTriggered = false;
            Times.setAlarms();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(mTimes.getName());
    }


    @Override
    public void onChanged(Times times) {
        mAdapter.clearAllItems();
        mAdapter.add(new AlarmsAdapter.SwitchItem() {
            @Override
            public String getName() {
                return getString(R.string.ongoingNotification);
            }

            @Override
            public void onChange(boolean checked) {
                mTimes.setOngoingNotificationActive(checked);
            }

            @Override
            public boolean getChecked() {
                return mTimes.isOngoingNotificationActive();
            }
        });


        mAdapter.add(getString(R.string.notification));


        for (final Alarm alarm : times.getUserAlarms()) {
            mAdapter.add(new AlarmsAdapter.SwitchItem() {
                @Override
                public String getName() {
                    return alarm.getTitle();
                }

                @Override
                public void onChange(boolean checked) {
                    alarm.setEnabled(checked);
                }

                @Override
                public boolean getChecked() {
                    return alarm.isEnabled();
                }

                @Override
                public void onClick() {
                    if (!getChecked()) {
                        Toast.makeText(getActivity(), R.string.activateForMorePrefs, Toast.LENGTH_LONG).show();
                    } else {
                        AlarmConfigFragment.create(alarm).show(getChildFragmentManager(), "alarmconfig");
                    }
                }

                @Override
                public boolean onLongClick() {
                    if (BuildConfig.DEBUG) {
                        AlarmService.setAlarm(getActivity(), new Pair<>(alarm, LocalDateTime.now().plusSeconds(5)));
                        mManuallyTriggered = true;
                        return true;
                    }
                    return super.onLongClick();
                }
            });
        }
    }
}
