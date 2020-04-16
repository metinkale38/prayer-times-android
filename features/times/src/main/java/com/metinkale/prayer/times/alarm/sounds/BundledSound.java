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

package com.metinkale.prayer.times.alarm.sounds;

import android.media.MediaPlayer;

import androidx.collection.ArraySet;

import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.times.Vakit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;


public class BundledSound extends Sound {
    private String name;


    BundledSound(String name) {
        this.name = name;
    }

    enum SoundType {
        Default,
        Fajr,
        FajrShort,
        Zuhr,
        Asr,
        Magrib,
        Ishaa,
        Dua,
        Takbir,
        Sela
    }

    private EnumMap<SoundType, Sound> subSounds = new EnumMap<>(SoundType.class);

    public Collection<Sound> getSubSounds() {
        return Collections.unmodifiableList(new ArrayList<>(subSounds.values()));
    }

    void addSound(SoundType type, Sound sound) {
        subSounds.put(type, sound);
    }

    @Override
    public Set<AppSound> getAppSounds() {
        ArraySet<AppSound> set = new ArraySet<>();
        for (Sound sound : subSounds.values()) {
            set.addAll(sound.getAppSounds());
        }
        return set;
    }

    @Override
    public String getShortName() {
        return getName();
    }

    @Override
    public MediaPlayer createMediaPlayer(Alarm alarm) {
        if (!isDownloaded())
            return null;
        if (alarm == null) {
            final Sound sound = subSounds.containsKey(SoundType.Default) ? subSounds.get(SoundType.Default) :
                    subSounds.containsKey(SoundType.Zuhr) ? subSounds.get(SoundType.Zuhr) :
                            subSounds.containsKey(SoundType.Asr) ? subSounds.get(SoundType.Asr) :
                                    subSounds.containsKey(SoundType.Magrib) ? subSounds.get(SoundType.Magrib) :
                                            subSounds.containsKey(SoundType.Ishaa) ? subSounds.get(SoundType.Ishaa) :
                                                    subSounds.containsKey(SoundType.Fajr) ? subSounds.get(SoundType.Fajr) :
                                                            subSounds.containsKey(SoundType.Takbir) ? subSounds.get(SoundType.Takbir) :
                                                                    subSounds.values().iterator().next();
            return sound.createMediaPlayer(alarm);
        } else {
            switch (Vakit.getByIndex(alarm.getCity().getCurrentTime())) {
                case FAJR:
                    if (subSounds.containsKey(SoundType.Fajr)) {
                        return subSounds.get(SoundType.Fajr).createMediaPlayer(alarm);
                    }
                case SUN:
                case DHUHR:
                    if (subSounds.containsKey(SoundType.Zuhr)) {
                        return subSounds.get(SoundType.Zuhr).createMediaPlayer(alarm);
                    }
                case ASR:
                    if (subSounds.containsKey(SoundType.Asr)) {
                        return subSounds.get(SoundType.Asr).createMediaPlayer(alarm);
                    }
                case MAGHRIB:
                    if (subSounds.containsKey(SoundType.Magrib)) {
                        return subSounds.get(SoundType.Magrib).createMediaPlayer(alarm);
                    }
                case ISHAA:
                    if (subSounds.containsKey(SoundType.Ishaa)) {
                        return subSounds.get(SoundType.Ishaa).createMediaPlayer(alarm);
                    }
                default:
                    if (subSounds.containsKey(SoundType.Takbir)) {
                        return subSounds.get(SoundType.Takbir).createMediaPlayer(alarm);
                    }
                    return subSounds.values().iterator().next().createMediaPlayer(alarm);
            }
        }
    }

    @Override
    public boolean isDownloaded() {
        boolean dled = true;
        for (Sound sound : subSounds.values()) {
            if (!sound.isDownloaded()) {
                dled = false;
                break;
            }
        }
        return dled;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (Sound sound : subSounds.values()) {
            size += sound.getSize();
        }
        return size;
    }

    @Override
    public int getId() {
        return name.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }
}
