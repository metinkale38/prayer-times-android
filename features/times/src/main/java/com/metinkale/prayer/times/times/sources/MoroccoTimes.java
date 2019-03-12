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
import com.metinkale.prayer.utils.LocaleUtils;

import org.joda.time.LocalDate;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MoroccoTimes extends WebTimes {

    @SuppressWarnings({"unused", "WeakerAccess"})
    public MoroccoTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public MoroccoTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Morocco;
    }

    @Override
    public synchronized String getName() {
        String name = super.getName();
        if (name != null && name.contains("(") && name.contains(")")) {
            if (new Locale("ar").getLanguage().equals(LocaleUtils.getLocale().getLanguage())) {
                return name.substring(name.indexOf("(") + 1, name.indexOf(")"));
            } else {
                return name.substring(0, name.indexOf(" ("));
            }
        }
        return name;
    }

    protected boolean sync() throws ExecutionException, InterruptedException {
        LocalDate ldate = LocalDate.now();
        int rY = ldate.getYear();
        int Y = rY;
        int m = ldate.getMonthOfYear();
        int x = 0;

        for (int M = m; (M <= (m + 1)) && (rY == Y); M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            String result = Ion.with(App.get())
                    .load("http://www.habous.gov.ma/prieres/defaultmois.php?ville=" + getId() + "&mois=" + M)
                    .userAgent(App.getUserAgent())
                    .setTimeout(3000)
                    .asString()
                    .get();
            String temp = result.substring(result.indexOf("colspan=\"4\" class=\"cournt\""));
            temp = temp.substring(temp.indexOf(">") + 1);
            temp = temp.substring(0, temp.indexOf("<")).replace(" ", "");
            int month = Integer.parseInt(temp.substring(0, temp.indexOf("/")));
            int year = Integer.parseInt(temp.substring(temp.indexOf("/") + 1));
            result = result.substring(result.indexOf("<td>") + 4);
            result = result.replace(" ", "").replace("\t", "").replace("\n", "").replace("\r", "");
            String[] zeiten = result.split("<td>");
            for (int i = 0; i < zeiten.length; i++) {
                int day = Integer.parseInt(extract(zeiten[i]));
                String imsak = extract(zeiten[++i]);
                String gunes = extract(zeiten[++i]);
                String ogle = extract(zeiten[++i]);
                String ikindi = extract(zeiten[++i]);
                String aksam = extract(zeiten[++i]);
                String yatsi = extract(zeiten[++i]);

                LocalDate localDate = new LocalDate(year, month, day);
                setTime(localDate, Vakit.FAJR, imsak);
                setTime(localDate, Vakit.SUN, gunes);
                setTime(localDate, Vakit.DHUHR, ogle);
                setTime(localDate, Vakit.ASR, ikindi);
                setTime(localDate, Vakit.MAGHRIB, aksam);
                setTime(localDate, Vakit.ISHAA, yatsi);
                x++;
            }


        }


        return x > 25;
    }

    private String extract(@NonNull String s) {
        return s.substring(0, s.indexOf("<"));
    }
}