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

package com.metinkale.prayer.times.fragments.calctime;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.PrayTimes;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * Created by metin on 10.10.2017.
 */

public class CustomTimeAdjustDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener {


    private PrayTimes mPrayTimes = new PrayTimes();
    private CalcTimes mCalcTime;
    private View mView;
    private Vakit mTime;
    private View mAngleDown;
    private EditText mAngleValue;
    private View mAngleTitle;
    private View mAngleUp;
    private View mMinsDown;
    private View mMinsUp;
    private View mAngleSuffix;
    private View mAsrText;
    private RadioButton mAsrAwwal;
    private RadioButton mAsrSani;
    private RadioButton mAsrBoth;
    private EditText mMinsValue;
    private double mAngle;
    private int mMins;


    public static CustomTimeAdjustDialogFragment create(CalcTimes calc, Vakit v, double angle, int mins) {
        Bundle bdl = new Bundle();
        bdl.putLong("cityId", calc.getID());
        bdl.putInt("time", v.ordinal());
        bdl.putDouble("angle", angle);
        bdl.putInt("mins", mins);
        CustomTimeAdjustDialogFragment frag = new CustomTimeAdjustDialogFragment();
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Bundle bdl = getArguments();
        mAngle = bdl.getDouble("angle");
        mMins = bdl.getInt("mins");
        mCalcTime.setAsrType(CalcTimes.AsrType.valueOf(bdl.getString("asrType", CalcTimes.AsrType.Shafi.name())));
        updateTime();

        Fragment frag = getParentFragment();
        if (frag instanceof CalcTimeConfDialogFragment)
            ((CalcTimeConfDialogFragment) frag).updateTimes();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bdl = getArguments();


        if (bdl != null && bdl.containsKey("cityId")) {
            mCalcTime = (CalcTimes) CalcTimes.getTimes(bdl.getLong("cityId"));
            mPrayTimes = mCalcTime.getPrayTimes();
            mTime = Vakit.values()[bdl.getInt("time", 0)];
            mAngle = bdl.getDouble("angle");
            mMins = bdl.getInt("mins");
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTime.getString())
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                bdl.putDouble("angle", mAngle);
                                bdl.putInt("mins", mMins);
                                bdl.putSerializable("asrType", mCalcTime.getAsrType().name());
                                dismiss();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView != null) return mView;
        mView = inflater.inflate(R.layout.calcmethod_custom, container, false);


        mAngleDown = mView.findViewById(R.id.angleDecr);
        mAngleUp = mView.findViewById(R.id.angleIncr);
        mAngleValue = mView.findViewById(R.id.angle);
        mAngleTitle = mView.findViewById(R.id.angleText);
        mAngleSuffix = mView.findViewById(R.id.angleSuffix);

        mMinsDown = mView.findViewById(R.id.minsDecr);
        mMinsUp = mView.findViewById(R.id.minsIncr);
        mMinsValue = mView.findViewById(R.id.mins);


        mAsrText = mView.findViewById(R.id.asrText);
        mAsrAwwal = mView.findViewById(R.id.asrAwwal);
        mAsrSani = mView.findViewById(R.id.asrSani);
        mAsrBoth = mView.findViewById(R.id.asrBoth);

        mAsrAwwal.setOnCheckedChangeListener(this);
        mAsrSani.setOnCheckedChangeListener(this);
        mAsrBoth.setOnCheckedChangeListener(this);

        initAngle();
        initMins();
        initAsr();

