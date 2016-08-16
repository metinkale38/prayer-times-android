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
import android.os.Looper;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.Geocoder;
import com.metinkale.prayerapp.settings.Prefs;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by metin on 05.01.2016.
 */
public class Cities extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 3;
    private static Cities mInstance;
    private static AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;
    private Executor mExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {
        @Override
        public void execute(Runnable command) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.execute(command);
            } else {
                command.run();
            }
        }
    };

    private Handler mHandler = new Handler();

    static synchronized Cities get() {
        if (mInstance == null) {
            mInstance = new Cities(App.getContext());
        }

        return mInstance;
    }

    public abstract static class Callback {
        public abstract void onResult(List result);
    }

    public static void list(final String source, final String country, final String state, final String city, final Callback cb) {
        final Cities c = get();
        c.mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<String> result = c.list(source, country, state, city);
                c.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onResult(result);
                    }
                });
            }
        });

    }


    public static void search2(final double lat, final double lng, final String country, final String city, final String q, final Callback cb) {
        final Cities c = get();
        c.mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Item> result = c.search2(lat, lng, country, city, q);
                    c.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onResult(result);
                        }
                    });
                } catch (IOException e) {
                    Crashlytics.setDouble("lat", lat);
                    Crashlytics.setDouble("lng", lng);
                    Crashlytics.setString("country", country);
                    Crashlytics.setString("city", city);
                    Crashlytics.setString("q", q);
                    Crashlytics.logException(e);
                } finally {

                }
            }
        });
    }

    public static void search(final String q, final Callback cb) {
        final Cities c = get();
        c.mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Item> result = c.search(q);
                c.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cb.onResult(result);
                    }
                });

            }
        });
    }

    private Cities(Context context) {
        super(context, DATABASE_NAME, null, Cities.DATABASE_VERSION);
        setForcedUpgrade(DATABASE_VERSION);
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


    private List<String> list(String source, String country, String state, String city) {
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
            if ("".equals(city)) {
                city = null;
            }
            if (source != null) {
                source = source.toUpperCase(Locale.GERMAN);
            }

            if (source == null) {
                resp.add("Diyanet");
                resp.add("IGMG");
                resp.add("Fazilet");
                resp.add("Semerkand");
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
                Cursor c = null;

                c = db.query(source, new String[]{"STATE"}, "COUNTRY = '" + country + "'", null, "STATE", null, "STATE");

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
                        String _country = c.getString(c.getColumnIndex("country"));
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


    List<Cities.Item> search(String q) {
        q = q.trim();
        String lang = Prefs.getLanguage();
        Geocoder.Address resp = null;
        try {
            resp = Geocoder.from(q, 1).get(0);
        } catch (Exception e) {
        }

        if (resp == null) {
            try {
                return search2(0, 0, q, q, q);
            } catch (IOException e) {
                Crashlytics.setDouble("lat", 0);
                Crashlytics.setDouble("lng", 0);
                Crashlytics.setString("country", q);
                Crashlytics.setString("city", q);
                Crashlytics.setString("q", q);
                Crashlytics.logException(e);
            }

        }
        double lat = resp.lat;
        double lng = resp.lng;
        String country = resp.country;
        String city = resp.city;

        try {
            return search2(lat, lng, country, city, q);
        } catch (IOException e) {
            Crashlytics.setDouble("lat", lat);
            Crashlytics.setDouble("lng", lng);
            Crashlytics.setString("country", country);
            Crashlytics.setString("city", city);
            Crashlytics.setString("q", q);
            Crashlytics.logException(e);
        }
        return null;

    }


    List<Cities.Item> search2(double lat, double lng, String country, String city, String q) throws SQLException, IOException {
        q = q.replace("'", "\'");
        String[] sources = {"Diyanet", "IGMG", "Fazilet", "NVC", "Semerkand"};

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
                if ("NVC".equals(source)) {
                    table = "NAMAZVAKTICOM";
                } else if ("Fazilet".equals(source)) {
                    table = "FAZILET";
                } else if ("Diyanet".equals(source)) {
                    table = "DIYANET";
                } else if ("IGMG".equals(source)) {
                    table = "IGMG";
                } else if ("Semerkand".equals(source)) {
                    table = "SEMERKAND";
                }

                String query;

                Cursor c = null;
                if ("NVC".equals(source)) {
                    c = db.query(table, null, "name like '%" + q + "%'", null, null, null, "abs(lat - " + lat + ") + abs(lng - " + lng + ")", "1");
                } else {
                    boolean diy = "Diyanet".equals(source) || "Semerkand".equals(source);
                    c = db.query(table, null, "city like '%" + q + "%'" + (diy ? " or state like '%" + q + "%' " : ""), null, null, null, "abs(lat - " + lat + ") + abs(lng - " + lng + ")", "1");
                }

                c.moveToFirst();
                if (c.isAfterLast()) {
                    c.close();
                    c = db.query(table, null, "abs(lat - " + lat + ") + abs(lng - " + lng + ") < 2", null, null, null, "abs(lat - " + lat + ") + abs(lng - " + lng + ")", "1");
                    c.moveToFirst();
                    if (c.isAfterLast()) {
                        c.close();
                        continue;
                    }
                }
                String id = "";
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

    public static class Item {
        public String city;
        public String country;
        public String id;
        public double lat;
        public double lng;
        public Source source;
    }
}