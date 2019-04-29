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

package com.metinkale.prayer.settings;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;

/**
 * Created by metin on 24.03.2016.
 */
public class VibrationPreference extends EditTextPreference {
    private EditText editText;



    //Called when addPreferencesFromResource() is called. Initializes basic paramaters
    public VibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(true);

    }


    //persist values and disassemble views
    protected void onDialogClosed(boolean positiveresult) {
        if (positiveresult && shouldPersist()) {
            String value = editText.getText().toString();
            if (callChangeListener(value)) {
                persistString(value);
            }
        }


        notifyChanged();
    }

    @Override
    protected void onClick() {
        RelativeLayout layout = new RelativeLayout(getContext());
        editText = new EditText(getContext());
        Button button = new Button(getContext());
        button.setText(R.string.test);
        button.setOnClickListener(view -> {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vib.vibrate(pattern, -1);
            }
        });
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        layout.addView(editText);
        layout.addView(button);
        editText.setText(getPersistedString("0 300 150 300 150 500"), TextView.BufferType.NORMAL);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) editText.getLayoutParams();
        params2.addRule(RelativeLayout.LEFT_OF, button.getId());

        AlertDialog.Builder dlg = new AlertDialog.Builder(getContext());
        dlg.setTitle(getDialogTitle());
        dlg.setView(layout);
        dlg.setPositiveButton(R.string.ok, (dialog, which) -> {
            onDialogClosed(true);
            dialog.dismiss();
        });
        dlg.setNegativeButton(R.string.cancel, (dialog, which) -> {
            onDialogClosed(false);
            dialog.dismiss();
        });
        dlg.show();
    }

    public void setValue(CharSequence value) {
        editText.setText(value);
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState myState = new SavedState(superState);
        if (editText != null)
            myState.text = editText.getText().toString();
        return myState;
    }


    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (editText != null) {
            editText.setText(myState.text);
        }
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(@NonNull Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @NonNull
                    public SavedState createFromParcel(@NonNull Parcel in) {
                        return new SavedState(in);
                    }

                    @NonNull
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}