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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;

/**
 * Created by metin on 25.07.17.
 */

public class LanguageFragment extends IntroFragment implements CompoundButton.OnCheckedChangeListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_language, container, false);
        RadioGroup radioGroup = v.findViewById(R.id.radioGroup);

        String[] langIds = getResources().getStringArray(R.array.language_val);
        String[] lang = getResources().getStringArray(R.array.language);
        String currentLang = Prefs.getLanguage();
        int pos = 0;
        for (int i = 0; i < lang.length; i++) {
            String id = langIds[i];
            String name = lang[i];
            if (id.equals(currentLang)) pos = i + 1;
            RadioButton button = new RadioButton(getContext());
            button.setTag(id);
            button.setText(name);
            button.setTextColor(Color.WHITE);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            int padding = (int) (button.getTextSize() / 2);
            button.setPadding(padding, padding, padding, padding);
            button.setOnCheckedChangeListener(this);
            radioGroup.addView(button);
        }
        ((RadioButton) radioGroup.getChildAt(pos)).setChecked(true);
        return v;
    }

    /*
    Answers.getInstance().logCustom(new CustomEvent("Language")
                        .putCustomAttribute("lang", lang)
                );
     */
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
}
