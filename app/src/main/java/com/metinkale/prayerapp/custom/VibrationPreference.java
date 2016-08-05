/*
 * Copyright (c) 2016 Metin Kale
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

package com.metinkale.prayerapp.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.metinkale.prayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by metin on 24.03.2016.
 */
public class VibrationPreference extends EditTextPreference {
    //Layout Fields
    private final RelativeLayout layout = new RelativeLayout(getContext());
    private final EditText editText = new EditText(getContext());
    private final Button button = new Button(getContext());

    public static long[] getPattern(Context c, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        List<Long> mills = new ArrayList<>();
        String txt = prefs.getString(key, "0 300 150 300 150 500");
        String[] split = txt.split(" ");
        for (String s : split) {
            if (!s.isEmpty()) {
                try {
                    mills.add(Long.parseLong(s));
                } catch (Exception ignore) {
                }
            }
        }
        long[] pattern = new long[mills.size()];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = mills.get(i);
        }
        return pattern;

    }

    //Called when addPreferencesFromResource() is called. Initializes basic paramaters
    public VibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(true);
        button.setText(R.string.test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Long> mills = new ArrayList<>();
                String txt = editText.getText().toString();
                String[] split = txt.split(" ");
                for (String s : split) {
                    if (!s.isEmpty()) {
                        try {
                            mills.add(Long.parseLong(s));
                        } catch (Exception ignore) {
                        }
                    }
                }

                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = new long[mills.size()];
                for (int i = 0; i < pattern.length; i++) {
                    pattern[i] = mills.get(i);
                }
                vib.vibrate(pattern, -1);
            }
        });
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    //Create the Dialog view
    @Override
    protected View onCreateDialogView() {
        button.setId(R.id.button5);
        layout.addView(editText);
        layout.addView(button);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) editText.getLayoutParams();
        params2.addRule(RelativeLayout.LEFT_OF, button.getId());
        return layout;
    }

    //Attach persisted values to Dialog
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editText.setText(getPersistedString("EditText"), TextView.BufferType.NORMAL);
    }

    //persist values and disassemble views
    @Override
    protected void onDialogClosed(boolean positiveresult) {
        super.onDialogClosed(positiveresult);
        if (positiveresult && shouldPersist()) {
            String value = editText.getText().toString();
            if (callChangeListener(value)) {
                persistString(value);
            }
        }

        ((ViewManager) editText.getParent()).removeView(editText);
        ((ViewManager) button.getParent()).removeView(button);
        ((ViewManager) layout.getParent()).removeView(layout);

        notifyChanged();
    }

    public void setValue(CharSequence value) {
        editText.setText(value);
    }
}