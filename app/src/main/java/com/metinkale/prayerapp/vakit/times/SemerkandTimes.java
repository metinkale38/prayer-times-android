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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;

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

    protected Builders.Any.F[] createIonBuilder() {
        String _id = getId();
        if (_id.equals("S_2_140_0")) {
            _id = "S_2_1052_0";
            setId(_id);
        }
        final int year = LocalDate.now().getYear();
        final String[] id = _id.split("_");
        return new Builders.Any.F[]{Ion.with(App.get())
                .load("http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + year + "&" + (id.length >= 3 || "0".equals(id[3]) ? "cityID=" + id[2] : "countyID=" + id[3]))};


    }

    protected boolean parseResult(String r) {
        LocalDate date = LocalDate.now();
        List<Day> result = new Gson().fromJson(r, new TypeToken<List<Day>>() {
        }.getType());
        for (Day d : result) {
            date = date.withDayOfYear(d.Day);
            setTimes(date, new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
        }
        return result.size() > 25;
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
