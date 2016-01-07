package com.metinkale.prayerapp.vakit.times;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;

public class SemerkandTimes extends WebTimes {

    SemerkandTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.IGMG;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        Calendar cal = Calendar.getInstance();
        int Y = cal.get(Calendar.YEAR);
        int i = 0;
        for (int y = Y; y <= Y + 1; y++) {
            cal.set(Calendar.YEAR, y);
            Gson gson = new GsonBuilder().create();
            try {

                String Url = "http://77.79.123.10/semerkandtakvimi/query/SalaatTimes?year=" + y + "&";
                String id[] = getId().split("_");
                if (!id[3].equals("0")) {
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
                    cal.set(Calendar.DAY_OF_YEAR, d.Day);
                    setTimes(cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), new String[]{d.Fajr, d.Tulu, d.Zuhr, d.Asr, d.Maghrib, d.Isha});
                    i++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i > 0;
    }


    static class Day {
        int Day;
        String Fajr, Tulu, Zuhr, Asr, Maghrib, Isha;
    }
}
