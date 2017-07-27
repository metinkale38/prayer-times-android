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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.about.AboutFragment;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by metin on 25.07.17.
 */

public class LastFragment extends IntroFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        email.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColor(Color.WHITE).setIcon(MaterialDrawableBuilder.IconValue.EMAIL).build(), null, null, null);
        vote.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColor(Color.WHITE).setIcon(MaterialDrawableBuilder.IconValue.STAR).build(), null, null, null);
        showIntro.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColor(Color.WHITE).setIcon(MaterialDrawableBuilder.IconValue.WINDOW_RESTORE).build(), null, null, null);
        share.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColor(Color.WHITE).setIcon(MaterialDrawableBuilder.IconValue.SHARE).build(), null, null, null);
        translate.setCompoundDrawables(MaterialDrawableBuilder.with(getActivity()).setSizeDp(24).setColor(Color.WHITE).setIcon(MaterialDrawableBuilder.IconValue.FLAG).build(), null, null, null);
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
        return Prefs.showIntro();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) return;
        String newlang = (String) compoundButton.getTag();
        if (Prefs.getLanguage().equals(newlang)) return;
        Utils.changeLanguage(newlang);
        getActivity().recreate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mail:
                AboutFragment.mail(getActivity());
                break;
            case R.id.translate:
                AboutFragment.translate(getActivity());
                break;
            case R.id.vote:
                AboutFragment.rate(getActivity());
                break;
          case R.id.showIntro:
                Prefs.setShowIntro(true);
                Prefs.setChangelogVersion(0);
                getActivity().recreate();
            case R.id.share:
                AboutFragment.share(getActivity());
        }
    }
}
