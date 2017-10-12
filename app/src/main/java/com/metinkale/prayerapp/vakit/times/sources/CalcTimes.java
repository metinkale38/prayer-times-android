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
import android.support.v4.app.FragmentActivity;
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
import com.metinkale.prayerapp.vakit.fragments.CalcTimeConfDialogFragment;
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
    private PrayTimes prayTimes;


    @SuppressWarnings({"unused", "WeakerAccess"})
    public CalcTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public CalcTimes(long id) {
        super(id);
    }

    public static void buildTemporaryTimes(String name, double lat, double lng, long id) {
        CalcTimes t = new CalcTimes(id);
        t.setSource(Source.Calc);
        if (lat > 48)
            t.getPrayTimes().setHighLatsAdjustment(Constants.HIGHLAT_ANGLEBASED);
        t.setName(name);
        t.getPrayTimes().setCoordinates(lat, lng, 0);
    }


    public PrayTimes getPrayTimes() {
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
    public static void add(@NonNull final FragmentActivity c, @NonNull final Bundle bdl) {
        CalcTimeConfDialogFragment frag = new CalcTimeConfDialogFragment();
        frag.setArguments(bdl);
        frag.show(c.getSupportFragmentManager(), "dlg");

        //  t.setName(bdl.getString("city"));
        // t.setLat(bdl.getDouble("lat"));
        //t.setLng(bdl.getDouble("lng"));
        //t.setAutoLocation(bdl.getBoolean("autoCity"));

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


    public void setPrayTimes(PrayTimes prayTimes) {
        this.prayTimes = prayTimes;
        save();
    }
}
