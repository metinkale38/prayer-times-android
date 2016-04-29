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
import android.preference.PreferenceManager.OnActivityResultListener;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.vakit.WidgetService;


public class Settings extends BaseActivity {

    private static final String DONATE_LINK_EN = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ADFQEQS8A6ZK6";
    private static final String DONATE_LINK_DE = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=GV6N4NZ6RELTU";
    private static final String DONATE_LINK_TR = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HPW6ZNL8GK77Y";
    private OnActivityResultListener mList;

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
        ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.sendemail)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Prefs.reset();
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

            findPreference("backupRestore").setOnPreferenceClickListener(this);

            findPreference("calendarIntegration").setOnPreferenceChangeListener(this);

            findPreference("donate").setOnPreferenceClickListener(this);

            findPreference("ongoingIcon").setOnPreferenceClickListener(this);

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if ("mail".equals(preference.getKey())) {
                sendMail(getActivity());
            } else if ("backupRestore".equals(preference.getKey())) {
                startActivity(new Intent(getActivity(), BackupRestoreActivity.class));
            } else if ("donate".equals(preference.getKey())) {
                Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.donateDlg);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.cancel();

                        String lang = Prefs.getLanguage();
                        String url = DONATE_LINK_EN;
                        if ("tr".equals(lang)) url = DONATE_LINK_TR;
                        else if ("de".equals(lang)) url = DONATE_LINK_DE;

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if ("ongoingIcon".equals(preference.getKey())) WidgetService.updateOngoing();
            return true;

        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            Prefs.reset();
            if ("language".equals(pref.getKey())) {

                Activity act = getActivity();
                act.finish();
                Intent i = new Intent(act, act.getClass());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(i);
            } else if ("calendarIntegration".equals(pref.getKey())) {
                MainIntentService.startCalendarIntegration(getActivity());
            }
            return true;
        }
    }

}