        ((TextView) mView.findViewById(R.id.timeTitle)).setText(mTime.getString());
        updateTime();
        return mView;
    }

    private void initAsr() {
        if (mTime == Vakit.ASR) {
            CalcTimes.AsrType type = mCalcTime.getAsrType();
            mAsrBoth.setChecked(CalcTimes.AsrType.Both == type);
            mAsrAwwal.setChecked(CalcTimes.AsrType.Shafi == type);
            mAsrSani.setChecked(CalcTimes.AsrType.Hanafi == type);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AlertDialog) getDialog()).setView(mView);
    }

    private String toStr(double d) {
        if (d == (long) d)
            return String.format(Locale.getDefault(), "%d", (long) d);
        else
            return String.format(Locale.getDefault(), "%.1f", Math.round(d * 10) / 10.0);
    }


    private void initAngle() {
        final Runnable incr = new Runnable() {
            @Override
            public void run() {
                mAngle += 0.1;
                mAngleValue.setText(toStr(mAngle));
                mAngleValue.postDelayed(this, 500);
                updateTime();
            }
        };

        final Runnable decr = new Runnable() {
            @Override
            public void run() {
                mAngle -= 0.1;
                mAngleValue.setText(toStr(mAngle));
                mAngleValue.postDelayed(this, 500);
                updateTime();
            }
        };

        mAngleUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.post(incr);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(incr);
                }
                return true;
            }
        });

        mAngleDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.post(decr);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(decr);
                }
                return true;
            }
        });

        mAngleValue.setText(toStr(mAngle));
        mAngleValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mAngle = Integer.parseInt(s.toString());
                    updateTime();
                } catch (NumberFormatException ignore) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void initMins() {
        final Runnable incr = new Runnable() {
            @Override
            public void run() {
                mMins += 1;
                mMinsValue.setText(LocaleUtils.formatNumber(mMins));
                mMinsValue.postDelayed(this, 500);
                updateTime();
            }
        };

        final Runnable decr = new Runnable() {
            @Override
            public void run() {
                mMins -= 1;
                mMinsValue.setText(LocaleUtils.formatNumber(mMins + ""));
                mMinsValue.postDelayed(this, 500);
                updateTime();
            }
        };

        mMinsUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.post(incr);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(incr);
                }
                return true;
            }
        });

        mMinsDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.post(decr);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(decr);
                }
                return true;
            }
        });

        mMinsValue.setText(LocaleUtils.formatNumber(mMins));
        mMinsValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mMins = Integer.parseInt(s.toString());
                    updateTime();
                } catch (NumberFormatException ignore) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void updateTime() {
        LocalDate today = LocalDate.now();

        if (mTime != Vakit.ASR) {
            mAsrText.setVisibility(View.GONE);
            mAsrAwwal.setVisibility(View.GONE);
            mAsrSani.setVisibility(View.GONE);
            mAsrBoth.setVisibility(View.GONE);
        }

        String time;
        switch (mTime) {

            case FAJR:
                mPrayTimes.tuneFajr(mAngle, mMins);
                mPrayTimes.tuneImsak(mAngle, mMins);
                break;
            case SUN:
                if (!"0".equals(mAngleValue.getText().toString()))
                    mAngleValue.setText("0");
                mAngleValue.setVisibility(View.GONE);
                mAngleTitle.setVisibility(View.GONE);
                mAngleDown.setVisibility(View.GONE);
                mAngleUp.setVisibility(View.GONE);
                mAngleSuffix.setVisibility(View.GONE);
                mAngle = 0;
                mPrayTimes.tuneSunrise(mMins);
                break;
            case DHUHR:
                if (!"0".equals(mAngleValue.getText().toString()))
                    mAngleValue.setText("0");
                mAngleValue.setVisibility(View.GONE);
                mAngleTitle.setVisibility(View.GONE);
                mAngleDown.setVisibility(View.GONE);
                mAngleUp.setVisibility(View.GONE);
                mAngleSuffix.setVisibility(View.GONE);
                mAngle = 0;
                mPrayTimes.tuneDhuhr(mMins);
                break;
            case ASR:
                if (!"0".equals(mAngleValue.getText().toString()))
                    mAngleValue.setText("0");
                mAngleValue.setVisibility(View.GONE);
                mAngleTitle.setVisibility(View.GONE);
                mAngleDown.setVisibility(View.GONE);
                mAngleUp.setVisibility(View.GONE);
                mAngleSuffix.setVisibility(View.GONE);
                mAngle = 0;
                mPrayTimes.tuneAsrHanafi(mMins);
                mPrayTimes.tuneAsrShafi(mMins);

                if (mAsrSani.isChecked()) {
                    mCalcTime.setAsrType(CalcTimes.AsrType.Hanafi);
                } else if (mAsrBoth.isChecked()) {
                    mCalcTime.setAsrType(CalcTimes.AsrType.Both);
                } else {
                    mCalcTime.setAsrType(CalcTimes.AsrType.Shafi);
                }
                break;
            case MAGHRIB:
                mPrayTimes.tuneMaghrib(mAngle, mMins);
                break;
            case ISHAA:
                mPrayTimes.tuneIshaa(mAngle, mMins);
                break;
        }


        if (mTime == Vakit.ASR && mCalcTime.getAsrType() == CalcTimes.AsrType.Both) {
            ((TextView) mView.findViewById(R.id.time)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, mTime.ordinal()).toLocalTime()) + " / " + LocaleUtils.formatTime(mCalcTime.getAsrThaniTime(today).toLocalTime()));
        } else {
            ((TextView) mView.findViewById(R.id.time)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, mTime.ordinal()).toLocalTime()));
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mAsrBoth.setChecked(mAsrBoth == buttonView);
            mAsrAwwal.setChecked(mAsrAwwal == buttonView);
            mAsrSani.setChecked(mAsrSani == buttonView);
            updateTime();
        }
    }
}
