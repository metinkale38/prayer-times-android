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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.Times;

/**
 * Created by Metin on 21.07.2015.
 */
public class SettingsFragment extends Fragment {

    private EditText mName;
    @NonNull
    private EditText[] mMins = new EditText[6];
    @NonNull
    private ImageView[] mMinus = new ImageView[6];
    @NonNull
    private ImageView[] mPlus = new ImageView[6];
    @Nullable
    private Times mTimes;
    private int[] mMinAdj;
    private EditText mTimeZone;

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.vakit_settings, container, false);
        mName = v.findViewById(R.id.name);
        ViewGroup tz = v.findViewById(R.id.tz);
        mTimeZone = tz.findViewById(R.id.timezonefix);
        tz.findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    float h = Float.parseFloat(mTimeZone.getText().toString());
                    h += 0.5;
                    mTimeZone.setText(h + "");
                } catch (Exception ignore) {
                }
            }
        });

        tz.findViewById(R.id.minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    double h = Double.parseDouble(mTimeZone.getText().toString());
                    h -= 0.5;
                    mTimeZone.setText(h + "");
                } catch (Exception ignore) {
                }
            }
        });

        mTimeZone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                if (mTimes == null) {
                    return;
                }
                try {
                    mTimes.setTZFix(Double.parseDouble(editable.toString()));
                } catch (Exception ignore) {
                }

            }
        });
        ViewGroup vg = v.findViewById(R.id.minAdj);
        for (int i = 1; i < 7; i++) {
            final int ii = i - 1;
            ViewGroup time = (ViewGroup) vg.getChildAt(i);
            mMins[ii] = time.findViewById(R.id.nr);
            mPlus[ii] = time.findViewById(R.id.plus);
            mMinus[ii] = time.findViewById(R.id.minus);
            mPlus[ii].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        int min = Integer.parseInt(mMins[ii].getText().toString());
                        min++;
                        mMins[ii].setText(min + "");
                    } catch (Exception ignore) {
                    }
                }
            });

            mMinus[ii].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        int min = Integer.parseInt(mMins[ii].getText().toString());
                        min--;
                        mMins[ii].setText(min + "");
                    } catch (Exception ignore) {
                    }

                }
            });

            mMins[ii].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(@NonNull Editable editable) {
                    if (mTimes == null) {
                        return;
                    }
                    try {
                        mMinAdj[ii] = Integer.parseInt(editable.toString());
                    } catch (Exception ignore) {
                    }
                    mTimes.setMinuteAdj(mMinAdj);
                }
            });
        }


        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(@NonNull Editable editable) {
                if (mTimes != null) {
                    mTimes.setName(editable.toString());
                }
            }
        });

        setTimes(mTimes);
        return v;
    }


    public void setTimes(@Nullable Times t) {
        if ((mName != null) && (t != null)) {
            mTimes = null;
            mMinAdj = t.getMinuteAdj();
            for (int i = 0; i < mMins.length; i++) {
                mMins[i].setText(mMinAdj[i] + "");
            }
            mName.setText(t.getName());
            mTimeZone.setText(t.getTZFix() + "");
        }
        mTimes = t;
    }
}
