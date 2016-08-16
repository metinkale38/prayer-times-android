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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.Utils;


public class Settings extends BaseActivity {

    public static void sendMail(Context ctx) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "metinkale38@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Namaz Vakti uygulamasi");
        String versionCode = "Undefined";
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }
        emailIntent.putExtra(Intent.EXTRA_TEXT, "===Device Information===\nManufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nAndroid Version: " + Build.VERSION.RELEASE + "\nApp Version Code: " + versionCode);
        ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.sendMail)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.init();
    }

    @Override
    public boolean setNavBar() {
        return false;
    }

    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            findPreference("mail").setOnPreferenceClickListener(this);

            findPreference("language").setOnPreferenceChangeListener(this);
            findPreference("numbers").setOnPreferenceChangeListener(this);

            findPreference("backupRestore").setOnPreferenceClickListener(this);

            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);

            findPreference("donate").setOnPreferenceClickListener(this);

            findPreference("ongoingIcon").setOnPreferenceClickListener(this);

            findPreference("arabicNames").setEnabled(false);

            findPreference("beta_tester").setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if ("mail".equals(preference.getKey())) {
                sendMail(getActivity());
            } else if ("backupRestore".equals(preference.getKey())) {
                startActivity(new Intent(getActivity(), BackupRestoreActivity.class));
            } else if ("beta_tester".equals(preference.getKey())) {
                String url = "https://play.google.com/apps/testing/com.metinkale.prayer";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } else if ("donate".equals(preference.getKey())) {
                Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.donateDlg);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.paypal, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                        String url = "http://www.paypal.me/metinkale38";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.bitcoin, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();
                        String url = "http://metinkale38.github.io/namaz-vakti-android/bitcoin.html";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });


                builder.setNeutralButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;

        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {

            if ("language".equals(pref.getKey()) || "digits".equals(pref.getKey())) {
                Utils.changeLanguage((String) newValue);
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
