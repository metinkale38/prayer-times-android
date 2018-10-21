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

package com.metinkale.prayer.times.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.metinkale.prayer.times.sounds.Sound;
import com.metinkale.prayer.times.sounds.Sounds;
import com.metinkale.prayer.times.sounds.UserSound;

import java.lang.reflect.Type;

import androidx.annotation.NonNull;

public class SoundSerializer implements JsonSerializer<Sound>, JsonDeserializer<Sound> {

    @Override
    public JsonElement serialize(Sound sound, Type arg1, JsonSerializationContext arg2) {
        if (sound == null) return null;
        if (sound instanceof UserSound) {
            return new JsonPrimitive(((UserSound) sound).getUri().toString());
        }
        return new JsonPrimitive(sound.getId());
    }

    @Override
    public Sound deserialize(@NonNull JsonElement jtz, Type arg1, JsonDeserializationContext arg2) {
        try {
            return Sounds.getSound(jtz.getAsInt());
        } catch (NumberFormatException e) {
            return Sounds.getSound(jtz.getAsString());
        }
    }
}