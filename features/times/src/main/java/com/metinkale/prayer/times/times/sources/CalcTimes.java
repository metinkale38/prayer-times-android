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

package com.metinkale.prayer.times.times.sources;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.fragments.calctime.CalcTimeConfDialogFragment;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.UUID;

import org.joda.time.LocalDate;
import org.metinkale.praytimes.HighLatsAdjustment;
import org.metinkale.praytimes.Method;
import org.metinkale.praytimes.PrayTimes;

import java.util.TimeZone;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
public class CalcTimes extends Times {

    private String method;
    private String adjMethod;
    private String juristic;
    private double[] customMethodParams = new double[6];
    private PrayTimes prayTimes;
    private AsrType asrType = AsrType.Shafi;

    public void setTimezone(TimeZone timeZone) {
        prayTimes.setTimezone(timeZone);
    }

    public enum AsrType {
        Shafi,
        Hanafi,
        Both
    }


    @SuppressWarnings({"unused"})
    public CalcTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public CalcTimes(long id) {
        super(id);
    }


    public static CalcTimes buildTemporaryTimes(String name, double lat, double lng, long id) {
        return buildTemporaryTimes(name, lat, lng, 0, id);
    }

    public boolean isTemporary() {
        return getID() < 0;
    }

    public void createFromTemporary() {
        if (!isTemporary())
            throw new RuntimeException("cannot call createFromTemporary() on non-temporary CalcTime");

        setID(UUID.asInt());
        save();
    }


    public static CalcTimes buildTemporaryTimes(String name, double lat, double lng, double elv, long id) {
        if (id > 0)
            throw new IllegalArgumentException("can not create Temporary Times with positive id!");
        CalcTimes t = new CalcTimes(id);
        t.setSource(Source.Calc);
        if (lat > 48)
            t.getPrayTimes().setHighLatsAdjustment(HighLatsAdjustment.AngleBased);
        t.setName(name);
        t.getPrayTimes().setCoordinates(lat, lng, elv);
        return t;
    }


    @NonNull
    public PrayTimes getPrayTimes() {
        if (prayTimes == null) {
            prayTimes = new PrayTimes();
            prayTimes.setCoordinates(getLat(), getLng(), 0);
            if (method == null) {
                Ion.with(App.get()).load("http://api.geonames.org/timezoneJSON?lat=" + getLat() + "&lng=" + getLng() + "&username=metnkale38")
                        .asJsonObject().setCallback((e, result) -> {
                    if (e != null)
                        Crashlytics.logException(e);
                    if (result != null)
                        try {
                            if (result.has("timezoneId"))
                                prayTimes.setTimezone(TimeZone.getTimeZone(result.get("timezoneId").getAsString()));
                        } catch (Exception ee) {
                            Crashlytics.logException(ee);
                        }
                });

                Ion.with(App.get()).load("http://api.geonames.org/gtopo30?lat=" + getLat() + "&lng=" + getLng() + "&username=metnkale38").asString()
                        .setCallback((e, result) -> {
                            if (e != null)
                                Crashlytics.logException(e);
                            try {
                                double m = Double.parseDouble(result);
                                if (m < -9000)
                                    m = 0;
                                prayTimes.setCoordinates(getLat(), getLng(), m);
                            } catch (Exception ee) {
                                Crashlytics.logException(ee);
                            }
                        });
            }

            if (method != null && !"Custom".equals(method)) {
                prayTimes.setMethod(Method.valueOf(method));
            } else {
                prayTimes.tuneFajr(customMethodParams[0], 0);
                prayTimes.tuneImsak(customMethodParams[0], 0);
                if (customMethodParams[3] != 1) {
                    prayTimes.tuneIshaa(customMethodParams[4], 0);
                } else {
                    prayTimes.tuneIshaa(0, (int) customMethodParams[4]);
                }
            }

            if ("Hanafi".equals(juristic))
                asrType = AsrType.Hanafi;
            else if ("Shafii".equals(juristic))
                asrType = AsrType.Shafi;

            if (adjMethod != null)
                switch (adjMethod) {
                    case "AngleBased":
                        prayTimes.setHighLatsAdjustment(HighLatsAdjustment.AngleBased);
                        break;
                    case "OneSeventh":
                        prayTimes.setHighLatsAdjustment(HighLatsAdjustment.OneSeventh);
                        break;
                    case "MidNight":
                        prayTimes.setHighLatsAdjustment(HighLatsAdjustment.NightMiddle);
                        break;
                }
            method = null;
            juristic = null;
            adjMethod = null;
            customMethodParams = null;
        }
        return prayTimes;
    }

    @SuppressLint("InflateParams")
    public static void add(@NonNull final FragmentActivity c, @NonNull final Bundle bdl) {
        CalcTimeConfDialogFragment frag = new CalcTimeConfDialogFragment();
        frag.setArguments(bdl);
        frag.show(c.getSupportFragmentManager(), "dlg");

    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Calc;
    }

    @Override
    protected String getStrTime(@NonNull LocalDate date, Vakit time) {
        getPrayTimes().setDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        switch (time) {
            case FAJR:
                return getPrayTimes().getTime(org.metinkale.praytimes.Times.Imsak);
            case SUN:
                return getPrayTimes().getTime(org.metinkale.praytimes.Times.Sunrise);
            case DHUHR:
                return getPrayTimes().getTime(org.metinkale.praytimes.Times.Dhuhr);
            case ASR:
                return getPrayTimes()
                        .getTime(asrType == AsrType.Hanafi ? org.metinkale.praytimes.Times.AsrHanafi : org.metinkale.praytimes.Times.AsrShafi);
            case MAGHRIB:
                return getPrayTimes().getTime(org.metinkale.praytimes.Times.Maghrib);
            case ISHAA:
                return getPrayTimes().getTime(org.metinkale.praytimes.Times.Ishaa);
        }
        return null;
    }

    @Override
    public String getSabah(LocalDate date) {
        String imsak = getPrayTimes().getTime(org.metinkale.praytimes.Times.Imsak);
        String fajr = getPrayTimes().getTime(org.metinkale.praytimes.Times.Fajr);
        if (!imsak.equals(fajr)) {
            return fajr;
        }
        return null;
    }

    @Override
    public String getAsrThani(LocalDate date) {
        if (asrType == AsrType.Both) {
            return getPrayTimes().getTime(org.metinkale.praytimes.Times.AsrHanafi);
        }
        return null;
    }

    public void setAsrType(AsrType type) {
        asrType = type;
        save();
    }

    public AsrType getAsrType() {
        return asrType;
    }

    public void setPrayTimes(PrayTimes prayTimes) {
        this.prayTimes = prayTimes;
        save();
    }

}
