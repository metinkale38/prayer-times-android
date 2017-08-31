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

package com.metinkale.prayerapp.vakit.times;

import android.database.SQLException;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.metinkale.prayerapp.utils.Geocoder;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Cities {

    @NonNull
    private static SoftReference<Cities> mInstance = new SoftReference<>(null);
    @NonNull
    private Handler mHandler = new Handler();
    @NonNull
    private Executor mThread = Executors.newSingleThreadExecutor();

    private Cities() {
    }


    @NonNull
    public static synchronized Cities get() {
        Cities cities = mInstance.get();
        if (cities == null) {
            cities = new Cities();
            mInstance = new SoftReference<>(cities);
        }
        return cities;
    }


    public void list(final long id, @NonNull final Callback<List<Entry>> callback) {
        mThread.execute(new Runnable() {
            @Override
            public void run() {
                final List<Entry> result = list(id);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(result);
                    }
                });
            }
        });
    }


    public void search(final String q, @NonNull final Callback<List<Entry>> callback) {
        Geocoder.search(q, new Geocoder.SearchCallback() {
            @Override
            public void onResult(List<Geocoder.Result> results) {
                final Geocoder.Result result = results == null || results.isEmpty() ? new Geocoder.Result() : results.get(0);
                mThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List<Entry> search = search(q, result);
                        Entry calc = new Entry();
                        calc.setSource(Source.Calc);
                        calc.setLat(result.lat);
                        calc.setLng(result.lng);
                        calc.setName(result.city);
                        calc.setCountry(result.country);
                        search.add(calc);
                        mHandler.post(new Runnable() {
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

    public void search(final double lat, final double lng, @NonNull final Callback<List<Entry>> callback) {
        final Cities cities = Cities.get();
        Geocoder.reverse(lat, lng,
                new Geocoder.ReverseCallback() {
                    @Override
                    public void onResult(final Entry e) {
                        mThread.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        final List<Entry> search = cities.search(lat, lng);
                                        if (e != null) {
                                            e.setSource(Source.Calc);
                                            search.add(e);
                                        }
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(search);
                                            }
                                        });
                                    }
                                }

                        );
                    }
                }

        );


    }


    @NonNull
    private List<Entry> list(long id) {
        List<Entry> entries = new ArrayList<>();
        if (id == 0) {
            for (Source source : Source.values()) {
                if (source.citiesId == 0) continue;
                Entry e = new Entry();
                e.setName(source.name);
                e.setId(source.citiesId);
                e.setSource(source);
                entries.add(e);
            }
            return entries;
        }
        CitiesSet cities = new CitiesSet((int) ((id << 32) >> 32));
        for (Entry e : cities) {

            if (e != null && e.getParent() == id)
                entries.add((Entry) e.clone());
        }
        return entries;
    }

    @NonNull
    private List<Entry> search(double lat, double lng) throws SQLException {

        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> map = new EnumMap<>(Source.class);
        for (Source source : Source.values()) {
            if (source.citiesId == 0) continue;
            CitiesSet entries = new CitiesSet(source);
            for (Entry entry : entries) {
                if (entry == null || entry.getKey() == null) continue;
                Source s = entry.getSource();
                Entry e = map.get(s);
                double latDist = Math.abs(lat - entry.getLat());
                double lngDist = Math.abs(lng - entry.getLng());
                if (e == null) {
                    if (latDist < 2 && lngDist < 2)
                        map.put(s, (Entry) entry.clone());
                } else {
                    if (latDist + lngDist < Math.abs(lat - e.getLat()) + Math.abs(lng - e.getLng())) {
                        map.put(s, (Entry) entry.clone());
                    }
                }
            }
        }
        items.addAll(map.values());
        return items;

    }


    @NonNull
    private List<Entry> search(String q, @NonNull Geocoder.Result r) throws SQLException {
        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> name = new EnumMap<>(Source.class);
        EnumMap<Source, Entry> pos = new EnumMap<>(Source.class);

        q = Entry.normalize(q);
        for (Source source : Source.values()) {
            if (source.citiesId == 0) continue;
            CitiesSet entries = new CitiesSet(source);
            for (Entry entry : entries) {
                if (entry == null || entry.getKey() == null) continue;
                Source s = entry.getSource();
                if (s == null) continue;
                String norm = entry.getNormalized();
                boolean contains = norm.contains(q);

                if (!contains && !name.containsKey(s) && r.lat != 0 && r.lng != 0) {
                    Entry e = pos.get(s);
                    double latDist = Math.abs(r.lat - entry.getLat());
                    double lngDist = Math.abs(r.lng - entry.getLng());
                    if (e == null) {
                        if (latDist < 2 && lngDist < 2)
                            pos.put(s, (Entry) entry.clone());
                    } else {
                        if (latDist + lngDist < Math.abs(r.lat - e.getLat()) + Math.abs(r.lng - e.getLng())) {
                            pos.put(s, (Entry) entry.clone());
                        }
                    }
                }

                if (!contains) continue;

                Entry e = name.get(s);
                if (e == null) {
                    name.put(s, (Entry) entry.clone());
                    continue;
                }

                double latDist = Math.abs(r.lat - entry.getLat());
                double lngDist = Math.abs(r.lng - entry.getLng());
                if (latDist + lngDist < Math.abs(r.lat - e.getLat()) + Math.abs(r.lng - e.getLng())) {
                    name.put(s, (Entry) entry.clone());
                }


            }
        }
        items.addAll(name.values());
        for (Entry e : pos.values()) {
            Source s = e.getSource();
            if (!name.containsKey(s)) items.add(e);
        }
        return items;

    }

    public abstract static class Callback<T> {
        public abstract void onResult(T result);
    }


}