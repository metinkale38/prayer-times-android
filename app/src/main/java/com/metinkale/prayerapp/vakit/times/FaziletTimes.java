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

import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FaziletTimes extends WebTimes {
    FaziletTimes() {
    }

    FaziletTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Fazilet;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        String[] a = getId().split("_");

        if (a.length < 4) {
            List<Cities.Item> items = Cities.get().search(getName());
            for (Cities.Item i : items) {
                if ((i.source == getSource()) && i.city.equals(getName())) {
                    setId(i.id);
                }
            }
        }

        int country = Integer.parseInt(a[1]);
        int state = Integer.parseInt(a[2]);
        int city = Integer.parseInt(a[3]);

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

        LocalDate ldate = LocalDate.now();
        int Y = ldate.getYear();
        int M = ldate.getMonthOfYear();


        URL url = new URL("http://www.fazilettakvimi.com/tr/namaz_vakitleri.html");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ulke_id", country);
        params.put("sehir_id", state);
        params.put("ilce_id", city);
        params.put("baslangic_tarihi", Y + "-" + az(M) + "-01");
        params.put("bitis_tarihi", (Y + 5) + "-12-31");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        OutputStream writer = conn.getOutputStream();
        writer.write(postDataBytes);

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        int i = 0;
        while ((line = reader.readLine()) != null) {
            if (line.contains("<tr class=\"acik\">") || line.contains("<tr class=\"koyu\">")) {
                String date = extractLine(reader.readLine());
                String[] dd = date.split(" ");
                int d = Integer.parseInt(dd[0]);
                int m = ay.indexOf(dd[1]) + 1;
                int y = Integer.parseInt(dd[2]);
                String[] times = new String[6];

                times[0] = extractLine(reader.readLine());//2
                reader.readLine();//3
                times[1] = extractLine(reader.readLine());//4
                reader.readLine();//5
                times[2] = extractLine(reader.readLine());//6
                times[3] = extractLine(reader.readLine());//7
                reader.readLine();//8
                times[4] = extractLine(reader.readLine());//9
                times[5] = extractLine(reader.readLine());//10
                setTimes(new LocalDate(y, m, d), times);

            }

        }
        return true;

    }
}