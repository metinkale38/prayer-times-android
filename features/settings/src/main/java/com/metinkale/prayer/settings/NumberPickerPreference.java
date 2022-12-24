/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;




//from: https://github.com/google/walt/blob/master/android/WALT/app/src/main/java/org/chromium/latency/walt/NumberPickerPreference.java
public class NumberPickerPreference extends DialogPreference {
    private int currentValue;
    private final int maxValue;
    private final int minValue;

    private static final int DEFAULT_value = 0;
    private static final int DEFAULT_maxValue = 0;
    private static final int DEFAULT_minValue = 0;

    private String defaultSummary;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (getSummary() != null)
            defaultSummary = getSummary().toString();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);

        try {
            maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_maxValue);
            minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_minValue);
        } finally {
            a.recycle();
        }

        setDialogLayoutResource(R.layout.numberpicker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

    }

    public int getValue() {
        return currentValue;
    }

    public void setValue(int value) {
        currentValue = value;
        persistInt(currentValue);
        if (defaultSummary == null)
            setSummary(getValue()+"");
        else
            setSummary(String.format(defaultSummary, getValue()));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_value);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(currentValue) : (Integer) defaultValue);
    }

    public static class NumberPickerPreferenceDialogFragmentCompat
            extends PreferenceDialogFragmentCompat {
        private static final String SAVE_STATE_VALUE = "NumberPickerPreferenceDialogFragment.value";
        private NumberPicker picker;
        private int currentValue = 1;

        public NumberPickerPreferenceDialogFragmentCompat() {
        }

        public static NumberPickerPreferenceDialogFragmentCompat newInstance(String key) {
            NumberPickerPreferenceDialogFragmentCompat fragment =
                    new NumberPickerPreferenceDialogFragmentCompat();
            Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                currentValue = getNumberPickerPreference().getValue();
            } else {
                currentValue = savedInstanceState.getInt(SAVE_STATE_VALUE);
            }
        }

        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putInt(SAVE_STATE_VALUE, currentValue);
        }

        private NumberPickerPreference getNumberPickerPreference() {
            return (NumberPickerPreference) this.getPreference();
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            picker = view.findViewById(R.id.numpicker_pref);
            picker.setMaxValue(getNumberPickerPreference().maxValue);
            picker.setMinValue(getNumberPickerPreference().minValue);
            picker.setValue(currentValue);
            picker.setWrapSelectorWheel(true);
        }

        @Override
        public void onDialogClosed(boolean b) {
            if (b) {
                int value = picker.getValue();
                if (getPreference().callChangeListener(value)) {
                    getNumberPickerPreference().setValue(value);
                }
            }
        }
    }
}