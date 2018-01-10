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

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.Source;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class IGMGTimes extends WebTimes {

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


    @NonNull
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
                    .load("http://www.igmg.org/wp-content/themes/igmg/include/gebetskalender_ajax.php?show_ajax_variable=" + id + "&show_month=" + (M - 1))
                    .setTimeout(3000)
                    .userAgent(App.getUserAgent())
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
                    setTimes(new LocalDate(_y, _m, _d), new String[]{imsak, gunes, ogle, ikindi, aksam, yatsi});
                } catch (IllegalFieldValueException ignore) {
                    i++;
                }
            }


        }


        return i > 25;
    }

}