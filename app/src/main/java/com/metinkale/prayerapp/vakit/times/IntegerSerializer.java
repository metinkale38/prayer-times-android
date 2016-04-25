package com.metinkale.prayerapp.vakit.times;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by metin on 24.04.2016.
 */
public class IntegerSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer> {

    @Override
    public JsonElement serialize(Integer arg0, Type arg1, JsonSerializationContext arg2) {
        if (arg0 == 0) return null;
        return new JsonPrimitive(arg0);
    }

    @Override
    public Integer deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        return arg0.getAsInt();
    }
}