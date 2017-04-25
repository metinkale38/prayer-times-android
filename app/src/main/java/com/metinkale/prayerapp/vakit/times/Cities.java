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
 *
 */

package com.metinkale.prayerapp.vakit.times;

import android.database.SQLException;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.metinkale.prayerapp.utils.Geocoder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Cities {

    @Nullable
    private static WeakReference<Cities> mInstance = new WeakReference<>(null);
    private final CitiesSet mEntries = new CitiesSet();
    @NonNull
    private Handler mHandler = new Handler();
    @NonNull
    private Executor mThread = Executors.newSingleThreadExecutor();

    private Cities() {
    }


    @Nullable
    public static synchronized Cities get() {
        Cities cities = mInstance.get();
        if (cities == null) {
            cities = new Cities();
            mInstance = new WeakReference<>(cities);
        }
        return cities;
    }


    public void list(final int id, @NonNull final Callback<List<Entry>> callback) {
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
                        calc.setKey("C_");
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
                                            e.setKey("C_");
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
    private List<Entry> list(int id) {
        List<Entry> entries = new ArrayList<>();
        for (Entry e : mEntries) {
            if (e != null && e.getParent() == id)
                entries.add((Entry) e.clone());
        }
        return entries;
    }

    @NonNull
    private List<Entry> search(double lat, double lng) throws SQLException {

        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> map = new EnumMap<>(Source.class);
        for (Entry entry : mEntries) {
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
        items.addAll(map.values());
        return items;

    }


    @NonNull
    private List<Entry> search(String q, @NonNull Geocoder.Result r) throws SQLException {
        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> name = new EnumMap<>(Source.class);
        EnumMap<Source, Entry> pos = new EnumMap<>(Source.class);

        q = Entry.normalize(q);

        for (Entry entry : mEntries) {
            if (entry == null || entry.getKey() == null) continue;
            Source s = entry.getSource();
            if (s == null) continue;
            String norm = entry.getNormalized();
            boolean contains = norm.contains(q);

            if (!contains && !name.containsKey(s)) {
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