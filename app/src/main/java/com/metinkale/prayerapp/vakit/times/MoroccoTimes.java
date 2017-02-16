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
import com.metinkale.prayerapp.settings.Prefs;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

class MoroccoTimes extends WebTimes {

    @SuppressWarnings("unused")
    MoroccoTimes() {
        super();
    }

    MoroccoTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Morocco;
    }

    @Override
    public synchronized String getName() {
        String name = super.getName();
        if (name.contains("(") && name.contains(")")) {
            if ("ar".equals(Prefs.getLanguage())) {
                return name.substring(name.indexOf("(") + 1, name.indexOf(")"));
            } else {
                return name.substring(0, name.indexOf(" ("));
            }
        }
        return name;
    }

    protected Builders.Any.F[] createIonBuilder() {
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
            queue.add(Ion.with(App.get())
                    .load("http://www.habous.gov.ma/prieres/defaultmois.php?ville=" + getId().substring(2) + "&mois=" + M)
                    .setTimeout(3000)
            );
        }


        return queue.toArray(new Builders.Any.F[queue.size()]);
    }

    @Override
    public synchronized String getId() {
        String id = super.getId();
        if (!id.contains("H_")) {
            setId("H_" + id);
            return "H_" + id;
        }
        return id;
    }

    protected boolean parseResult(String result) {
        String temp = result.substring(result.indexOf("colspan=\"4\" class=\"cournt\""));
        temp = temp.substring(temp.indexOf(">") + 1);
        temp = temp.substring(0, temp.indexOf("<")).replace(" ", "");
        int month = Integer.parseInt(temp.substring(0, temp.indexOf("/")));
        int year = Integer.parseInt(temp.substring(temp.indexOf("/") + 1));
        result = result.substring(result.indexOf("<td>") + 4);
        result = result.replace(" ", "").replace("\t", "").replace("\n", "").replace("\r", "");
        String[] zeiten = result.split("<td>");
        int x = 0;
        for (int i = 0; i < zeiten.length; i++) {
            int day = Integer.parseInt(extract(zeiten[i]));
            String imsak = extract(zeiten[++i]);
            String gunes = extract(zeiten[++i]);
            String ogle = extract(zeiten[++i]);
            String ikindi = extract(zeiten[++i]);
            String aksam = extract(zeiten[++i]);
            String yatsi = extract(zeiten[++i]);

            setTimes(new LocalDate(year, month, day), new String[]{imsak, gunes, ogle, ikindi, aksam, yatsi});
            x++;
        }


        return x > 25;
    }

    private String extract(String s) {
        return s.substring(0, s.indexOf("<"));
    }
}