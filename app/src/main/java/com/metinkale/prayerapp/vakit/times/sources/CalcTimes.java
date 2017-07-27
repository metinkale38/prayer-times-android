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

package com.metinkale.prayerapp.vakit.times.sources;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.Constants;
import org.metinkale.praytimes.Method;
import org.metinkale.praytimes.PrayTimes;

import java.util.TimeZone;

public class CalcTimes extends Times {

    private String method;
    private String adjMethod;
    private String juristic;
    private double[] customMethodParams = new double[6];
    private transient PrayTimes mPrayTime;


    @SuppressWarnings("unused")
    CalcTimes() {
        super();
    }

    CalcTimes(long id) {
        super(id);
    }

    public static void buildTemporaryTimes(String name, double lat, double lng, long id) {
        CalcTimes t = new CalcTimes(id);
        t.setSource(Source.Calc);
        if (lat > 48)
            t.getPrayTimes().setHighLatsAdjustment(Constants.HIGHLAT_ANGLEBASED);
        t.setName(name);
        t.setLat(lat);
        t.setLng(lng);
    }

    private PrayTimes getPrayTimes() {
        if (mPrayTime == null) {
            mPrayTime = new PrayTimes();
            mPrayTime.setCoordinates(getLat(), getLng(), 0);
            if (method == null) {
                Ion.with(App.get())
                        .load("http://api.geonames.org/timezoneJSON?lat=" + getLat() + "&lng=" + getLng() + "&username=metnkale38")
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) Crashlytics.logException(e);
                                if (result != null)
                                    try {
                                        mPrayTime.setTimezone(TimeZone.getTimeZone(result.get("timezoneId").getAsString()));
                                    } catch (Exception ee) {
                                        Crashlytics.logException(ee);
                                    }
                            }
                        });

                Ion.with(App.get())
                        .load("http://api.geonames.org/gtopo30?lat=" + getLat() + "&lng=" + getLng() + "&username=metnkale38")
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e == null)
                                    Crashlytics.logException(e);
                                try {
                                    double m = Double.parseDouble(result);
                                    if (m < -9000) m = 0;
                                    mPrayTime.setCoordinates(getLat(), getLng(), m);
                                } catch (Exception ee) {
                                    Crashlytics.logException(ee);
                                }
                            }
                        });
            }

            if (method != null && !"Custom".equals(method)) {
                mPrayTime.setMethod(Method.valueOf(method));
            } else {
                mPrayTime.setFajrDegrees(customMethodParams[0]);
                mPrayTime.setDhuhrMins(customMethodParams[2]);
                mPrayTime.setIshaTime(customMethodParams[4], customMethodParams[3] == 1);
            }

            if ("Hanafi".equals(juristic))
                mPrayTime.setAsrJuristic(Constants.JURISTIC_HANAFI);
            else if ("Shafii".equals(juristic))
                mPrayTime.setAsrJuristic(Constants.JURISTIC_STANDARD);

            if (adjMethod != null)
                switch (adjMethod) {
                    case "AngleBased":
                        mPrayTime.setHighLatsAdjustment(Constants.HIGHLAT_ANGLEBASED);
                        break;
                    case "OneSeventh":
                        mPrayTime.setHighLatsAdjustment(Constants.HIGHLAT_ONESEVENTH);
                        break;
                    case "MidNight":
                        mPrayTime.setHighLatsAdjustment(Constants.HIGHLAT_NIGHTMIDDLE);
                        break;
                }
            method = null;
            juristic = null;
            adjMethod = null;
            customMethodParams = null;
        }
        return mPrayTime;
    }

    @SuppressLint("InflateParams")
    public static void add(@NonNull final Activity c, @NonNull final Bundle bdl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        final LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.calcmethod_dialog, null);
        ListView lv = view.findViewById(R.id.listView);
        final Spinner sp = view.findViewById(R.id.spinner);
        final Spinner sp2 = view.findViewById(R.id.spinner2);

        lv.setAdapter(new ArrayAdapter<String>(c, R.layout.calcmethod_dlgitem, R.id.legacySwitch,
                c.getResources().getStringArray(R.array.calculationMethods)) {
            String[] desc = c.getResources().getStringArray(R.array.calculationMethodsDesc);

            @NonNull
            @Override
            public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
                ViewGroup vg = (ViewGroup) super.getView(pos, convertView, parent);
                ((TextView) vg.getChildAt(0)).setText(getItem(pos));
                ((TextView) vg.getChildAt(1)).setText(desc[pos]);
                return vg;
            }

            @Override
            public int getCount() {
                return 8;
            }
        });

        sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.adjMethod)));
        sp2.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, c.getResources().getStringArray(R.array.juristicMethod)));

        builder.setTitle(R.string.calcMethod);
        builder.setView(view);
        final AlertDialog dlg = builder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Method method1 = pos >= Method.values().length ? null : Method.values()[pos];

                if (method1 == null) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
                    final View custom = inflater.inflate(R.layout.calcmethod_custom_dialog, null);

                    builder1.setView(custom);
                    final Dialog dlg1 = builder1.show();
                    custom.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CalcTimes t = new CalcTimes(System.currentTimeMillis());
                            t.setSource(Source.Calc);
                            t.setName(bdl.getString("city"));
                            t.setLat(bdl.getDouble("lat"));
                            t.setLng(bdl.getDouble("lng"));
                            TextView imsakPicker = custom.findViewById(R.id.imsakPicker);
                            TextView fajrPicker = custom.findViewById(R.id.fajrPicker);
                            TextView zuhrPicker = custom.findViewById(R.id.zuhrPicker);
                            TextView maghribPicker = custom.findViewById(R.id.maghribPicker);
                            TextView ishaPicker = custom.findViewById(R.id.ishaPicker);

                            RadioButton imsakMin = custom.findViewById(R.id.imsakTimeBased);
                            RadioButton maghribMin = custom.findViewById(R.id.maghribTimeBased);
                            RadioButton ishaMin = custom.findViewById(R.id.ishaTimeBased);


                            t.getPrayTimes().setImsakTime(Double.parseDouble(imsakPicker.getText().toString()), imsakMin.isChecked());
                            t.getPrayTimes().setFajrDegrees(Double.parseDouble(fajrPicker.getText().toString()));
                            t.getPrayTimes().setDhuhrMins(Double.parseDouble(zuhrPicker.getText().toString()));
                            t.getPrayTimes().setMaghribTime(Double.parseDouble(maghribPicker.getText().toString()), maghribMin.isChecked());
                            t.getPrayTimes().setIshaTime(Double.parseDouble(ishaPicker.getText().toString()), ishaMin.isChecked());

                            t.getPrayTimes().setAsrJuristic(sp2.getSelectedItemPosition() + 1);
                            t.getPrayTimes().setHighLatsAdjustment(sp.getSelectedItemPosition());
                            t.setSortId(99);
                            c.finish();
                            dlg1.cancel();

                            Answers.getInstance().logCustom(new CustomEvent("AddCity")
                                    .putCustomAttribute("Source", Source.Calc.name())
                                    .putCustomAttribute("City", bdl.getString("city"))
                                    .putCustomAttribute("Method", "Custom")
                                    .putCustomAttribute("Juristic", sp2.getSelectedItemPosition() + "")
                                    .putCustomAttribute("AdjMethod", sp.getSelectedItemPosition() + "")
                            );
                        }
                    });
                } else {
                    CalcTimes t = new CalcTimes(System.currentTimeMillis());
                    t.setSource(Source.Calc);
                    t.setName(bdl.getString("city"));
                    t.setLat(bdl.getDouble("lat"));
                    t.setLng(bdl.getDouble("lng"));
                    t.getPrayTimes().setMethod(method1);
                    t.getPrayTimes().setAsrJuristic(sp2.getSelectedItemPosition() + 1);
                    t.getPrayTimes().setHighLatsAdjustment(sp.getSelectedItemPosition());
                    t.setSortId(99);
                    c.finish();

                    Answers.getInstance().logCustom(new CustomEvent("AddCity")
                            .putCustomAttribute("Source", Source.Calc.name())
                            .putCustomAttribute("City", bdl.getString("city"))
                            .putCustomAttribute("Method", method1.name())
                            .putCustomAttribute("Juristic", sp2.getSelectedItemPosition() + "")
                            .putCustomAttribute("AdjMethod", sp.getSelectedItemPosition() + ""));
                }
                dlg.cancel();
            }
        });

        try {
            dlg.show();
        } catch (WindowManager.BadTokenException ignore) {
        }
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Calc;
    }

    @Override
    public String _getTime(@NonNull LocalDate date, int time) {
        getPrayTimes().setCoordinates(getLat(), getLng(), 0);
        getPrayTimes().setDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        switch (time) {
            case 0:
                return getPrayTimes().getTime(Prefs.showExtraTimes() ? Constants.TIMES_IMSAK : Constants.TIMES_FAJR);
            case 1:
                return getPrayTimes().getTime(Constants.TIMES_SUNRISE);
            case 2:
                return getPrayTimes().getTime(Constants.TIMES_DHUHR);
            case 3:
                return getPrayTimes().getTime(Prefs.showExtraTimes() ? Constants.TIMES_ASR_SHAFII : Constants.TIMES_ASR);
            case 4:
                return getPrayTimes().getTime(Constants.TIMES_MAGHRIB);
            case 5:
                return getPrayTimes().getTime(Constants.TIMES_ISHA);
            case 6:
                return getPrayTimes().getTime(Constants.TIMES_FAJR);
            case 7:
                return getPrayTimes().getTime(Constants.TIMES_ASR_HANAFI);
        }
        return "00:00";
    }


}
