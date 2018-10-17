package com.metinkale.prayer.times.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.times.sounds.Sound;
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
