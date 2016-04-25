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
    }

    SemerkandTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Semerkand;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        LocalDate ldate = LocalDate.now();
        int Y = ldate.getYear();
        int i = 0;
        for (int y = Y; y <= Y + 1; y++) {
            ldate = ldate.withYear(y);
            Gson gson = new GsonBuilder().create();
            try {

                String Url = "http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + y + "&";
                String[] id = getId().split("_");
                if (!"0".equals(id[3])) {
                    Url += "countyID=" + id[3];
                } else {
                    Url += "cityID=" + id[2];
                }
                URL url = new URL(Url);
                URLConnection connection = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

                List<Day> resp = gson.fromJson(in, new TypeToken<List<Day>>() {
                }.getType());
                in.close();

                for (Day d : resp) {
                    ldate = ldate.withDayOfWeek(d.Day);
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
        String Fajr, Tulu, Zuhr, Asr, Maghrib, Isha;
    }
}
