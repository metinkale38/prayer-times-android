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

package com.metinkale.prayerapp.intro;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.about.AboutFragment;
import com.metinkale.prayerapp.calendar.WebViewFragment;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;

/**
 * Created by metin on 25.07.17.
 */

public class ChangelogFragment extends IntroFragment {
    public static final int CHANGELOG_VERSION = 20;

    @Override
    public boolean shouldShow() {
        return true;
    }

    @Override
    public boolean allowTouch() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_changelog, container, false);

        WebView wv = v.findViewById(R.id.webview);
        String lang = Utils.getLanguage("en", "de", "tr");
        wv.loadUrl("file:///android_asset/" + lang + "/changelog.htm");
        wv.setBackgroundColor(Color.TRANSPARENT);
        return v;
    }

    @Override
    protected void onSelect() {

    }

    @Override
    protected void onEnter() {

    }

    @Override
    protected void onExit() {

    }


}
