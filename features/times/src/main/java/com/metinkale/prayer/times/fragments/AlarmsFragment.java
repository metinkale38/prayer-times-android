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

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metinkale.prayer.CrashReporter;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.utils.BatteryOptimizationsKt;

public class AlarmsFragment extends Fragment implements Observer<Times> {
    private Times mTimes;
    private AlarmsAdapter mAdapter;

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
        View v = inflater.inflate(R.layout.vakit_notprefs, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.recycler);

        mTimes = Times.getTimes(getArguments().getLong("city", 0));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new AlarmsAdapter();
        recyclerView.setAdapter(mAdapter);

        mTimes.observe(this, this);
        onChanged(mTimes);

        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.add) {
            Alarm alarm = new Alarm();
            alarm.setCity(mTimes);
            mTimes.getUserAlarms().add(alarm);
            AlarmConfigFragment.create(alarm).show(this);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onPause() {
        super.onPause();
        mTimes.save();
        getActivity().setTitle(getString(R.string.appName));
        Times.setAlarms();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(mTimes.getName());
    }


    @Override
    public void onChanged(final Times times) {
        mAdapter.clearAllItems();
        mAdapter.add(new AlarmsAdapter.SwitchItem() {
            @Override
            public String getName() {
                return getString(R.string.ongoingNotification);
            }

            @Override
            public void onChange(boolean checked) {
                mTimes.setOngoingNotificationActive(checked);
                InternalBroadcastReceiver.sender(getActivity()).sendTimeTick();
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
                    if (checked && !BatteryOptimizationsKt.isIgnoringBatteryOptimizations(getActivity())) {
                        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                        dialog.setTitle(R.string.batteryOptimizations);
                        dialog.setMessage(getString(R.string.batteryOptimizationsText));
                        dialog.setCancelable(false);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> BatteryOptimizationsKt.checkBatteryOptimizations(getActivity()));
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> {
                        });
                        dialog.show();

                    }
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
                        try {
                            AlarmConfigFragment.create(alarm).show(AlarmsFragment.this);
                        } catch (Exception e) {
                            CrashReporter.recordException(e);
                        }
                    }
                }

                @Override
                public boolean onLongClick() {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(mTimes.getName())
                            .setMessage(getString(R.string.delAlarmConfirm, alarm.getTitle()))
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                mTimes.getUserAlarms().remove(alarm);
                                onChanged(times);
                            }).show();
                    return true;
                }
            });
        }
        mAdapter.add("");

        mAdapter.add(new AlarmsAdapter.Button() {
            @Override
            public String getName() {
                return getString(R.string.addAlarm);
            }

            @Override
            public void onClick() {
                Alarm alarm = new Alarm();
                alarm.setCity(mTimes);
                mTimes.getUserAlarms().add(alarm);
                AlarmConfigFragment.create(alarm).show(AlarmsFragment.this);
            }

            @Override
            public boolean onLongClick() {
                return false;
            }
        });
    }
}
