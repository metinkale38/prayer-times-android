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

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.metinkale.prayer.Preferences;
import com.metinkale.prayer.utils.LocaleUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by metin on 25.07.17.
 */

public class LanguageFragment extends IntroFragment implements CompoundButton.OnCheckedChangeListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.intro_language, container, false);
        RadioGroup radioGroup = v.findViewById(R.id.radioGroup);
        
        List<LocaleUtils.Translation> langs = LocaleUtils.getSupportedLanguages(getActivity());
        String currentLang = Preferences.LANGUAGE.get();
        int pos = 0;
        for (int i = 0; i < langs.size(); i++) {
            LocaleUtils.Translation lang = langs.get(i);
            if (lang.getLanguage().equals(currentLang))
                pos = i + 1;
            RadioButton button = new RadioButton(getContext());
            button.setTag(lang.getLanguage());
            button.setText(lang.getDisplayText());
            button.setTextColor(getResources().getColor(R.color.white));
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            int padding = (int) (button.getTextSize() / 2);
            button.setPadding(padding, padding, padding, padding);
            button.setOnCheckedChangeListener(this);
            radioGroup.addView(button);
        }
        if (pos != 0)
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
        return Preferences.SHOW_INTRO.get();
    }
    
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b)
            return;
        String newlang = (String) compoundButton.getTag();
        if (Preferences.LANGUAGE.get().equals(newlang))
            return;
        Preferences.LANGUAGE.set(newlang);
    
        getActivity().finish();
        getActivity().overridePendingTransition( 0, 0);
        startActivity(getActivity().getIntent());
        getActivity().overridePendingTransition( 0, 0);
    }
}
