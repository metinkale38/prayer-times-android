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
 *
 */

package com.metinkale.prayerapp.about;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.settings.Prefs;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

/**
 * Created by metin on 30.10.16.
 */
public class AboutAct extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);


        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.version)).setText(pInfo.versionName + " (" + pInfo.versionCode + ")");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void donate(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.donateDlg);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.paypal, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dlg, int i) {
                        dlg.cancel();
                        String url = "http://www.paypal.me/metinkale38";
                        openUrl(url);
                    }
                }
        );
        builder.setNegativeButton(R.string.bitcoin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dlg, int i) {
                dlg.cancel();
                String url = "http://metinkale38.github.io/namaz-vakti-android/bitcoin.html";
                openUrl(url);
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dlg, int i) {
                dlg.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "donate")
        );
    }

    private void openUrl(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(AboutAct.this, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    public void github(View view) {
        String url = "https://github.com/metinkale38/prayer-times-android";
        openUrl(url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "github")
        );
    }

    public void rate(View view) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Couldn't launch the market", Toast.LENGTH_LONG).show();
        }

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "rate")
        );
    }

    public void translate(View view) {
        String url = "https://crowdin.com/project/prayer-times-android";
        openUrl(url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "translate")
        );
    }

    public void reportBug(View view) {
        String url = "https://github.com/metinkale38/prayer-times-android/issues";
        openUrl(url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "reportBug")
        );
    }

    public void licenses(View view) {
        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/license.html");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.license)).setView(wv).setCancelable(false);
        builder.setNegativeButton(getResources().getString(R.string.ok), null);
        builder.show();

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "licenses")
        );
    }

    public void LibLicences(View view) {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle(getString(R.string.library_licenses))
                .withLibraries()
                .start(this);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "libLicenses")
        );
    }

    public void mail(@NonNull View view) {
        sendMail(view.getContext());

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "mail")
        );
    }

    public static void sendMail(@NonNull Context ctx) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "metinkale38@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.appName) + " (com.metinkaler.prayer)");
        String versionCode = "Undefined";
        String versionName = "Undefined";
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode + "";
            versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName + "";
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "===Device Information===" +
                        "\nUUID: " + Prefs.getUUID() +
                        "\nManufacturer: " + Build.MANUFACTURER +
                        "\nModel: " + Build.MODEL +
                        "\nAndroid Version: " + Build.VERSION.RELEASE +
                        "\nApp Version Name: " + versionName +
                        "\nApp Version Code: " + versionCode +
                        "\n======================\n\n");
        ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.mail)));
    }

    public void beta(View view) {
        String url = "https://play.google.com/apps/testing/com.metinkale.prayer";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "beta")
        );
    }
}
