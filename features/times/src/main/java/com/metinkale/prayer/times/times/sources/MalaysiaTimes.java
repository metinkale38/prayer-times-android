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

package com.metinkale.prayer.times.times.sources;

import androidx.annotation.NonNull;

import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;

import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

/**
 * Created by metin on 12.02.2017.
 */
public class MalaysiaTimes extends WebTimes {
    @SuppressWarnings({"unused", "WeakerAccess"})
    public MalaysiaTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public MalaysiaTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Malaysia;
    }


    protected boolean sync() throws ExecutionException, InterruptedException {
        LocalDate ldate = LocalDate.now();
        int rY = ldate.getYear();
        int Y = rY;
        int m = ldate.getMonthOfYear();
        String[] split = getId().split("_");
        if (split.length < 2) delete();
        split[0] = URLEncoder.encode(split[0]);
        split[1] = URLEncoder.encode(split[1]);

        int x = 0;
        for (int M = m; (M <= (m + 1)) && (rY == Y); M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            String result = Ion.with(App.get())
                    .load("http://www.e-solat.gov.my/web/waktusolat.php?negeri=" + split[0] + "&state=" + split[0] + "&zone=" + split[1] + "&year="
                            + Y + "&jenis=year&bulan=" + M)
                    .setTimeout(3000)
                    .userAgent(App.getUserAgent())
                    .asString()
                    .get();
            String date = result.substring(result.indexOf("muatturun"));
            date = date.substring(date.indexOf("year=") + 5);
            int year = Integer.parseInt(date.substring(0, 4));
            date = date.substring(date.indexOf("bulan") + 6);
            int month = Integer.parseInt(date.substring(0, date.indexOf("\"")));

            result = result.substring(result.indexOf("#7C7C7C"));
            result = result.substring(0, result.indexOf("<table"));
            FOR1:
            for (result = result.substring(result.indexOf("<tr", 1));
                 result.contains("tr ");
                 result = result.substring(result.indexOf("<tr", 1))) {
                try {
                    result = result.substring(result.indexOf("<font") + 1);
                    String d = extract(result);
                    if (d.startsWith("Tarikh")) continue;
                    int day = Integer.parseInt(d.substring(0, 2));
                    result = result.substring(result.indexOf("<font") + 1);
                    result = result.substring(result.indexOf("<font") + 1);
                    String imsak = extract(result);
                    result = result.substring(result.indexOf("<font") + 1);
                    result = result.substring(result.indexOf("<font") + 1);
                    String sun = extract(result);
                    result = result.substring(result.indexOf("<font") + 1);
                    String ogle = extract(result);
                    result = result.substring(result.indexOf("<font") + 1);
                    String ikindi = extract(result);
                    result = result.substring(result.indexOf("<font") + 1);
                    String aksam = extract(result);
                    result = result.substring(result.indexOf("<font") + 1);
                    String yatsi = extract(result);

                    String[] array = new String[]{imsak, sun, ogle, ikindi, aksam, yatsi};
                    for (int i = 0; i < array.length; i++) {
                        array[i] = array[i].replace(".", ":");
                        if (array[i].length() == 4) array[i] = "0" + array[i];
                        if (array[i].length() != 5) continue FOR1;
                    }
                    LocalDate localDate = new LocalDate(year, month, day);
                    setTime(localDate, Vakit.FAJR, array[0]);
                    setTime(localDate, Vakit.SUN, array[1]);
                    setTime(localDate, Vakit.DHUHR, array[2]);
                    setTime(localDate, Vakit.ASR, array[3]);
                    setTime(localDate, Vakit.MAGHRIB, array[4]);
                    setTime(localDate, Vakit.ISHAA, array[5]);
                    x++;
                } catch (Exception ignore) {
                }
            }

        }


        return x > 25;
    }


    private String extract(@NonNull String s) {
        return s.substring(s.indexOf(">") + 1, s.indexOf("<"))
                .replace("\n", "").replace(" ", "").replace("\t", "");
    }
}
