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

package com.metinkale.prayer.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Module;
import com.metinkale.prayer.Prefs;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static com.metinkale.prayer.utils.AboutShortcuts.beta;
import static com.metinkale.prayer.utils.AboutShortcuts.github;
import static com.metinkale.prayer.utils.AboutShortcuts.mail;
import static com.metinkale.prayer.utils.AboutShortcuts.rate;
import static com.metinkale.prayer.utils.AboutShortcuts.reportBug;
import static com.metinkale.prayer.utils.AboutShortcuts.share;
import static com.metinkale.prayer.utils.AboutShortcuts.translate;

/**
 * Created by metin on 30.10.16.
 */
public class AboutFragment extends BaseActivity.MainFragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_main, container, false);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) v.findViewById(R.id.version)).setText(pInfo.versionName + " (" + pInfo.versionCode + ")");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        v.findViewById(R.id.beta).setOnClickListener(this);
        v.findViewById(R.id.mail).setOnClickListener(this);
        v.findViewById(R.id.libLicences).setOnClickListener(this);
        v.findViewById(R.id.licenses).setOnClickListener(this);
        v.findViewById(R.id.reportBug).setOnClickListener(this);
        v.findViewById(R.id.translate).setOnClickListener(this);
        v.findViewById(R.id.rate).setOnClickListener(this);
        v.findViewById(R.id.github).setOnClickListener(this);
        v.findViewById(R.id.share).setOnClickListener(this);
        v.findViewById(R.id.showIntro).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.beta) {
            beta(getActivity());
        } else if (i == R.id.mail) {
            mail(getActivity());
        } else if (i == R.id.libLicences) {
            libLicences(getActivity());
        } else if (i == R.id.licenses) {
            licenses(getActivity());
        } else if (i == R.id.reportBug) {
            reportBug(getActivity());
        } else if (i == R.id.translate) {
            translate(getActivity());
        } else if (i == R.id.rate) {
            rate(getActivity());
        } else if (i == R.id.github) {
            github(getActivity());
        } else if (i == R.id.share) {
            share(getActivity());
        } else if (i == R.id.showIntro) {
            Prefs.setShowIntro(true);
            Prefs.setChangelogVersion(0);
            Module.INTRO.launch(getActivity());
        }
    }


    public static void licenses(@NonNull Context ctx) {
        WebView wv = new WebView(ctx);
        wv.loadUrl("file:///android_asset/license.html");
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getResources().getString(R.string.license)).setView(wv).setCancelable(false);
        builder.setNegativeButton(ctx.getResources().getString(R.string.ok), null);
        builder.show();

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "licenses")
        );
    }


    public static void libLicences(@NonNull Context ctx) {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle(ctx.getString(R.string.library_licenses))
                .withLibraries()
                .start(ctx);

        Answers.getInstance().logCustom(new CustomEvent("About")
                .putCustomAttribute("action", "libLicenses")
        );
    }


}
