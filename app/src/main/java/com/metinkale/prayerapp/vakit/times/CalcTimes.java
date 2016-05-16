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

package com.metinkale.prayerapp.vakit.times;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.PrayTime.AdjMethod;
import com.metinkale.prayerapp.vakit.times.PrayTime.Juristic;
import com.metinkale.prayerapp.vakit.times.PrayTime.Method;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.TimeZone;

public class CalcTimes extends Times {

    private String method;
    private String adjMethod;
    private String juristic;
    private transient PrayTime mPrayTime;


    CalcTimes() {
    }

    CalcTimes(long id) {
        super(id);
    }


    private PrayTime getPrayTime() {
        if (mPrayTime == null) {
            mPrayTime = new PrayTime();
            try {
                mPrayTime.setCalcMethod(getMethod());
                mPrayTime.setAsrJuristic(getJuristic());
                mPrayTime.setAdjustHighLats(getAdjMethod());
            } catch (NullPointerException e) {
            }
            mPrayTime.setTimeZone(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000d * 60d * 60d));
        }
        return mPrayTime;
    }

    @SuppressLint("InflateParams")
    public static void add(final Activity c, final Bundle bdl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.calcmethod_dialog, null);
        ListView lv = (ListView) view.findViewById(R.id.listView);
        final Spinner sp = (Spinner) view.findViewById(R.id.spinner);
        final Spinner sp2 = (Spinner) view.findViewById(R.id.spinner2);

        lv.setAdapter(new ArrayAdapter<Method>(c, R.layout.calcmethod_dlgitem, R.id.legacySwitch, Method.values()) {
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                ViewGroup vg = (ViewGroup) super.getView(pos, convertView, parent);
                ((TextView) vg.getChildAt(0)).setText(getItem(pos).title);
                ((TextView) vg.getChildAt(1)).setText(getItem(pos).desc);
                return vg;
            }

        });


        sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.adjMethod)));
        sp2.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.juristicMethod)));


        builder.setTitle(R.string.calcMethod);
        builder.setView(view);
        final AlertDialog dlg = builder.create();
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long index) {

                CalcTimes t = new CalcTimes(System.currentTimeMillis());
                t.setSource(Source.Calc);
                t.setName(bdl.getString("city"));
                t.setLat(bdl.getDouble("lat"));
                t.setLng(bdl.getDouble("lng"));
                t.setMethod(Method.values()[pos]);
                t.setJuristic(Juristic.values()[sp2.getSelectedItemPosition()]);
                t.setAdjMethod(AdjMethod.values()[sp.getSelectedItemPosition()]);
                t.setSortId(99);
                dlg.cancel();
                c.finish();

            }

        });

        dlg.show();

    }

    @Override
    public Source getSource() {
        return Source.Calc;
    }

    @Override
    public String _getTime(LocalDate date, int time) {
        List<String> times = getPrayTime().getDatePrayerTimes(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), getLat(), getLng());
        times.remove(4);
        return times.get(time);
    }


    public synchronized PrayTime.Method getMethod() {
        return PrayTime.Method.valueOf(method);
    }

    public synchronized void setMethod(PrayTime.Method value) {
        method = value.name();
        save();
    }

    public synchronized PrayTime.Juristic getJuristic() {
        return PrayTime.Juristic.valueOf(juristic);
    }

    public synchronized void setJuristic(PrayTime.Juristic value) {
        juristic = value.name();
        save();
    }

    public synchronized PrayTime.AdjMethod getAdjMethod() {
        return PrayTime.AdjMethod.valueOf(adjMethod);
    }

    public synchronized void setAdjMethod(PrayTime.AdjMethod value) {
        adjMethod = value.name();
        save();
    }


}
