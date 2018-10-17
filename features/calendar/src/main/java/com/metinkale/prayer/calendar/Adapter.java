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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.metinkale.prayer.HijriDate;
import com.metinkale.prayer.utils.Utils;

import org.joda.time.LocalDate;

import java.util.Locale;

public class Adapter extends ArrayAdapter<int[]> {
    @NonNull
    private final Context context;
    private ArrayMap<HijriDate, Integer> holydays;
    private boolean hasInfo;

    public Adapter(@NonNull Context context, int year) {
        super(context, R.layout.calendar_item);
        this.context = context;
        holydays = HijriDate.getHolydaysForGregYear(year);

        Locale lang = Utils.getLocale();
        hasInfo = (new Locale("de").getLanguage().equals(lang.getLanguage())
                || new Locale("tr").getLanguage().equals(lang.getLanguage()));
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calendar_item, parent, false);

            vh = new ViewHolder();
            vh.name = convertView.findViewById(R.id.name);
            vh.date = convertView.findViewById(R.id.date);
            vh.hicri = convertView.findViewById(R.id.hicri);
            vh.next = convertView.findViewById(R.id.next);
            vh.next.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
            convertView.setTag(R.id.viewholder, vh);
        } else {
            vh = (ViewHolder) convertView.getTag(R.id.viewholder);
        }

        HijriDate hijri = holydays.keyAt(pos);
        int holyday = holydays.get(hijri);
        LocalDate greg = hijri.getLocalDate();

        vh.hicri.setText(Utils.format(hijri));
        vh.date.setText(Utils.format(greg));
        vh.name.setText(Utils.getHolyday(holyday - 1));
        convertView.setTag(holyday);
        return convertView;
    }

    @Override
    public int getCount() {
        return holydays.size();
    }

    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView hicri;
        ImageView next;
    }

}