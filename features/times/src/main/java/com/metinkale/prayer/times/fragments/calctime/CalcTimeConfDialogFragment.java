/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.fragments.calctime;

import android.content.DialogInterface;
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
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.times.times.sources.CalcTimes;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.UUID;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.HighLatsAdjustment;
import org.metinkale.praytimes.Method;
import org.metinkale.praytimes.PrayTimes;

import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by metin on 10.10.2017.
 */

public class CalcTimeConfDialogFragment extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Spinner mCalcMethod, mHLAdj;
    private Method mMethod = Method.MWL;
    private PrayTimes mPrayTimes = new PrayTimes();
    private CalcTimes mCalcTime;
    private TimeZone mTz;
    private View mView;

    public static CalcTimeConfDialogFragment forCity(CalcTimes calc) {
        Bundle bdl = new Bundle();
        bdl.putLong("cityId", calc.getID());
        CalcTimeConfDialogFragment frag = new CalcTimeConfDialogFragment();
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Times.clearTemporaryTimes();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.calcmethod_dialog, container, false);


        mCalcMethod = mView.findViewById(R.id.calcMethod);
        mHLAdj = mView.findViewById(R.id.highLatsSpinner);


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
                    .setCallback((e, result) -> {
                        if (e != null)
                            Crashlytics.logException(e);
                        if (result != null)
                            try {
                                TimeZone tz = TimeZone.getTimeZone(result.get("timezoneId").getAsString());
                                if (tz != null) {
                                    mTz = tz;
                                    mPrayTimes.setTimezone(mTz);
                                }
                            } catch (Exception ee) {
                                Crashlytics.logException(ee);
                            }
                    });

            if (elv == 0)
                Ion.with(App.get()).load("http://api.geonames.org/gtopo30?lat=" + lat + "&lng=" + lng + "&username=metnkale38").asString()
                        .setCallback((e, result) -> {
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
                        });
        }


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
                return getDropDownView(pos, convertView, parent);
            }

            @Override
            public int getCount() {
                return 8;
            }
        });


        mCalcMethod.setOnItemSelectedListener(this);
        mHLAdj.setOnItemSelectedListener(this);


        mCalcMethod.setSelection(0);


        mView.findViewById(R.id.fajrEdit).setOnClickListener(v -> {
            mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
            CustomTimeAdjustDialogFragment.create(mCalcTime, Vakit.FAJR, mPrayTimes.getFajrAngle(), mPrayTimes.getFajrMinuteAdjust()).show(getChildFragmentManager(), "");
        });

        mView.findViewById(R.id.sunEdit).setOnClickListener(v -> {
            mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
            CustomTimeAdjustDialogFragment.create(mCalcTime, Vakit.SUN, 0, mPrayTimes.getSunriseMinuteAdjust()).show(getChildFragmentManager(), "");
        });

        mView.findViewById(R.id.asrEdit).setOnClickListener(v -> {
            mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
            CustomTimeAdjustDialogFragment.create(mCalcTime, Vakit.ASR, 0, mPrayTimes.getAsrShafiMinuteAdjust()).show(getChildFragmentManager(), "");
        });

        mView.findViewById(R.id.maghribEdit).setOnClickListener(v -> {
            mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
            CustomTimeAdjustDialogFragment.create(mCalcTime, Vakit.MAGHRIB, mPrayTimes.getMaghribAngle(), mPrayTimes.getMaghribMinuteAdjust()).show(getChildFragmentManager(), "");
        });

        mView.findViewById(R.id.ishaaEdit).setOnClickListener(v -> {
            mCalcMethod.setSelection(mCalcMethod.getAdapter().getCount() - 1);
            CustomTimeAdjustDialogFragment.create(mCalcTime, Vakit.ISHAA, mPrayTimes.getImsakAngle(), mPrayTimes.getIshaaMinuteAdjust()).show(getChildFragmentManager(), "");
        });

        mView.findViewById(R.id.save).setOnClickListener(v -> {
            mCalcTime.createFromTemporary();
            dismiss();
        });

        updateTimes();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTimes();
    }

    void updateTimes() {
        LocalDate today = LocalDate.now();
        ((TextView) mView.findViewById(R.id.fajr)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.FAJR.ordinal()).toLocalTime()));
        ((TextView) mView.findViewById(R.id.sun)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.SUN.ordinal()).toLocalTime()));
        ((TextView) mView.findViewById(R.id.dhuhr)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.DHUHR.ordinal()).toLocalTime()));
        ((TextView) mView.findViewById(R.id.asr)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.ASR.ordinal()).toLocalTime()));
        ((TextView) mView.findViewById(R.id.maghrib)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.MAGHRIB.ordinal()).toLocalTime()));
        ((TextView) mView.findViewById(R.id.ishaa)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.ISHAA.ordinal()).toLocalTime()));

        if (mCalcTime.getAsrType() == CalcTimes.AsrType.Both) {
            ((TextView) mView.findViewById(R.id.asr)).setText(LocaleUtils.formatTime(mCalcTime.getTime(today, Vakit.ASR.ordinal()).toLocalTime()) + " / " + LocaleUtils.formatTime(mCalcTime.getAsrThaniTime(today).toLocalTime()));
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (adapterView == mCalcMethod) {
            if (pos < Method.values().length) {
                mMethod = Method.values()[pos];
                mPrayTimes.setMethod(mMethod);
                mPrayTimes.setTimezone(mTz);
                updateTimes();
            }
        } else if (adapterView == mHLAdj) {
            mPrayTimes.setHighLatsAdjustment(HighLatsAdjustment.values()[pos]);
            updateTimes();

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
