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

import com.crashlytics.android.Crashlytics;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.other.Source;
import org.joda.time.LocalDate;

import java.util.List;

class SemerkandTimes extends WebTimes {

    @SuppressWarnings("unused")
    SemerkandTimes() {
        super();
    }

    SemerkandTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Semerkand;
    }

    @Override
    public void syncTimes() {
        setLastSyncTime(System.currentTimeMillis());
        final int year = LocalDate.now().getYear();
        final String[] id = getId().split("_");


        Ion.with(App.getContext())
                .load("http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + year + "&" + ("0".equals(id[3]) ? "cityID=" + id[2] : "countyID=" + id[3]))
                .setTimeout(3000)
                .as(new TypeToken<List<Day>>() {
                })
                .setCallback(new FutureCallback<List<Day>>() {
                    @Override
                    public void onCompleted(Exception e, List<Day> result) {

                        if (e != null) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            return;
                        }
                        LocalDate date = LocalDate.now();
                        for (Day d : result) {
                            date = date.withDayOfYear(d.Day);
                            setTimes(date, new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
                        }


                        Ion.with(App.getContext())
                                .load("http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + (year + 1) + "&" + ("0".equals(id[3]) ? "cityID=" + id[2] : "countyID=" + id[3]))
                                .setTimeout(3000)
                                .as(new TypeToken<List<Day>>() {
                                }).setCallback(new FutureCallback<List<Day>>() {
                            @Override
                            public void onCompleted(Exception e, List<Day> result) {
                                if (e != null) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    return;
                                }
                                LocalDate date = LocalDate.now().withYear(year + 1);
                                for (Day d : result) {
                                    date = date.withDayOfYear(d.Day);
                                    setTimes(date, new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
                                }
                            }
                        });
                    }
                });


    }

    private static class Day {
        int Day;
        String Fajr;
        String Tulu;
        String Zuhr;
        String Asr;
        String Maghrib;
        String Isha;
    }

}
