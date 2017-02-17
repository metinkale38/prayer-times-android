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

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.Geocoder;
import com.metinkale.prayerapp.utils.SimpleIntArrayMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Cities {

    @Nullable
    private static WeakReference<Cities> mInstance = new WeakReference<Cities>(null);
    private final Context mContext;
    private final SimpleIntArrayMap<Entry> mEntries = new SimpleIntArrayMap<>(112665);
    @NonNull
    private Handler mHandler = new Handler();
    @NonNull
    private Executor mThread = Executors.newSingleThreadExecutor();

    private Cities(Context context) {
        mContext = context;
        mThread.execute(() -> {
            try {
                loadTSV();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadTSV() throws IOException {
        BufferedReader is = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.cities)));
        String line;
        while ((line = is.readLine()) != null) {
            if (line.isEmpty()) continue;
            MyFastTokenizer st = new MyFastTokenizer(line, "\t");
            Entry e = new Entry();
            e.setId(st.nextInt());
            e.setParent(st.nextInt());
            e.setLat(st.nextDouble());
            e.setLng(st.nextDouble());
            e.setKey(st.nextString());
            e.setName(st.nextString());
            e.setNormalized(normalize(e.getName()));
            if (e.getKey() != null) {
                Entry parent = e;
                Entry tmp = mEntries.get(parent.getParent());
                while (tmp.getParent() != 0) {
                    parent = tmp;
                    tmp = mEntries.get(parent.getParent());
                }
                e.setCountry(parent.getName());
            }

            mEntries.put(e.getId(), e);
        }
    }

    @Nullable
    public static synchronized Cities get() {
        Cities cities = mInstance.get();
        if (cities == null) {
            cities = new Cities(App.get());
            mInstance = new WeakReference<Cities>(cities);
        }
        return cities;
    }


    public void list(final int id, @NonNull final Callback<List<Entry>> callback) {
        mThread.execute(() -> {
            final List<Entry> result = list(id);
            mHandler.post(() -> callback.onResult(result));
        });
    }


    public void search(final String q, @NonNull final Callback<List<Entry>> callback) {
        Geocoder.search(q, results -> {
            final Geocoder.Result result = results == null || results.isEmpty() ? new Geocoder.Result() : results.get(0);
            mThread.execute(() -> {
                final List<Entry> search = search(q, result);
                Entry calc = new Entry();
                calc.setKey("C_");
                calc.setLat(result.lat);
                calc.setLng(result.lng);
                calc.setName(result.city);
                calc.setCountry(result.country);
                search.add(calc);
                mHandler.post(() -> callback.onResult(search));
            });
        });

    }

    public void search(final double lat, final double lng, @NonNull final Callback<List<Entry>> callback) {
        final Cities cities = Cities.get();
        Geocoder.reverse(lat, lng, e -> mThread.execute(() -> {
            final List<Entry> search = cities.search(lat, lng);
            e.setKey("C_");
            search.add(e);
            mHandler.post(() -> callback.onResult(search));
        }));


    }


    @NonNull
    private List<Entry> list(int id) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < mEntries.size(); i++) {
            Entry e = mEntries.get(i);
            if (e != null && e.getParent() == id)
                entries.add(e);
        }
        return entries;
    }

    @NonNull
    private List<Entry> search(double lat, double lng) throws SQLException {

        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> map = new EnumMap<Source, Entry>(Source.class);
        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.get(i);
            if (entry == null || entry.getKey() == null) continue;
            Source s = entry.getSource();
            Entry e = map.get(s);
            double latDist = Math.abs(lat - entry.getLat());
            double lngDist = Math.abs(lng - entry.getLng());
            if (e == null) {
                if (latDist < 2 && lngDist < 2)
                    map.put(s, entry);
            } else {
                if (latDist + lngDist < Math.abs(lat - e.getLat()) + Math.abs(lng - e.getLng())) {
                    map.put(s, entry);
                }
            }
        }
        items.addAll(map.values());
        return items;

    }


    @NonNull
    private List<Entry> search(String q, @NonNull Geocoder.Result r) throws SQLException {
        List<Entry> items = new ArrayList<>();
        EnumMap<Source, Entry> name = new EnumMap<Source, Entry>(Source.class);
        EnumMap<Source, Entry> pos = new EnumMap<Source, Entry>(Source.class);

        q = normalize(q);

        for (int i = 0; i < mEntries.size(); i++) {
            Entry entry = mEntries.get(i);
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
                        pos.put(s, entry);
                } else {
                    if (latDist + lngDist < Math.abs(r.lat - e.getLat()) + Math.abs(r.lng - e.getLng())) {
                        pos.put(s, entry);
                    }
                }
            }

            if (!contains) continue;

            Entry e = name.get(s);
            if (e == null) {
                name.put(s, entry);
                continue;
            }

            double latDist = Math.abs(r.lat - entry.getLat());
            double lngDist = Math.abs(r.lng - entry.getLng());
            if (e == null) {
                if (latDist < 2 && lngDist < 2)
                    name.put(s, entry);
            } else {
                if (latDist + lngDist < Math.abs(r.lat - e.getLat()) + Math.abs(r.lng - e.getLng())) {
                    name.put(s, entry);
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

    private String normalize(@NonNull String s) {
        StringBuilder builder = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 0x41 && c <= 0x5A) {//A-Z
                builder.append((char) (c + 0x20));
            } else if (c >= 0x61 && c <= 0x7A) {//a-z
                builder.append(c);
            } else {
                switch (c) {
                    case 'é':
                    case 'è':
                    case 'ê':
                    case 'ë':
                    case 'È':
                    case 'É':
                    case 'Ë':
                    case 'Ê':
                        builder.append("e");
                        break;
                    case 'Ç':
                    case 'ç':
                        builder.append("c");
                        break;
                    case 'Ğ':
                    case 'ğ':
                        builder.append("g");
                        break;
                    case 'ı':
                    case 'İ':
                    case 'ï':
                    case 'î':
                    case 'Ï':
                    case 'Î':
                        builder.append("i");
                        break;
                    case 'Ö':
                    case 'ö':
                    case 'Ô':
                        builder.append("o");
                        break;
                    case 'Ş':
                    case 'ş':
                        builder.append("s");
                        break;
                    case 'Ä':
                    case 'ä':
                    case 'à':
                    case 'â':
                    case 'À':
                    case 'Â':
                        builder.append("a");
                        break;
                    case 'ü':
                    case 'Ü':
                    case 'û':
                    case 'ù':
                    case 'Û':
                    case 'Ù':
                        builder.append("u");
                        break;
                    default:
                        builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    private static class MyFastTokenizer {
        @NonNull
        private final String delim;
        private final int dsize;
        private String str;
        private int start = 0;

        public MyFastTokenizer(String str, @NonNull String delim) {
            this.str = str;
            this.delim = delim;
            this.dsize = delim.length();
        }

        @NonNull
        public String nextString() {
            int size = str.indexOf(delim, start);
            if (size < 0) {
                size = str.length();
            }
            try {
                return str.substring(start, size);
            } finally {
                start = size + dsize;
            }
        }

        public double nextDouble() {
            String str = nextString();
            if (str == null || str.isEmpty()) return 0;
            return Double.parseDouble(str);
        }

        public int nextInt() {
            String str = nextString();
            if (str == null || str.isEmpty()) return 0;
            return Integer.parseInt(str);
        }
    }
}