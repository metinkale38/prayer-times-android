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

package com.metinkale.prayer.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.base.R;


public class AboutShortcuts {
    public static void share(Context ctx) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.shareText));
        sendIntent.setType("name/plain");
        ctx.startActivity(sendIntent);
    }

    private static void openUrl(Context ctx, String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ctx.getResources().getColor(R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(ctx, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            ctx.startActivity(i);
        }
    }


    public static void github(@NonNull Context ctx) {
        String url = "https://github.com/metinkale38/prayer-times-android";
        openUrl(ctx, url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "github")
        );
    }


    public static void rate(@NonNull Context ctx) {
        Uri uri = Uri.parse("market://details?id=" + ctx.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            ctx.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, "Couldn't launch the market", Toast.LENGTH_LONG).show();
        }

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "rate")
        );

        //AppRatingDialog.setInstalltionTime(Long.MAX_VALUE); //never show the rating dialog :)
    }


    public static void translate(@NonNull Context ctx) {
        String url = "https://crowdin.com/project/prayer-times-android";
        openUrl(ctx, url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "translate")
        );
    }


    public static void reportBug(@NonNull Context ctx) {
        String url = "https://github.com/metinkale38/prayer-times-android/issues";
        openUrl(ctx, url);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "reportBug")
        );
    }



    public static void mail(@NonNull Context ctx) {
        sendMail(ctx);

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
                        "\nUUID: " + Preferences.UUID.get() +
                        "\nManufacturer: " + Build.MANUFACTURER +
                        "\nModel: " + Build.MODEL +
                        "\nAndroid Version: " + Build.VERSION.RELEASE +
                        "\nApp Version Name: " + versionName +
                        "\nApp Version Code: " + versionCode +
                        "\n======================\n\n");
        ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.mail)));
    }

    public static void beta(Context ctx) {
        String url = "https://play.google.com/apps/testing/com.metinkale.prayer";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        ctx.startActivity(i);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "beta")
        );
    }

}
