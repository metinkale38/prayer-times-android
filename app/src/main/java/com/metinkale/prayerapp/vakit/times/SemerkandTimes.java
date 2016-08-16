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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class SemerkandTimes extends WebTimes {
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
    public boolean syncTimes() throws Exception {
        LocalDate ldate = LocalDate.now();
        int Y = ldate.getYear();
        int i = 0;
        for (int y = Y; y <= (Y + 1); y++) {
            ldate = ldate.withYear(y);
            Gson gson = new GsonBuilder().create();
            try {

                String Url = "http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + y + "&";
                String[] id = getId().split("_");
                if ("0".equals(id[3])) {
                    Url += "cityID=" + id[2];
                } else {
                    Url += "countyID=" + id[3];
                }
                URL url = new URL(Url);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

                List<Day> resp = gson.fromJson(in, new TypeToken<List<Day>>() {
                }.getType());
                in.close();

                for (Day d : resp) {
                    ldate = ldate.withDayOfYear(d.Day);
                    setTimes(ldate, new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
                    i++;
                }

            } catch (Exception e) {
            }
        }
        return i > 0;
    }


    static class Day {
        int Day;
        String Fajr;
        String Tulu;
        String Zuhr;
        String Asr;
        String Maghrib;
        String Isha;
    }
}
