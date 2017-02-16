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

package com.metinkale.prayerapp.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.widget.Toast;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.about.AboutAct;
import com.metinkale.prayerapp.settings.Prefs;

public class Changelog {
    private static final int CHANGELOG_VERSION = 17;

    public static void start(@NonNull Context c) {
        if (Prefs.getChangelogVersion() < CHANGELOG_VERSION) {
            getDialog(c).show();
            Prefs.setChangelogVersion(CHANGELOG_VERSION);
        }
    }

    private static AlertDialog getDialog(@NonNull final Context c) {
        WebView wv = new WebView(c);
        String lang = Prefs.getLanguage();
        if (lang.equals("ar") || lang.equals("fr")) lang = "en";
        wv.loadUrl("file:///android_asset/" + lang + "/changelog.htm");

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(c.getResources().getString(R.string.changelog)).setView(wv).setCancelable(false);

        builder.setNegativeButton(c.getResources().getString(R.string.ok), (dialog, which) -> {

        });

        builder.setNeutralButton(c.getResources().getString(R.string.sendMail), (dialog, which) -> AboutAct.sendMail(c));

        builder.setPositiveButton(c.getResources().getString(R.string.vote), (dialog, which) -> {
            Uri uri = Uri.parse("market://details?id=" + c.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                c.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(c, "Couldn't launch the market", Toast.LENGTH_LONG).show();
            }
        });

        return builder.create();
    }
}
