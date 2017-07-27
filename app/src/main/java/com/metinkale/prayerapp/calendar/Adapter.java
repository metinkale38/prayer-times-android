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

package com.metinkale.prayerapp.calendar;

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
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.settings.Prefs;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Locale;

public class Adapter extends ArrayAdapter<int[]> {
    @NonNull
    private final Context context;
    private List<int[]> days;
    private boolean hasInfo;

    public Adapter(@NonNull Context context, int year) {
        super(context, R.layout.names_item);
        this.context = context;
        days = HicriDate.getHolydays(year);

        Locale lang = Utils.getLocale();
        hasInfo = (new Locale("de").equals(lang) || new Locale("tr").equals(lang));
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

        int[] h = days.get(pos);

        vh.hicri.setText(Utils.format(new HicriDate(h[HicriDate.HY], h[HicriDate.HM], h[HicriDate.HD])));
        vh.date.setText(Utils.format(new LocalDate(h[HicriDate.GY], h[HicriDate.GM], h[HicriDate.GD])));
        vh.name.setText(Utils.getHolyday(h[HicriDate.DAY] - 1));
        convertView.setTag(h[HicriDate.DAY]);
        return convertView;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView hicri;
        ImageView next;
    }

}