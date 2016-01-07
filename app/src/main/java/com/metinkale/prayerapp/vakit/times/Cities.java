package com.metinkale.prayerapp.vakit.times;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.custom.Geocoder;
import com.metinkale.prayerapp.settings.Prefs;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by metin on 05.01.2016.
 */
public class Cities extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 2;

    public Cities(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade(DATABASE_VERSION);
    }

    public static List<String> list(String source, String country, String state, String city) {
        List<String> resp = new ArrayList<>();
        Cities cities = null;
        SQLiteDatabase db = null;
        try {
            cities = new Cities(App.getContext());
            db = cities.getReadableDatabase();


            if ("".equals(source)) source = null;
            if ("".equals(country)) country = null;
            if ("".equals(state)) state = null;
            if ("".equals(city)) city = null;
            if (source != null) source = source.toUpperCase(Locale.GERMAN);

            if (source == null) {
                resp.add("Diyanet");
                resp.add("IGMG");
                resp.add("Fazilet");
                resp.add("Semerkand");
                return resp;
            } else if (country == null) {
                Cursor c = db.query(source, new String[]{"COUNTRY"}, null, null, "COUNTRY", null, "COUNTRY");
                c.moveToFirst();
                if (!c.isAfterLast())
                    do {
                        resp.add(c.getString(0));
                    } while (c.moveToNext());
                c.close();
                return resp;
            } else if (state == null) {
                Cursor c = null;
                if (source.equals("DIYANET")) {
                    c = db.query(source, new String[]{"cityId"}, "COUNTRY = '" + country + "'", null, null, null, null, "1");
                    c.moveToFirst();
                    int cityId = c.getInt(0);
                    c.close();
                    if (cityId == 0)
                        c = db.query(source, null, " COUNTRY = '" + country + "'", null, null, null, "STATE");
                    else
                        c = db.query(source, new String[]{"state"}, " COUNTRY = '" + country + "'", null, "STATE", null, "STATE");
                } else
                    c = db.query(source, null, "COUNTRY = '" + country + "'", null, null, null, "CITY");

                c.moveToFirst();
                if (!c.isAfterLast())
                    do {
                        if (source.equals("IGMG"))
                            resp.add(c.getString(c.getColumnIndex("state")) + ",I_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "," + c.getDouble(c.getColumnIndex("lat")) + "," + c.getDouble(c.getColumnIndex("lng")));
                        else if (source.equals("FAZILET"))
                            resp.add(c.getString(c.getColumnIndex("city")) + ",F_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId  ")) + "_" + c.getString(c.getColumnIndex("cityId")) + "," + c.getDouble(c.getColumnIndex("lat")) + "," + c.getDouble(c.getColumnIndex("lng")));
                        else if (c.getColumnIndex("countryId") == -1)
                            resp.add(c.getString(c.getColumnIndex("state")));
                        else
                            resp.add(c.getString(c.getColumnIndex("state")) + ",D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "," + c.getDouble(c.getColumnIndex("lat")) + "," + c.getDouble(c.getColumnIndex("lng")));
                    } while (c.moveToNext());
                c.close();
                return resp;
            } else {
                Cursor c = db.query(source, null, "COUNTRY = '" + country + "' AND STATE = '" + state + "'", null, null, null, "city");

                c.moveToFirst();
                if (!c.isAfterLast())
                    do {
                        resp.add(c.getString(c.getColumnIndex("city")) + ",D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId")) + "," + c.getDouble(c.getColumnIndex("lat")) + "," + c.getDouble(c.getColumnIndex("lng")));
                    } while (c.moveToNext());
                c.close();

                return resp;

            }
        } finally {
            if (db != null)
                db.close();
            if (cities != null)
                cities.close();
        }

    }


    public static List<Item> search(String q) {
        String lang = Prefs.getLanguage();
        Geocoder.Response resp;
        try {
            resp = Geocoder.from(q, 1, lang).get(0);
        } catch (Exception e) {
            return null;
        }

        double lat = resp.lat;
        double lng = resp.lon;
        String country = resp.address.country;
        String city = null;
        if (resp.address.city != null) {
            city = resp.address.city;
        } else if (resp.address.county != null) {
            city = resp.address.county;
        } else if (resp.address.state != null) {
            city = resp.address.state;
        }

        try {
            return search2(lat, lng, country, city, q);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static List<Item> search2(double lat, double lng, String country, String city, String q) throws SQLException, IOException {

        String[] sources = new String[]{"Diyanet", "IGMG", "Fazilet", "NVC", "Semerkand"};

        List<Item> items = new ArrayList<Item>();
        Item calc = new Item();
        calc.source = TimesBase.Source.Calc;
        calc.city = city;
        calc.country = country;
        calc.lat = lat;
        calc.lng = lng;
        items.add(calc);

        Cities cities = null;
        SQLiteDatabase db = null;

        try {
            cities = new Cities(App.getContext());
            db = cities.getReadableDatabase();

            for (String source : sources) {
                String table = null;
                if (source.equals("NVC")) {
                    table = "NAMAZVAKTICOM";
                } else if (source.equals("Fazilet")) {
                    table = "FAZILET";
                } else if (source.equals("Diyanet")) {
                    table = "DIYANET";
                } else if (source.equals("IGMG")) {
                    table = "IGMG";
                } else if (source.equals("Semerkand")) {
                    table = "SEMERKAND";
                }

                String query;

                Cursor c = null;
                if (source.equals("NVC")) {
                    c = db.query(table, null, "name like '%" + q + "%'", null, null, null, "abs(lat - " + lat + ") + abs(lng-" + lng + ")", "1");
                } else {
                    boolean diy = source.equals("Diyanet") || source.equals("Semerkand");
                    c = db.query(table, null, "city like '%" + q + "%'" + (diy ? "or state like '%" + q + "%' " : ""), null, null, null, "abs(lat - " + lat + ") + abs(lng-" + lng + ")", "1");
                }

                c.moveToFirst();
                if (c.isAfterLast()) {
                    c.close();
                    c = db.query(table, null, "abs(lat-" + lat + ") + abs(lng-" + lng + ") < 2", null, null, null, "abs(lat - " + lat + ") + abs(lng-" + lng + ")", "1");
                    c.moveToFirst();
                    if (c.isAfterLast()) {
                        c.close();
                        continue;
                    }
                }
                String id = "";
                Item item = new Item();
                if (source.equals("NVC")) {
                    item.source = TimesBase.Source.NVC;
                    item.city = c.getString(c.getColumnIndex("name"));
                    item.id = c.getString(c.getColumnIndex("id"));

                    if (item.city == null || item.city.equals("")) {
                        item.city = NVCTimes.getName(item.id);
                    }

                } else if (source.equals("IGMG")) {
                    item.source = TimesBase.Source.IGMG;
                    item.country = c.getString(c.getColumnIndex("country"));
                    item.city = c.getString(c.getColumnIndex("state"));
                    id = "I_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId"));
                    item.id = id.replace("-1", "nix");
                } else if (source.equals("Fazilet")) {
                    item.source = TimesBase.Source.Fazilet;
                    item.country = c.getString(c.getColumnIndex("country"));
                    item.city = c.getString(c.getColumnIndex("city"));
                    item.id = "F_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId"));
                } else if (source.equals("Diyanet")) {
                    item.source = TimesBase.Source.Diyanet;
                    if (c.getInt(c.getColumnIndex("cityId")) == 0) {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("state"));
                        item.id = "D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_0";
                    } else {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("city"));
                        item.id = "D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId"));

                    }
                } else if (source.equals("Semerkand")) {
                    item.source = TimesBase.Source.Semerkand;
                    if (c.getInt(c.getColumnIndex("cityId")) == 0) {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("state"));
                        item.id = "S_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_0";
                    } else {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("city"));
                        item.id = "S_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId"));

                    }
                }
                item.lat = c.getDouble(c.getColumnIndex("lat"));
                item.lng = c.getDouble(c.getColumnIndex("lng"));
                c.close();

                items.add(item);

            }
            db.close();

        } finally {
            if (db != null)
                db.close();
            if (cities != null)
                cities.close();
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