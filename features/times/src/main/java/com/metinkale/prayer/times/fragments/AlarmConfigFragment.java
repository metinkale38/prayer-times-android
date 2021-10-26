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

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.times.BuildConfig;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.alarm.AlarmService;
import com.metinkale.prayer.times.alarm.sounds.Sound;
import com.metinkale.prayer.times.alarm.sounds.SoundChooser;
import com.metinkale.prayer.times.alarm.sounds.SoundChooserAdapter;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.utils.HorizontalNumberWheel;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.PermissionUtils;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.LocalDateTime;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class AlarmConfigFragment extends DialogFragment {
    private TextView[] mWeekdays = new TextView[7];
    private MaterialIconView[] mTimesViews = new MaterialIconView[6];
    private TextView[] mWeekdaysText = new TextView[7];
    private TextView[] mTimesText = new TextView[6];
    private Switch mVibrate;
    private Switch mRandomSound;
    private Switch mAutoDelete;
    private HorizontalNumberWheel mMinute;
    private TextView mMinuteText;
    private RecyclerView mSounds;
    private Alarm mAlarm;
    private int mColorOn = App.get().getResources().getColor(R.color.colorPrimary);
    private int mColorOff = App.get().getResources().getColor(R.color.foregroundSecondary);
    private Button mAddSound;
    private SeekBar mVolumeBar;
    private Spinner mVolumeSpinner;
    private TextView mVolumeTitle;
    private SoundChooserAdapter mAdapter;
    private Button mDelete;
    private Times mTimes;
    private View mSilenterUp, mSilenterDown;
    private EditText mSilenterValue;

    public static AlarmConfigFragment create(Alarm alarm) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", alarm.getCity().getID());
        bdl.putInt("id", alarm.getId());
        AlarmConfigFragment frag = new AlarmConfigFragment();
        frag.setArguments(bdl);
        return frag;
    }

    public void show(Fragment frag) {
        if (frag.getResources().getBoolean(R.bool.large_layout)) {
            FragmentManager fragmentManager = frag.getChildFragmentManager();
            this.show(fragmentManager, "alarmconfig");
        } else {
            ((BaseActivity) frag.getActivity()).moveToFrag(this);
        }
    }

    @Override
    public void dismiss() {
        if (getResources().getBoolean(R.bool.large_layout)) super.dismiss();
        else getActivity().onBackPressed();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vakit_notprefs_item, container, false);

        mSilenterDown = view.findViewById(R.id.silenterDecr);
        mSilenterUp = view.findViewById(R.id.silenterIncr);
        mSilenterValue = view.findViewById(R.id.silenter);

        mVolumeBar = view.findViewById(R.id.volume);
        mVolumeSpinner = view.findViewById(R.id.volumeSpinner);
        mVolumeTitle = view.findViewById(R.id.volumeText);

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
        mTimesText[0].setText(Vakit.FAJR.getString());

        mMinute = view.findViewById(R.id.timeAdjust);
        mMinuteText = view.findViewById(R.id.timeText);

        mSounds = view.findViewById(R.id.audio);
        mAddSound = view.findViewById(R.id.addSound);

        mDelete = view.findViewById(R.id.delete);


        mVibrate = view.findViewById(R.id.vibrate);
        mRandomSound = view.findViewById(R.id.randomsound);
        mAutoDelete = view.findViewById(R.id.deleteAfterSound);


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bdl = getArguments();
        mTimes = Times.getTimes(bdl.getLong("city"));
        if (mTimes == null) {
            dismiss();
            return;
        }
        mAlarm = mTimes.getAlarm(bdl.getInt("id"));


        initMinuteAdj();
        initWeekdays();
        initTimes();
        initSounds();
        initVolume();
        initVibrate();
        initRandomSound();
        initDelete();
        initAutodelete();
        initSilenter();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSilenter() {
        final Runnable incr = new Runnable() {
            @Override
            public void run() {
                mAlarm.setSilenter(mAlarm.getSilenter() + 1);
                mSilenterValue.setText(LocaleUtils.formatNumber(mAlarm.getSilenter()));
                mSilenterValue.postDelayed(this, 500);
            }
        };

        final Runnable decr = new Runnable() {
            @Override
            public void run() {
                mAlarm.setSilenter(mAlarm.getSilenter() - 1);
                mSilenterValue.setText(LocaleUtils.formatNumber(mAlarm.getSilenter()));
                mSilenterValue.postDelayed(this, 500);
            }
        };

        mSilenterUp.setOnTouchListener((v, event) -> {
            if (!PermissionUtils.get(getActivity()).pNotPolicy) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.post(incr);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.removeCallbacks(incr);
            }
            return true;
        });

        mSilenterDown.setOnTouchListener((v, event) -> {
            if (!PermissionUtils.get(getActivity()).pNotPolicy) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.post(decr);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.removeCallbacks(decr);
            }
            return true;
        });
        View.OnClickListener listener = v -> PermissionUtils.get(getActivity()).needNotificationPolicy(getActivity());
        mSilenterUp.setOnClickListener(listener);
        mSilenterDown.setOnClickListener(listener);
        mSilenterValue.setOnClickListener(listener);

        mSilenterValue.setText(LocaleUtils.formatNumber(mAlarm.getSilenter()));
        mSilenterValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mAlarm.setSilenter(Integer.parseInt(s.toString()));
                } catch (NumberFormatException ignore) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    private void initMinuteAdj() {
        mMinute.setListener(newValue -> {

            mAlarm.setMins(newValue);

            if (newValue == 0) {
                mMinuteText.setText(R.string.onTime);
            } else if (newValue < 0) {
                mMinuteText.setText(getString(R.string.beforeTime, -newValue));
            } else {
                mMinuteText.setText(getString(R.string.afterTime, newValue));
            }
        });


        int value = mAlarm.getMins();
        int max = 180;
        if (Math.abs(value) > max) {
            max = Math.abs(value);
        }
        mMinute.setMax(max);
        mMinute.setMin(-max);
        mMinute.setValue(value);

    }

    private void initAutodelete() {
        mAutoDelete.setChecked(mAlarm.isRemoveNotification());
        mAutoDelete.setOnCheckedChangeListener((buttonView, isChecked) -> mAlarm.setRemoveNotification(isChecked));
    }

    private void initVibrate() {
        mVibrate.setChecked(mAlarm.isVibrate());
        mVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> mAlarm.setVibrate(isChecked));
    }

    private void initRandomSound() {
        mRandomSound.setChecked(mAlarm.isRandomSound());
        mRandomSound.setOnCheckedChangeListener((buttonView, isChecked) -> mAlarm.setRandomSound(isChecked));
    }

    private void initDelete() {
        mDelete.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle(mTimes.getName())
                .setMessage(getString(R.string.delAlarmConfirm, mAlarm.getTitle()))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mTimes.getUserAlarms().remove(mAlarm);
                    dismiss();
                }).show());

        if (BuildConfig.DEBUG)
            mDelete.setOnLongClickListener(v -> {
                getActivity().getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                AlarmService.setAlarm(getActivity(), new Pair<>(mAlarm, LocalDateTime.now().plusSeconds(5)));
                return true;
            });

    }

    private void initTimes() {
        for (int i = 0; i < 6; i++) {
            final Vakit time = Vakit.getByIndex(i);
            mTimesText[i].setText(time.getString());

            setTime(time, isTime(time));

            mTimesViews[i].setOnClickListener(v -> {
                getActivity().getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                setTime(time, !isTime(time));
            });
        }
    }

    private void initWeekdays() {
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
            mWeekdays[i].setText(weekdays[i + 1].replace("ال", "").substring(0, 1));
            mWeekdaysText[i].setText(weekdays[i + 1]);
            final int weekday = i + 1;

            setWeekday(i + 1, isWeekday(weekday));

            mWeekdays[i].setOnClickListener(v -> {
                getActivity().getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                setWeekday(weekday, !isWeekday(weekday));
            });
        }
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

    private void initSounds() {
        final List<Sound> sounds = mAlarm.getSounds();
        mAdapter = new SoundChooserAdapter(mSounds, sounds, mAlarm, false);
        mAdapter.setVolume(mAlarm.getVolume());
        mAdapter.setShowRadioButton(false);
        mSounds.setAdapter(mAdapter);
        mVolumeBar.setVisibility(mAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        mVolumeTitle.setVisibility(mAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        mVolumeSpinner.setVisibility(mAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);

        mAutoDelete.setVisibility(sounds.isEmpty() ? View.GONE : View.VISIBLE);
        mAddSound.setOnClickListener(v -> SoundChooser.create(mAlarm).show(getChildFragmentManager(), "soundchooser"));


        mAdapter.setOnItemClickListener((vh, sound) -> {
            View v = vh.getView();
            if (sounds.contains(sound)) {
                v.setOnLongClickListener(v1 -> {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(sound.getName())
                            .setMessage(getString(R.string.delAlarmConfirm, sound.getName()))
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                sounds.remove(sound);
                                initSounds();
                                initVolume();
                            }).show();
                    return true;
                });
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

        mWeekdays[weekday - 1].setTextColor(enable ? mColorOn : mColorOff);
    }

    private void setTime(final Vakit time, boolean enable) {
        if (enable) {
            mAlarm.getTimes().add(time);
        } else if (mAlarm.getTimes().size() > 1) {
            mAlarm.getTimes().remove(time);
        } else {
            return;
        }

        mTimesViews[time.ordinal()].setColor(enable ? mColorOn : mColorOff);
    }


    public void onSoundsChanged() {
        initSounds();
    }

    @Override
    public void onPause() {
        mAdapter.resetAudios();
        mAlarm.getCity().save();
        super.onPause();
    }


}
