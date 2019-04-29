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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by metin on 12.02.2017.
 */
public class IndonesiaTimes extends WebTimes {
    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Indonesia;
    }


    protected boolean sync() throws ExecutionException, InterruptedException {
        LocalDate date = LocalDate.now();

        int i = 0;
        i += syncDate(date);
        date = date.plusMonths(1);
        i += syncDate(date);
        return i > 25;
    }

    private int syncDate(LocalDate date) throws ExecutionException, InterruptedException {
        JsonObject result = Ion.with(App.get())
                .load("http://sihat.kemenag.go.id/site/get_waktu_sholat")
                .userAgent(App.getUserAgent())
                .setHeader("Referer", "http://sihat.kemenag.go.id/waktu-sholat")
                .setBodyParameter("tahun", date.getYear() + "")
                .setBodyParameter("bulan", date.getMonthOfYear() + "")
                .setBodyParameter("h", "0")
                .setBodyParameter("lokasi", getId())
                .asJsonObject()
                .get();

        if (!"Success".equals(result.get("message").getAsString())) return 0;
        result = result.getAsJsonObject("data");
        int i = 0;
        for (Map.Entry<String, JsonElement> entry : result.entrySet()) {
            LocalDate ld = LocalDate.parse(entry.getKey());
            JsonObject day = entry.getValue().getAsJsonObject();
            setTime(ld, Vakit.FAJR, day.get("imsak").getAsString());
            setTime(ld, Vakit.SUN, day.get("terbit").getAsString());
            setTime(ld, Vakit.DHUHR, day.get("dzuhur").getAsString());
            setTime(ld, Vakit.ASR, day.get("ashar").getAsString());
            setTime(ld, Vakit.MAGHRIB, day.get("maghrib").getAsString());
            setTime(ld, Vakit.ISHAA, day.get("isya").getAsString());
            setSabah(ld, day.get("subuh").getAsString());
            i++;


        }

        return i;
    }


}
