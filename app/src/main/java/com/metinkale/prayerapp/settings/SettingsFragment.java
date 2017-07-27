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

package com.metinkale.prayerapp.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.MainActivity;
import com.metinkale.prayerapp.utils.NumberPickerPreference;
import com.metinkale.prayerapp.utils.Utils;

import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.settings);
        if (getArguments() != null && getArguments().getBoolean("showKerahatDuration"))
            setPreferenceScreen((PreferenceScreen) findPreference("kerahatDuration"));
        else {
            findPreference("language").setOnPreferenceChangeListener(this);
            findPreference("numbers").setOnPreferenceChangeListener(this);

            findPreference("backupRestore").setOnPreferenceClickListener(this);

            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);

            findPreference("ongoingIcon").setOnPreferenceClickListener(this);

            findPreference("ongoingNumber").setOnPreferenceClickListener(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                findPreference("ongoingNumber").setEnabled(false);
            }

            if (Build.VERSION.SDK_INT < 24)
                findPreference("showLegacyWidgets").setEnabled(false);

            findPreference("arabicNames").setEnabled(!new Locale("ar").equals(Utils.getLocale()));

            PreferenceScreen screen = (PreferenceScreen) findPreference("kerahatDuration");
            screen.setOnPreferenceClickListener(this);
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
                ((MainActivity) getActivity()).moveToFrag(frag);
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
                handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment())
                        .onPreferenceDisplayDialog(this, preference);
            }
            if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
                handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                        .onPreferenceDisplayDialog(this, preference);
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
        } else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference pref, Object newValue) {

        if ("language".equals(pref.getKey()) || "digits".equals(pref.getKey())) {
            if ("language".equals(pref.getKey())) Utils.changeLanguage((String) newValue);
            Activity act = getActivity();
            act.finish();
            Intent i = new Intent(act, act.getClass());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            act.startActivity(i);

            Answers.getInstance().logCustom(new CustomEvent("Language")
                    .putCustomAttribute("lang", (String) newValue)
            );
        }
        return true;
    }
}

