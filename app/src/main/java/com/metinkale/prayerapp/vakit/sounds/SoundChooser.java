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

package com.metinkale.prayerapp.vakit.sounds;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.fragments.AlarmConfigFragment;
import com.metinkale.prayerapp.vakit.alarm.Alarm;
import com.metinkale.prayerapp.vakit.times.Times;

public class SoundChooser extends DialogFragment {

    private SoundChooserAdapter mAdapter;
    private RecyclerView mRecyclerView;


    public static SoundChooser create(Alarm alarm) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", alarm.getCity().getID());
        bdl.putInt("id", alarm.getId());
        SoundChooser frag = new SoundChooser();
        frag.setArguments(bdl);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bdl = getArguments();
        final Alarm alarm = Times.getTimes(bdl.getLong("city")).getAlarm(bdl.getInt("id"));


        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.soundPicker)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Sound selected = mAdapter.getSelected();
                                if (selected != null) {
                                    alarm.getSounds().add(selected);
                                }
                                ((AlarmConfigFragment) getParentFragment()).onSoundsChanged();
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        b.setView(createView(getActivity().getLayoutInflater(), null, null));
        return b.create();
    }

    @Nullable
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sound_chooser, container, false);
        mRecyclerView = view.findViewById(R.id.list);
        if (Sounds.getSounds().isEmpty()) {
            Sounds.downloadData(getActivity(), new Runnable() {
                @Override
                public void run() {
                    init();
                }
            });
        }

        init();
        return view;
    }

    private void init() {
        mAdapter = new SoundChooserAdapter(mRecyclerView, Sounds.getRootSounds());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.resetAudios();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

}