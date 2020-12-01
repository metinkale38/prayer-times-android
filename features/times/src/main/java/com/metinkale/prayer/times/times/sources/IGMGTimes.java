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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.BuildConfig;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class IGMGTimes extends WebTimes {


    // old API url shows wrong times, so we have to wipe times
    // TODO delete after some time
    private boolean fixedApiUrl = false;

    @SuppressWarnings({"unused", "WeakerAccess"})
    public IGMGTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public IGMGTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.IGMG;
    }


    @Nullable
    @Override
    protected String getStrTime(LocalDate date, Vakit time) {
        if (!fixedApiUrl) {
            clearTimes();
            fixedApiUrl = true;
        }
        return super.getStrTime(date, time);
    }

    protected boolean sync() throws ExecutionException, InterruptedException {
        String path = getId().replace("nix", "-1");
        String[] a = path.split("_");
        int id = Integer.parseInt(a[0]);
        if (id <= 0 && a.length > 1)
            id = Integer.parseInt(a[1]);

        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = from.plusYears(1);


        PrayerTimesResponse result = Ion.with(App.get())
                .load("GET", "https://live.igmgapp.org:8091/api/Calendar/GetPrayerTimes" +
                        "?cityID=" + id +
                        "&from=" + from.toString("dd.MM.yyyy") +
                        "&to=" + to.toString("dd.MM.yyyy"))
                .addHeader("X-API-Key", App.get().getString(R.string.IGMGApiKey))
                .userAgent(App.getUserAgent())
                .setTimeout(3000)
                .as(PrayerTimesResponse.class)
                .get();


        int i = 0;
        for (PrayerTimesResponse.PrayerTimesEntry entry : result.list) {
            String[] splitDate = entry.date.split("\\.");
            LocalDate localDate = new LocalDate(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[0]));
            setTime(localDate, Vakit.FAJR, entry.fajr);
            setTime(localDate, Vakit.SUN, entry.sunrise);
            setTime(localDate, Vakit.DHUHR, entry.dhuhr);
            setTime(localDate, Vakit.ASR, entry.asr);
            setTime(localDate, Vakit.MAGHRIB, entry.maghrib);
            setTime(localDate, Vakit.ISHAA, entry.ishaa);
            i++;
        }


        return i > 25;
    }


    public static class PrayerTimesResponse {
        List<PrayerTimesEntry> list;

        public static class PrayerTimesEntry {
            String date;
            String fajr;
            String sunrise;
            String dhuhr;
            String asr;
            String maghrib;
            String ishaa;
        }

    }

}