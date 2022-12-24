/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.times;

import android.database.SQLException;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metinkale.prayer.utils.Geocoder;

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
    private final Handler mHandler = new Handler();
    @NonNull
    private final Executor mThread = Executors.newSingleThreadExecutor();

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
        mThread.execute(() -> {
            final List<Entry> result = list(id);
            mHandler.post(() -> callback.onResult(result));
        });
    }


    public void search(final String q, @NonNull final Callback<List<Entry>> callback) {
        Geocoder.search(q, result -> mThread.execute(() -> {
            final List<Entry> search = search(q, result);
            if (result != null) {
                Entry calc = new Entry();
                calc.setSource(Source.Calc);
                calc.setLat(result.getLat());
                calc.setLng(result.getLon());
                calc.setName(result.getName());
                calc.setCountry(result.getCountry());
                search.add(calc);
            }
            mHandler.post(() -> callback.onResult(search));
        }));

    }

    public void search(final double lat, final double lng, @NonNull final Callback<List<Entry>> callback) {
        final Cities cities = Cities.get();
        Geocoder.reverse(lat, lng,
                result -> mThread.execute(
                        () -> {
                            final List<Entry> search = cities.search(lat, lng);
                            Entry e = new Entry();
                            e.setSource(Source.Calc);
                            if (result == null) {
                                e.setCountry("?");
                                e.setName("?");
                                e.setLat(lat);
                                e.setLng(lng);
                            } else {
                                e.setCountry(result.getCountry());
                                e.setName(result.getName());
                                e.setLat(result.getLat());
                                e.setLng(result.getLon());
                            }
                            search.add(e);
                            mHandler.post(() -> callback.onResult(search));
                        }

                )

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
        return new ArrayList<>(map.values());

    }


    @NonNull
    private List<Entry> search(String q, @Nullable Geocoder.Result result) throws SQLException {
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

                if (!contains && result != null && !name.containsKey(s) && result.getLat() != 0 && result.getLon() != 0) {
                    Entry e = pos.get(s);
                    double latDist = Math.abs(result.getLat() - entry.getLat());
                    double lngDist = Math.abs(result.getLon() - entry.getLng());
                    if (e == null) {
                        if (latDist < 2 && lngDist < 2)
                            pos.put(s, (Entry) entry.clone());
                    } else {
                        if (latDist + lngDist < Math.abs(result.getLat() - e.getLat()) + Math.abs(result.getLon() - e.getLng())) {
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

                if (result != null) {
                    double latDist = Math.abs(result.getLat() - entry.getLat());
                    double lngDist = Math.abs(result.getLon() - entry.getLng());
                    if (latDist + lngDist < Math.abs(result.getLat() - e.getLat()) + Math.abs(result.getLon() - e.getLng())) {
                        name.put(s, (Entry) entry.clone());
                    }
                }


            }
        }
        List<Entry> items = new ArrayList<>(name.values());
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