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

package com.metinkale.prayer.times.alarm.sounds;

import android.media.MediaPlayer;

import com.metinkale.prayer.times.alarm.Alarm;

import java.util.Collections;
import java.util.Set;

public abstract class Sound {
    Sound() {
        Sounds.addSound(this);
    }

    public abstract String getName();

    public abstract String getShortName();

    abstract MediaPlayer createMediaPlayer(Alarm alarm);

    public abstract boolean isDownloaded();

    public abstract int getSize();

    public abstract int getId();

    public Set<AppSound> getAppSounds() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Sound)) return false;
        return ((Sound) obj).getId() == getId();
    }

}
