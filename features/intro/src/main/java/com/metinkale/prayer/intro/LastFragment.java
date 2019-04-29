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

package com.metinkale.prayer.intro;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.utils.AboutShortcuts;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by metin on 25.07.17.
 */

public class LastFragment extends IntroFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_last, container, false);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) v.findViewById(R.id.version)).setText(pInfo.versionName + " (" + pInfo.versionCode + ")");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Button email = v.findViewById(R.id.mail);
        Button vote = v.findViewById(R.id.vote);
        Button showIntro = v.findViewById(R.id.showIntro);
        Button share = v.findViewById(R.id.share);
        Button translate = v.findViewById(R.id.translate);

        email.setOnClickListener(this);
        vote.setOnClickListener(this);
        showIntro.setOnClickListener(this);
        share.setOnClickListener(this);
        translate.setOnClickListener(this);

        email.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColorResource(R.color.background).setIcon(MaterialDrawableBuilder.IconValue.EMAIL).build(), null, null, null);
        vote.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColorResource(R.color.background).setIcon(MaterialDrawableBuilder.IconValue.STAR).build(), null, null, null);
        showIntro.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColorResource(R.color.background).setIcon(MaterialDrawableBuilder.IconValue.WINDOW_RESTORE).build(), null, null, null);
        share.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColorResource(R.color.background).setIcon(MaterialDrawableBuilder.IconValue.SHARE).build(), null, null, null);
        translate.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColorResource(R.color.background).setIcon(MaterialDrawableBuilder.IconValue.FLAG).build(), null, null, null);
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

    @Override
    public boolean allowTouch() {
        return true;
    }

    @Override
    protected boolean shouldShow() {
        return Preferences.SHOW_INTRO.get();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) return;
        String newlang = (String) compoundButton.getTag();
        if (Preferences.LANGUAGE.get().equals(newlang)) return;
        Preferences.LANGUAGE.set(newlang);
        getActivity().recreate();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.mail) {
            AboutShortcuts.mail(getActivity());

        } else if (i == R.id.translate) {
            AboutShortcuts.translate(getActivity());

        } else if (i == R.id.vote) {
            AboutShortcuts.rate(getActivity());

        } else if (i == R.id.showIntro) {
            Preferences.SHOW_INTRO.set(true);
            Preferences.CHANGELOG_VERSION.set(0);
            getActivity().recreate();

            AboutShortcuts.share(getActivity());
        } else if (i == R.id.share) {
            AboutShortcuts.share(getActivity());
        }
    }
}
