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

package com.metinkale.prayer.times.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.alarm.sounds.Sound;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Times;

import java.util.TimeZone;

public class GSONFactory {

    public static Gson build() {
        GsonBuilder b = new GsonBuilder();
        if (BuildConfig.DEBUG) {
            b.setPrettyPrinting();
        }
    
        RuntimeTypeAdapterFactory<Times> subTypeFactory = RuntimeTypeAdapterFactory
                .of(Times.class, "source");
        for (Source source : Source.values()) {
            subTypeFactory = subTypeFactory.registerSubtype(source.clz, source.name());
        }


        BooleanSerializer booleanSerializer = new BooleanSerializer();
        b.registerTypeAdapterFactory(subTypeFactory);
        b.registerTypeAdapter(Boolean.class, booleanSerializer);
        b.registerTypeAdapter(boolean.class, booleanSerializer);
        b.registerTypeAdapter(TimeZone.class, new TimezoneSerializer());
        b.registerTypeAdapter(Sound.class, new SoundSerializer());

        return b.create();
    }

}
