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

import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;

import org.joda.time.LocalDate;

class CSVTimes extends WebTimes {


    private boolean firstSync = true;

    @SuppressWarnings("unused")
    CSVTimes() {
        super();
    }

    CSVTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.CSV;
    }

    protected Builders.Any.F[] createIonBuilder() {
        return new Builders.Any.F[]{
                Ion.with(App.getContext())
                        .load(getId())
                        .setTimeout(3000)};
    }

    protected boolean parseResult(String result) {
        int i = 0;
        String[] lines = result.replace("\"", "").split("\n");

        FOR:
        for (String line : lines) {
            try {
                String sep1 = null, sep2 = null;
                if (line.contains(";")) sep1 = ";";
                else if (line.contains(",")) sep1 = ",";
                else if (line.contains("\t")) sep1 = "\t";
                else if (line.contains(" ")) sep1 = " ";

                if (sep1 == null) continue;
                if (!sep1.equals(" ")) line = line.replace(" ", "");
                String parts[] = line.split(sep1);

                if (parts[0].contains("-")) sep2 = "-";
                else if (parts[0].contains(".")) sep2 = ".";
                else if (parts[0].contains("_")) sep2 = ".";

                if (sep2 == null) continue;

                String date[] = parts[i].split(sep2);
                int year = Integer.parseInt(date[2]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[0]);
                for (int l = 1; l < 7; l++) {
                    if (parts[l].length() != 5 ||
                            parts[l].charAt(2) != ':' ||
                            !Character.isDigit(parts[l].charAt(0)) ||
                            !Character.isDigit(parts[l].charAt(1)) ||
                            !Character.isDigit(parts[l].charAt(3)) ||
                            !Character.isDigit(parts[l].charAt(4))) continue FOR;

                }
                setTimes(new LocalDate(Math.max(year, day), month, Math.min(year, day)),
                        new String[]{parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]});
                i++;
            } catch (Exception ignore) {
            }
        }

        if (firstSync && i == 0) {
            Toast.makeText(App.getContext(), "Invalid CSV", Toast.LENGTH_LONG).show();
            delete();
        }
        firstSync = false;
        return i > 0;
    }


}
