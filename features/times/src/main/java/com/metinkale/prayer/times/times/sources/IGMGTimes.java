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
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;

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

        LocalDate ldate = LocalDate.now();
        int rY = ldate.getYear();
        int Y = rY;
        int m = ldate.getMonthOfYear();

        int i = 0;
        for (int M = m; (M <= (m + 2)) && (rY == Y); M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            String result = Ion.with(App.get())
                    .load("POST", "https://www.igmg.org/wp-content/themes/igmg/include/gebetskalender_ajax_api.php")
                    .userAgent(App.getUserAgent())
                    .setTimeout(3000)
                    .setBodyParameter("show_ajax_variable", "" + id)
                    .setBodyParameter("show_month", "" + (M - 1))
                    .asString()
                    .get();

            result = result.substring(result.indexOf("<div class='zeiten'>") + 20);
            String[] zeiten = result.split("</div><div class='zeiten'>");
            for (String zeit : zeiten) {
                if (zeit.contains("turkish")) {
                    continue;
                }
                String tarih = extractLine(zeit.substring(zeit.indexOf("tarih")));
                String imsak = extractLine(zeit.substring(zeit.indexOf("imsak")));
                String gunes = extractLine(zeit.substring(zeit.indexOf("gunes")));
                String ogle = extractLine(zeit.substring(zeit.indexOf("ogle")));
                String ikindi = extractLine(zeit.substring(zeit.indexOf("ikindi")));
                String aksam = extractLine(zeit.substring(zeit.indexOf("aksam")));
                String yatsi = extractLine(zeit.substring(zeit.indexOf("yatsi")));

                int _d = Integer.parseInt(tarih.substring(0, 2));
                int _m = Integer.parseInt(tarih.substring(3, 5));
                int _y = Integer.parseInt(tarih.substring(6, 10));
                try {
                    LocalDate localDate = new LocalDate(_y, _m, _d);
                    setTime(localDate, Vakit.FAJR, imsak);
                    setTime(localDate, Vakit.SUN, gunes);
                    setTime(localDate, Vakit.DHUHR, ogle);
                    setTime(localDate, Vakit.ASR, ikindi);
                    setTime(localDate, Vakit.MAGHRIB, aksam);
                    setTime(localDate, Vakit.ISHAA, yatsi);
                    i++;
                } catch (IllegalFieldValueException ignore) {
                }
            }


        }


        return i > 25;
    }

}