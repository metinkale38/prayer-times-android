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

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Geocoder;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Cities extends SQLiteAssetHelper {

    private static Cities mInstance;
    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 5;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;
    private Handler mHandler = new Handler();
    private Executor mThread = Executors.newSingleThreadExecutor();

    private Cities(Context context) {
        super(context, DATABASE_NAME, null, Cities.DATABASE_VERSION);
        setForcedUpgrade(DATABASE_VERSION);
    }

    private static synchronized Cities get() {
        if (mInstance == null) {
            mInstance = new Cities(App.getContext());
        }

        return mInstance;
    }


    private synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            mDatabase = mInstance.getWritableDatabase();
        }
        return mInstance.mDatabase;
    }

    private synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
            mDatabase = null;
        }
    }


    public static void list(final String source, final String country, final String state, final Callback callback) {
        final Cities cities = Cities.get();
        cities.mThread.execute(new Runnable() {
            @Override
            public void run() {
                final List<String> result = cities.list(source, country, state);
                cities.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(result);
                    }
                });
            }
        });
    }

    private List<String> list(String source, String country, String state) {
        List<String> resp = new ArrayList<>();
        try {
            SQLiteDatabase db = openDatabase();


            if ("".equals(source)) {
                source = null;
            }
            if ("".equals(country)) {
                country = null;
            }
            if ("".equals(state)) {
                state = null;
            }

            if (source != null) {
                source = source.toUpperCase(Locale.GERMAN);
            }

            if (source == null) {
                resp.add("Diyanet");
                resp.add("IGMG");
                resp.add("Fazilet");
                resp.add("Semerkand");
                resp.add("Morocco");
                return resp;
            } else if (source.equals("MOROCCO")) {
                Cursor c = db.query("MAROC", null, null, null, null, null, "ar".equals(Prefs.getLanguage()) ? "name" : "name_en");

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    do {

                        String name = c.getString(c.getColumnIndex("ar".equals(Prefs.getLanguage()) ? "name" : "name_en"));
                        String id = c.getString(c.getColumnIndex("id"));
                        double _lat = c.getDouble(c.getColumnIndex("lat"));
                        double _lng = c.getDouble(c.getColumnIndex("lng"));
                        resp.add(name + ";" + id + ";" + _lat + ";" + _lng);
                    } while (c.moveToNext());
                }
                c.close();

                return resp;
            } else if (country == null) {
                Cursor c = db.query(source, new String[]{"COUNTRY"}, null, null, "COUNTRY", null, "COUNTRY");
                c.moveToFirst();
                if (!c.isAfterLast()) {
                    do {
                        resp.add(c.getString(0));
                    } while (c.moveToNext());
                }
                c.close();
                return resp;
            } else if (state == null) {
                Cursor c = db.query(source, new String[]{"STATE"}, "COUNTRY = '" + country + "'", null, "STATE", null, "STATE");

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    do {
                        String _state = c.getString(c.getColumnIndex("state"));
                        resp.add(_state);
                    } while (c.moveToNext());
                }
                c.close();
                return resp;
            } else {
                Cursor c = db.query(source, null, "COUNTRY = '" + country + "' AND STATE = '" + state + "'", null, null, null, "city");

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    do {
                        String _state = c.getString(c.getColumnIndex("state"));
                        String _city = c.getString(c.getColumnIndex("city"));
                        String _countryId = c.getString(c.getColumnIndex("countryId"));
                        String _stateId = c.getString(c.getColumnIndex("stateId"));
                        String _cityId = c.getString(c.getColumnIndex("cityId"));
                        double _lat = c.getDouble(c.getColumnIndex("lat"));
                        double _lng = c.getDouble(c.getColumnIndex("lng"));

                        String name = _city == null || "".equals(_city) ? _state : _city;
                        String id = source.substring(0, 1) + "_" + _countryId + "_" + _stateId + "_" + _cityId;
                        resp.add(name + ";" + id + ";" + _lat + ";" + _lng);
                    } while (c.moveToNext());
                }
                c.close();

                return resp;

            }
        } finally {
            closeDatabase();
        }

    }

    public static void search(final String q, final Callback callback) {
        final Cities cities = Cities.get();
        Geocoder.search(q, new Geocoder.SearchCallback() {
            @Override
            public void onResult(List<Geocoder.Result> results) {
                final Geocoder.Result result = results == null || results.isEmpty() ? null : results.get(0);
                cities.mThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List search = cities.search(result.city, result.state, result.country, result.lat, result.lng, q);
                        cities.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(search);
                            }
                        });
                    }
                });
            }
        });

    }

    public static void search(double lat, double lng, final Callback callback) {
        final Cities cities = Cities.get();
        Geocoder.reverse(lat, lng, new Geocoder.ReverseCallback() {
            @Override
            public void onResult(final String city, final String state, final String country, final double lat, final double lng) {
                cities.mThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List search = cities.search(city, state, country, lat, lng, null);
                        cities.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(search);
                            }
                        });
                    }
                });
            }

        });

    }

    private List<Cities.Item> search(String city, String state, String country, double lat, double lng, String q) throws SQLException {
        if (q != null) q = q.replace("'", "\'");
        String[] sources = {"Diyanet", "IGMG", "Fazilet", "NVC", "Semerkand", "Maroc"};

        List<Cities.Item> items = new ArrayList<>();
        Cities.Item calc = new Cities.Item();
        calc.source = Source.Calc;
        calc.city = city;
        calc.country = country;
        calc.lat = lat;
        calc.lng = lng;
        items.add(calc);

        try {
            SQLiteDatabase db = openDatabase();

            for (String source : sources) {
                String table = null;
                String searchrow1 = null;
                String searchrow2 = null;
                switch (source) {
                    case "NVC":
                        searchrow1 = "name";
                        table = "NAMAZVAKTICOM";
                        break;
                    case "Fazilet":
                        searchrow1 = "city";
                        table = "FAZILET";
                        break;
                    case "Diyanet":
                        searchrow1 = "city";
                        searchrow2 = "state";
                        table = "DIYANET";
                        break;
                    case "Semerkand":
                        searchrow1 = "city";
                        searchrow2 = "state";
                        table = "SEMERKAND";
                        break;
                    case "IGMG":
                        searchrow1 = "city";
                        table = "IGMG";
                        break;
                    case "Maroc":
                        searchrow1 = "name";
                        searchrow2 = "name_en";
                        table = "Maroc";
                        break;
                }


                String sort = "abs(lat - " + lat + ") + abs(lng - " + lng + ")";
                String limit = "1";
                Cursor c = null;


                //search q in names
                if (q != null)
                    if (searchrow1 != null && searchrow2 != null) {
                        c = db.query(table, null, searchrow1 + " like '%" + q + "%'" + " or " + searchrow2 + " like '%" + q + "%' ", null, null, null, sort, limit);
                    } else if (searchrow1 != null) {
                        c = db.query(table, null, searchrow1 + " like '%" + q + "%'", null, null, null, sort, limit);
                    }

                if (c != null) c.moveToFirst();
                if (c == null || c.isAfterLast()) {
                    if (c != null) c.close();
                    c = db.query(table, null, "abs(lat - " + lat + ") + abs(lng - " + lng + ") < 2", null, null, null, sort, limit);
                    c.moveToFirst();
                    if (c.isAfterLast()) {
                        c.close();
                        continue;
                    }
                }
                String id;
                Cities.Item item = new Cities.Item();
                if ("NVC".equals(source)) {
                    item.source = Source.NVC;
                    item.city = c.getString(c.getColumnIndex("name"));
                    item.id = c.getString(c.getColumnIndex("id"));
                    if ((item.city == null) || "".equals(item.city)) {
                        item.city = NVCTimes.getName(item.id);
                    }

                } else if ("IGMG".equals(source)) {
                    item.source = Source.IGMG;
                    item.country = c.getString(c.getColumnIndex("country"));
                    item.city = c.getString(c.getColumnIndex("state"));
                    id = "I_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_0";
                    item.id = id.replace("-1", "nix");
                }
                if ("Maroc".equals(source)) {
                    item.source = Source.Morocco;
                    item.country = "Morocco";
                    item.city = c.getString(c.getColumnIndex("ar".equals(Prefs.getLanguage()) ? "name" : "name_en"));
                    item.id = c.getString(c.getColumnIndex("id"));
                } else if ("Fazilet".equals(source)) {
                    item.source = Source.Fazilet;
                    item.country = c.getString(c.getColumnIndex("country"));
                    item.city = c.getString(c.getColumnIndex("city"));
                    item.id = "F_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId"));
                } else if ("Diyanet".equals(source)) {
                    item.source = Source.Diyanet;
                    if (c.getInt(c.getColumnIndex("cityId")) == 0) {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("state"));
                        item.id = "D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_0";
                    } else {
                        item.country = c.getString(c.getColumnIndex("country"));
                        item.city = c.getString(c.getColumnIndex("city"));
                        item.id = "D_" + c.getString(c.getColumnIndex("countryId")) + "_" + c.getString(c.getColumnIndex("stateId")) + "_" + c.getString(c.getColumnIndex("cityId"));

                    }
                } else if ("Semerkand".equals(source)) {
                    item.source = Source.Semerkand;
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

                if ((lat != 0) || (lng != 0) || (item.lat != 0) || (item.lng != 0)) {
                    items.add(item);
                }

            }

        } catch (SQLiteException e) {
            //Crashlytics.logException(e);
            mInstance = null;
            setForcedUpgrade();
        } finally {
            closeDatabase();
        }
        return items;

    }

    public abstract static class Callback {
        public abstract void onResult(List result);
    }

    public static class Item {
        public String city;
        public String country;
        public String id;
        public double lat;
        public double lng;
        public Source source;
    }
}