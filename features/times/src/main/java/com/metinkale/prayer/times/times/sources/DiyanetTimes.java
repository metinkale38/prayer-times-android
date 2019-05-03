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


import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;

public class DiyanetTimes extends WebTimes {

    @SuppressWarnings({"unused", "WeakerAccess"})
    public DiyanetTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public DiyanetTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Diyanet;
    }


    protected boolean sync() throws ExecutionException, InterruptedException {
        String id = getId();
        id = id.substring(id.lastIndexOf("_") + 1); // backwarts compability

        String result = Ion.with(App.get()).load("http://namazvakti.diyanet.gov.tr/wsNamazVakti.svc").userAgent(App.getUserAgent())
                .setHeader("Content-Type", "text/xml; charset=utf-8").setHeader("SOAPAction", "http://tempuri.org/IwsNamazVakti/AylikNamazVakti")
                .setStringBody(
                        "<v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                                "<v:Header /><v:Body>" + "<AylikNamazVakti xmlns=\"http://tempuri.org/\" id=\"o0\" c:root=\"1\">" +
                                "<IlceID i:type=\"d:int\">" + id + "</IlceID>" +
                                "<username i:type=\"d:string\">namazuser</username>" + "<password i:type=\"d:string\">NamVak!14</password>" +
                                "</AylikNamazVakti></v:Body></v:Envelope>").asString().get();

        result = result.substring(result.indexOf("<a:NamazVakti>") + 14);
        result = result.substring(0, result.indexOf("</AylikNamazVaktiResult>"));
        String[] days = result.split("</a:NamazVakti><a:NamazVakti>");
        int i = 0;
        for (String day : days) {
            String[] parts = day.split("><a:");

            String[] times = new String[6];
            String date = null;
            for (String part : parts) {
                if (!part.contains(">"))
                    continue;
                String name = part.substring(0, part.indexOf('>'));
                if (name.contains(":"))
                    name = name.substring(name.indexOf(':') + 1);
                String content = part.substring(part.indexOf('>') + 1);
                content = content.substring(0, content.indexOf('<'));
                switch (name) {
                    case "Imsak":
                        times[0] = content;
                        break;
                    case "Gunes":
                        times[1] = content;
                        break;
                    case "Ogle":
                        times[2] = content;
                        break;
                    case "Ikindi":
                        times[3] = content;
                        break;
                    case "Aksam":
                        times[4] = content;
                        break;
                    case "Yatsi":
                        times[5] = content;
                        break;
                    case "MiladiTarihKisa":
                        date = content;
                        break;
                }
            }
            String[] d = date.split("\\.");
            LocalDate ld = new LocalDate(Integer.parseInt(d[2]), Integer.parseInt(d[1]), Integer.parseInt(d[0]));
            setTime(ld, Vakit.FAJR, times[0]);
            setTime(ld, Vakit.SUN, times[1]);
            setTime(ld, Vakit.DHUHR, times[2]);
            setTime(ld, Vakit.ASR, times[3]);
            setTime(ld, Vakit.MAGHRIB, times[4]);
            setTime(ld, Vakit.ISHAA, times[5]);
            i++;
        }
        return i > 25;
    }


}
