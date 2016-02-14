package com.metinkale.prayerapp.custom;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Geocoder
{

    public static List<Response> from(String address, int limit, String lang)
    {
        if(address.contains(";"))
        {
            String a[] = address.split(";");
            try
            {
                List<Response> resp = new ArrayList<>();
                resp.add(from(Double.parseDouble(a[0]), Double.parseDouble(a[1]), limit, lang));
                return resp;
            } catch(Exception ignore)
            {
            }
        }
        Gson gson = new GsonBuilder().create();
        try
        {

            URL url = new URL("http://nominatim.openstreetmap.org/search?q=" + address.replace(" ", "+") + "&format=json&limit=" + limit + "&addressdetails=1&accept-language=" + lang);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

            List<Response> resp = gson.fromJson(in, new TypeToken<List<Response>>()
            {
            }.getType());
            in.close();
            return resp;
        } catch(Exception e)
        {
            Crashlytics.logException(e);
        }

        return null;

    }

    public static Response from(double lat, double lng, int limit, String lang)
    {
        Gson gson = new GsonBuilder().create();
        try
        {

            URL url = new URL("http://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lng + "&format=json&limit=" + limit + "&addressdetails=1&accept-language=" + lang);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

            Response resp = gson.fromJson(in, new TypeToken<Response>()
            {
            }.getType());
            in.close();
            return resp;
        } catch(Exception e)
        {
            Crashlytics.logException(e);
        }

        return null;

    }

    public static class Response
    {
        public double lat, lon;
        public double[] boundingbox;
        public String display_name;
        public Address address;

    }

    public static class Address
    {
        public String city, county, state, country, country_code, continent;
    }

}