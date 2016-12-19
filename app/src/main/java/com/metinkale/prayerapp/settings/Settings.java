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

package com.metinkale.prayerapp.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.Utils;


public class Settings extends BaseActivity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }


    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            findPreference("language").setOnPreferenceChangeListener(this);
            findPreference("numbers").setOnPreferenceChangeListener(this);

            findPreference("backupRestore").setOnPreferenceClickListener(this);

            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);

            findPreference("ongoingIcon").setOnPreferenceClickListener(this);

            findPreference("ongoingNumber").setOnPreferenceClickListener(this);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                findPreference("ongoingNumber").setEnabled(false);
            }
            findPreference("arabicNames").setEnabled(!Prefs.getLanguage().equals("ar"));

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if ("backupRestore".equals(preference.getKey())) {
                startActivity(new Intent(getActivity(), BackupRestoreActivity.class));
            }
            return true;

        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {

            if ("language".equals(pref.getKey()) || "digits".equals(pref.getKey())) {
                if ("language".equals(pref.getKey())) Utils.changeLanguage((String) newValue);
                Activity act = getActivity();
                act.finish();
                Intent i = new Intent(act, act.getClass());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(i);
            }
            return true;
        }
    }

}
