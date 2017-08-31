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
import android.util.Log;

import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.cookie.CookieMiddleware;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.vakit.times.Source;

import org.joda.time.LocalDate;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by metin on 12.02.2017.
 */
public class UAETimes extends WebTimes {
    @SuppressWarnings("unused")
    UAETimes() {
        super();
    }

    @Override
    protected boolean sync() throws ExecutionException, InterruptedException {
        Ion ion = Ion.getDefault(App.get());
        CookieMiddleware middleware = ion.getCookieMiddleware();
        middleware.clear();

        Response<String> resp = Ion.with(App.get())
                .load("https://www.awqaf.gov.ae/MonthlyPrayerTimes.aspx?lang=EN")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
                .setHeader("Referer", "https://www.awqaf.gov.ae/MonthlyPrayerTimes.aspx?lang=EN")
                .setHeader("Accept-Language", "de-DE,de;q=0.8,tr;q=0.6,en-US;q=0.4,en;q=0.2")
                .asString()
                .withResponse()
                .get();


        String asp = resp.getResult();
        asp = asp.substring(asp.indexOf("<form"));
        asp = asp.substring(0, asp.indexOf("form>"));

        HashMap<String, List<String>> params = new HashMap<>();

        while (asp.contains("<input")) {
            asp = asp.substring(asp.indexOf("<input") + 1);
            String input = asp.substring(0, asp.indexOf("/>"));
            String name = input.substring(input.indexOf("name=") + 6);
            name = name.substring(0, name.indexOf("\""));


            String value = input.substring(input.indexOf("value=") + 7);
            value = value.substring(0, value.indexOf("\""));

            if (!input.contains("value="))
                value = "";

            Log.e("FORM", name + " = " + value);
            params.put(name, Collections.singletonList(value));
        }


        params.put("ctl00$Contents$MonthlyPrayerTimes1$ddlMonths", Collections.singletonList(new HicriDate(LocalDate.now()).Month + ""));
        params.put("ctl00$Contents$MonthlyPrayerTimes1$ddlCities", Collections.singletonList("Abu Dhabi"));
        params.put("ctl00$Contents$Rating1$btnPostComment", Collections.singletonList(""));
        params.put("__EVENTTARGET",
                Collections.singletonList("ctl00$Contents$MonthlyPrayerTimes1$ddlMonths"));

        resp = Ion.with(App.get())
                .load("POST", "https://www.awqaf.gov.ae/MonthlyPrayerTimes.aspx?lang=EN")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
                .setHeader("Referer", "https://www.awqaf.gov.ae/MonthlyPrayerTimes.aspx?lang=EN")
                .setHeader("Accept-Language", "de-DE,de;q=0.8,tr;q=0.6,en-US;q=0.4,en;q=0.2")
                .setHeader("Upgrade-Insecure-Requests","1")
                .setHeader("Cache-Control","max-age=0")
                .setLogging("FORM", Log.ERROR)
                .setBodyParameters(params)
                .asString()
                .withResponse()
                .get();

        String result = resp.getResult();

        result = result.substring(result.indexOf("Contents_MonthlyPrayerTimes1_GridView1"));
        result = result.substring(result.indexOf("<td"));
        result = result.substring(result.lastIndexOf("</tr>"));

        int i = 0;
        while (result.contains("\t<td>")) {
            result = result.substring(result.indexOf("\t<td>") + 1);
            String date = result.substring(result.indexOf("</td><td>") + 9);
            String imsak = result.substring(result.indexOf("</td><td>") + 9);
            String fajr = imsak.substring(imsak.indexOf("</td><td>") + 9);
            String shruuq = fajr.substring(fajr.indexOf("</td><td>") + 9);
            String zuhr = shruuq.substring(shruuq.indexOf("</td><td>") + 9);
            String asr = zuhr.substring(zuhr.indexOf("</td><td>") + 9);
            String magrib = asr.substring(asr.indexOf("</td><td>") + 9);
            String isha = magrib.substring(magrib.indexOf("</td><td>") + 9);

            i++;
            Log.e("UAE", date);
        }

        return i > 20;
    }

    public UAETimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.UAE;
    }


    private String extract(@NonNull String s) {
        return s.substring(s.indexOf(">") + 1, s.indexOf("<"))
                .replace("\n", "").replace(" ", "").replace("\t", "");
    }
}
