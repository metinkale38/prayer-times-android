package com.metinkale.prayerapp.vakit.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayerapp.vakit.sounds.Sound;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.sources.WebTimes;

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
