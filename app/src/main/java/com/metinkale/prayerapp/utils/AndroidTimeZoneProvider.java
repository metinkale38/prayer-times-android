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

package com.metinkale.prayerapp.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v4.util.SimpleArrayMap;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.Provider;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by metin on 13.02.2017.
 */
public class AndroidTimeZoneProvider implements Provider {
    @NonNull
    private SimpleArrayMap<String, WeakReference<DateTimeZone>> cache = new SimpleArrayMap<>();

    public AndroidTimeZoneProvider() {

    }

    @Override
    public DateTimeZone getZone(@NonNull String id) {
        if (id.equals("UTC")) return DateTimeZone.UTC;
        WeakReference<DateTimeZone> wr = cache.get(id);
        if (wr != null) {
            DateTimeZone dtz = wr.get();
            if (dtz != null) return dtz;
        }
        TimeZone tz = TimeZone.getTimeZone(id);
        DateTimeZone dtz = new MyDateTimeZone(id, tz);
        cache.put(id, new WeakReference<>(dtz));
        return dtz;
    }

    @NonNull
    @Override
    public Set<String> getAvailableIDs() {
        ArraySet<String> set = new ArraySet<>();
        Collections.addAll(set, TimeZone.getAvailableIDs());
        return set;

    }


    private class MyDateTimeZone extends DateTimeZone {
        private TimeZone tz;
        @NonNull
        private long[] transitions = new long[0];

        MyDateTimeZone(@NonNull String id, @NonNull TimeZone tz) {
            super(id);
            this.tz = tz;

            try {
                Field f = tz.getClass().getDeclaredField("mTransitions");
                f.setAccessible(true);
                long[] transitions = (long[]) f.get(tz);
                if (transitions == null) return;
                this.transitions = new long[transitions.length];
                System.arraycopy(transitions, 0, this.transitions, 0, transitions.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Nullable
        @Override
        public String getNameKey(long l) {
            return null;
        }

        @Override
        public int getOffset(long l) {
            return tz.getOffset(System.currentTimeMillis());
        }

        @Override
        public int getStandardOffset(long l) {
            return tz.getRawOffset();
        }

        @Override
        public boolean isFixed() {
            return !tz.useDaylightTime();
        }

        @Override
        public long nextTransition(long l) {
            if (transitions.length == 0) return Long.MAX_VALUE;
            int i = 0;
            long time = System.currentTimeMillis() / 1000;
            while (i < transitions.length && transitions[i] < time) {
                i++;
            }
            return i >= transitions.length ? 0 : transitions[i];
        }

        @Override
        public long previousTransition(long l) {
            if (transitions.length == 0) return 0;
            int i = -1;
            long time = System.currentTimeMillis() / 1000;
            while (i + 1 < transitions.length && transitions[i + 1] < time) {
                i++;
            }
            return i >= 0 && i >= transitions.length ? 0 : transitions[i];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MyDateTimeZone)) return false;
            return ((MyDateTimeZone) o).getID().equals(getID());
        }
    }
}
