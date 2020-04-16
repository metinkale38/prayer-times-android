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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.TimeZone;

public class TimezoneSerializer implements JsonSerializer<TimeZone>, JsonDeserializer<TimeZone> {

    @Nullable
    @Override
    public JsonElement serialize(TimeZone tz, Type arg1, JsonSerializationContext arg2) {
        return tz == null ? null : new JsonPrimitive(tz.getID());
    }

    @NonNull
    @Override
    public TimeZone deserialize(@NonNull JsonElement jtz, Type arg1, JsonDeserializationContext arg2) {
        return jtz.isJsonNull() ? TimeZone.getDefault() : TimeZone.getTimeZone(jtz.getAsString());
    }
}