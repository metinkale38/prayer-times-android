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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;

import java.util.List;
import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.settings);
        if (getArguments() != null && getArguments().getBoolean("showKerahatDuration"))
            setPreferenceScreen((PreferenceScreen) findPreference("kerahatDuration"));
        else {
       /*     findPreference("language").setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.TRANSLATE)
                    .setColor(getResources().getColor(R.color.foregroundSecondary)).build());
            findPreference("arabicNames").setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CLOCK_OUT)
                    .setColor(getResources().getColor(R.color.foregroundSecondary)).build());
            findPreference("digits").setIcon(MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.NUMERIC)
                    .setColor(getResources().getColor(R.color.foregroundSecondary)).build());
            findPreference("calendarIntegration").setIcon(
                    MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CALENDAR)
                            .setColor(getResources().getColor(R.color.foregroundSecondary)).build());
            findPreference("kerahatDuration").setIcon(
                    MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.CLOCK_ALERT)
                            .setColor(getResources().getColor(R.color.foregroundSecondary)).build());
         */

            findPreference("numbers").setOnPreferenceChangeListener(this);
            findPreference("backupRestore").setOnPreferenceClickListener(this);
            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);
            findPreference("ongoingIcon").setOnPreferenceClickListener(this);
            findPreference("ongoingNumber").setOnPreferenceClickListener(this);
            findPreference("kerahatDuration").setOnPreferenceClickListener(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                findPreference("ongoingNumber").setEnabled(false);
            }

            if (Build.VERSION.SDK_INT < 24)
                findPreference("showLegacyWidgets").setEnabled(false);


            findPreference("arabicNames").setEnabled(!new Locale("ar").getLanguage().equals(LocaleUtils.getLocale().getLanguage()));

            ListPreference lang = (ListPreference) findPreference("language");
            lang.setOnPreferenceChangeListener(this);

            List<LocaleUtils.Translation> languages = LocaleUtils.getSupportedLanguages(getActivity());
            CharSequence[] entries = new CharSequence[languages.size()];
            CharSequence[] values = new CharSequence[languages.size()];
            for (int i = 0; i < languages.size(); i++) {
                LocaleUtils.Translation trans = languages.get(i);
                entries[i] = trans.getDisplayText();
                values[i] = trans.getLanguage();
            }
            lang.setEntries(entries);
            lang.setEntryValues(values);


            int[][] colors = {{0, 0},
                    {0xFFFFFFFF, 0xFF333333},
                    {0xFFDDDDDD, 0xFF222222},
                    {0xFFBBBBBB, 0xFF111111},
                    {0xFF999999, 0xFF000000},
                    {0xFF666666, 0xFFFFFFFF},
                    {0xFF444444, 0xFFEEEEEE},
                    {0xFF222222, 0xFFDDDDDD},
                    {0xFF000000, 0xFFCCCCCC}
            };

            Preference ongoingColor = findPreference("ongoingColor");

            ongoingColor.setOnPreferenceClickListener(e -> {
                AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                dlg.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.select_dialog_singlechoice) {
                    @Override
                    public int getCount() {
                        return colors.length;
                    }

                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        CheckedTextView v = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.select_dialog_singlechoice, parent, false);
                        int p = (int) Utils.convertDpToPixel(getContext(), 4);
                        v.setPadding(4 * p, p, 4 * p, p);
                        v.setText(position == 0 ? "System" : "Abc");
                        v.setBackgroundColor(position == 0 ? Color.WHITE : colors[position][0]);
                        v.setTextColor(position == 0 ? Color.BLACK : colors[position][1]);
                        v.setChecked(colors[position][0] == Preferences.ONGOING_BG_COLOR.get() && colors[position][1] == Preferences.ONGOING_TEXT_COLOR.get());
                        return v;
                    }
                }, (dialog, which) -> {
                    Preferences.ONGOING_BG_COLOR.set(colors[which][0]);
                    Preferences.ONGOING_TEXT_COLOR.set(colors[which][1]);
                });
                dlg.show();
                return true;

            });
        }

    }


    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        switch (preference.getKey()) {
            case "backupRestore":
                startActivity(new Intent(getActivity(), BackupRestoreActivity.class));
                return true;
            case "kerahatDuration":
                SettingsFragment frag = new SettingsFragment();
                Bundle bdl = new Bundle();
                bdl.putBoolean("showKerahatDuration", true);
                frag.setArguments(bdl);
                ((BaseActivity) getActivity()).moveToFrag(frag);
                return true;
        }
        return false;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof NumberPickerPreference) {
            boolean handled = false;
            if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
                handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
            }
            if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
                handled = ((OnPreferenceDisplayDialogCallback) getActivity()).onPreferenceDisplayDialog(this, preference);
            }

            if (handled) {
                return;
            }

            if (getFragmentManager().findFragmentByTag("numberpicker") != null) {
                return;
            }

            DialogFragment f = NumberPickerPreference.NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), "numberpicker");
        } else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference pref, Object newValue) {

        if ("language".equals(pref.getKey()) || "digits".equals(pref.getKey())) {
            if ("language".equals(pref.getKey()))
                Preferences.LANGUAGE.set((String) newValue);
            Activity act = getActivity();
            act.finish();
            Intent i = new Intent(act, act.getClass());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            act.startActivity(i);

            Answers.getInstance().logCustom(new CustomEvent("Language").putCustomAttribute("lang", (String) newValue));
        }
        return true;
    }
}

