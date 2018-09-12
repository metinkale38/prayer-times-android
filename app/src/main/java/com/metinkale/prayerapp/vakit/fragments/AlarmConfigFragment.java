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
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.sounds.Sound;
import com.metinkale.prayerapp.vakit.sounds.SoundChooser;
import com.metinkale.prayerapp.vakit.sounds.SoundChooserAdapter;
import com.metinkale.prayerapp.vakit.alarm.Alarm;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AlarmConfigFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    private TextView[] mWeekdays = new TextView[7];
    private MaterialIconView[] mTimesViews = new MaterialIconView[6];
    private TextView[] mWeekdaysText = new TextView[7];
    private TextView[] mTimesText = new TextView[6];
    private Switch mVibrate;
    private Switch mAutoDelete;
    private SeekBar mMinute;
    private TextView mMinuteText;
    private RecyclerView mSounds;
    private boolean mMinuteExact = false;
    private Alarm mAlarm;
    private int mDefaultColor = App.get().getResources().getColor(R.color.theme_default_primary);
    private Button mAddSound;
    private SeekBar mVolumeBar;
    private Spinner mVolumeSpinner;
    private SoundChooserAdapter mAdapter;
    private Button mDelete;
    private Times mTimes;

    public static AlarmConfigFragment create(Alarm alarm) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", alarm.getCity().getID());
        bdl.putInt("id", alarm.getId());
        AlarmConfigFragment frag = new AlarmConfigFragment();
        frag.setArguments(bdl);
        return frag;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vakit_notprefs_item, container, false);

        mVolumeBar = view.findViewById(R.id.volume);
        mVolumeSpinner = view.findViewById(R.id.volumeSpinner);

        mWeekdays[0] = view.findViewById(R.id.sunday);
        mWeekdays[1] = view.findViewById(R.id.monday);
        mWeekdays[2] = view.findViewById(R.id.tuesday);
        mWeekdays[3] = view.findViewById(R.id.wednesday);
        mWeekdays[4] = view.findViewById(R.id.thursday);
        mWeekdays[5] = view.findViewById(R.id.friday);
        mWeekdays[6] = view.findViewById(R.id.saturday);

        mWeekdaysText[0] = view.findViewById(R.id.sundayText);
        mWeekdaysText[1] = view.findViewById(R.id.mondayText);
        mWeekdaysText[2] = view.findViewById(R.id.tuesdayText);
        mWeekdaysText[3] = view.findViewById(R.id.wednesdayText);
        mWeekdaysText[4] = view.findViewById(R.id.thursdayText);
        mWeekdaysText[5] = view.findViewById(R.id.fridayText);
        mWeekdaysText[6] = view.findViewById(R.id.saturdayText);

        mTimesViews[0] = view.findViewById(R.id.fajr);
        mTimesViews[1] = view.findViewById(R.id.sunrise);
        mTimesViews[2] = view.findViewById(R.id.zuhr);
        mTimesViews[3] = view.findViewById(R.id.asr);
        mTimesViews[4] = view.findViewById(R.id.magrib);
        mTimesViews[5] = view.findViewById(R.id.isha);


        mTimesText[0] = view.findViewById(R.id.fajrText);
        mTimesText[1] = view.findViewById(R.id.sunText);
        mTimesText[2] = view.findViewById(R.id.zuhrText);
        mTimesText[3] = view.findViewById(R.id.asrText);
        mTimesText[4] = view.findViewById(R.id.magribText);
        mTimesText[5] = view.findViewById(R.id.ishaaText);

        mMinute = view.findViewById(R.id.timeAdjust);
        mMinuteText = view.findViewById(R.id.timeText);

        mSounds = view.findViewById(R.id.audio);
        mAddSound = view.findViewById(R.id.addSound);

        mDelete = view.findViewById(R.id.delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(mTimes.getName())
                        .setMessage(getString(R.string.delConfirm, mAlarm.getTitle()))
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTimes.getUserAlarms().remove(mAlarm);
                                dismiss();
                            }
                        }).show();
            }
        });

        return view;
    }

    private void initVolume() {
        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mVolumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(getString(R.string.customVolume), getString(R.string.volumeRingtone), getString(R.string.volumeNotification),
                        getString(R.string.volumeAlarm), getString(R.string.volumeMedia)));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mVolumeSpinner.setAdapter(dataAdapter);
        mVolumeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVolumeBar.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                switch (position) {
                    case 0:
                        mAlarm.setVolume(mVolumeBar.getProgress());
                        break;
                    case 1:
                        mAlarm.setVolume(Alarm.VOLUME_MODE_RINGTONE);
                        break;
                    case 2:
                        mAlarm.setVolume(Alarm.VOLUME_MODE_NOTIFICATION);
                        break;
                    case 3:
                        mAlarm.setVolume(Alarm.VOLUME_MODE_ALARM);
                        break;
                    case 4:
                        mAlarm.setVolume(Alarm.VOLUME_MODE_MEDIA);
                        break;
                }
                mAdapter.setVolume(mAlarm.getVolume());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (mAlarm.getVolume() >= 0) {
            mVolumeBar.setProgress(mAlarm.getVolume());
            mVolumeBar.setVisibility(View.VISIBLE);
            mVolumeSpinner.setSelection(0);
        } else {
            mVolumeBar.setProgress(mVolumeBar.getMax() / 2);
            mVolumeBar.setVisibility(View.GONE);
            mVolumeSpinner.setSelection(-mAlarm.getVolume());
        }


        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlarm.setVolume(progress);
                mAdapter.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bdl = getArguments();
        mTimes = Times.getTimes(bdl.getLong("city"));
        if (mTimes == null) {
            dismiss();
            return;
        }
        mAlarm = mTimes.getAlarm(bdl.getInt("id"));


        mVibrate = view.findViewById(R.id.vibrate);
        mAutoDelete = view.findViewById(R.id.deleteAfterSound);

        String[] weekdays = new DateFormatSymbols().getWeekdays();

        //rotate arrays to match first weekday
        int firstWeekday = Calendar.getInstance().getFirstDayOfWeek();
        if (firstWeekday != Calendar.SUNDAY) {//java default is sunday, nothing to do
            TextView[] wds = mWeekdays.clone();
            TextView[] wdTs = mWeekdaysText.clone();
            for (int i = 0; i < 7; i++) {
                mWeekdays[i] = wds[(8 - firstWeekday + i) % 7];
                mWeekdaysText[i] = wdTs[(8 - firstWeekday + i) % 7];
            }
        }

        for (int i = 0; i < 7; i++) {
            mWeekdays[i].setText(weekdays[i + 1].substring(0, 1));
            mWeekdaysText[i].setText(weekdays[i + 1]);
            final int weekday = i + 1;

            setWeekday(i + 1, isWeekday(weekday));

            mWeekdays[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setWeekday(weekday, !isWeekday(weekday));
                }
            });
        }

        for (int i = 0; i < 6; i++) {
            final Vakit time = Vakit.getByIndex(i);
            mTimesText[i].setText(time.getString());

            setTime(time, isTime(time));

            mTimesViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTime(time, !isTime(time));
                }
            });
        }


        mVibrate.setChecked(mAlarm.isVibrate());
        mAutoDelete.setChecked(mAlarm.isRemoveNotification());

        mVibrate.setOnCheckedChangeListener(this);
        mAutoDelete.setOnCheckedChangeListener(this);


        mMinute.setOnSeekBarChangeListener(this);
        int progress = mAlarm.getMins();
        mMinuteExact = Math.abs(mAlarm.getMins()) % 5 != 0;
        if (!mMinuteExact) {
            progress /= 5;
        }

        if (Math.abs(progress) > mMinute.getMax() / 2) {
            mMinute.setMax(Math.abs(progress) * 2);
        }

        progress += mMinute.getMax() / 2;
        mMinute.setProgress(progress);


        mAddSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundChooser.create(mAlarm).show(getChildFragmentManager(), "soundchooser");
            }
        });

        initVolume();
        initSounds();
    }

    private void initSounds() {
        final List<Sound> sounds = mAlarm.getSounds();
        mAdapter = new SoundChooserAdapter(mSounds, sounds);
        mAdapter.setVolume(mAlarm.getVolume());
        mAdapter.setShowRadioButton(false);
        mSounds.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SoundChooserAdapter.OnClickListener() {
            @Override
            public void onItemClick(SoundChooserAdapter.ItemVH vh, final Sound sound) {
                View v = vh.getView();
                if (sounds.contains(sound)) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(sound.getName())
                                    .setMessage(getString(R.string.delConfirm, sound.getName()))
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            sounds.remove(sound);
                                            mAdapter.update();
                                        }
                                    }).show();
                            return true;
                        }
                    });
                }
            }
        });

    }

    private boolean isWeekday(int weekday) {
        return mAlarm.getWeekdays().contains(weekday);
    }

    private boolean isTime(Vakit time) {
        return mAlarm.getTimes().contains(time);
    }

    private void setWeekday(final int weekday, boolean enable) {
        if (enable) {
            mAlarm.getWeekdays().add(weekday);
        } else if (mAlarm.getWeekdays().size() > 1) {
            mAlarm.getWeekdays().remove(weekday);
        } else {
            return;
        }

        mWeekdays[weekday - 1].setTextColor(enable ? mDefaultColor : Color.GRAY);
    }

    private void setTime(final Vakit time, boolean enable) {
        if (enable) {
            mAlarm.getTimes().add(time);
        } else if (mAlarm.getTimes().size() > 1) {
            mAlarm.getTimes().remove(time);
        } else {
            return;
        }

        mTimesViews[time.index].setColor(enable ? mDefaultColor : Color.GRAY);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        progress -= seekBar.getMax() / 2;
        if (!mMinuteExact) {
            progress *= 5;
        }
        mAlarm.setMins(progress);

        if (progress == 0) {
            mMinuteText.setText(R.string.onTime);
        } else if (progress < 0) {
            mMinuteText.setText(getString(R.string.beforeTime, -progress));
        } else {
            mMinuteText.setText(getString(R.string.afterTime, progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //not needed
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //not needed
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mVibrate) {
            mAlarm.setVibrate(isChecked);
        } else if (buttonView == mAutoDelete) {
            mAlarm.setRemoveNotification(isChecked);
        }
    }


    public void onSoundsChanged() {
        initSounds();
    }

    @Override
    public void onPause() {
        mAlarm.getCity().save();
        super.onPause();
    }
}
