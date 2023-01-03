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

package com.metinkale.prayer.intro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metinkale.prayer.utils.LocaleUtils;


/**
 * Created by metin on 25.07.17.
 */

public class ChangelogFragment extends IntroFragment {
    
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_changelog, container, false);
        
        final WebView md = v.findViewById(R.id.markdownview);
        String lang = LocaleUtils.getLanguage("en", "de", "tr");
        switch (lang) {
            case "en":
                // TODO fix Markdown
               // md.loadMarkdownFile("file:///android_asset/english.md", "file:///android_asset/style.css");
                break;
            case "de":
               // md.loadMarkdownFile("file:///android_asset/german.md", "file:///android_asset/style.css");
                break;
            case "tr":
               // md.loadMarkdownFile("file:///android_asset/turkish.md", "file:///android_asset/style.css");
                break;
        }
        
        int color = MainActivity.blendColors(Color.WHITE, getBackgroundColor(), 0.2f);
        md.setBackgroundColor(color);
        v.findViewById(R.id.changelog).setBackgroundColor(color);
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
