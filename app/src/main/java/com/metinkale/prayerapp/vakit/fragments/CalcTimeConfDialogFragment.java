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

package com.metinkale.prayerapp.vakit.fragments;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.metinkale.prayerapp.utils.UUID;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.sources.CalcTimes;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.Constants;
import org.metinkale.praytimes.Method;
import org.metinkale.praytimes.PrayTimes;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by metin on 10.10.2017.
 */

public class CalcTimeConfDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, TextWatcher, View.OnClickListener {
    private Spinner mCalcMethod, mAsrJur, mHLAdj;
    private EditText mValues[] = new EditText[7];
    private TextView mTimes[] = new TextView[7];
    private RadioGroup mRadios[] = new RadioGroup[7];
    private PrayTimes mPrayTimes = new PrayTimes();
    private boolean mFixingData;
    private TimeZone mTz;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calcmethod_dialog, container, false);

        mCalcMethod = v.findViewById(R.id.calcMethod);
        mAsrJur = v.findViewById(R.id.asrJur);
        mHLAdj = v.findViewById(R.id.highlatsAdj);

        mValues[0] = v.findViewById(R.id.imsakValue);
        mValues[1] = v.findViewById(R.id.fajrValue);
        mValues[2] = v.findViewById(R.id.sunValue);
        mValues[3] = v.findViewById(R.id.dhuhrValue);
        mValues[4] = v.findViewById(R.id.asrValue);
        mValues[5] = v.findViewById(R.id.maghribValue);
        mValues[6] = v.findViewById(R.id.ishaaValue);

        mTimes[0] = v.findViewById(R.id.imsakTime);
        mTimes[1] = v.findViewById(R.id.fajrTime);
        mTimes[2] = v.findViewById(R.id.sunTime);
        mTimes[3] = v.findViewById(R.id.dhuhrTime);
        mTimes[4] = v.findViewById(R.id.asrTime);
        mTimes[5] = v.findViewById(R.id.maghribTime);
        mTimes[6] = v.findViewById(R.id.ishaaTime);

        mRadios[0] = v.findViewById(R.id.imsakRadio);
        mRadios[1] = v.findViewById(R.id.fajrRadio);
        mRadios[2] = v.findViewById(R.id.sunRadio);
        mRadios[3] = v.findViewById(R.id.dhuhrRadio);
        mRadios[4] = v.findViewById(R.id.asrRadio);
        mRadios[5] = v.findViewById(R.id.maghribRadio);
        mRadios[6] = v.findViewById(R.id.ishaaRadio);

        for (TextView tv : mValues) {
            tv.addTextChangedListener(this);
        }


        Bundle bdl = getArguments();
        final double lat = bdl.getDouble("lat", 0);
        final double lng = bdl.getDouble("lng", 0);
        final double elv = bdl.getDouble("elv", 0);
        mPrayTimes.setCoordinates(lat, lng, elv);
        LocalDate date = LocalDate.now();
        mPrayTimes.setDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        mHLAdj.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.adjMethod)));
        mAsrJur.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.juristicMethod)));

        mCalcMethod.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.calcmethod_dlgitem, R.id.legacySwitch,
                getResources().getStringArray(R.array.calculationMethods)) {
            String[] desc = getResources().getStringArray(R.array.calculationMethodsDesc);
            String[] names = getResources().getStringArray(R.array.calculationMethodsShort);

            @Override
            public View getDropDownView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
                ViewGroup vg = (ViewGroup) convertView;
                if (vg == null)
                    vg = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.calcmethod_dlgitem, parent, false);
                ((TextView) vg.getChildAt(0)).setText(getItem(pos));
                ((TextView) vg.getChildAt(1)).setText(desc[pos]);
                return vg;
            }

            @NonNull
            @Override
            public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) convertView;
                if (tv == null)
                    tv = (TextView) LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_spinner_item, parent, false);
                tv.setText(names[pos]);
                return tv;
            }

            @Override
            public int getCount() {
                return 8;
            }
        });


        mCalcMethod.setOnItemSelectedListener(this);
        mAsrJur.setOnItemSelectedListener(this);
        mHLAdj.setOnItemSelectedListener(this);

        mRadios[0].setOnCheckedChangeListener(this);
        mRadios[5].setOnCheckedChangeListener(this);
        mRadios[6].setOnCheckedChangeListener(this);


        mPrayTimes.setTimezone(TimeZone.getDefault());
        Ion.with(App.get())
                .load("http://api.geonames.org/timezoneJSON?lat=" + lat + "&lng=" + lng + "&username=metnkale38")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) Crashlytics.logException(e);
                        if (result != null)
                            try {
                                mTz = TimeZone.getTimeZone(result.get("timezoneId").getAsString());
                                mPrayTimes.setTimezone(mTz);
                            } catch (Exception ee) {
                                Crashlytics.logException(ee);
                            }
                        updateTimes();
                    }
                });

        Ion.with(App.get())
                .load("http://api.geonames.org/gtopo30?lat=" + lat + "&lng=" + lng + "&username=metnkale38")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e == null)
                            Crashlytics.logException(e);
                        try {
                            double m = Double.parseDouble(result);
                            if (m < -9000) m = 0;
                            mPrayTimes.setCoordinates(lat, lng, m);
                        } catch (Exception ee) {
                            Crashlytics.logException(ee);
                        }
                        updateTimes();
                    }
                });

        mCalcMethod.setSelection(0);

        v.findViewById(R.id.ok).setOnClickListener(this);
        return v;
    }


    private void fixData() {
        mFixingData = true;
        if (mPrayTimes.isImsakTimeInMins()) {
            ((RadioButton) mRadios[0].getChildAt(1)).setChecked(true);
        } else {
            ((RadioButton) mRadios[0].getChildAt(0)).setChecked(true);
        }

        if (mPrayTimes.isMaghribTimeInMins()) {
            ((RadioButton) mRadios[5].getChildAt(1)).setChecked(true);
        } else {
            ((RadioButton) mRadios[5].getChildAt(0)).setChecked(true);
        }

        if (mPrayTimes.isIshaTimeInMins()) {
            ((RadioButton) mRadios[6].getChildAt(1)).setChecked(true);
        } else {
            ((RadioButton) mRadios[6].getChildAt(0)).setChecked(true);
        }

        mValues[0].setText(toString(mPrayTimes.getImsakValue()));
        mValues[1].setText(toString(mPrayTimes.getFajrDegrees()));
        mValues[3].setText(toString(mPrayTimes.getDhuhrMins()));
        mValues[5].setText(toString(mPrayTimes.getMaghribValue()));
        mValues[6].setText(toString(mPrayTimes.getIshaValue()));


        updateTimes();
        mFixingData = false;
    }

    private void updateTimes() {
        try {
            mTimes[0].setText(mPrayTimes.getTime(Constants.TIMES_IMSAK));
            mTimes[1].setText(mPrayTimes.getTime(Constants.TIMES_FAJR));
            mTimes[2].setText(mPrayTimes.getTime(Constants.TIMES_SUNRISE));
            mTimes[3].setText(mPrayTimes.getTime(Constants.TIMES_DHUHR));
            mTimes[4].setText(mPrayTimes.getTime(Constants.TIMES_ASR));
            mTimes[5].setText(mPrayTimes.getTime(Constants.TIMES_MAGHRIB));
            mTimes[6].setText(mPrayTimes.getTime(Constants.TIMES_ISHA));
        } catch (NullPointerException ignored) {
        }
    }

    private String toString(double d) {
        if (d == (long) d)
            return String.format(Locale.getDefault(), "%d", (long) d);
        else
            return String.format(Locale.getDefault(), "%.1f", d);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (mFixingData) return;
        if (adapterView == mCalcMethod) {
            if (pos < Method.values().length) {

                mPrayTimes.setMethod(Method.values()[pos]);
                mPrayTimes.setTimezone(mTz);
                fixData();
                return;
            }
        } else if (adapterView == mAsrJur) {
            mPrayTimes.setAsrJuristic(pos + 1);
        } else if (adapterView == mHLAdj) {
            mPrayTimes.setHighLatsAdjustment(pos);
        }
        updateTimes();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        if (mFixingData) return;
        mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
        switch (radioGroup.getId()) {
            case R.id.imsakRadio:
                mPrayTimes.setImsakTime(mPrayTimes.getImsakValue(), i == R.id.imsakTimeBased);
                break;
            case R.id.maghribRadio:
                mPrayTimes.setMaghribTime(mPrayTimes.getMaghribValue(), i == R.id.maghribTimeBased);
                break;
            case R.id.ishaaRadio:
                mPrayTimes.setIshaTime(mPrayTimes.getIshaValue(), i == R.id.ishaTimeBased);
                break;
        }

        updateTimes();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (mFixingData) return;
        try {
            mPrayTimes.setImsakTime(Double.parseDouble(mValues[0].getText().toString()), mPrayTimes.isImsakTimeInMins());
            mPrayTimes.setFajrDegrees(Double.parseDouble(mValues[1].getText().toString()));
            mPrayTimes.setDhuhrMins(Double.parseDouble(mValues[3].getText().toString()));
            mPrayTimes.setMaghribTime(Double.parseDouble(mValues[5].getText().toString()), mPrayTimes.isMaghribTimeInMins());
            mPrayTimes.setIshaTime(Double.parseDouble(mValues[6].getText().toString()), mPrayTimes.isIshaTimeInMins());

            updateTimes();
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onClick(View view) {
        CalcTimes ct=new CalcTimes( UUID.asInt());
        ct.setPrayTimes(mPrayTimes);
        ct.setName(getArguments().getString("city"));
        ct.setLat(mPrayTimes.getLatitude());
        ct.setLng(mPrayTimes.getLongitude());
        ct.setElv(mPrayTimes.getElevation());

        Answers.getInstance().logCustom(new CustomEvent("AddCity")
                .putCustomAttribute("Source", Source.Calc.name())
                .putCustomAttribute("City", ct.getName())
        );
        dismiss();
    }
}
