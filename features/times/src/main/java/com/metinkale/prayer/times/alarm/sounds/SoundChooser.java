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

package com.metinkale.prayer.times.alarm.sounds;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.github.florent37.inlineactivityresult.InlineActivityResult;
import com.github.florent37.inlineactivityresult.Result;
import com.github.florent37.inlineactivityresult.callbacks.ActivityResultListener;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.fragments.AlarmConfigFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.utils.FileChooser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

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
                        (dialog, whichButton) -> {
                            Sound selected = mAdapter.getSelected();
                            if (selected != null) {
                                alarm.getSounds().add(selected);
                            }
                            ((AlarmConfigFragment) getParentFragment()).onSoundsChanged();
                            dialog.dismiss();
                        }
                )
                .setNegativeButton(R.string.cancel,
                        (dialog, whichButton) -> dialog.dismiss()
                ).setNeutralButton(R.string.other, (dialog, which) -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.other)
                                    .setItems(R.array.soundPicker, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which) {
                                            if (which == 0) {
                                                FileChooser chooser = new FileChooser(getActivity());
                                                chooser.showDialog();
                                                chooser.setFileListener(new FileChooser.FileSelectedListener() {
                                                    @Override
                                                    public void fileSelected(File file) {
                                                        Sound selected = UserSound.create(Uri.fromFile(file));
                                                        if (selected != null) {
                                                            alarm.getSounds().add(selected);
                                                        }
                                                        ((AlarmConfigFragment) getParentFragment()).onSoundsChanged();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            } else {
                                                Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                                i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                                                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
                                                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                                                Intent chooserIntent = Intent.createChooser(i, getActivity().getString(R.string.sound));
                                                startActivityForResult(chooserIntent, 0);
                                            }
                                        }
                                    });
                            builder.show();
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
            Sounds.downloadData(getActivity(), this::init);
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