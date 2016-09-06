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
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.other.Source;
import org.joda.time.LocalDate;

class DiyanetTimes extends WebTimes {

    @SuppressWarnings("unused")
    DiyanetTimes() {
        super();
    }

    DiyanetTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Diyanet;
    }

    @Override
    public void syncTimes() {
        setLastSyncTime(System.currentTimeMillis());
        String path = getId();

        if (!path.startsWith("D_")) {
            delete();
            return;
        }
        if ("D_13_1008_0".equals(path)) {
            path = "D_13_10080_9206";
        }
        String[] a = path.split("_");


        int state = Integer.parseInt(a[2]);
        int city = 0;
        if (a.length == 4) {
            city = Integer.parseInt(a[3]);
        }

        Ion.with(App.getContext()).load("http://namazvakitleri.diyanet.gov.tr/wsNamazVakti.svc")
                .setHeader("Content-Type", "text/xml; charset=utf-8")
                .setHeader("SOAPAction", "http://tempuri.org/IwsNamazVakti/AylikNamazVakti")
                .setTimeout(3000)
                .setStringBody("<v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "<v:Header /><v:Body>" +
                        "<AylikNamazVakti xmlns=\"http://tempuri.org/\" id=\"o0\" c:root=\"1\">" +
                        "<IlceID i:type=\"d:int\">" + (city == 0 ? state : city) + "</IlceID>" +
                        "<username i:type=\"d:string\">namazuser</username>" +
                        "<password i:type=\"d:string\">NamVak!14</password>" +
                        "</AylikNamazVakti></v:Body></v:Envelope>")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {

                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        result = result.substring(result.indexOf("<a:NamazVakti>") + 14);
                        result = result.substring(0, result.indexOf("</AylikNamazVaktiResult>"));
                        String[] days = result.split("</a:NamazVakti><a:NamazVakti>");
                        for (String day : days) {
                            String[] parts = day.split("><a:");

                            String[] times = new String[6];
                            String date = null;
                            for (String part : parts) {
                                String name = part.substring(0, part.indexOf(">"));
                                name = name.substring(name.indexOf(":") + 1);
                                String content = part.substring(part.indexOf(">") + 1);
                                content = content.substring(0, content.indexOf("<"));
                                if ("Imsak".equals(name)) {
                                    times[0] = content;
                                } else if ("Gunes".equals(name)) {
                                    times[1] = content;
                                } else if ("Ogle".equals(name)) {
                                    times[2] = content;
                                } else if ("Ikindi".equals(name)) {
                                    times[3] = content;
                                } else if ("Aksam".equals(name)) {
                                    times[4] = content;
                                } else if ("Yatsi".equals(name)) {
                                    times[5] = content;
                                } else if ("MiladiTarihKisa".equals(name)) {
                                    date = content;
                                }
                            }
                            String[] d = date.split("\\.");
                            setTimes(new LocalDate(Integer.parseInt(d[2]), Integer.parseInt(d[1]), Integer.parseInt(d[0])), times);
                        }

                    }
                });


    }


}
