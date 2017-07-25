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

import android.support.annotation.NonNull;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.FastParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by metin on 28.02.2017.
 */

class CitiesSet implements Iterable<Entry> {

    @Override
    public Iterator<com.metinkale.prayerapp.vakit.times.Entry> iterator() {
        return new MyIterator();
    }


    private class MyIterator implements Iterator<Entry> {
        private BufferedReader is;
        private Entry entry;
        private Entry cache = new Entry();

        MyIterator() {
            is = new BufferedReader(new InputStreamReader(App.get().getResources().openRawResource(R.raw.cities)));
        }

        @Override
        public boolean hasNext() {
            if (entry == null)
                entry = next();
            return entry != null;
        }

        @Override
        public Entry next() {
            if (entry != null) {
                Entry e = entry;
                entry = null;
                return e;
            }
            try {
                String line;
                do {
                    line = is.readLine();
                    if (line == null) {
                        is.close();
                        is = null;
                        return null;
                    }
                } while (line.isEmpty());

                MyFastTokenizer st = new MyFastTokenizer(line, "\t");
                Entry e = cache;
                e.setId(st.nextInt());
                e.setParent(st.nextInt());
                e.setLat(st.nextDouble());
                e.setLng(st.nextDouble());
                e.setKey(st.nextString());
                e.setName(st.nextString());
                e.setCountry(null);
                return e;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private static class MyFastTokenizer {
        @NonNull
        private final String delim;
        private final int dsize;
        private String str;
        private int start = 0;

        MyFastTokenizer(String str, @NonNull String delim) {
            this.str = str;
            this.delim = delim;
            this.dsize = delim.length();
        }

        @NonNull
        String nextString() {
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

        double nextDouble() {
            String str = nextString();
            if (str.isEmpty()) return 0;
            return FastParser.parseDouble(str);
        }

        int nextInt() {
            String str = nextString();
            if (str.isEmpty()) return 0;
            return FastParser.parseInt(str);
        }


    }

}
