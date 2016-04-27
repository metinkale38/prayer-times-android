package com.metinkale.prayerapp.vakit.times.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by metin on 24.04.2016.
 */
public class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

    @Override
    public JsonElement serialize(Boolean arg0, Type arg1, JsonSerializationContext arg2) {
        return arg0 ? new JsonPrimitive(1) : null;
    }

    @Override
    public Boolean deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        return arg0.getAsInt() == 1;
    }
}