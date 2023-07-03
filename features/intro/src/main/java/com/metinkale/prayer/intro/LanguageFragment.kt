/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.intro

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.utils.LocaleUtils.getSupportedLanguages

/**
 * Created by metin on 25.07.17.
 */
class LanguageFragment : IntroFragment(), CompoundButton.OnCheckedChangeListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_language, container, false)
        val radioGroup = v.findViewById<RadioGroup>(R.id.radioGroup)
        val langs = getSupportedLanguages(
            requireActivity()
        )
        val currentLang: String = Preferences.LANGUAGE
        var pos = 0
        for (i in langs.indices) {
            val lang = langs[i]
            if (lang.language == currentLang) pos = i + 1
            val button = RadioButton(context)
            button.tag = lang.language
            button.text = lang.displayText
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            val padding = (button.textSize / 2).toInt()
            button.setPadding(padding, padding, padding, padding)
            button.setOnCheckedChangeListener(this)
            radioGroup.addView(button)
        }
        if (pos != 0) (radioGroup.getChildAt(pos) as RadioButton).isChecked = true
        return v
    }

    /*
    Answers.getInstance().logCustom(new CustomEvent("Language")
                        .putCustomAttribute("lang", lang)
                );
     */
    override fun onSelect() {}
    override fun onEnter() {}
    override fun onExit() {}
    override fun allowTouch(): Boolean {
        return true
    }

    override fun shouldShow(): Boolean {
        return Preferences.SHOW_INTRO
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        if (!b) return
        val newlang = compoundButton.tag as String
        if (Preferences.LANGUAGE.equals(newlang)) return
        Preferences.LANGUAGE = newlang
        requireActivity().overridePendingTransition(0, 0)
        startActivity(requireActivity().intent)
        requireActivity().overridePendingTransition(0, 0)
        requireActivity().finish()
    }
}