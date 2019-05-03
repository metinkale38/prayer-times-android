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

import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;

import java.util.concurrent.ExecutionException;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

public class CSVTimes extends WebTimes {


    private boolean firstSync = true;


    @SuppressWarnings({"unused", "WeakerAccess"})
    public CSVTimes(long id) {
        super(id);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public CSVTimes() {
        super();
    }

    @Override
    boolean sync() throws ExecutionException, InterruptedException {
        String result = Ion.with(App.get()).load(getId()).setTimeout(3000).asString().get();

        int i = 0;
        String[] lines = result.replace("\"", "").replace("\r", "").split("\n");

        FOR:
        for (String line : lines) {
            try {
                String sep1 = null, sep2 = null, sep3 = null;
                if (line.contains(";"))
                    sep1 = ";";
                else if (line.contains(","))
                    sep1 = ",";
                else if (line.contains("\t"))
                    sep1 = "\t";
                else if (line.contains(" "))
                    sep1 = " ";

                if (sep1 == null)
                    continue;
                if (!sep1.equals(" "))
                    line = line.replace(" ", "");
                String parts[] = line.split(sep1);

                if (parts[0].contains("-"))
                    sep2 = "-";
                else if (parts[0].contains("."))
                    sep2 = "\\.";
                else if (parts[0].contains("_"))
                    sep2 = "_";

                if (sep2 == null)
                    continue;

                String date[] = parts[0].split(sep2);
                int year = date.length > 2 ? Integer.parseInt(date[2]) : LocalDate.now().getYear();
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[0]);

                if (parts[1].contains(":"))
                    sep3 = ":";
                else if (parts[1].contains("-"))
                    sep3 = "-";
                else if (parts[1].contains("."))
                    sep3 = "\\.";
                else if (parts[1].contains("_"))
                    sep3 = "_";


                for (int l = 1; l < 7; l++) {
                    parts[i] = parts[i].replace(sep3, ":");
                    if (parts[i].length() == 4)
                        parts[i] = "0" + parts[i];

                    if (parts[l].length() != 5 || parts[l].charAt(2) != ':' || !Character.isDigit(parts[l].charAt(0)) ||
                            !Character.isDigit(parts[l].charAt(1)) || !Character.isDigit(parts[l].charAt(3)) ||
                            !Character.isDigit(parts[l].charAt(4)))
                        continue FOR;

                }

                LocalDate ld = new LocalDate(Math.max(year, day), month, Math.min(year, day));
                setTime(ld, Vakit.FAJR, parts[1]);
                setTime(ld, Vakit.SUN, parts[2]);
                setTime(ld, Vakit.DHUHR, parts[3]);
                setTime(ld, Vakit.ASR, parts[4]);
                setTime(ld, Vakit.MAGHRIB, parts[5]);
                setTime(ld, Vakit.ISHAA, parts[6]);

                i++;
            } catch (Exception ignore) {
            }
        }

        if (firstSync && i == 0) {
            Toast.makeText(App.get(), "Invalid CSV", Toast.LENGTH_LONG).show();
            delete();
        }
        firstSync = false;
        return i > 0;
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.CSV;
    }
}
