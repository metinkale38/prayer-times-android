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

package com.metinkale.prayer.times.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.utils.UUID;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.HighLatsAdjustment;
import org.metinkale.praytimes.Method;
import org.metinkale.praytimes.PrayTimes;

import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by metin on 10.10.2017.
 */

public class CalcTimeConfDialogFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Spinner mCalcMethod, mHLAdj;
    private PrayTimes mPrayTimes = new PrayTimes();
    private CalcTimes mCalcTime;
    private TimeZone mTz;
    private CityFragment mFragment;
    
    public static CalcTimeConfDialogFragment forCity(CalcTimes calc) {
        Bundle bdl = new Bundle();
        bdl.putLong("cityId", calc.getID());
        CalcTimeConfDialogFragment frag = new CalcTimeConfDialogFragment();
        frag.setArguments(bdl);
        return frag;
    }
    
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calcmethod_dialog, container, false);
        
        
      
        mCalcMethod = v.findViewById(R.id.calcMethod);
        mHLAdj = v.findViewById(R.id.highlatsAdj);
        
        
        Bundle bdl = getArguments();
        if (bdl == null) {
            dismiss();
            return null;
        }
        if (getArguments().containsKey("cityId")) {
            mCalcTime = (CalcTimes) CalcTimes.getTimes(getArguments().getLong("cityId"));
            mPrayTimes = mCalcTime.getPrayTimes();
            mTz = mPrayTimes.getTimeZone();
        } else {
            final double lat = bdl.getDouble("lat", 0);
            final double lng = bdl.getDouble("lng", 0);
            final double elv = bdl.getDouble("elv", 0);
            
            mCalcTime = CalcTimes.buildTemporaryTimes(getArguments().getString("city"), lat, lng, elv, -1);
            mPrayTimes = mCalcTime.getPrayTimes();
            
            mPrayTimes.setTimezone(TimeZone.getDefault());
            Ion.with(App.get()).load("http://api.geonames.org/timezoneJSON?lat=" + lat + "&lng=" + lng + "&username=metnkale38").asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null)
                                Crashlytics.logException(e);
                            if (result != null)
                                try {
                                    mTz = TimeZone.getTimeZone(result.get("timezoneId").getAsString());
                                    mPrayTimes.setTimezone(mTz);
                                } catch (Exception ee) {
                                    Crashlytics.logException(ee);
                                }
                        }
                    });
            
            if (elv == 0)
                Ion.with(App.get()).load("http://api.geonames.org/gtopo30?lat=" + lat + "&lng=" + lng + "&username=metnkale38").asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null)
                                    Crashlytics.logException(e);
                                try {
                                    double m = Double.parseDouble(result);
                                    if (m < -9000)
                                        m = 0;
                                    mPrayTimes.setCoordinates(lat, lng, m);
                                } catch (Exception ee) {
                                    Crashlytics.logException(ee);
                                }
                            }
                        });
        }
        
        mFragment = new CityFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("city", mCalcTime.getID());
        mFragment.setArguments(bdl);
        getChildFragmentManager().beginTransaction().replace(R.id.fragment, mFragment).commit();
    
    
    
        LocalDate date = LocalDate.now();
        mPrayTimes.setDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        mHLAdj.setAdapter(
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.adjMethod)));
        
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
        mHLAdj.setOnItemSelectedListener(this);
        
        
        mCalcMethod.setSelection(0);
        
        //  v.findViewById(R.id.ok).setOnClickListener(this);
        return v;
    }
    
    
    private String toString(double d) {
        if (d == (long) d)
            return String.format(Locale.getDefault(), "%d", (long) d);
        else
            return String.format(Locale.getDefault(), "%.1f", d);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (adapterView == mCalcMethod) {
            if (pos < Method.values().length) {
                
                mPrayTimes.setMethod(Method.values()[pos]);
                mPrayTimes.setTimezone(mTz);
                return;
            }
        } else if (adapterView == mHLAdj) {
            mPrayTimes.setHighLatsAdjustment(HighLatsAdjustment.values()[pos]);
        }
        
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    
    }
    
    
    @Override
    public void onClick(View view) {
        if (mCalcTime == null) {
            mCalcTime = new CalcTimes(UUID.asInt());
            mCalcTime.setPrayTimes(mPrayTimes);
            mCalcTime.setName(getArguments().getString("city"));
            mCalcTime.setAutoLocation(getArguments().getBoolean("autoCity"));
            mCalcTime.setLat(mPrayTimes.getLatitude());
            mCalcTime.setLng(mPrayTimes.getLongitude());
            mCalcTime.setElv(mPrayTimes.getElevation());
            Answers.getInstance().logCustom(
                    new CustomEvent("AddCity").putCustomAttribute("Source", Source.Calc.name()).putCustomAttribute("City", mCalcTime.getName()));
        } else {
            mCalcTime.setPrayTimes(mPrayTimes);
        }
        dismiss();
    }
}
