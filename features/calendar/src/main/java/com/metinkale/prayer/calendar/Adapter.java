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

package com.metinkale.prayer.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

public class Adapter extends ArrayAdapter<int[]> {
    @NonNull
    private final Context context;
    private final boolean isHijri;
    private List<Pair<HijriDate, Integer>> holydays;
    private boolean hasInfo;

    public Adapter(@NonNull Context context, int year, boolean isHijri) {
        super(context, R.layout.calendar_item);
        this.context = context;
        this.isHijri = isHijri;
        holydays = isHijri ? HijriDate.getHolydaysForHijriYear(year) : HijriDate.getHolydaysForGregYear(year);


        Locale lang = LocaleUtils.getLocale();
        hasInfo = (new Locale("de").getLanguage().equals(lang.getLanguage()) || new Locale("tr").getLanguage().equals(lang.getLanguage()));
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calendar_item, parent, false);

            vh = new ViewHolder();
            vh.view = convertView;
            vh.name = convertView.findViewById(R.id.name);
            vh.date = convertView.findViewById(isHijri ? R.id.hicri : R.id.date);
            vh.hicri = convertView.findViewById(isHijri ? R.id.date : R.id.hicri);
            vh.next = convertView.findViewById(R.id.next);
            convertView.setTag(R.id.viewholder, vh);
        } else {
            vh = (ViewHolder) convertView.getTag(R.id.viewholder);
        }

        Pair<HijriDate, Integer> pair = holydays.get(pos);
        HijriDate hijri = pair.first;
        int holyday = pair.second;
        if (holyday == 0) {
            LocalDate greg = hijri.getLocalDate();
            vh.hicri.setText(LocaleUtils.formatDate(hijri));
            vh.date.setText(LocaleUtils.formatDate(greg));
            vh.name.setVisibility(View.GONE);
            vh.next.setVisibility(View.GONE);
            vh.view.setBackgroundResource(R.color.backgroundSecondary);
        } else {
            LocalDate greg = hijri.getLocalDate();
            vh.hicri.setText(LocaleUtils.formatDate(hijri));
            vh.date.setText(LocaleUtils.formatDate(greg));
            vh.name.setText(LocaleUtils.getHolyday(holyday));
            vh.next.setVisibility(hasInfo ? View.VISIBLE : View.INVISIBLE);
            vh.name.setVisibility(View.VISIBLE);
            vh.view.setBackgroundColor(Color.TRANSPARENT);
        }
        convertView.setTag(holyday);
        return convertView;
    }

    @Override
    public int getCount() {
        return holydays.size();
    }

    private static class ViewHolder {
        View view;
        TextView name;
        TextView date;
        TextView hicri;
        ImageView next;
    }

}