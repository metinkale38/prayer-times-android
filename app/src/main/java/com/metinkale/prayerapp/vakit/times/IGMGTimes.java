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

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.other.Source;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

class IGMGTimes extends WebTimes {

    @SuppressWarnings("unused")
    IGMGTimes() {
        super();
    }

    IGMGTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.IGMG;
    }


    protected Builders.Any.F[] createIonBuilder() {
        String path = getId().replace("nix", "-1");
        String[] a = path.split("_");
        int world = Integer.parseInt(a[1]);
        int germany = Integer.parseInt(a[2]);

        LocalDate ldate = LocalDate.now();
        int rY = ldate.getYear();
        int Y = rY;
        int m = ldate.getMonthOfYear();

        final List<Builders.Any.B> queue = new ArrayList<>();
        for (int M = m; (M <= (m + 1)) && (rY == Y); M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            queue.add(Ion.with(App.getContext())
                    .load("https://www.igmg.org/wp-content/themes/igmg/include/gebetskalender_ajax.php?show_ajax_variable=" + (germany > 0 ? germany : world) + "&show_month=" + (M - 1))
                    .setTimeout(3000)
            );
        }


        return queue.toArray(new Builders.Any.F[queue.size()]);
    }

    protected boolean parseResult(String result) {
        result = result.substring(result.indexOf("<div class='zeiten'>") + 20);
        String[] zeiten = result.split("</div><div class='zeiten'>");
        int i = 0;
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
            i++;
            setTimes(new LocalDate(_y, _m, _d), new String[]{imsak, gunes, ogle, ikindi, aksam, yatsi});
        }

        return i > 0;
    }
}