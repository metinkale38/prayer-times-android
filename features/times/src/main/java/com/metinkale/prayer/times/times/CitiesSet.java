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

package com.metinkale.prayer.times.times;

import com.metinkale.prayer.App;
import com.metinkale.prayer.utils.FastTokenizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by metin on 28.02.2017.
 */

class CitiesSet implements Iterable<Entry> {

    private final Source source;

    public CitiesSet(Source source) {
        this.source = source;
    }

    public CitiesSet(int citiesId) {
        for (Source source : Source.values()) {
            if (source.citiesId == citiesId) {
                this.source = source;
                return;
            }
        }
        throw new RuntimeException("bad cities id");
    }

    @Override
    public Iterator<com.metinkale.prayer.times.times.Entry> iterator() {
        return new MyIterator();
    }


    private class MyIterator implements Iterator<Entry> {
        private BufferedReader is;
        private Entry entry;
        private Entry cache = new Entry();

        MyIterator() {
            is = new BufferedReader(new InputStreamReader(App.get().getResources().openRawResource(source.citiesId)));
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

                FastTokenizer st = new FastTokenizer(line, "\t");
                Entry e = cache;
                long id = st.nextInt();
                e.setId(id << 32 | (long) source.citiesId);
                long parent = st.nextInt();
                e.setParent(parent << 32 | (long) source.citiesId);
                e.setLat(st.nextDouble());
                e.setLng(st.nextDouble());
                e.setKey(st.nextString());
                e.setName(st.nextString());
                e.setCountry(null);
                e.setSource(source);
                return e;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
