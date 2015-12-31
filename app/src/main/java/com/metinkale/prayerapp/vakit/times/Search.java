package com.metinkale.prayerapp.vakit.times;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.metinkale.prayerapp.App;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Search {

    public static List<Item> search(String q) {
        String url = App.API_URL+"/api/search.php?q=" + q.replace(" ", "+");

        Gson gson = new Gson();
        List<Item> items;
        try {
            InputStream is = new URL(url).openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            items = gson.fromJson(br, new TypeToken<ArrayList<Item>>() {
            }.getType());

            is.close();
            isr.close();
            br.close();
        } catch (Exception e) {
            items = new ArrayList<>();
        }

        return items;

    }

    public static class Item {
        public String city;
        public String country;
        public String id;
        public double lat;
        public double lng;
        public Times.Source source;
    }
}
