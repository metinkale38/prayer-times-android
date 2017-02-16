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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.names.Adapter.Item;

class Adapter extends ArrayAdapter<Item> {

    Adapter(@NonNull Context context, @NonNull Item[] objects) {
        super(context, 0, objects);


    }


    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
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
            if (i.name == null) {
                vh.name.setVisibility(View.GONE);
            } else {
                vh.name.setText(i.name);
                vh.name.setVisibility(View.VISIBLE);
            }
            vh.arabic.setVisibility(View.VISIBLE);
        }

        if (i.desc == null) {
            vh.meaning.setVisibility(View.GONE);
        } else {
            vh.meaning.setText(i.desc);
        }

        return convertView;
    }

    static class Item {
        String arabic;
        String name;
        String desc;

        @NonNull
        @Override
        public String toString() {
            return arabic + " " + name + " " + desc;
        }
    }

    private static class ViewHolder {
        TextView name;
        TextView meaning;
        TextView arabic;
        ImageView arabicImg;
    }
}