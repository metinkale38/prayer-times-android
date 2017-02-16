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
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static com.metinkale.prayerapp.Utils.az;

class FaziletTimes extends WebTimes {

    @SuppressWarnings("unused")
    FaziletTimes() {
        super();
    }

    FaziletTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Fazilet;
    }


    protected Builders.Any.F[] createIonBuilder() {
        String[] a = getId().split("_");

        if (a.length < 4) {
            Crashlytics.setString("FAZILET_ID", getId());
            Crashlytics.logException(new Exception("INVALID ID IN FAZILET"));
            this.delete();
            return new Builders.Any.F[0];
        }
        int country = Integer.parseInt(a[1]);
        int state = Integer.parseInt(a[2]);
        int city = Integer.parseInt(a[3]);


        LocalDate ldate = LocalDate.now();
        int Y = ldate.getYear();
        int M = ldate.getMonthOfYear();


        return new Builders.Any.F[]{
                Ion.with(App.get())
                        .load("http://www.fazilettakvimi.com/tr/namaz_vakitleri.html")
                        .setTimeout(3000)
                        .setBodyParameter("ulke_id", "" + country)
                        .setBodyParameter("sehir_id", "" + state)
                        .setBodyParameter("ilce_id", "" + city)
                        .setBodyParameter("baslangic_tarihi", Y + "-" + az(M) + "-01")
                        .setBodyParameter("bitis_tarihi", (Y + 5) + "-12-31")

        };
    }

    protected boolean parseResult(String result) {
        List<String> ay = new ArrayList<>();
        ay.add("Ocak");
        ay.add("Şubat");
        ay.add("Mart");
        ay.add("Nisan");
        ay.add("Mayıs");
        ay.add("Haziran");
        ay.add("Temmuz");
        ay.add("Ağustos");
        ay.add("Eylül");
        ay.add("Ekim");
        ay.add("Kasım");
        ay.add("Aralık");

        int c = 0;
        String lines[] = result.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("<tr class=\"acik\">") || line.contains("<tr class=\"koyu\">")) {
                String date = extractLine(lines[++i]);
                String[] dd = date.split(" ");
                int d = Integer.parseInt(dd[0]);
                int m = ay.indexOf(dd[1]) + 1;
                int y = Integer.parseInt(dd[2]);
                String[] times = new String[6];

                times[0] = extractLine(lines[++i]);//2
                i++;//3
                times[1] = extractLine(lines[++i]);//4
                i++;//5
                times[2] = extractLine(lines[++i]);//6
                times[3] = extractLine(lines[++i]);//7
                i++;//8
                times[4] = extractLine(lines[++i]);//9
                times[5] = extractLine(lines[++i]);//10
                setTimes(new LocalDate(y, m, d), times);
                c++;

            }
        }

        return c > 25;
    }


}