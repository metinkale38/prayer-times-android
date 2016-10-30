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
 */

package com.metinkale.prayerapp.names;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.names.Adapter.Item;
import com.metinkale.prayerapp.settings.Prefs;

public class Adapter extends ArrayAdapter<Item> {

    private int lang;

    public Adapter(Context context, Item[] objects) {
        super(context, 0, objects);
        lang = Language.valueOf((Prefs.getLanguage() == null) ? "tr" : Prefs.getLanguage()).ordinal();

    }


    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.names_item, parent, false);
            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.name);
            vh.arabicImg = (ImageView) convertView.findViewById(R.id.arabicImg);
            vh.arabic = (TextView) convertView.findViewById(R.id.arabicTxt);
            vh.meaning = (TextView) convertView.findViewById(R.id.meaning);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }


        Item i = getItem(pos);
        if (pos == 0) {
            vh.arabicImg.setVisibility(View.VISIBLE);
            vh.arabicImg.setImageResource(R.drawable.allah);
            vh.name.setVisibility(View.GONE);
            vh.arabic.setVisibility(View.GONE);
        } else {
            vh.arabicImg.setImageDrawable(null);
            vh.arabicImg.setVisibility(View.GONE);
            vh.arabic.setText(i.arabic);
            if (lang == Language.ar.ordinal()) {
                vh.name.setVisibility(View.GONE);
            }
            else {
                vh.name.setText(i.name[lang]);
                vh.name.setVisibility(View.VISIBLE);
            }
            vh.arabic.setVisibility(View.VISIBLE);
        }

        if (lang == Language.ar.ordinal()) {
            vh.meaning.setVisibility(View.GONE);
        }
        else {
            vh.meaning.setText(i.meaning[lang]);
        }

        return convertView;
    }

    enum Language {
        tr, de, en, ar
    }

    static class Item {
        String arabic;
        String[] name = new String[3];
        String[] meaning = new String[3];

        @Override
        public String toString() {
            return arabic + " " + name[0] + " " + name[1] + " " + name[2] + " " + meaning[0] + " " + meaning[1] + " " + meaning[2];
        }
    }

    static class ViewHolder {
        TextView name;
        TextView meaning;
        TextView arabic;
        ImageView arabicImg;
    }
}