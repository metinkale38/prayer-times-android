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

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.Source;

import org.joda.time.LocalDate;

import java.util.List;

public class SemerkandTimes extends WebTimes {

    @SuppressWarnings("unused")
    SemerkandTimes() {
        super();
    }

    SemerkandTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Semerkand;
    }

    @NonNull
    protected Builders.Any.F[] createIonBuilder() {
        String _id = getId();

        final int year = LocalDate.now().getYear();
        final String[] ids = _id.split("_");
        if (ids.length != 2) {
            clearTimes();
            setIssue(App.get().getString(R.string.pleaseDeleteAndReadd));
            save();
            return new Builders.Any.F[0];
        }
        char type = ids[1].charAt(0);
        String id = ids[1].substring(1);
        return new Builders.Any.F[]{Ion.with(App.get())
                .load("http://semerkandtakvimi.semerkandmobile.com/salaattimes?year=" + year + "&"
                + (type == 'c' ? "cityId=" : "districtId=") + id)};


    }

    protected boolean parseResult(String r) {
        LocalDate date = LocalDate.now();
        List<Day> result = new Gson().fromJson(r, new TypeToken<List<Day>>() {
        }.getType());
        for (Day d : result) {
            date = date.withDayOfYear(d.DayOfYear);
            setTimes(date, new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
        }
        return result.size() > 25;
    }


    private static class Day {
        int DayOfYear;
        String Fajr;
        String Tulu;
        String Zuhr;
        String Asr;
        String Maghrib;
        String Isha;
    }

}
